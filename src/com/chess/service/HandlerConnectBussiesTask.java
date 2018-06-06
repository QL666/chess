package com.chess.service;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import javax.swing.JOptionPane;

import com.chess.game.ChessBoard;
import com.chess.game.Game;
import com.chess.game.Room;
import com.chess.pieces.BlackPiece;
import com.chess.pieces.WhitePiece;
import com.chess.server.Server;
import com.chess.util.CloseResource;
import com.chess.util.PointUtil;
import com.chess.util.ServerAndCilentUtil;

public class HandlerConnectBussiesTask implements Runnable {
	private ServerSocket server;
	private ServerSocket serverSocket;
	private Socket socket;
	private Room room;

	public HandlerConnectBussiesTask(ServerSocket server, ServerSocket serverSocket, Room room) {
		this.server = server;
		this.serverSocket = serverSocket;
		this.room = room;
	}

	public Socket getSocket() {
		return socket;
	}

	@Override
	public void run() {
		try {
			boolean surrender = false;
			List<Point> whiteList = ChessBoard.getWhiteList();
			
			while (true) {
				socket = server.accept();
				
				Game.connect = true;
				
				while (Judger.winner == Judger.UNKNOW) { // 没有获胜方
					InputStream inputStream = socket.getInputStream();
					
					BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
					
					String info = reader.readLine();
					
					if (info != null) {
						if (Room.CLOSE.equals(info)) { // 连接方逃跑
							Judger.escaper = WhitePiece.WHITE;
							break;
						}
						
						if (Room.CONFIRM_WIN.equals(info)) { // 接收到客户端发送的确认服务端胜利的信息
							
							break;
						}
						
						if (Room.SURRENDER.equals(info)) { // 收到连接方认输信息
							ServerAndCilentUtil.sendConfirmInfo(Room.CONFIRM_SURRENDER, socket); // 发送一个确认收到连接方的认输信息
							
							surrender = true;
							Judger.winner = BlackPiece.BLACK;
							break;
						}
						
						if (Room.CONFIRM_SURRENDER.equals(info)) { // 认输的消息得到确认
							
							break;
						}
						
						if (Room.PEACE.equals(info)) { // 对方请求和棋
							int confirm = JOptionPane.showConfirmDialog(room, "对方请求和棋,是否同意?");
							
							if (confirm == 0) { // 同意和棋
								ServerAndCilentUtil.sendConfirmInfo(Room.CONFIRM_PEACE, socket); // 发送同意和棋信息给客户端
								
								Judger.peace = true;
								break;
								
							} else { // 拒绝和棋
								ServerAndCilentUtil.sendConfirmInfo(Room.REFUSE_PEACE, socket);
								
								continue;
							}
							
						}
						
						if (Room.CONFIRM_PEACE.equals(info)) { // 对方同意和棋
							Judger.peace = true;
							break;
							
						}
						
						if (Room.REFUSE_PEACE.equals(info)) { // 对方不同意和棋
							JOptionPane.showMessageDialog(room, "对方不同意你的和棋请求!");
							continue;
						}
						
						if (Room.REGRET.equals(info)) {   // 对方请求悔棋
							int confirm = JOptionPane.showConfirmDialog(room, "对方请求悔棋,是否同意?");
							
							if (confirm == 0) { 
								ServerAndCilentUtil.sendConfirmInfo(Room.CONFIRM_REGRET, socket);  // 发送同意悔棋消息
								
								Point point = whiteList.remove(whiteList.size() - 1); // 删除最后一个白子
								/// 重新将坐标点加入集合
								List<Point> points = ChessBoard.getPoints();
								points.add(point);
								
								// 重绘
								room.getCb().repaint();
								
								// 后退
								BlackPiece.isDown = true;
								
							} else { // 拒绝对方悔棋
								ServerAndCilentUtil.sendConfirmInfo(Room.REFUSE_REGRET, socket);
							}
							
							continue;
							
						}
						
						
						if (Room.CONFIRM_REGRET.equals(info)) { // 对方同意悔棋
							// 删除最后一个黑子
							List<Point> blackList = ChessBoard.getBlackList();
							Point point = blackList.remove(blackList.size() - 1);
							
							/// 重新将坐标点加入集合
							List<Point> points = ChessBoard.getPoints();
							points.add(point);
							
							// 重绘
							room.getCb().repaint();
							
							// 后退
							room.setRegret(false);
							BlackPiece.isDown = false;
							continue;
						}
						
						if (Room.REFUSE_REGRET.equals(info)) { // 对方不同意悔棋
							JOptionPane.showMessageDialog(room, "对方不同意你的悔棋请求!");
							
							continue;
						}
						
						if (Room.TIMEOVER.equals(info)) { // 时间耗尽
							
							// 发送确认信息
							ServerAndCilentUtil.sendConfirmInfo(Room.CONFIRM_TIMEOVER, socket);
							
							break;
							
						}
						
						if (Room.CONFIRM_TIMEOVER.equals(info)) {
							break;
						}
						
						String[] split = info.split(",");
						
						Graphics g = room.getG();
						
						g.setColor(Color.WHITE);
						g.fillOval(Integer.parseInt(split[0]) - 10, Integer.parseInt(split[1]) - 10, 20, 20);
						g.setColor(Color.BLACK);
						room.setVisible(true);
						whiteList.add(new Point(Integer.parseInt(split[0]), Integer.parseInt(split[1])));
						
						if (split.length > 2) { // 白方获胜
							// 发送一个确认白方胜利的信息
							ServerAndCilentUtil.sendConfirmInfo(Room.CONFIRM_WIN, socket);
							
							Judger.winner = WhitePiece.WHITE;
							break;
						}
						
						PointUtil.removePoint(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
						
						room.setRegret(false);
						BlackPiece.isDown = false;
					}
					
					
				}
				int confirm = -1;    //// 0 1 2 -1
				if (Judger.escaper == WhitePiece.WHITE) {
					confirm = JOptionPane.showConfirmDialog(room, "白方玩家逃跑,你获得了胜利!是否继续?");
					
				}
				
				if (Judger.peace == true) {
					confirm = JOptionPane.showConfirmDialog(room, "和棋!是否继续?");
				}
				
				if (Judger.timeOver == BlackPiece.BLACK) {
					confirm = JOptionPane.showConfirmDialog(room, "时间用尽!你输了,是否继续?");
				}
				
				if (Judger.timeOver == WhitePiece.WHITE) {
					confirm = JOptionPane.showConfirmDialog(room, "对方时间用尽!你获得了胜利,是否继续?");
				}
				
				if (Judger.winner == BlackPiece.BLACK) {
					if (surrender) {
						confirm = JOptionPane.showConfirmDialog(room, "白方投降,你获得了胜利!是否继续?");
					} else {
						confirm = JOptionPane.showConfirmDialog(room, "你获得了胜利!是否继续?");
						
					}
					
				}
				
				if (Judger.winner == WhitePiece.WHITE && Judger.escaper != BlackPiece.BLACK) {
					confirm = JOptionPane.showConfirmDialog(room, "你输了!是否继续?");
					
				}
				if (confirm != 0) break;
				
				//////初始化
				Game.initProperty();
				room.getCb().repaint();
				room.waitPlayer();
				room.initTime();
			}
			

		} catch (IOException e) {
		} finally {
			CloseResource.close(socket);
			CloseResource.close(server);
			CloseResource.close(serverSocket);
			
			room.setVisible(false);
			CloseResource.free(room);
			
			new Game();
		}
	}

}
