package com.gacha.common;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	
	public static void main(String[] args) {
		try(ServerSocket serverSocket = new ServerSocket(9999)) {
			System.out.println("✅ 서버 시작: 포트 9999");
			while(true) {
				Socket clientSocket = serverSocket.accept();
				System.out.println("클라이언트 접속 " 
						+ clientSocket.getRemoteSocketAddress());
				ClientHandler handler = new ClientHandler(clientSocket);
				new Thread(handler).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
