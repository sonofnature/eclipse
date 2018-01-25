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
 * �ӽ����ϼ�¼���û���껬���Ĺ켣���������ļ��С��Ե�����ʼ���Ե���������
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

		frame = new JFrame("���켣��¼��");
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

		label = new JLabel("���ڼ�¼");
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

		if (label.getText().equals("���ڼ�¼")) {// ��ʼ��¼
			frame.addMouseMotionListener(this);
			label.setText("���ڼ�¼");
			onetrack = "0,0,0|";
			t0 = System.currentTimeMillis();
			x0 = e.getX();
			y0 = e.getY();
		} else {// ����һ����¼
			frame.removeMouseMotionListener(this);
			label.setText("���ڼ�¼");
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
