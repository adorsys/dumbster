package com.dumbster.smtp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import com.dumbster.smtp.action.Connect;

public class SmtpClientSession implements Runnable {

	private Socket socket;
	private List<SmtpMessage> serverMessages;
	private SmtpMessage msg;
	private SmtpResponse smtpResponse;

	public SmtpClientSession(Socket socket, List<SmtpMessage> messages) {
		this.socket = socket;
		this.serverMessages = messages;
		msg = new SmtpMessage();
		SmtpRequest smtpRequest = initializeStateMachine();
		smtpResponse = smtpRequest.execute(serverMessages, msg);
	}

	private void sessionLoop() throws IOException {
		BufferedReader input = getSocketInput();
		PrintWriter out = getSocketOutput();

		SmtpState smtpState = sendInitialResponse(out);

		while (smtpState != SmtpState.CONNECT) {
			String line = input.readLine();

			if (line == null) {
				break;
			}

			SmtpRequest request = smtpState.createRequest(line);
			SmtpResponse response = request.execute(serverMessages, msg);
			storeInputInMessage(request, response);
			sendResponse(out, response);
			smtpState = response.getNextState();
			saveAndRefreshMessageIfComplete(smtpState);
		}
	}

	private void saveAndRefreshMessageIfComplete(SmtpState smtpState) {
		if (smtpState == SmtpState.QUIT) {
			serverMessages.add(msg);
			System.out.println(msg);
			msg = new SmtpMessage();
		}
	}

	private void storeInputInMessage(SmtpRequest request, SmtpResponse response) {
		String params = request.getParams();
		if (params != null) {
			if (SmtpState.DATA_HDR.equals(response.getNextState())) {
				int headerNameEnd = params.indexOf(':');
				if (headerNameEnd >= 0) {
					String name = params.substring(0, headerNameEnd).trim();
					String value = params.substring(headerNameEnd + 1).trim();
					msg.addHeader(name, value);
				}
			} else if (SmtpState.DATA_BODY == response.getNextState()) {
				msg.appendBody(params);
			}
		}
	}

	private SmtpState sendInitialResponse(PrintWriter out) {
		sendResponse(out, smtpResponse);
		return smtpResponse.getNextState();
	}

	private SmtpRequest initializeStateMachine() {
		SmtpState smtpState = SmtpState.CONNECT;
		SmtpRequest smtpRequest = new SmtpRequest(new Connect(), "", smtpState);
		return smtpRequest;
	}

	private BufferedReader getSocketInput() throws IOException {
		return new BufferedReader(
				new InputStreamReader(socket.getInputStream()));
	}

	private PrintWriter getSocketOutput() throws IOException {
		return new PrintWriter(socket.getOutputStream());
	}

	private static void sendResponse(PrintWriter out, SmtpResponse smtpResponse) {
		if (smtpResponse.getCode() > 0) {
			int code = smtpResponse.getCode();
			String message = smtpResponse.getMessage();
			out.print(code + " " + message + "\r\n");
			out.flush();
		}
	}

	@Override
	public void run() {
		try {
			sessionLoop();
		} catch (Exception e) {
		} finally {
			try {
				socket.close();
			} catch (Exception e2) {
			}
		}
	}

}