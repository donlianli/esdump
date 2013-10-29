package com.donlian.esdump;

import org.junit.Test;

public class EsdumpTest {
	@Test
	public void entryTest(){
		System.setProperty("mode", "backup");
		System.setProperty("addr", "10.8.210.192:9350");
		System.setProperty("cluster", "wowogoods");
//		System.setProperty("index", "goods_city_1");
		System.setProperty("backupDir", "d:\\temp\\es");
		Esdump.main(null);
	}
}
