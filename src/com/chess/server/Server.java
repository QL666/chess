package com.chess.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.chess.game.Game;
import com.chess.game.Room;
import com.chess.service.HandlerConnectBussiesTask;
import com.chess.service.HandlerConnectCountTask;

public class Server {
	private ServerSocket server;
	private HandlerConnectBussiesTask task;
	private ServerSocket serverSocket;
	
	public Server(Room room) throws IOException {
		init(room);
		
	}

	private void init(Room room) throws IOException {
		
		//////////////////////限制连接数量
		serverSocket  = new ServerSocket(Game.PORT2);
		
		Game.getThreadPool().execute(new HandlerConnectCountTask(serverSocket));
		
		server = new ServerSocket(Game.PORT);

		task = new HandlerConnectBussiesTask(server, serverSocket, room);
		Game.getThreadPool().execute(task);
	}
	
	public ServerSocket getServerSocket() {
		return serverSocket;
	}

	public Socket getSocket() {
		return task.getSocket();
	}

	public ServerSocket getServer() {
		return server;
	}
	
}
