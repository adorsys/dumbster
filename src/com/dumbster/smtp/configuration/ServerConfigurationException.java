package com.dumbster.smtp.configuration;

public class ServerConfigurationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8024833212418135307L;

	public ServerConfigurationException() {
		super();
	}

	public ServerConfigurationException(String message) {
		super(message);
	}

	public ServerConfigurationException(String message, Throwable t) {
		super(message, t);
	}

	public ServerConfigurationException(Throwable t) {
		super(t);
	}
}
