package com.dumbster.smtp.configuration;


public abstract class ConfigValue<T> {
	
	ServerConfiguration config;
	ConfigItem<T> item;
	T value;
	
	ConfigValue(ServerConfiguration config, ConfigItem<T> item) {
		this.config = config;
		this.item = item;
	}
	
	public ServerConfiguration withValue(T value) {
		this.value = value;
		return config.put(item, this);
	}
	
	public T getValue() {
		return value;
	}
}
