package com.donlian.esdump;

import org.junit.Test;

import com.donlian.esdump.common.InputParaTag;

public class EsdumpTest {
	@Test
	public void entryTest(){
		System.setProperty(InputParaTag.MODE, "backup");
		System.setProperty(InputParaTag.ADDR, "10.8.210.192:9350");
		System.setProperty(InputParaTag.CLUSTER, "wowogoods");
		System.setProperty(InputParaTag.INDEX, "goods_city_1");
		System.setProperty(InputParaTag.BACKUP_DIR, "d:\\temp\\es");
		Esdump.main(null);
	}
}
