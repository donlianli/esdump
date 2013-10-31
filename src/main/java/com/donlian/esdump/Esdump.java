package com.donlian.esdump;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.donlian.esdump.backup.BackupService;
import com.donlian.esdump.common.InputParaTag;
import com.donlian.esdump.enu.DataMode;
import com.donlian.esdump.guice.DefaultModule;
import com.donlian.esdump.restore.RestoreService;
import com.donlian.esdump.util.ValidateUtils;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * 备份程序入口
 * @author donlianli@126.com
 *
 */
public class Esdump {
	private static Logger logger = LogManager.getLogger(Esdump.class);
	public static void main(String[] args) {
		/**
		 * 校验参数
		 */
		ValidateUtils.validMode();
		/**
		 * 初始化guice,准备对应资源
		 */
		 Injector injector = Guice.createInjector(new DefaultModule()); 
		 
		/**
		 * 根据输入的模式，进入对应的入口
		 */
		String mode = System.getProperty(InputParaTag.MODE, "backup");
		DataMode m = DataMode.valueOfEnum(mode);
		switch(m){
		case BACKUP:{
			logger.info("backup mode");
			BackupService service = injector.getInstance(BackupService.class);
			service.backup();
			break;
		}
		case RESTORE:{
			logger.info("restore mode");
			RestoreService service = injector.getInstance(RestoreService.class);
			service.restore();
			break;
		}
		case PIPE:{
			logger.info("pipeline mode");
			break;
		}
		}
	}

}
