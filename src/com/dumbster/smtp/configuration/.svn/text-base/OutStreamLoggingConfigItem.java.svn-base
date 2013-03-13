package com.dumbster.smtp.configuration;

import java.io.OutputStream;



public class OutStreamLoggingConfigItem extends ConfigItem<OutputStream>{

	public static final OutStreamLoggingConfigItem outstream = new OutStreamLoggingConfigItem();
	
	private OutStreamLoggingConfigItem() {}
	
	@Override
	public ConfigValue<OutputStream> makeConfigValue(ServerConfiguration config)  {
		return new OutStreamConfigValue(config, this);
	}

	@Override
	boolean isValid(OutputStream configValue) {
		return configValue != null;
	}
	
	@Override
	public String toString() {
		return "OutstreamLogging";
	}

}
