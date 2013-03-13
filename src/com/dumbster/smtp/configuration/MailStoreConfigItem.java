package com.dumbster.smtp.configuration;


public class MailStoreConfigItem extends ConfigItem<String>{

	public static final MailStoreConfigItem mailStoreClass = new MailStoreConfigItem();
	
	private MailStoreConfigItem() {}
	
	@Override
	public ConfigValue<String> makeConfigValue(ServerConfiguration config)  {
		return new StringConfigValue(config, this);
	}

	@Override
	boolean isValid(String configValue) {
		try {
			@SuppressWarnings({ "unused", "unchecked" })
			Class<ConfigItem<String>> clazz = (Class<ConfigItem<String>>) Class.forName(configValue);
		} catch (ClassNotFoundException e) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return "MailStoreClass";
	}
}
