package com.donlian.esdump.util;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.donlian.esdump.enu.DataMode;
/**
 * 参数校验工具类
 * @author donlianli@126.com
 * 兼顾错误输出的责任
 */
public class ValidateUtils {
	private static Logger logger = LogManager.getLogger(ValidateUtils.class);
	private ValidateUtils(){};
	/**
	 * 数据模式校验
	 */
	public static void validMode(){
		String mode = System.getProperty("mode", "backup");
		DataMode m = DataMode.valueOfEnum(mode);
		checkNotNull(m,"invalide mode:"+mode);
	}
}
