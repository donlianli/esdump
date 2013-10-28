package com.donlian.esdump.backup;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.jackson.core.JsonEncoding;
import org.elasticsearch.common.jackson.core.JsonFactory;
import org.elasticsearch.common.jackson.core.JsonGenerator;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;

import com.donlian.esdump.common.Tag;
import com.donlian.esdump.config.ConfigService;


public class BackupServiceImpl implements BackupService{
	private static Logger logger = LogManager.getLogger(BackupServiceImpl.class);
	private ConfigService configService;
	public ConfigService getConfigService() {
		return configService;
	}
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}
	public void backup() {
		
		
	}
	/**
	 * 备份结构定义
	 * @param string
	 * @throws IOException 
	 */
	public  void backupMapping(String indexName) throws IOException {
		File file = new File(configService.getBackupMapFilePath(indexName));
		JsonFactory jfactory = new JsonFactory();  
	    JsonGenerator jGenerator = jfactory.createGenerator(file, JsonEncoding.UTF8);  
		long startTime = System.currentTimeMillis();
		Client client = configService.getBackupClient();
		ClusterState cs = client.admin().cluster().prepareState()
				.setFilterIndices(indexName)
				.execute().actionGet().getState();
		IndexMetaData imd = cs.getMetaData().index(indexName);	
		Map<String, MappingMetaData> typeMap = imd.mappings();
		int mappingCount =0;
		for(Map.Entry<String, MappingMetaData> entry : typeMap.entrySet()){
			//type名称
			String typeName = entry.getKey();
			mappingCount++;
			MappingMetaData typeDesc = entry.getValue();
			try {
				jGenerator.writeStartObject();
				
				jGenerator.writeStringField(Tag.INDEX_NAME_TAG, indexName); // 
				jGenerator.writeStringField(Tag.TYPE_NAME_TAG, typeName); // 
				String mapping = typeDesc.source().toString();
				jGenerator.writeStringField(Tag.MAPPING_TAG, mapping); // 
				jGenerator.writeEndObject();

				jGenerator.writeRaw("\n");
			} catch (Exception e) {
				e.printStackTrace();
			}	
		}
		jGenerator.flush();
		jGenerator.close();
		logger.info("fetch count:{}" , mappingCount);
		logger.debug("useTime:{}", (System.currentTimeMillis() - startTime));
	}
	
	public  void backupDoc(String indexName) throws Exception{
		File file = new File(configService.getBackupDataFilePath(indexName));
		JsonFactory jfactory = new JsonFactory();  
	    /*** write to file ***/  
	    JsonGenerator jGenerator = jfactory.createGenerator(file, JsonEncoding.UTF8);  
		long startTime = System.currentTimeMillis();
		Client esClient = configService.getBackupClient();
		SearchResponse searchResponse = esClient.prepareSearch(indexName)
				// 加上这个据说可以提高性能，但第一次却不返回结果
				.setSearchType(SearchType.SCAN)
				// 实际返回的数量为5*index的主分片格式
				.setSize(1000)
				// 这个游标维持多长时间
				.setScroll(TimeValue.timeValueMinutes(10000))
				.execute().actionGet();
		if(searchResponse.getScrollId() != null){
			// 第一次查询，只返回数量和一个scrollId
			long docCount = searchResponse.getHits().getTotalHits();
			long fetchCount = 0;
			logger.info("total:{}" , docCount);
			if(docCount==0){
				/**
				 * 无数据
				 */
				logger.info("{} no data",indexName);
				return ;
			}
			int failedCount=0;
			while (fetchCount < docCount && failedCount<3) {
				// 只要取不够，一直取
				// 使用上次的scrollId继续访问
				searchResponse = esClient
						.prepareSearchScroll(searchResponse.getScrollId())
						.setScroll(TimeValue.timeValueMinutes(8)).execute()
						.actionGet();
				if(searchResponse.getHits().hits().length==0){
					failedCount++;
				}
				else {
					fetchCount += searchResponse.getHits().hits().length;
					if (fetchCount % 1000 == 0) {
						System.out.println(fetchCount + " fetched");
					}
					for (SearchHit hit : searchResponse.getHits()) {
						jGenerator.writeStartObject();
						
						String indexNm=hit.getIndex();
						jGenerator.writeStringField(Tag.INDEX_NAME_TAG, indexNm); // 
						String typeNm=hit.getType();
						jGenerator.writeStringField(Tag.TYPE_NAME_TAG, typeNm); // 
						String docId = hit.getId();
						jGenerator.writeStringField(Tag.DOC_ID_TAG, docId); // 
						String doc = hit.getSourceAsString();
						jGenerator.writeStringField(Tag.SOURCE_TAG, doc); // 
						jGenerator.writeEndObject();
						jGenerator.writeRaw("\n");
					}
				}
			}
			jGenerator.flush();
			jGenerator.close();
			logger.info("fetch count:{}" ,fetchCount);
			logger.debug("useTime:{}", (System.currentTimeMillis() - startTime));
		}
		else {
			/**
			 * 未获得scrollId
			 * TODO
			 */
		}
	}
}
