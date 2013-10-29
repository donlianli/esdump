package com.donlian.esdump.guice;


import com.donlian.esdump.backup.BackupService;
import com.donlian.esdump.backup.BackupServiceImpl;
import com.donlian.esdump.config.ConfigService;
import com.donlian.esdump.config.ConfigServiceImpl;
import com.donlian.esdump.restore.RestoreService;
import com.donlian.esdump.restore.RestoreServiceImpl;
import com.google.inject.AbstractModule;

public class DefaultModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(ConfigService.class).to(ConfigServiceImpl.class);  
		bind(BackupService.class).to(BackupServiceImpl.class);  
		bind(RestoreService.class).to(RestoreServiceImpl.class);  
	}
}
