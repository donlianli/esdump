package com.donlian.esdump.enu;

import java.util.HashMap;
import java.util.Map;
/**
 * 
 * @author donlianli@126.com
 * 备份模式
 */
public enum DataMode {
	/**
	 * 备份模式
	 */
	BACKUP("backup"),
	/**
	 * 恢复模式
	 */
	RESTORE("restore"),	
	/**
	 * 管道模式
	 */
	PIPE("pipe");		
	
	private String mode;
	private DataMode(String mode){
		this.mode = mode==null?null:mode.toLowerCase();
	}
	private static Map<String,DataMode> selfMap = new HashMap<String,DataMode>();
	static {
		for(DataMode m : DataMode.values()){
			selfMap.put(m.mode, m);
		}
	}
	public static DataMode valueOfEnum(String key){
		return selfMap.get(key);
	}
}
