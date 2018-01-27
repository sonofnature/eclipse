package com.bird.accountcheck.server;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

class GUI  {
	private JFrame frame;
	private JPanel panel;
	private JScrollPane spane;
	private JTextArea textarea;

	public GUI(Server owner) {
		// 初始化界面
		frame = new JFrame("企业公示信息查询服务器");
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
		spane.setSize(600, 610);
		spane.setLocation(20, 20);
		panel.add(spane);

		textarea = new JTextArea("服务器开始工作\n\n");
		textarea.setFont(new Font("TimesRoman", Font.PLAIN, 15));
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
}
