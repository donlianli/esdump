package com.donlian.esdump.restore;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequestBuilder;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.jackson.core.JsonFactory;
import org.elasticsearch.common.jackson.core.JsonParser;

import com.donlian.esdump.common.Tag;
import com.donlian.esdump.config.ConfigService;
import com.google.inject.Inject;

public class RestoreServiceImpl  implements RestoreService{
	private static Logger logger = LogManager.getLogger(RestoreServiceImpl.class);
	private ExecutorService exec;  
	@Inject
	public RestoreServiceImpl(ConfigService configService){
		setConfigService(configService);
	}
	private ConfigService configService;
	public ConfigService getConfigService() {
		return configService;
	}
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}
	public void restore() {
		logger.debug("enter restore method");
		configService.initRestoreConfig();
		List<String> restoreIndices = configService.getRestoreIndices();
		if(restoreIndices == null || restoreIndices.size()==0){
			restoreIndices = findAllIndexesFromDir();
		}
		if(restoreIndices!=null && restoreIndices.size()>0){
			logger.debug("search index list:[{}]",restoreIndices);
			if(configService.disableMultiThread()){
				for(String indexName : restoreIndices){
					restoreByIndexName(indexName);
				}
			}
			else {
				//采用队列多线程模式
				int cpuCoreNumber = Runtime.getRuntime().availableProcessors();  
				logger.debug("cpu core(threadpool number):{}",cpuCoreNumber);
		        exec = Executors.newFixedThreadPool(cpuCoreNumber);  
		        int indexCount=0;
		        for(String indexName : restoreIndices){
		        	final String iName = indexName;
		        	exec.submit(new Callable<Object>(){
						public Object call() throws Exception {
							restoreByIndexName(iName);
							return null;
						}
		        	});
		        	indexCount++;
				}
		        logger.info("{} index will be restore",indexCount);
				try {
					exec.shutdown();
					exec.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		else {
			logger.info("find no indices to restore");
		}
		configService.getRestoreClient().close();
		logger.debug("exit restore method");
	}
	/**
	 * 根据数据文件查找所有的索引名称
	 * @return
	 */
	private List<String> findAllIndexesFromDir() {
		Set<String> indexSet = new HashSet<String>();
		File dir = new File(configService.getRestoreDir());
		if (!dir.exists()) {
			logger.error("文件或目录不存在");
			return Collections.emptyList();
		}
		String[] subFiles = dir.list();
		for (int i = 0; i < subFiles.length; i++) {
			String fileName = subFiles[i];
			if (fileName.endsWith(ConfigService.DATA_EXT)
					|| fileName.endsWith(ConfigService.MAP_EXT)) {
				String indexName = fileName.replace(
						"." + ConfigService.DATA_EXT, "").replace(
						"." + ConfigService.MAP_EXT, "");
				indexSet.add(indexName);
			}
		}
		return new ArrayList<String>(indexSet);
	}
	public void restoreByIndexName(String indexName) {
		try {
			restoreMapping(indexName);
		} catch (Exception e) {
			logger.error("restore index:{} mapping error",indexName);
			e.printStackTrace();
		}
		try {
			restoreDoc(indexName);
		} catch (Exception e) {
			logger.error("restore index:{} document error",indexName);
			e.printStackTrace();
		}
	}
	public void restoreMapping(String indexName) throws Exception{
		File file = new File(configService.getRestoreMapFilePath(indexName));
		JsonFactory jfactory = new JsonFactory();
		JsonParser jParser = jfactory.createParser(file);
		int mappinpCount=0;
		while (!jParser.isClosed()) {
			jParser.nextToken();
			String fieldname = jParser.getCurrentName();
			if (Tag.INDEX_NAME_TAG.equals(fieldname)) {
				jParser.nextToken();
				String iName = jParser.getText(),
						typeName="",mapping="";
				//表示数据段开始
				// 当前结点为indexName
				jParser.nextToken();
				fieldname = jParser.getCurrentName();
				if (Tag.TYPE_NAME_TAG.equals(fieldname)) {
					// 当前结点为type
					jParser.nextToken();
					typeName = jParser.getText();
				}
				else {
					logger.error("expect typeName,actural name:"+fieldname); 
				}
				jParser.nextToken();
				fieldname = jParser.getCurrentName();
				if (Tag.MAPPING_TAG.equals(fieldname)) {
					jParser.nextToken();
					mapping = jParser.getText();
				}
				else {
					logger.error("expect docId,actural name:"+fieldname); 
				}
				restoreMapping(iName,typeName,mapping);
				mappinpCount++;
			}
		}
		jParser.close();
		logger.info("restore {} mapping count:{}" , indexName,mappinpCount);
	}
	public void restoreMapping(String indexName, String typeName,
			String mapping) {
		Client client = configService.getRestoreClient();
		IndicesExistsResponse respone = client.admin().indices().exists(new IndicesExistsRequest(indexName)).actionGet();
		if(!respone.isExists()){
			CreateIndexResponse  indexresponse = client.admin().indices()
					//这个索引库的名称还必须不包含大写字母
					.prepareCreate(indexName).execute().actionGet();
					if(!indexresponse.isAcknowledged()){
						logger.error("restore {} mapping  create index  error:",indexName);
					};
		}
		
		//如果是在两台机器上，下面直接putMapping可能会报异常
		PutMappingRequestBuilder builder = client.admin().indices().preparePutMapping(indexName);
		//testType就像当于数据的table
		builder.setType(typeName);
		builder.setSource(mapping);
		PutMappingResponse  response = builder.execute().actionGet();
		if(!response.isAcknowledged()){
			logger.error("restore {} mapping  create mapping[{}]  error:",indexName,typeName);
		}
	}
	
	public void restoreDoc(String indexName) throws Exception{
		File file = new File(configService.getRestoreDataFilePath(indexName));
		JsonFactory jfactory = new JsonFactory();
		JsonParser jParser = jfactory.createParser(file);
		int n=0;
		ESIndexer indexer = new ESIndexer();
		while (!jParser.isClosed()) {
			jParser.nextToken();
			String fieldname = jParser.getCurrentName();
			if (Tag.INDEX_NAME_TAG.equals(fieldname)) {
				//表示数据段开始
				// 当前结点为indexName
				jParser.nextToken();
				n++;
				String iName = jParser.getText();
				String typeName="",docId="",docSource="";
				jParser.nextToken();
				fieldname = jParser.getCurrentName();
				if (Tag.TYPE_NAME_TAG.equals(fieldname)) {
					// 当前结点为type
					jParser.nextToken();
					typeName = jParser.getText();
				}
				else {
					logger.error("expect typeName,actural name:{}",fieldname); 
				}
				jParser.nextToken();
				fieldname = jParser.getCurrentName();
				if (Tag.DOC_ID_TAG.equals(fieldname)) {
					//文档id
					jParser.nextToken();
					docId = jParser.getText();
				}
				else {
					logger.error("expect docId,actural name{}",fieldname); 
				}
				jParser.nextToken();
				fieldname = jParser.getCurrentName();
				if (Tag.SOURCE_TAG.equals(fieldname)) {
					//文档对象
					jParser.nextToken();
					docSource = jParser.getText();
				}
				else {
					logger.error("expect docSource,actural name:{}",fieldname); 
				}
				indexer.bulkIndex(iName,typeName,docId,docSource);
				if(n%configService.getBulkIndexCount() == 0){
					indexer.flushData();
				}
			}
		}
		logger.info("index '{}' restore document count:{}",indexName,n);
		indexer.flushData();
		jParser.close();
	}
	
	private class ESIndexer{
		private List<Map<String,String>> dataList = new ArrayList<Map<String,String>>();
		public void bulkIndex(String indexName, String typeName,
				String docId, String docSource) {
			Map<String,String> map = new HashMap<String,String>();
			map.put(Tag.INDEX_NAME_TAG, indexName);
			map.put(Tag.TYPE_NAME_TAG, typeName);
			map.put(Tag.DOC_ID_TAG, docId);
			map.put(Tag.SOURCE_TAG, docSource);
			dataList.add(map);
		}
		public void flushData() {
			Client client = configService.getRestoreClient();
			BulkRequestBuilder bulkRequest = client.prepareBulk();
			for(Map<String,String> map : dataList){
				IndexRequestBuilder indexRequest = client.prepareIndex(map.get(Tag.INDEX_NAME_TAG),map.get(Tag.TYPE_NAME_TAG))
				        .setId(map.get(Tag.DOC_ID_TAG))
						//指定不重复的ID		
				        .setSource(map.get(Tag.SOURCE_TAG));
						//添加到builder中
						bulkRequest.add(indexRequest);
			}
			BulkResponse bulkResponse = bulkRequest.execute().actionGet();
			if (bulkResponse.hasFailures()) {
				logger.error(bulkResponse.buildFailureMessage());
			}
			dataList.clear();
		}
	}
}
