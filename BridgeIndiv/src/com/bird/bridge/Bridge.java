package com.bird.bridge;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.bird.util.Tool;
import com.bird.util.Messager;

/**
 * socket通信桥接器，对于桥接的两端负责实时传输数据
 */
public class Bridge {
	public static String ERROR_FILE = "error.txt";// 错误日志

	public static void main(String[] args) {
		Tool.errOutTo(ERROR_FILE);
		try {
			new Bridge().start();
		} catch (Exception e) {
			Tool.printErr(e);
		}
	}

	boolean start;// 开始信号量
	private GUI gui;
	private ServerSocket sevsock;
	private Socket socket1;
	private Socket socket2;

	public Bridge() {
		start = false;
		gui = new GUI(this);
	}

	public void start() {
		String sevIP;
		int sevport;
		while (start == false) {
			Tool.runNoException(() -> Thread.sleep(300));
		}
		sevIP = gui.getIP();
		sevport = gui.getPort();
		try {
			sevsock = new ServerSocket(sevport, 1);
		} catch (IOException e) {
			log("监听端口被占用：" + sevport);
			return;
		}

		log("开始工作");
		while (true) {
			try {
				socket1 = sevsock.accept();
				socket2 = new Socket(sevIP, sevport);
				log("桥接成功：" + socket1.getInetAddress() + "<===>" + socket2.getInetAddress());
				ArrayList<byte[]> sendBuf1 = new ArrayList<>();
				ArrayList<byte[]> recvBuf1 = new ArrayList<>();
				ArrayList<byte[]> sendBuf2 = new ArrayList<>();
				ArrayList<byte[]> recvBuf2 = new ArrayList<>();
				new Thread(new Messager(socket1, sendBuf1, recvBuf1)).start();
				new Thread(new Messager(socket2, sendBuf2, recvBuf2)).start();

				while (true) {
					if (recvBuf1.size() > 0) {
						byte[] b = getMsg(recvBuf1);
						if (b == null) {
							break;
						} else {
							addMsg(sendBuf2, b);
						}
					}
					if (recvBuf2.size() > 0) {
						byte[] b = getMsg(recvBuf2);
						if (b == null) {
							break;
						} else {
							addMsg(sendBuf1, b);
						}
					}
					Tool.runNoException(() -> Thread.sleep(10));
				}
			} catch (IOException e) {
				log("桥接失败");
			} finally {
				disconnect();
			}
		}
	}

	private void addMsg(ArrayList<byte[]> list, byte[] b) {
		synchronized (list) {
			list.add(b);
		}
	}

	private byte[] getMsg(ArrayList<byte[]> list) {
		byte[] b = new byte[0];
		synchronized (list) {
			if (list.size() > 0) {
				b = list.get(0);
				list.remove(0);
			}
		}
		return b;
	}

	/**
	 * 将两端的Socket都关闭
	 */
	void disconnect() {
		Tool.runNoException(() -> socket1.close());
		Tool.runNoException(() -> socket2.close());
		log("桥接断开");
	}

	void exit() {
		Tool.runNoException(() -> disconnect());
		Tool.runNoException(() -> sevsock.close());
		System.exit(0);
	}

	void log(String s) {
		gui.log(s);
	}
}

/************************************************************************************/
class GUI {

	private JFrame frame;
	private JPanel panel;
	private JLabel label1;
	private JLabel label2;
	private JTextField text1;
	private JTextField text2;
	private JButton button;
	private JScrollPane spanel;
	private JTextArea textarea;

	public GUI(final Bridge owner) {
		Font font = new Font("TimesRoman", Font.PLAIN, 15);

		frame = new JFrame("网络桥接器");
		frame.setSize(500, 500);
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

		label1 = new JLabel("服务器IP");
		label1.setFont(font);
		label1.setSize(100, 50);
		label1.setLocation(30, 10);
		panel.add(label1);

		text1 = new JTextField("192.168.40.59");
		text1.setFont(font);
		text1.setSize(200, 50);
		text1.setLocation(130, 10);
		panel.add(text1);

		label2 = new JLabel("端口");
		label2.setFont(font);
		label2.setSize(100, 70);
		label2.setLocation(30, 60);
		panel.add(label2);

		text2 = new JTextField("12346");
		text2.setFont(font);
		text2.setSize(200, 50);
		text2.setLocation(130, 70);
		panel.add(text2);

		button = new JButton("开始");
		button.setFont(font);
		button.setSize(80, 80);
		button.setLocation(370, 20);
		panel.add(button);
		button.addActionListener(e -> owner.start = true);

		spanel = new JScrollPane();
		spanel.setSize(450, 300);
		spanel.setLocation(20, 130);
		panel.add(spanel);

		textarea = new JTextArea();
		textarea.setFont(font);
		spanel.add(textarea);
		spanel.setViewportView(textarea);

		frame.setVisible(true);
	}

	String getIP() {
		return text1.getText();
	}

	int getPort() {
		return Integer.valueOf(text2.getText());
	}

	void log(String s) {
		// 如果日志过多，就清除一半
		if (textarea.getText().length() > 100000) {
			String s1 = textarea.getText();
			s1 = s1.substring(s1.length() / 2);
			textarea.setText(s1);
		}
		// 添加一行日志
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		String time = formatter.format(new Date());
		textarea.append(time + "  " + s + "\n");
		textarea.setCaretPosition(textarea.getText().length());
	}
}
