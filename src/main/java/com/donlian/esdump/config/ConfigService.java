package com.donlian.esdump.config;

import java.util.List;

import org.elasticsearch.client.Client;

/**
 * 
 * @author donlianli@126.com
 *
 */
public interface ConfigService {
	/**
	 * 默认备份还原集群名称
	 */
	public static String DEFAULT_CLUSTER_NAME="elasticsearch";
	/**
	 * 默认备份还原机器地址
	 */
	public static String DEFAULT_ADDRESS = "localhost:9200";
	/**
	 * 
	 */
	public static String DATA_EXT="docs";
	/**
	 * 
	 */
	public static String MAP_EXT = "map";
	/**
	 * 批量索引的阈值
	 * @return
	 */
	int getBulkIndexCount();
	/**
	 * 获得备份目录
	 * @return
	 */
	String getBackupDirectory();
	/**
	 * 根据索引名称，获得对应的
	 * 备份文件绝对路径
	 * @param indexName
	 * @return
	 */
	String getBackupMapFilePath(String indexName);
	/**
	 * 根据索引名称，获得
	 * 备份mapping文件绝对路径
	 * @param indexName
	 * @return
	 */
	String getBackupDataFilePath(String indexName);
	/**
	 * 恢复目录
	 * @return
	 */
	String getRestoreDirectory();
	
	/**
	 * 根据索引名称，获得对应的
	 * 恢复文件绝对路径
	 * @param indexName
	 * @return
	 */
	String getRestoreMapFilePath(String indexName);
	/**
	 * 根据索引名称，获得
	 * 恢复mapping文件绝对路径
	 * @param indexName
	 * @return
	 */
	String getRestoreDataFilePath(String indexName);
	/**
	 * 获得备份客户端
	 * @return Client
	 */
	Client getBackupClient();
	/**
	 * 获得来源客户端
	 * @return Client
	 */
	Client getSourceClient();
	/**
	 * 获得导入目标客户端
	 * @return Client
	 */
	Client getTargetClient();
	/**
	 * 禁用多线程
	 * 默认是启动多线程备份和还原
	 * @return
	 */
	boolean disableMultiThread();
	/**
	 * 设置备份目录
	 * @param backupDir
	 */
	public void setBackupDir(String backupDir);
	/**
	 * 获得备份目录
	 * @return
	 */
	public String getBackupDir();
	/**
	 * 设置恢复文件目录
	 * @param restoreDir
	 */
	public void setRestoreDir(String restoreDir);
	/**
	 * 获得恢复文件目录
	 * @return
	 */
	public String getRestoreDir();
	/**
	 * 获得备份集群名称
	 * @return
	 */
	public String getBackupClusterName();
	/**
	 * 设置备份集群名称
	 * @return
	 */
	public void setBackupClusterName(String clusterName);
	/**
	 * 初始化备份配置
	 * @param config
	 */
	public void initBackupConfig();
	public List<String> getBackupIndices() ;
	
}
