package com.gacha.common;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable, IOInterface {

	private Socket clientSocket;
	
	private ObjectOutputStream out;
	private ObjectInputStream in;
	
	public ClientHandler(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}
	
	@Override
	public void run() {
		try {
			this.out = new ObjectOutputStream(clientSocket.getOutputStream());
			this.in = new ObjectInputStream(clientSocket.getInputStream());
			
			MainController main = new MainController(this);
			main.execute();
			
		} catch (IOException | ClassNotFoundException e) {
			System.err.println(clientSocket.getInetAddress() 
					+ " ❌ 클라이언트와의 연결이 끊어졌습니다. " + e.getMessage());
			e.printStackTrace();
		} finally {
			UserSession.set(null);
			System.err.println("클라이언트 퇴장 "
					+clientSocket.getRemoteSocketAddress());
			try {
				if (in != null) in.close();
	            if (out != null) out.close();
				clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	@Override
	public void print(String msg) throws IOException {
		out.writeObject(msg);
        out.flush();
	}

	@Override
	public String read() throws IOException, ClassNotFoundException {
		return (String) in.readObject();
	}

}
