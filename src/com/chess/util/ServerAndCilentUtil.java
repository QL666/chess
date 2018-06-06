package com.chess.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

import com.chess.game.Room;
import com.chess.service.Judger;

public class ServerAndCilentUtil {
	
	/**
	 * 发送落子情况
	 * @param x
	 * @param y
	 * @param color
	 * @throws IOException 
	 */
	public static void sendPoint(int x, int y, Socket socket) throws IOException {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			
			if (Judger.winner == Judger.UNKNOW) {
				writer.write(x + "," + y + "\r");
				
			} else {
				writer.write(x + "," + y + "," + Room.WIN + "\r");
			}
			
			writer.flush();
		
	}
	
	/**
	 * 发送确认信息
	 * @param info
	 * @param socket
	 * @throws IOException
	 */
	public static void sendConfirmInfo(String info, Socket socket) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

		writer.write(info + "\r");
		writer.flush();
	}
}
