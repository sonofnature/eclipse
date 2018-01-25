package com.bird.accountcheck.server;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

class GUI implements MouseListener, KeyListener {
	private JFrame frame;
	private JPanel panel;
	private JScrollPane spane;
	private JTextArea textarea;

	public GUI(Server owner) {
		// 初始化界面
		frame = new JFrame("账户年检服务器");
		frame.setSize(650, 700);
		frame.setLocation(300, 10);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				owner.exit();
			}
		});

		panel = new JPanel();
		panel.setBackground(Color.LIGHT_GRAY);
		panel.setLayout(null);
		frame.add(panel);

		spane = new JScrollPane();
		spane.setSize(600, 560);
		spane.setLocation(20, 70);
		panel.add(spane);

		textarea = new JTextArea();
		textarea.setFont(new Font("TimesRoman", Font.PLAIN, 15));
		textarea.addMouseListener(this);
		//textarea.addKeyListener(this);

		spane.add(textarea);
		spane.setViewportView(textarea);

		frame.setVisible(true);
		frame.setExtendedState(JFrame.ICONIFIED);
	}

	/**
	 * 往文本区域添加文本，不添加任何其他字符，同时会将光标定位到最后
	 * 
	 * @param s
	 */
	public void addText(String s) {
		textarea.append(s);
		int len = textarea.getText().length();
		textarea.setCaretPosition(len);
	}

	public String getText() {
		return textarea.getText();
	}

	public void setText(String s) {
		textarea.setText(s);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		addText("keyPress:" + System.currentTimeMillis()+"\n");
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		addText("keyRelease:" + System.currentTimeMillis()+"\n");

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		addText("mousePressed:" + System.currentTimeMillis()+"\n");

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		addText("mouseReleased:" + System.currentTimeMillis()+"\n");

	}

}
