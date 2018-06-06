package com.chess.game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.chess.event.MyWindowEvent;
import com.chess.pieces.BlackPiece;
import com.chess.pieces.WhitePiece;
import com.chess.server.Server;
import com.chess.service.Judger;
import com.chess.timer.BlackTimer;
import com.chess.timer.WhiteTimer;
import com.chess.util.PointUtil;
import com.chess.util.ServerAndCilentUtil;

/**
 * 主机创建的房间
 * 
 * @author ma
 *
 */
public class Room extends JFrame {
	public static final int WIDTH = 550;
	public static final int HEIGHT = 450;
	public static final int BOARD_WIDTH = 360;// 棋盘宽度

	public static final int ROW_LINE_SPACE = 30; // 横线间隔
	public static final int CRO_LINE_SPACE = 20; // 纵线间隔

	public static final int TIME = 5; // 一局的时间
	
	///////////////////////// 服务端与客户端通信信息
	public static final String STARTED = "true"; // 游戏已就绪
	public static final String CLOSE = "close"; // 房间关闭信息
	public static final String WIN = "win"; // 一方获胜
	public static final String PEACE = "peace"; // 一方求和
	public static final String REGRET = "regret"; // 一方悔棋
	public static final String SURRENDER = "surrender"; // 一方认输
	public static final String TIMEOVER = "timeover"; // 一方时间用尽

	public static final String CONFIRM_TIMEOVER = "confirm timeover"; // 确认时间用尽
	public static final String CONFIRM_REGRET = "confirm regret"; // 同意悔棋
	public static final String CONFIRM_PEACE = "confirm peace"; // 同意和棋
	public static final String CONFIRM_WIN = "confirm win"; // 确认收到获胜信息
	public static final String CONFIRM_SURRENDER = "confirm surrender"; // 确认收到认输信息
	
	public static final String REFUSE_REGRET = "refuse regret"; // 拒绝悔棋
	public static final String REFUSE_PEACE = "refuse peace"; // 拒绝和棋
	
	////////////////////////
	private volatile boolean regret = false; // 是否可以悔棋

	public void setRegret(boolean regret) {
		this.regret = regret;
	}

	private static final int CHOOSE_COUNT = 3;
	private static final int TEXT_COUNT = 6;
	private static final int PANEL_COUNT = 2;

	private JPanel[] jps = new JPanel[PANEL_COUNT];
	private ChessBoard cb = new ChessBoard();

	public ChessBoard getCb() {
		return cb;
	}

	private JButton[] jbs = new JButton[CHOOSE_COUNT];
	private JLabel[] jls = new JLabel[TEXT_COUNT];

	public JLabel[] getJls() {
		return jls;
	}

	private ClickButton clickB = new ClickButton(); // 点击按钮
	private MyMouseClick mmc = new MyMouseClick(); // 鼠标点击棋盘

	private Server server; // 服务端
	private Socket socket; // 客户端的socket

	private MyWindowEvent defaultEvent;

