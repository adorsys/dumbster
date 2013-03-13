package com.dumbster.smtp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.Socket;

public class SocketWrapper implements IOSource {
	private Socket socket;
	static final String FILE_PATTERN = "mail_%s_%s.log";
	private final static String PATH_PATTERN = "%s/" + FILE_PATTERN;
	

	private BufferedWriter logger;

	public SocketWrapper(Socket socket) {
		this(socket, (OutputStream) null);
	}

	/**
	 * You can enable debugging capability by just specifying a directory where
	 * to put the debugging information. Every client session will be stored in
	 * a separate file. If you don't specify a directory the debugger will use
	 * the current path.
	 * 
	 * @param socket
	 *            socket to read/write from
	 * @param debug
	 *            path where to store session information
	 * @throws IOException
	 */
	public SocketWrapper(Socket socket, File debug) {
		this(socket, createLogWriterFromFile(debug, socket));
	}

	/**
	 * Gives you a bit more flexibility by explicitly choosing your logging
	 * sink.
	 * 
	 * @param socket
	 *            socket to read/write from
	 * @param o
	 *            outstream to write logging information to
	 */
	public SocketWrapper(Socket socket, OutputStream o) {
		this.socket = socket;
		if (o != null) {
			logger = new BufferedWriter(new OutputStreamWriter(o));
		}
	}

	private static OutputStream createLogWriterFromFile(File debug,
			Socket socket) {
		OutputStream res = null;
		if (debug.isDirectory()) {
			try {
				res = new FileOutputStream(createFile(debug, socket));
			} catch (IOException e) {
				System.out.println("Can not create log writer.");
			}
		} else {
			System.out.println("File `" + debug.toString()
					+ "´ is not a directory. Can not activate dubugging.");
		}
		return res;
	}

	private static File createFile(final File dir, final Socket socket) {
		File res = null;
		if (dir.canWrite()) {
			res = new File(String.format(
					PATH_PATTERN,
					new Object[] { dir.getAbsolutePath(),
							socket.getInetAddress().getHostAddress(),
							String.valueOf(System.currentTimeMillis()) }));
		} else {
			System.out.println("Can not write to directory `" + dir + "´");
		}
		return res;
	}

	public BufferedReader getInputStream() throws IOException {
		return new OutputLoggingWrapper(new InputStreamReader(
				socket.getInputStream()));
	}

	public PrintWriter getOutputStream() throws IOException {
		return new InputLoggingWrapper(socket.getOutputStream());
	}
	
	public void close() throws IOException {
		socket.close();
		if (logger != null) {
			logger.close();
		}
	}
	
	private void writeLog(String s) {
		if (logger != null) {
			try {
				logger.write(s + "\n");
				logger.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private class InputLoggingWrapper extends PrintWriter {

		public InputLoggingWrapper(OutputStream arg0) {
			super(arg0);
		}

		@Override
		public void print(String s) {
			super.write(s);
			writeLog(s);
		}
	}

	private class OutputLoggingWrapper extends BufferedReader {

		public OutputLoggingWrapper(Reader in) {
			super(in);
		}

		@Override
		public String readLine() throws IOException {
			String line = super.readLine();
			writeLog(line);
			return line;
		}
	}
}
