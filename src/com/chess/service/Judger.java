package com.chess.service;

import com.chess.game.ChessBoard;
import com.chess.game.Game;
import com.chess.pieces.BlackPiece;
import com.chess.pieces.WhitePiece;

public class Judger {
	public static final int UNKNOW = -1;
	public static volatile int mover = BlackPiece.BLACK; // 哪方走
	public static volatile int winner = UNKNOW; // 胜利方
	public static volatile int escaper = UNKNOW; // 逃跑方
	public static volatile boolean peace = false;  // 和棋
	public static volatile int timeOver = UNKNOW; // 哪一方时间耗尽
	private static int[][] pieces = ChessBoard.getPieces();

	/**
	 * 判断是否有人获胜
	 * 
	 * @return 获胜方
	 */
	public static int judgeWinner() {
		int count = 0;

		for (int i = 0; i < pieces.length; i++) {
			for (int j = 0; j < pieces[i].length; j++) {
				if (pieces[i][j] == BlackPiece.BLACK) { // 黑子
					count = blackJudgeRight(i, j, BlackPiece.BLACK);
					if (count == Game.WIN_COUNT) {
						return BlackPiece.BLACK;
					}
					
					count = blackJudgeDown(i, j, BlackPiece.BLACK);
					if (count == Game.WIN_COUNT) {
						return BlackPiece.BLACK;
					}

					count = blackJudgeRightDown(i, j, BlackPiece.BLACK);
					if (count == Game.WIN_COUNT) {
						return BlackPiece.BLACK;
					}

					count = blackJudgeLeftDown(i, j, BlackPiece.BLACK);
					if (count == Game.WIN_COUNT) {
						return BlackPiece.BLACK;
					}
					
				} else if (pieces[i][j] == WhitePiece.WHITE) { // 白子
					count = blackJudgeRight(i, j, WhitePiece.WHITE);
					if (count == Game.WIN_COUNT) {
						return WhitePiece.WHITE;
					}

					count = blackJudgeDown(i, j, WhitePiece.WHITE);
					if (count == Game.WIN_COUNT) {
						return WhitePiece.WHITE;
					}

					count = blackJudgeRightDown(i, j, WhitePiece.WHITE);
					if (count == Game.WIN_COUNT) {
						return WhitePiece.WHITE;
					}

					count = blackJudgeLeftDown(i, j, WhitePiece.WHITE);
					if (count == Game.WIN_COUNT) {
						return WhitePiece.WHITE;
					}
				}
			}
		}
		
		return UNKNOW;
	}

	/**
	 * 走右边判断
	 * 
	 * @param i
	 * @param j
	 * @return
	 */
	private static int blackJudgeRight(int i, int j, int color) {
		// 右方没有子
		if (i == pieces.length)
			return 0;

		// 不是黑子
		if (pieces[i][j] != color)
			return 0;

		int count = 1;
		count += blackJudgeRight(++i, j, color);

		return count;
	}

	/**
	 * 走下边判断
	 * 
	 * @param i
	 * @param j
	 * @return
	 */
	private static int blackJudgeDown(int i, int j, int color) {
		// 下方没有子
		if (j == pieces[i].length)
			return 0;

		// 不是黑子
		if (pieces[i][j] != color)
			return 0;

		int count = 1;
		count += blackJudgeDown(i, ++j, color);

		return count;
	}

	/**
	 * 走右下边判断
	 * 
	 * @param i
	 * @param j
	 * @return
	 */
	private static int blackJudgeRightDown(int i, int j, int color) {
		// 右下方没有子
		if (i == pieces.length || j == pieces[i].length)
			return 0;

		// 不是黑子
		if (pieces[i][j] != color)
			return 0;

		int count = 1;

		count += blackJudgeRightDown(++i, ++j, color);

		return count;
	}

	/**
	 * 走左下边判断
	 * 
	 * @param i
	 * @param j
	 * @return
	 */
	private static int blackJudgeLeftDown(int i, int j, int color) {
		// 右下方没有子
		if (i == 0 || j == pieces[i].length)
			return 0;

		// 不是黑子
		if (pieces[i][j] != color)
			return 0;

		int count = 1;
		count += blackJudgeLeftDown(--i, ++j, color);

		return count;
	}
}