	public Server getServer() {
		return server;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public Room() {
		try {
			init();
		} catch (UnknownHostException e) {
			JOptionPane.showMessageDialog(this, "创建房间失败!");
		}
	}

	private void init() throws UnknownHostException {
		this.setTitle("五子棋联机版,您的主机ip:" + InetAddress.getLocalHost().getHostAddress());
		this.setBounds(Game.X, Game.Y, WIDTH, HEIGHT);
		this.setLayout(null);

		cb.setBounds(0, 0, BOARD_WIDTH, HEIGHT);

		for (int i = 0; i < jps.length; i++) {
			jps[i] = new JPanel();
		}

		//////////////////// 初始化面板
		jps[0].setBounds(BOARD_WIDTH, 0, WIDTH - BOARD_WIDTH, HEIGHT / 3);
		jps[0].setBackground(Color.CYAN);
		jps[0].setLayout(new GridLayout(3, 2));

		jps[1].setBounds(BOARD_WIDTH, HEIGHT / 3, WIDTH - BOARD_WIDTH, HEIGHT - jps[0].getHeight());
		jps[1].setBackground(Color.blue);
		jps[1].setLayout(null);

		//////////////////// 初始化标签
		jls[0] = new JLabel("限制时间: ");
		jls[1] = new JLabel(TIME + "分种");
		jls[2] = new JLabel("黑方用时: ");
		jls[3] = new JLabel("0分00秒");
		jls[4] = new JLabel("白方用时: ");
		jls[5] = new JLabel("0分00秒");

		for (int i = 0; i < jls.length; i++) {
			jps[0].add(jls[i]);
		}

		//////////////////// 初始化按钮
		jbs[0] = new JButton("认输");
		jbs[1] = new JButton("求和");
		jbs[2] = new JButton("悔棋");

		int x = 405;
		int y = 180;
		for (int i = 0; i < jbs.length; i++) {
			jbs[i].setBounds(x, y, 100, 35);
			jbs[i].addActionListener(clickB);
			this.add(jbs[i]);
			y += 60;
		}

		cb.addMouseListener(mmc);
		this.add(cb);
		this.add(jps[0]);
		this.add(jps[1]);

		this.setResizable(false);
		this.setVisible(true);

	}

	private Graphics g;// 画笔

	public Graphics getG() {
		return g;
	}

	private MyWindowEvent serverEvent; // 服务端窗口事件
	private MyWindowEvent cilentEvent; // 客户端窗口事件

	private List<Point> blackList;
	private List<Point> whiteList;

	/**
	 * 等待玩家加入游戏
	 * 
	 * @param room
	 */
	public void waitPlayer() {
		regret = false;
		defaultEvent = new MyWindowEvent(server.getServer(), server.getServerSocket(), this);
		this.addWindowListener(defaultEvent);

		for (int i = 0; i < jbs.length; i++) {
			jbs[i].setEnabled(false);

		}

		Game.getThreadPool().execute(new Runnable() {

			@Override
			public void run() {
				while (!Game.connect) {

				}
				startGame(BlackPiece.BLACK);
			}
		});
	}

	private BlackTimer bTimer;
	private WhiteTimer wTimer;
	
	public void initTime() {
		jls[3].setText("00:00");
		jls[5].setText("00:00");
	}
	
	/**
	 * 开始游戏
	 */
	public void startGame(int color) {
		initTime();
		
		bTimer = new BlackTimer(this);
		bTimer.start();
		
		wTimer = new WhiteTimer(this);
		wTimer.start();
		
		this.removeWindowListener(defaultEvent);

		for (int i = 0; i < jbs.length; i++) {
			jbs[i].setEnabled(true);

		}

		if (color == BlackPiece.BLACK) {
			Judger.mover = BlackPiece.BLACK;// 黑方
			serverEvent = new MyWindowEvent(server.getServer(), server.getServerSocket(), this);
			this.addWindowListener(serverEvent);
			JOptionPane.showMessageDialog(this, "有玩家进入了游戏");

		} else if (color == WhitePiece.WHITE) {
			Judger.mover = WhitePiece.WHITE;// 白方
			// 后手
			WhitePiece.isDown = true;
			BlackPiece.isDown = true;

			cilentEvent = new MyWindowEvent(socket, this);
			this.addWindowListener(cilentEvent);
			JOptionPane.showMessageDialog(this, "成功加入游戏");
		}

		g = cb.getGraphics();
		blackList = ChessBoard.getBlackList();
		whiteList = ChessBoard.getWhiteList();
	}

	// 表示棋子位置
	private static int[][] pieces = ChessBoard.getPieces();

	class MyMouseClick extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			if (!Game.connect)
				return;

			int x = e.getX();
			int y = e.getY();

			int[] point = PointUtil.judgePoint(x, y);
			if (point != null && Judger.winner == Judger.UNKNOW && Judger.escaper == Judger.UNKNOW) {
				boolean flag = false;

				if (BlackPiece.isDown == false && Judger.mover == BlackPiece.BLACK) { // 黑方走的
					try {
						pieces[point[0] / 20][point[1] / 30] = BlackPiece.BLACK; // 定位黑棋
						blackList.add(new Point(point[0], point[1]));

						if (blackList.size() > 4) {
							int winner = Judger.judgeWinner();
							if (winner == BlackPiece.BLACK) { // 主机获胜
								Judger.winner = BlackPiece.BLACK;
							}

						}

						ServerAndCilentUtil.sendPoint(point[0], point[1], server.getSocket());

					} catch (IOException e1) {

					} finally {
						regret = true;
						BlackPiece.isDown = true; // 黑方已经落子
						flag = true;
					}

				} else if (WhitePiece.isDown == false && BlackPiece.isDown && Judger.mover == WhitePiece.WHITE) {// 黑方走完，白方走
					try {
						pieces[point[0] / 20][point[1] / 30] = WhitePiece.WHITE; // 定位白棋
						whiteList.add(new Point(point[0], point[1]));

						if (whiteList.size() > 4) {
							int winner = Judger.judgeWinner();

							if (winner == WhitePiece.WHITE) { // 白方获胜
								Judger.winner = WhitePiece.WHITE;
							}

						}

						ServerAndCilentUtil.sendPoint(point[0], point[1], socket);

					} catch (IOException e1) {
					} finally {
						regret = true;
						WhitePiece.isDown = true; // 白方落子
						BlackPiece.isDown = false; // 黑方未落
						flag = true;
					}
				}

				if (flag) { // 有效的点击
					PointUtil.removePoint(point[0], point[1]);
					g.fillOval(point[0] - 10, point[1] - 10, 20, 20);// 以半径画圆
				}

			}
		}

	}

	class ClickButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (!Game.connect)
				return;

			if (e.getSource() == jbs[0]) {
				int confirm = JOptionPane.showConfirmDialog(Room.this, "你确定认输吗?");

				if (confirm == 0) { // 确定认输
					try {
						if (Judger.mover == BlackPiece.BLACK) { // 黑方认输
							Socket s = server.getSocket();

							ServerAndCilentUtil.sendConfirmInfo(SURRENDER, s);

							Judger.winner = WhitePiece.WHITE;

						} else { // 白方认输
							ServerAndCilentUtil.sendConfirmInfo(SURRENDER, socket);

							Judger.winner = BlackPiece.BLACK;
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}

			} else if (e.getSource() == jbs[1]) {
				int confirm = JOptionPane.showConfirmDialog(Room.this, "你确定要求和吗?");

				if (confirm == 0) { // 确定求和
					try {
						if (Judger.mover == BlackPiece.BLACK) { // 黑方求和
							Socket s = server.getSocket();

							ServerAndCilentUtil.sendConfirmInfo(PEACE, s);

						} else { // 白方求和
							ServerAndCilentUtil.sendConfirmInfo(PEACE, socket);

						}
					} catch (IOException e1) {

					}
				}
				
			} else if (e.getSource() == jbs[2]) {
				if (regret) { // 可以悔棋
					int confirm = JOptionPane.showConfirmDialog(Room.this, "你确定要悔棋吗?");
					
					if (confirm == 0) { // 确认悔棋
						try {
							if (Judger.mover == BlackPiece.BLACK) { // 黑方请求悔棋
								Socket s = server.getSocket();
								
								ServerAndCilentUtil.sendConfirmInfo(REGRET, s);
								
							} else { // 白方请求悔棋
								ServerAndCilentUtil.sendConfirmInfo(REGRET, socket);
								
							}
						} catch (IOException e1) {
							
						}
						
					}

				} else {
					JOptionPane.showMessageDialog(Room.this, "现在不能悔棋");
				}
			}
		}
	}
}
