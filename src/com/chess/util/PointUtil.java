package com.chess.util;

import java.awt.Point;
import java.util.Iterator;
import java.util.List;

import com.chess.game.ChessBoard;

public class PointUtil {
	public static final int LEGAL_SPACE = 8; // 允许的点击误差
	
	/**
	 * 判断坐标点是否存在
	 * @param x
	 * @param y
	 * @return
	 */
	public static int[] judgePoint(int x, int y) {
		List<Point> points = ChessBoard.getPoints();
		int [] ps = new int[2];
		
		Iterator<Point> iterator = points.iterator();
		
		while (iterator.hasNext()) {
			Point point = iterator.next();
			double x2 = point.getX();
			double y2 = point.getY();
			
			if ((x < x2 + LEGAL_SPACE && x > x2 - LEGAL_SPACE && y < y2 + LEGAL_SPACE && y > y2 - LEGAL_SPACE)) {
				ps[0] = (int) x2;
				ps[1] = (int) y2;
				
				return ps;
			}
		}
		
		return null;
	}
	
	/**
	 * 删除对应的坐标点
	 * @param x
	 * @param y
	 */
	public static void removePoint(int x, int y) {
		List<Point> points = ChessBoard.getPoints();
		
		Iterator<Point> iterator = points.iterator();
		
		while (iterator.hasNext()) {
			Point point = iterator.next();
			double x2 = point.getX();
			double y2 = point.getY();
			
			if (x == x2 && y == y2) {
				iterator.remove();
				break;
			}
		}
	}
}
