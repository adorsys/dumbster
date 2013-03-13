package com.dumbster.smtp.configuration;

public abstract class ConfigItem<T> {
	abstract ConfigValue<T> makeConfigValue(ServerConfiguration config);

	@SuppressWarnings("unchecked")
	boolean isValid(ConfigValue<?> configValue) {
		try {
			return isValid((T) configValue.getValue());
		} catch (ClassCastException e) {
			return false;
		}
	}

	abstract boolean isValid(T configValue);
}
