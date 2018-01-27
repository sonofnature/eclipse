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
	private ArrayList<String> clicks;// �������λ������

	private JFrame frame;
	private JPanel panel;
	private JScrollPane spane;
	private JLabel label;
	private JLabel imlabel;
	private JTextField textfield;
	private JButton startbutton;// ��ʼ������ť
	private JButton okbutton;
	private JButton resetbutton;
	private JTextArea textarea;

	public GUI(final Client owner) {
		this.owner = owner;
		clicks = new ArrayList<>();

		// ��ʼ������
		Font font = new Font("TimesRoman", Font.PLAIN, 15);

		frame = new JFrame("��ҵ��Ϣ���ʲ�ѯ");
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

		label = new JLabel("�����������");
		panel.add(label);
		label.setBounds(20, 10, 100, 50);
		label.setFont(font);

		textfield = new JTextField();
		panel.add(textfield);
		textfield.setBounds(120, 10, 380, 50);
		textfield.setFont(font);

		startbutton = new JButton("��ѯ");
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

		resetbutton = new JButton("����");
		panel.add(resetbutton);
		resetbutton.setBounds(200, 550, 80, 40);
		resetbutton.setFont(font);
		resetbutton.addActionListener((e) -> resetAction());

		okbutton = new JButton("ȷ��");
		panel.add(okbutton);
		okbutton.setBounds(350, 550, 80, 40);
		okbutton.setFont(font);
		okbutton.addActionListener((e) -> okAction());

		spane = new JScrollPane();
		panel.add(spane);
		spane.setSize(600, 560);
		spane.setLocation(20, 70);

		textarea = new JTextArea("��ӭʹ�ã������������������������ҵ����������ƺ���\"��ѯ\"\r\n\r\n");
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

	/**
	 * "��ʼ"����
	 */
	public void startAction() {
		owner.startAction();
	}

	/**
	 * 
	 * @return �ı������ݣ�ȥ���հ�
	 */
	public String getKeyword() {
		return textfield.getText().trim();
	}

	/**
	 * ʹ��ѯ��ť��Ч
	 */
	public void enableQuery() {
		startbutton.setEnabled(true);
	}

	/**
	 * ʹ��ѯ��ť��Ч
	 */
	public void disableQuery() {
		startbutton.setEnabled(false);
	}

	/**
	 * ʹ����������Ч
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
	 * ʹ��������ʧЧ
	 */
	public void disableActive() {
		imlabel.setVisible(false);
		okbutton.setVisible(false);
		resetbutton.setVisible(false);
		spane.setVisible(true);
	}

	/**
	 * "ȷ��"��ť��Ӧ
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
	 * "����"��ť��Ӧ
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
