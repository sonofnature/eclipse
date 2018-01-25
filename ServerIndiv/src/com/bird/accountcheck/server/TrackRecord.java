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

/* ͨ��һ�����ڲɼ��˹��ƶ����Ĺ켣�����浽track.txt�У������ļ���ȡ��δ�еľ��루0-220������ͼ�����˹�������Ȼ��ɼ����ݱ��浽�ļ��У�
 * ��������Ҫ��������ĳ������Ĺ켣ֻҪ���ļ����ҵ���Ӧ����������ɾ����Ȼ�����и��༴�ɣ�
 * track.txt���ݸ�ʽ���ļ���һ��˵����
 * ��ʼ״̬��ͬĿ¼����track.txt�ļ���
 * ��ֹ״̬��������������켣���ݵ��ļ��У�
 * */

public class TrackRecord implements MouseListener, MouseMotionListener {
	int d;// ·������
	int index;// �켣�����
	int sx, sy, x, y, x0, y0;// (x0,y0)�϶�ʱ�ϴ����λ��
	long st, t;
	Graphics g;
	JFrame frame;
	String trackfile;// ���·�������ļ�
	String s, shuoming;
	boolean red;// �յ����Ƿ��Ǻ�ɫ
	boolean[] good;// ��ʶĳ�����ȵĹ켣�Ƿ��ܴﵽһ���ĳɹ���
	int[][][] track;

	public static void main(String[] args) {
		new TrackRecord();
	}

	public TrackRecord() {
		// ��ʼ������
		frame = new JFrame("�˹�����϶��켣��¼��");
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
		// ��ȡ�켣�����������������
		for (int i = 0; i < 220; i++)
			good[i] = false;
		good[0] = true;// ����Ϊ0����Ϊ�ܶ���ʧ�ܣ�����������

		try {
			BufferedReader fr = new BufferedReader(new FileReader(trackfile));
			shuoming = fr.readLine();// ����˵��
			String s = null;
			while ((s = fr.readLine()) != null) {
				if (s.trim().isEmpty())
					continue;

				int dis = Integer.valueOf(s.trim());

				// ���ڶ���
				s = fr.readLine();
				StringTokenizer st = new StringTokenizer(s, "{,};");
				track[dis][0][0] = Integer.valueOf(st.nextToken());
				track[dis][0][1] = Integer.valueOf(st.nextToken());
				track[dis][0][2] = Integer.valueOf(st.nextToken());
				if (track[dis][0][1] + track[dis][0][2] < 5 || track[dis][0][1] > 2 * track[dis][0][2])// �ɹ��ʴﵽ��׼
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
		final int maxint = 2100000000;// Ϊ��ֹ�����������ֵ����
		try {
			BufferedWriter fw = new BufferedWriter(new FileWriter(trackfile));
			fw.write(shuoming + "\n");
			for (int i = 0; i < track.length; i++) {
				fw.write(i + "\n");

				// ��ֹ����̫�����
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
			g.drawString("���еĹ켣���м�¼��������5s���Զ��ر�", 200, 100);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			exit();
		}

		// ��ʼ��
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
		g.drawString("�밴������϶����ұߵ�����λ�ã�ֱ����ɺ��ߣ�·�����ȣ�" + d + "    ���ô�����" + track[d][0][0], 50, 100);
		g.drawLine(sx, sy - 100, sx, sy - 160);
		g.drawLine(sx + d, sy - 100, sx + d, sy - 160);
	}

	public void mouseReleased(MouseEvent e) {
		track[d][index][2] = -1;// ���켣��β

		if (x - sx == d) { // ���ƶ����ȸպ��������
			good[d] = true;
			track[d][0][0]++;// ���ô�����1
			track[d][0][1] = 0;
			track[d][0][2] = 0;
		}
	}

	public void mouseDragged(MouseEvent e) {
		// ��¼�켣
		x = e.getX();
		y = e.getY();
		t = System.currentTimeMillis();

		if (index >= track[d].length - 1)
			index = track[d].length - 2;
		track[d][index][0] = x - sx;
		track[d][index][1] = y - sy;
		track[d][index][2] = (int) (t - st);
		index++;

		// ����ϴ�����
		g.setColor(Color.WHITE);
		g.drawLine(x0, sy - 100, x0, sy - 160);

		// �����ڵ���
		g.setColor(Color.BLACK);
		g.drawLine(x, sy - 100, x, sy - 160);

		// ���պ����յ�ʱ�߱�ɺ�ɫ
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
