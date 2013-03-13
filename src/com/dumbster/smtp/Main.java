package com.dumbster.smtp;

import static com.dumbster.smtp.configuration.FileLoggingConfigItem.logFile;
import static com.dumbster.smtp.configuration.PortConfigItem.port;
import static com.dumbster.smtp.configuration.MailStoreConfigItem.mailStoreClass;

import java.util.logging.Level;
import java.util.logging.Logger;

import jargs.gnu.CmdLineParser;
import jargs.gnu.CmdLineParser.IllegalOptionValueException;
import jargs.gnu.CmdLineParser.Option;
import jargs.gnu.CmdLineParser.UnknownOptionException;

import com.dumbster.smtp.configuration.ServerConfiguration;

public class Main {

	private static final int DEFAULT_PORT = 25;
	private static final String DEFAULT_MAILSTORE = RollingMailStore.class
			.getCanonicalName();

	public static void main(String[] args) {
		SmtpServer server;
		ServerConfiguration config = parseConfigurationconfig(args);
		if (config != null) {
			server = SmtpServerFactory.startServer(config);
			if (server != null) {
				server.setThreaded(true);
				Logger.getLogger(Main.class.getCanonicalName()).log(Level.INFO, "Dumbster SMTP Server started.");
			}
		}
	}

	private static ServerConfiguration parseConfigurationconfig(String[] args) {
		for (String arg : args) {
			if (arg.matches("-(h|-help)")) {
				printUsage();
				return null;
			}
		}
		return args == null || args.length == 0 ? getDefaultConfig()
				: parseCustomConfig(args);
	}

	private static ServerConfiguration getDefaultConfig() {
		return ServerConfiguration.is(port).withValue(DEFAULT_PORT);
	}

	private static ServerConfiguration parseCustomConfig(String[] args) {
		ServerConfiguration config = null;
		String logPathValue = null;
		Integer portValue = null;
		String storeClassName = null;
		CmdLineParser parser = new CmdLineParser();
		Option logPathArg = parser.addStringOption('l', "logpath");
		Option portArg = parser.addIntegerOption('p', "port");
		Option storeArg = parser.addIntegerOption('s', "storeclass");
		try {
			parser.parse(args);
			logPathValue = (String) parser.getOptionValue(logPathArg);
			portValue = (Integer) parser.getOptionValue(portArg);
			storeClassName = (String) parser.getOptionValue(storeArg);

			config = ServerConfiguration.is(port).withValue(portValue);
			if (logPathValue != null) {
				config.and(logFile).withValue(logPathValue);
			}
			if (storeClassName != null) {
				config.and(mailStoreClass).withValue(storeClassName);
			} else {
				config.and(mailStoreClass).withValue(DEFAULT_MAILSTORE);
			}

		} catch (IllegalOptionValueException e) {
			printUsage();
		} catch (UnknownOptionException e) {
			printUsage();
		}
		return config;
	}

	private static void printUsage() {
		System.err
				.println("Usage: Dumbster [{-l,--logpath}] [{-p,--port}] [{-h,--help}] [{-s,--storeclass}]");
	}
}
