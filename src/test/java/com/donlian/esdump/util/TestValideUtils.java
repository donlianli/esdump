package com.donlian.esdump.util;

import org.junit.Test;

import com.donlian.esdump.common.InputParaTag;

public class TestValideUtils {
	
	@Test
	public void valideMode(){
		System.setProperty(InputParaTag.MODE, "hello");
		ValidateUtils.validMode();
	}
}
