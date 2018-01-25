package com.bird.accountcheck.server;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.swing.JFrame;

import com.bird.util.Tool;

/* 通过一个窗口采集人工移动鼠标的轨迹，保存到track.txt中，它从文件中取出未有的距离（0-220）生成图像让人工滑动，然后采集数据保存到文件中；
 * 因此如果想要重新生成某个距离的轨迹只要在文件中找到对应的两行数据删除，然后运行该类即可；
 * track.txt数据格式见文件第一行说明。
 * 初始状态：同目录下有track.txt文件；
 * 终止状态：添加了若干条轨迹数据到文件中；
 * */

public class TrackRecord implements MouseListener, MouseMotionListener {
	int d;// 路径长度
	int index;// 轨迹点序号
	int sx, sy, x, y, x0, y0;// (x0,y0)拖动时上次鼠标位置
	long st, t;
	Graphics g;
	JFrame frame;
	String trackfile;// 鼠标路径生成文件
	String s, shuoming;
	boolean red;// 终点线是否是红色
	boolean[] good;// 标识某个长度的轨迹是否能达到一定的成功率
	int[][][] track;

	public static void main(String[] args) {
		new TrackRecord();
	}

	public TrackRecord() {
		// 初始化窗口
		frame = new JFrame("人工鼠标拖动轨迹记录器");
		frame.setSize(600, 600);
		frame.setBackground(Color.WHITE);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addMouseListener(this);
		frame.addMouseMotionListener(this);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});
		frame.setVisible(true);

		red = false;
		trackfile = "track.txt";
		g = frame.getGraphics();
		good = new boolean[220];
		track = new int[220][300][3];

		for (int i = 0; i < track.length; i++) {
			track[i][0][0] = 0;
			track[i][0][1] = 0;
			track[i][0][2] = 0;
			track[i][1][2] = -1;
		}
		// 读取轨迹，并标出符合条件的
		for (int i = 0; i < 220; i++)
			good[i] = false;
		good[0] = true;// 距离为0的因为很多误报失败，故特殊设置

		try {
			BufferedReader fr = new BufferedReader(new FileReader(trackfile));
			shuoming = fr.readLine();// 跳过说明
			String s = null;
			while ((s = fr.readLine()) != null) {
				if (s.trim().isEmpty())
					continue;

				int dis = Integer.valueOf(s.trim());

				// 读第二行
				s = fr.readLine();
				StringTokenizer st = new StringTokenizer(s, "{,};");
				track[dis][0][0] = Integer.valueOf(st.nextToken());
				track[dis][0][1] = Integer.valueOf(st.nextToken());
				track[dis][0][2] = Integer.valueOf(st.nextToken());
				if (track[dis][0][1] + track[dis][0][2] < 5 || track[dis][0][1] > 2 * track[dis][0][2])// 成功率达到标准
					good[dis] = true;

				int i = 1;
				while (st.hasMoreTokens()) {
					track[dis][i][0] = Integer.valueOf(st.nextToken());
					track[dis][i][1] = Integer.valueOf(st.nextToken());
					track[dis][i][2] = Integer.valueOf(st.nextToken());
					i++;
				}
				track[dis][i][2] = -1;
			}
			fr.close();
		} catch (Exception e) {
			Tool.printErr(e);
		}
	}

	protected void exit() {
		final int maxint = 2100000000;// 为防止超过整数最大值设置
		try {
			BufferedWriter fw = new BufferedWriter(new FileWriter(trackfile));
			fw.write(shuoming + "\n");
			for (int i = 0; i < track.length; i++) {
				fw.write(i + "\n");

				// 防止数字太大溢出
				if (track[i][0][0] > maxint)
					track[i][0][0] = maxint;
				if (track[i][0][1] > maxint || track[i][0][2] > maxint) {
					track[i][0][1] = track[i][0][1] / 2;
					track[i][0][2] = track[i][0][2] / 2;
				}

				for (int j = 0; j < track[i].length; j++) {
					if (track[i][j][2] == -1)
						break;
					fw.write("{" + track[i][j][0] + "," + track[i][j][1] + "," + track[i][j][2] + "};");
				}
				fw.newLine();
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	public void mousePressed(MouseEvent e) {
		for (d = 0; d < 220; d++) {
			if (!good[d])
				break;
		}
		if (d == 220) {
			g.clearRect(0, 0, frame.getWidth(), frame.getHeight());
			g.setColor(Color.BLACK);
			g.drawString("所有的轨迹都有记录，程序将在5s后自动关闭", 200, 100);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			exit();
		}

		// 初始化
		red = false;
		sx = e.getX();
		sy = e.getY();
		st = System.currentTimeMillis();
		x0 = sx;
		y0 = sy;
		index = 2;
		track[d][1][0] = 0;
		track[d][1][1] = 0;
		track[d][1][2] = 0;

		g.clearRect(0, 0, frame.getWidth(), frame.getHeight());
		g.setColor(Color.BLACK);
		g.drawString("请按下鼠标拖动到右边的竖线位置，直到变成红线，路径长度：" + d + "    重置次数：" + track[d][0][0], 50, 100);
		g.drawLine(sx, sy - 100, sx, sy - 160);
		g.drawLine(sx + d, sy - 100, sx + d, sy - 160);
	}

	public void mouseReleased(MouseEvent e) {
		track[d][index][2] = -1;// 给轨迹结尾

		if (x - sx == d) { // 当移动长度刚好是所需的
			good[d] = true;
			track[d][0][0]++;// 重置次数加1
			track[d][0][1] = 0;
			track[d][0][2] = 0;
		}
	}

	public void mouseDragged(MouseEvent e) {
		// 记录轨迹
		x = e.getX();
		y = e.getY();
		t = System.currentTimeMillis();

		if (index >= track[d].length - 1)
			index = track[d].length - 2;
		track[d][index][0] = x - sx;
		track[d][index][1] = y - sy;
		track[d][index][2] = (int) (t - st);
		index++;

		// 清除上次线条
		g.setColor(Color.WHITE);
		g.drawLine(x0, sy - 100, x0, sy - 160);

		// 画现在的线
		g.setColor(Color.BLACK);
		g.drawLine(x, sy - 100, x, sy - 160);

		// 当刚好在终点时线变成红色
		if (x - sx == d) {
			red = true;
			g.setColor(Color.RED);
			g.drawLine(sx + d, sy - 100, sx + d, sy - 160);
		} else {
			if (red) {
				red = false;
				g.setColor(Color.BLACK);
				g.drawLine(sx + d, sy - 100, sx + d, sy - 160);
			}
		}
		x0 = x;
		y0 = y;
	}

	public void mouseClicked(MouseEvent arg0) {
	}

	public void mouseEntered(MouseEvent arg0) {
	}

	public void mouseExited(MouseEvent arg0) {
	}

	public void mouseMoved(MouseEvent e) {
	}
}
