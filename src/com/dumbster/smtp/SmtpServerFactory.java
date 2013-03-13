package com.dumbster.smtp;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dumbster.smtp.configuration.ServerConfiguration;
import com.dumbster.smtp.configuration.ServerConfigurationException;

/**
 * User: rj Date: Aug 28, 2011 Time: 6:48:14 AM
 */
public class SmtpServerFactory {
	/**
	 * Creates a new server instance according to to given configuration. 
	 * If the configuration isn't valid in any parameter the factory won't create
	 * a new instance and returns null.
	 * 
	 * @param config user-specific configuration 
	 * @return new server if there were no configuration issues
	 */
	public static SmtpServer startServer(ServerConfiguration config) {
		Logger logger = Logger.getLogger(SmtpServerFactory.class.getCanonicalName());
		logger.log(Level.FINEST, "### Serverconfiguration ###\n"+config);
		try {
			config.validate();
			SmtpServer server = new SmtpServer(config);
			executeServer(server);
			waitForReadyServer(server);
			return server;
		} catch(ServerConfigurationException e) {
			logger.log(Level.SEVERE, e.getMessage());
		}
		return null;
	}

	private static void executeServer(SmtpServer server) {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.execute(server);
	}

	private static void waitForReadyServer(SmtpServer server) {
		while (!server.isReady()) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static SmtpServer startServer() {
		return startServer(null);
	}
}
