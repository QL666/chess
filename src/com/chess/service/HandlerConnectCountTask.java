package com.chess.service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import com.chess.game.Game;
import com.chess.game.Room;
import com.chess.server.Server;
import com.chess.util.CloseResource;
import com.chess.util.ServerAndCilentUtil;

public class HandlerConnectCountTask implements Runnable {

	private ServerSocket serverSocket;

	public HandlerConnectCountTask(ServerSocket serverSocket) {
		super();
		this.serverSocket = serverSocket;
	}

	@Override
	public void run() {
		try {
			while (true) {
				Socket socket = serverSocket.accept();
				
				if (Game.connect) {
					ServerAndCilentUtil.sendConfirmInfo(Room.STARTED, socket);
					
				} else {
					ServerAndCilentUtil.sendConfirmInfo("\r", socket);
				}
				
			}
		} catch (IOException e) {
		} finally {
		}
	}

}
