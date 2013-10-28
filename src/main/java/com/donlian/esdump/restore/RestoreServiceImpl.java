package com.donlian.esdump.restore;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.jackson.core.JsonFactory;
import org.elasticsearch.common.jackson.core.JsonParser;

import com.donlian.esdump.common.Tag;
import com.donlian.esdump.config.ConfigService;

public class RestoreServiceImpl implements RestoreService{
	private static Logger logger = LogManager.getLogger(RestoreServiceImpl.class);
	private ConfigService configService;
	public ConfigService getConfigService() {
		return configService;
	}
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}
	public void restore() {
		// TODO Auto-generated method stub
		
	}
	public void restore(String indexName) throws Exception {
		File file = new File(configService.getRestoreDataFilePath(indexName));
		JsonFactory jfactory = new JsonFactory();
		JsonParser jParser = jfactory.createParser(file);
		int n=0;
		// loop until token equal to "}"
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
				bulkIndex(iName,typeName,docId,docSource);
				if(n%configService.getBulkIndexCount() == 0){
					flushData();
				}
			}
		}
		flushData();
		jParser.close();
	}
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
		Client client = configService.getTargetClient();
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
		else {
			logger.info("{} bulk indexed",dataList.size());
		}
		dataList.clear();
	}
}
