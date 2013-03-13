package com.dumbster.smtp.configuration;

import java.io.File;


public class FileLoggingConfigItem extends ConfigItem<String>{

	public static final FileLoggingConfigItem logFile = new FileLoggingConfigItem();
	
	private FileLoggingConfigItem() {}
	
	@Override
	public ConfigValue<String> makeConfigValue(ServerConfiguration config)  {
		return new StringConfigValue(config, this);
	}

	@Override
	boolean isValid(String configValue) {
		if (configValue != null && !configValue.isEmpty()) {
			File f = new File(configValue);
			return f.isDirectory() && f.canWrite();
		}
		return false;
	}

	public String toString() {
		return "FileLogging";
	}
}
