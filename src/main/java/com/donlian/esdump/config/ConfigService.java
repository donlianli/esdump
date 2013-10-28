package com.donlian.esdump.config;

import org.elasticsearch.client.Client;

/**
 * 
 * @author donlianli@126.com
 *
 */
public interface ConfigService {
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
}
