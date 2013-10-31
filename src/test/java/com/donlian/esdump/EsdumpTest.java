package com.donlian.esdump;

import org.junit.Test;

import com.donlian.esdump.common.InputParaTag;

public class EsdumpTest {
	@Test
	public void backupTest(){
		System.setProperty(InputParaTag.MODE, "backup");
		System.setProperty(InputParaTag.ADDR, "10.8.210.192:9350");
		System.setProperty(InputParaTag.CLUSTER, "wowogoods");
		System.setProperty(InputParaTag.INDEX, "goods_city_1,goods_city_2,,goods_city_3");
		System.setProperty(InputParaTag.BACKUP_DIR, "d:\\temp\\es");
		Esdump.main(null);
	}
	
	@Test
	public void restoreTest(){
		System.setProperty(InputParaTag.MODE, "restore");
		System.setProperty(InputParaTag.ADDR, "10.9.23.8:9300");
		System.setProperty(InputParaTag.CLUSTER, "wowogoods");
//		System.setProperty(InputParaTag.INDEX, "goods_city_1");
		System.setProperty(InputParaTag.RESTORE_DIR, "d:\\temp\\es");
		Esdump.main(null);
	}
}
