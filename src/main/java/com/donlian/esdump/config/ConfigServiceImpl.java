package com.donlian.esdump.config;

import java.io.File;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import com.donlian.esdump.common.InputParaTag;
import com.google.common.base.Splitter;

import static com.google.common.base.Strings.isNullOrEmpty;

public class ConfigServiceImpl implements ConfigService {
	private static Logger logger = LogManager.getLogger(ConfigServiceImpl.class);
	private Client backupClient;
	private Client restoreClient;
	private Client sourceClient;
	private Client targetClient;
	private String clusterName;
	private List<String> backupIndices;
	private List<String> restoreIndices;
	/**
	 * bulkindex的默认数量
	 */
	private int bulkCount=10000;
	/**
	 * 默认备份目录
	 */
	private String backupDir="d:/temp/es";
	/**
	 * 默认还原目录
	 */
	private String restoreDir = backupDir;
	/**
	 * 默认启用多线程
	 */
	private boolean disableMultiThread=false;
	
	public int getBulkIndexCount() {
		return bulkCount;
	}

	public String getBackupDirectory() {
		return backupDir;
	}

	public String getBackupMapFilePath(String indexName) {
		return getBackupDir()+System.getProperty("file.separator")+indexName+"." + MAP_EXT;
	}

	public String getBackupDataFilePath(String indexName) {
		return getBackupDir()+System.getProperty("file.separator")+indexName+"."+ DATA_EXT;
	}

	public String getRestoreDirectory() {
		return restoreDir;
	}

	public String getRestoreMapFilePath(String indexName) {
		return getRestoreDirectory()+System.getProperty("file.separator")+indexName+"." + MAP_EXT;
	}

	public String getRestoreDataFilePath(String indexName) {
		return getRestoreDirectory()+System.getProperty("file.separator")+indexName+"."+ DATA_EXT;
	}

	public Client getBackupClient() {
		return backupClient;
	}
	public Client getRestoreClient() {
		return restoreClient;
	}
	public Client getSourceClient() {
		return sourceClient;
	}

	public Client getTargetClient() {
		return targetClient;
	}

	public boolean disableMultiThread() {
		return disableMultiThread;
	}

	public int getBulkCount() {
		return bulkCount;
	}

	public void setBulkCount(int bulkCount) {
		this.bulkCount = bulkCount;
	}

	public String getBackupDir() {
		return backupDir;
	}

	public void setBackupDir(String backupDir) {
		this.backupDir = backupDir;
	}

	public String getRestoreDir() {
		return restoreDir;
	}

	public void setRestoreDir(String restoreDir) {
		this.restoreDir = restoreDir;
	}

	public boolean isDisableMultiThread() {
		return disableMultiThread;
	}

	public void setDisableMultiThread(boolean disableMultiThread) {
		this.disableMultiThread = disableMultiThread;
	}


	/**
	 * 初始化备份设置
	 */
	public void initBackupConfig() {
		logger.debug("initBackupConfig");
		/**
		 * 备份机器
		 */
		String address = System.getProperty(InputParaTag.ADDR, DEFAULT_ADDRESS);
		List<String> list = Splitter.on(':').trimResults()
					       .omitEmptyStrings()
					       .splitToList(address);
		String host = list.get(0);
		String port = list.get(1);
		/**
		 * 设置集群名称
		 */
		String clusterName = System.getProperty(InputParaTag.CLUSTER, DEFAULT_CLUSTER_NAME);
		setBackupClusterName(clusterName);
		Settings settings = ImmutableSettings.settingsBuilder()
				//指定集群名称
                .put("cluster.name", clusterName)
                //探测集群中机器状态
                .put("client.transport.sniff", true).build();
		/*
		 * 创建客户端，所有的操作都由客户端开始，这个就好像是JDBC的Connection对象
		 * 用完记得要关闭
		 */
		Client client = new TransportClient(settings)
		.addTransportAddress(new InetSocketTransportAddress(host, Integer.parseInt(port)));
		this.backupClient = client;
		
		/**
		 * 设置备份目录，没有则以当前目录为准
		 */
		String backupDir = System.getProperty(InputParaTag.BACKUP_DIR, getDefaultDir());
		setBackupDir(backupDir);
		if(logger.isInfoEnabled()){
			logger.info("clusterName:{},connect use address:{}:{},backup directory:{}"
					,clusterName,host,port,backupDir);
		}
		logger.debug("initBackupConfig over");
		/**
		 * 设置备份特定的索引
		 */
		String index = System.getProperty(InputParaTag.INDEX, null);
		if(!isNullOrEmpty(index)){
			List<String> indexes = Splitter.on(',').trimResults()
				       .omitEmptyStrings()
				       .splitToList(index);
			backupIndices = indexes;
		}
	}
	/**
	 * 初始化恢复设置
	 */
	public void initRestoreConfig() {
		logger.debug("initRestoreConfig");
		/**
		 * 备份机器
		 */
		String address = System.getProperty(InputParaTag.ADDR, DEFAULT_ADDRESS);
		List<String> list = Splitter.on(':').trimResults()
					       .omitEmptyStrings()
					       .splitToList(address);
		String host = list.get(0);
		String port = list.get(1);
		/**
		 * 设置集群名称
		 */
		String clusterName = System.getProperty(InputParaTag.CLUSTER, DEFAULT_CLUSTER_NAME);
		setBackupClusterName(clusterName);
		Settings settings = ImmutableSettings.settingsBuilder()
				//指定集群名称
                .put("cluster.name", clusterName)
                //探测集群中机器状态
                .put("client.transport.sniff", false).build();
		/*
		 * 创建客户端，所有的操作都由客户端开始，这个就好像是JDBC的Connection对象
		 * 用完记得要关闭
		 */
		Client client = new TransportClient(settings)
		.addTransportAddress(new InetSocketTransportAddress(host, Integer.parseInt(port)));
		this.restoreClient = client;
		/**
		 * 设置恢复目录，没有则以当前目录为准
		 */
		String restoreDire = System.getProperty(InputParaTag.RESTORE_DIR, getDefaultDir());
		setRestoreDir(restoreDire);
		if(logger.isInfoEnabled()){
			logger.info("clusterName:{},connect use address:{}:{},restore directory:{}"
					,clusterName,host,port,restoreDire);
		}
		logger.debug("initRestoreConfig over");
		/**
		 * 设置备份特定的索引
		 */
		String index = System.getProperty(InputParaTag.INDEX, null);
		if(!isNullOrEmpty(index)){
			List<String> indexes = Splitter.on(',').trimResults()
				       .omitEmptyStrings()
				       .splitToList(index);
			restoreIndices = indexes;
		}
	}
	private String getDefaultDir() {
		File directory = new File("");//设定为当前文件夹
		try{
		   return directory.getCanonicalPath();//获取标准的路径
		}catch(Exception e){
			return backupDir;
		}
	}

	public String getBackupClusterName() {
		return clusterName;
	}
	public void setBackupClusterName(String clusterName) {
		this.clusterName=clusterName;
	}

	public List<String> getBackupIndices() {
		return backupIndices;
	}

	public List<String> getRestoreIndices() {
		return restoreIndices;
	}

	public void setRestoreIndices(List<String> restoreIndices) {
		this.restoreIndices = restoreIndices;
	}
	
}
