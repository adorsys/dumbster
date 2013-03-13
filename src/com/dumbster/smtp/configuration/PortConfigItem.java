package com.dumbster.smtp.configuration;


public class PortConfigItem extends ConfigItem<Integer>{
	public static final PortConfigItem port = new PortConfigItem();
	
	private PortConfigItem(){}

	@Override
	public ConfigValue<Integer> makeConfigValue(ServerConfiguration config) {
		return new IntegerConfigValue(config, this);
	}

	@Override
	boolean isValid(Integer configValue) {
		return configValue != null && configValue > 0 && configValue <= 65536; 
	}
	
	@Override
	public String toString() {
		return "Port";
	}
}
