package com.chess.util;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class CloseResource {
	public static void free(Object object) {
		object = null;
		System.gc();
	}
	
	public static void close(Object object) {
		if (object != null) {
			try {
				if (object instanceof Socket) {
					Socket socket = (Socket)object;
					if (!socket.isClosed()) {
						socket.close();
					}
				}
				
				if (object instanceof ServerSocket) {
					ServerSocket socket = (ServerSocket)object;
					if (!socket.isClosed()) {
						socket.close();
					}
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
}
