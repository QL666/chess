package com.chess.service;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;

import javax.swing.JOptionPane;

import com.chess.game.ChessBoard;
import com.chess.game.Game;
import com.chess.game.Room;
import com.chess.pieces.BlackPiece;
import com.chess.pieces.WhitePiece;
import com.chess.util.CloseResource;
import com.chess.util.PointUtil;
import com.chess.util.ServerAndCilentUtil;

public class ReceiveServerTask implements Runnable {
	private Socket socket;
	private Room room;

	public ReceiveServerTask(Socket socket, Room room) {
		this.socket = socket;
		this.room = room;
	}

	@Override
	public void run() {
		boolean surrender = false;
		try {
			List<Point> blackList = ChessBoard.getBlackList();
			while (Judger.winner == Judger.UNKNOW) { // 没有获胜方一直运行
				InputStream inputStream = socket.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

				String info = reader.readLine();
				if (info != null) {
					if (Room.CLOSE.equals(info)) { // 主机逃跑
						Judger.escaper = BlackPiece.BLACK;
						break;
					}

					if (Room.CONFIRM_WIN.equals(info)) { // 接收到服务端发送的确认客户端胜利的信息
						break;
					}

					if (Room.SURRENDER.equals(info)) { // 收到主机方认输信息
						// 发送一个确认收到主机方认输的消息
						ServerAndCilentUtil.sendConfirmInfo(Room.CONFIRM_SURRENDER, socket);

						surrender = true;
						Judger.winner = WhitePiece.WHITE;
						break;
					}

					if (Room.CONFIRM_SURRENDER.equals(info)) { // 发送给主机的认输信息得到确认
						break;

					}

					if (Room.PEACE.equals(info)) { // 收到主机方求和信息
						int confirm = JOptionPane.showConfirmDialog(room, "对方请求和棋,是否同意?");

						if (confirm == 0) { // 同意和棋
							ServerAndCilentUtil.sendConfirmInfo(Room.CONFIRM_PEACE, socket); // 发送同意和棋信息给服务端

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

					if (Room.REGRET.equals(info)) { // 对方请求悔棋
						int confirm = JOptionPane.showConfirmDialog(room, "对方请求悔棋,是否同意?");

						if (confirm == 0) {
							ServerAndCilentUtil.sendConfirmInfo(Room.CONFIRM_REGRET, socket); // 发送同意悔棋消息

							Point point = blackList.remove(blackList.size() - 1); // 删除最后一个黑子
							/// 重新将坐标点加入集合
							List<Point> points = ChessBoard.getPoints();
							points.add(point);

							// 重绘
							room.getCb().repaint();

							// 后退
							WhitePiece.isDown = true;
							BlackPiece.isDown = false;
							
						} else { // 不同意悔棋
							ServerAndCilentUtil.sendConfirmInfo(Room.REFUSE_REGRET, socket);
							
						}

						continue;
					}

					if (Room.CONFIRM_REGRET.equals(info)) { // 对方同意悔棋
						// 删除最后一个白子
						List<Point> whiteList = ChessBoard.getWhiteList();
						Point point = whiteList.remove(whiteList.size() - 1);

						/// 重新将坐标点加入集合
						List<Point> points = ChessBoard.getPoints();
						points.add(point);

						// 重绘
						room.getCb().repaint();

						// 后退
						room.setRegret(false);
						WhitePiece.isDown = false;
						BlackPiece.isDown = true;
						continue;

					}
					
					if (Room.REFUSE_REGRET.equals(info)) { // 对方不同意悔棋
						JOptionPane.showMessageDialog(room, "对方不同意你的悔棋请求!");
						
						continue;
					}
					
					if (Room.TIMEOVER.equals(info)) { // 时间用尽
						ServerAndCilentUtil.sendConfirmInfo(Room.CONFIRM_TIMEOVER, socket);
						
						break;
						
					}
					
					if (Room.CONFIRM_TIMEOVER.equals(info)) {
						break;
					}

					String[] split = info.split(",");
					Graphics g = room.getG();
					g.setColor(Color.BLACK);
					g.fillOval(Integer.parseInt(split[0]) - 10, Integer.parseInt(split[1]) - 10, 20, 20);
					room.setVisible(true);
					blackList.add(new Point(Integer.parseInt(split[0]), Integer.parseInt(split[1])));

					if (split.length > 2) { // 主机方获胜
						// 发送确认信息
						ServerAndCilentUtil.sendConfirmInfo(Room.CONFIRM_WIN, socket);

						Judger.winner = BlackPiece.BLACK;
						break;
					}

					g.setColor(Color.WHITE);

					PointUtil.removePoint(Integer.parseInt(split[0]), Integer.parseInt(split[1]));

					room.setRegret(false);

					WhitePiece.isDown = false; // 白方标记为未走
					BlackPiece.isDown = true;
				}

			}
		} catch (Exception e) {
		} finally {
			if (Judger.escaper == BlackPiece.BLACK) {
				JOptionPane.showMessageDialog(room, "主机退出了房间!你获得了胜利");

			}

			if (Judger.peace == true) {
				JOptionPane.showMessageDialog(room, "和棋!");
			}

			if (Judger.winner == BlackPiece.BLACK && Judger.escaper != WhitePiece.WHITE) {
				JOptionPane.showMessageDialog(room, "你输了!");

			}
			
			if (Judger.timeOver == WhitePiece.WHITE) {
				JOptionPane.showMessageDialog(room, "时间用尽!你输了!");
			}
			
			if (Judger.timeOver == BlackPiece.BLACK) {
				JOptionPane.showMessageDialog(room, "对方时间用尽!你获得了胜利!");
			}

			if (Judger.winner == WhitePiece.WHITE) {
				if (surrender) {
					JOptionPane.showMessageDialog(room, "黑方投降,你获得了胜利!");
				} else {
					JOptionPane.showMessageDialog(room, "你获得了胜利!");

				}
			}

			room.setVisible(false);
			CloseResource.free(room);

			new Game();
		}
	}

}
