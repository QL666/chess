package com.chess.timer;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JLabel;

import com.chess.game.Room;
import com.chess.pieces.BlackPiece;
import com.chess.pieces.WhitePiece;
import com.chess.service.Judger;
import com.chess.util.ServerAndCilentUtil;

public class BlackTimer {
	private int minite = 0; // 分种
	private int second = 0; // 秒钟
	private long delayTime = 1000; // 计时的延迟时间
	private final long period = 1000; // 计时周期

	public int getMinite() {
		return minite;
	}

	public void setMinite(int minite) {
		this.minite = minite;
	}

	public int getSecond() {
		return second;
	}

	public void setSecond(int second) {
		this.second = second;
	}

	public long getDelayTime() {
		return delayTime;
	}

	public void setDelayTime(long delayTime) {
		this.delayTime = delayTime;
	}

	private Timer timer;
	private TimerTask task;

	public BlackTimer(final Room room) {
		// 标签
		final JLabel[] jls = room.getJls();
		//////////// 计时器
		timer = new Timer();

		task = new TimerTask() {

			@Override
			public void run() {
				if ((BlackPiece.isDown == false && Judger.mover == BlackPiece.BLACK) || 
						(WhitePiece.isDown == true && Judger.mover == WhitePiece.WHITE)) { // 两方计时
					second++;
					if (second == 60) { // 60秒
						second = 0;
						minite++;
						
						
					}

					if (minite == 0) {
						jls[3].setText("0分:" + second + "秒");

					} else {
						jls[3].setText(minite + "分:" + second + "秒");
					}
					
					try {
						if (minite == Room.TIME) { // 主机方时间耗尽
							if (Judger.mover == WhitePiece.WHITE) {
								ServerAndCilentUtil.sendConfirmInfo(Room.TIMEOVER, room.getSocket());
								
							}
							Judger.timeOver = BlackPiece.BLACK;
							cancel();
						}
					} catch (IOException e) {
					}
				} 

			}
		};
	}

	public void start() {
		timer.schedule(task, delayTime, period);
	}

	public void cancel() {
		timer.cancel();
	}
}
