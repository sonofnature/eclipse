package com.bird.accountcheck.client;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

class GUI implements MouseListener {
	private Client owner;
	private ArrayList<String> clicks;// 鼠标点击的位置序列

	private JFrame frame;
	private JPanel panel;
	private JScrollPane spane;
	private JLabel label;
	private JLabel imlabel;
	private JTextField textfield;
	private JButton startbutton;// 开始结束按钮
	private JButton okbutton;
	private JButton resetbutton;
	private JTextArea textarea;

	public GUI(final Client owner) {
		this.owner = owner;
		clicks = new ArrayList<>();

		// 初始化界面
		Font font = new Font("TimesRoman", Font.PLAIN, 15);

		frame = new JFrame("企业信息单笔查询");
		frame.setSize(650, 700);
		frame.setLocation(300, 10);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				owner.exit();
			}
		});

		panel = new JPanel();
		frame.add(panel);
		panel.setBackground(Color.LIGHT_GRAY);
		panel.setLayout(null);

		label = new JLabel("信用码或名称");
		panel.add(label);
		label.setBounds(20, 10, 100, 50);
		label.setFont(font);

		textfield = new JTextField();
		panel.add(textfield);
		textfield.setBounds(120, 10, 380, 50);
		textfield.setFont(font);

		startbutton = new JButton("查询");
		panel.add(startbutton);
		startbutton.setBounds(510, 10, 100, 50);
		startbutton.setFont(font);
		startbutton.addActionListener((e) -> startAction());

		imlabel = new JLabel();
		panel.add(imlabel);
		imlabel.setBounds(150, 130, 350, 370);
		imlabel.setHorizontalAlignment(JLabel.LEFT);
		imlabel.setVerticalAlignment(JLabel.TOP);
		imlabel.addMouseListener(this);

		resetbutton = new JButton("重置");
		panel.add(resetbutton);
		resetbutton.setBounds(200, 550, 80, 40);
		resetbutton.setFont(font);
		resetbutton.addActionListener((e) -> resetAction());

		okbutton = new JButton("确定");
		panel.add(okbutton);
		okbutton.setBounds(350, 550, 80, 40);
		okbutton.setFont(font);
		okbutton.addActionListener((e) -> okAction());

		spane = new JScrollPane();
		panel.add(spane);
		spane.setSize(600, 560);
		spane.setLocation(20, 70);

		textarea = new JTextArea("欢迎使用，请在上面的输入栏内输入企业信用码或名称后点击\"查询\"\r\n\r\n");
		spane.add(textarea);
		textarea.setFont(font);
		textarea.setLineWrap(true);
		spane.setViewportView(textarea);

		frame.setVisible(true);
		imlabel.setVisible(false);
		okbutton.setVisible(false);
		resetbutton.setVisible(false);
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

	/**
	 * "开始"动作
	 */
	public void startAction() {
		owner.startAction();
	}

	/**
	 * 
	 * @return 文本域内容，去除空白
	 */
	public String getKeyword() {
		return textfield.getText().trim();
	}

	/**
	 * 使查询按钮有效
	 */
	public void enableQuery() {
		startbutton.setEnabled(true);
	}

	/**
	 * 使查询按钮无效
	 */
	public void disableQuery() {
		startbutton.setEnabled(false);
	}

	/**
	 * 使交互界面有效
	 */
	public void enableActive(BufferedImage image) {
		imlabel.setIcon(new ImageIcon(image));
		spane.setVisible(false);
		imlabel.setVisible(true);
		okbutton.setVisible(true);
		resetbutton.setVisible(true);
		frame.setExtendedState(JFrame.NORMAL);
		frame.requestFocus();
	}

	/**
	 * 使交互界面失效
	 */
	public void disableActive() {
		imlabel.setVisible(false);
		okbutton.setVisible(false);
		resetbutton.setVisible(false);
		spane.setVisible(true);
	}

	/**
	 * "确定"按钮响应
	 */
	public void okAction() {
		String s = "";
		for (int i = 0; i < clicks.size(); i++) {
			s += clicks.get(i) + "|";
		}
		clicks.clear();
		owner.sendString(s);
		disableActive();
		owner.goon();
	}

	/**
	 * "重置"按钮响应
	 */
	public void resetAction() {
		clicks.clear();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		clicks.add(String.valueOf(e.getX()));
		clicks.add(String.valueOf(e.getY()));
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}
}
