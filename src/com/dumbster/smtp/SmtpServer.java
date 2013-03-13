/*
 * Dumbster - a dummy SMTP server
 * Copyright 2004 Jason Paul Kitchen
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dumbster.smtp;

import static com.dumbster.smtp.configuration.FileLoggingConfigItem.logFile;
import static com.dumbster.smtp.configuration.MailStoreConfigItem.mailStoreClass;
import static com.dumbster.smtp.configuration.OutStreamLoggingConfigItem.outstream;
import static com.dumbster.smtp.configuration.PortConfigItem.port;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.dumbster.smtp.configuration.ServerConfiguration;

/**
 * Dummy SMTP server for testing purposes.
 */
public class SmtpServer implements Runnable {
	private static final int SERVER_SOCKET_TIMEOUT = 5000;
	private static final int MAX_THREADS = 10;

	private volatile MailStore mailStore = new RecipientMailStore();
	private volatile boolean stopped = true;
	private volatile boolean ready = false;
	private volatile boolean threaded = false;

	private ServerSocket serverSocket;
	private ServerConfiguration config;

	SmtpServer(ServerConfiguration config) {
		this.config = config;
	}

	public boolean isReady() {
		return ready;
	}

	public void run() {
		stopped = false;
		try {
			initializeServerSocket();
			initializeMailStore();
			serverLoop();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ready = false;
			if (serverSocket != null) {
				try {
					serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void initializeServerSocket() throws Exception {
		serverSocket = new ServerSocket(config.getConfigValue(port));
		serverSocket.setSoTimeout(SERVER_SOCKET_TIMEOUT);
	}
	
	private void initializeMailStore() throws Exception {
		String storeClazz = config.getConfigValue(mailStoreClass);
		if (storeClazz != null) {
			// Should fail due to only valid configurations passed by the factory
			mailStore = (MailStore) Class.forName(storeClazz).newInstance();
		} else {
			// Default implementation is the unmodified RollingMailStore
			mailStore = new RollingMailStore();
		}
	}


	private void serverLoop() throws IOException {
		int poolSize = threaded ? MAX_THREADS : 1;
		ExecutorService threadExecutor = Executors.newFixedThreadPool(poolSize);
		while (!isStopped()) {
			threadExecutor.execute(new ClientSession(createSocketWrapper(),
					mailStore));
		}
		ready = false;
	}

	private IOSource createSocketWrapper() throws IOException {
		final String log = config.getConfigValue(logFile);
		if (log != null) {
			return new SocketWrapper(clientSocket(), new File(log));	
		} else {
			final OutputStream stream = config.getConfigValue(outstream);
			if (stream != null) {
				return new SocketWrapper(clientSocket(), stream);
			}
		}
		return new SocketWrapper(clientSocket());
	}

	private Socket clientSocket() throws IOException {
		Socket socket = null;
		while (socket == null) {
			socket = accept();
		}
		return socket;
	}

	private Socket accept() {
		try {
			ready = true;
			return serverSocket.accept();
		} catch (Exception e) {
			return null;
		}
	}

	public boolean isStopped() {
		return stopped;
	}

	public synchronized void stop() {
		stopped = true;
		try {
			serverSocket.close();
		} catch (IOException ignored) {
		}
	}

	public MailMessage[] getMessages() {
		return mailStore.getMessages();
	}

	public MailMessage[] getMessagesFor(String recipient) {
		// TODO Is there a way to implement this without any interface change?
		return mailStore instanceof RecipientMailStore ? ((RecipientMailStore) mailStore)
				.getMessageFor(recipient) : mailStore.getMessages();
	}

	public MailMessage getMessage(int i) {
		return mailStore.getMessage(i);
	}

	public int getEmailCount() {
		return mailStore.getEmailCount();
	}

	public void anticipateMessageCountFor(int messageCount, int ticks) {
		int tickdown = ticks;
		while (mailStore.getEmailCount() < messageCount && tickdown > 0) {
			tickdown--;
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	/**
	 * Toggles if the SMTP server is single or multi-threaded for response to
	 * SMTP sessions.
	 * 
	 * @param threaded
	 */
	public void setThreaded(boolean threaded) {
		this.threaded = threaded;
	}

	public void setMailStore(MailStore mailStore) {
		this.mailStore = mailStore;
	}

}
