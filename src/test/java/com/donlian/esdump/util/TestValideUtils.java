package com.donlian.esdump.util;

import org.junit.Test;

public class TestValideUtils {
	
	@Test
	public void valideMode(){
		System.setProperty("mode", "hello");
		ValidateUtils.valideMode();
	}
}
