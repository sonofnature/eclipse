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
		// ��ʼ������
		frame = new JFrame("��ҵ��ʾ��Ϣ��ѯ������");
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

		textarea = new JTextArea("��������ʼ����\n\n");
		textarea.setFont(new Font("TimesRoman", Font.PLAIN, 15));
		spane.add(textarea);
		spane.setViewportView(textarea);

		frame.setVisible(true);
		frame.setExtendedState(JFrame.ICONIFIED);
	}

	/**
	 * ���ı���������ı���������κ������ַ���ͬʱ�Ὣ��궨λ�����
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
