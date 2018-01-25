package com.bird.accountcheck.server;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * 从界面上记录下用户鼠标滑过的轨迹，保存在文件中。以单击开始，以单击结束。
 * 
 * @author a
 *
 */
public class TrackRecorder2 implements MouseMotionListener, MouseListener {
	public static String TRACK_FILE = "track.txt";

	private long t0;
	private long x0;
	private long y0;
	private String onetrack;
	private FileWriter fw;
	private JFrame frame;
	private JPanel panel;
	private JLabel label;

	public static void main(String[] args) {
		new TrackRecorder2();
	}

	public TrackRecorder2() {
		onetrack = "";
		try {
			fw = new FileWriter(TRACK_FILE,true);
		} catch (IOException e) {
			e.printStackTrace();
		}

		frame = new JFrame("鼠标轨迹记录器");
		frame.setSize(1000, 700);
		frame.setLocation(300, 10);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});

		panel = new JPanel();
		panel.setBackground(Color.LIGHT_GRAY);
		panel.setLayout(null);
		frame.add(panel);

		label = new JLabel("不在记录");
		label.setBounds(280, 10, 100, 50);
		panel.add(label);

		frame.addMouseListener(this);
		frame.setVisible(true);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		long dt = System.currentTimeMillis() - t0;
		long x = e.getX() - x0;
		long y = e.getY() - y0;
		t0 = t0 + dt;
		onetrack += dt + "," + x + "," + y + "|";
	}

	@Override
	public void mouseClicked(MouseEvent e) {

		if (label.getText().equals("不在记录")) {// 开始记录
			frame.addMouseMotionListener(this);
			label.setText("正在记录");
			onetrack = "0,0,0|";
			t0 = System.currentTimeMillis();
			x0 = e.getX();
			y0 = e.getY();
		} else {// 结束一条记录
			frame.removeMouseMotionListener(this);
			label.setText("不在记录");
			saveTrack(onetrack);
		}
	}

	public void exit() {
		try {
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	private void saveTrack(String t) {
		try {
			fw.write(t + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

}
