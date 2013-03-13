package com.dumbster.smtp.configuration;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class ServerConfiguration {

	private Map<ConfigItem<?>, ConfigValue<?>> config;

	private ServerConfiguration() {
		config = new HashMap<ConfigItem<?>, ConfigValue<?>>();
	}

	public static <T> ConfigValue<T> is(ConfigItem<T> item) {
		return item.makeConfigValue(new ServerConfiguration());
	}

	public <T> ConfigValue<T> and(ConfigItem<T> item) {
		return item.makeConfigValue(this);
	}

	<T> ServerConfiguration put(ConfigItem<T> item, ConfigValue<T> value) {
		config.put(item, value);
		return this;
	}

	@SuppressWarnings("unchecked")
	// uncritical due to type-safe insertion
	public <T> T getConfigValue(ConfigItem<T> item) {
		ConfigValue<?> o = config.get(item);
		return o != null ? (T) o.getValue() : null;
	}

	public boolean validate() throws ServerConfigurationException {
		boolean isValid = true;
		for (Iterator<Entry<ConfigItem<?>, ConfigValue<?>>> iterator = config
				.entrySet().iterator(); iterator.hasNext() && isValid;) {
			Entry<ConfigItem<?>, ConfigValue<?>> e = iterator.next();
			isValid = e.getKey().isValid(e.getValue());
			if (!isValid) {
				throw new ServerConfigurationException(
						"Configuration error occured while validating `"
								+ e.getKey()
								+ "´ with value `"
								+ (e.getValue() != null ? e.getValue()
										.getValue() : "null") + "´");
			}
		}
		return isValid;
	}

	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();
		int i = 0;
		int size = config.entrySet().size();
		for (Entry<ConfigItem<?>, ConfigValue<?>> e : config.entrySet()) {
			res.append(e.getKey().getClass().getSimpleName())
					.append(": ")
					.append(e.getValue() != null
							&& ((ConfigValue<?>) e.getValue()).getValue() != null ? ((ConfigValue<?>) e
							.getValue()).getValue().toString() : "null");
			if (i++ < size - 1) {
				res.append("\n");
			}
		}
		return res.toString();
	}
}
