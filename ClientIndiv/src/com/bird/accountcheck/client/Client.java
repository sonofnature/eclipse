package com.bird.accountcheck.client;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;
import javax.imageio.ImageIO;
import com.bird.util.Tool;
import com.melloware.jintellitype.JIntellitype;
import com.bird.util.Messager;

public class Client {

	public static final String LOG_FOLD = "log";
	public static final String CONFIG_FILE = "config.txt";
	public static final String ERROR_FILE = "error.txt";

	public static void main(String[] args) {
		Tool.errOutTo(ERROR_FILE);
		try {
			new Client().start();
		} catch (Exception e) {
			Tool.printErr(e, "");
		}
	}

	// 共享变量，多线程操作
	private boolean start;
	private boolean stop;
	private ArrayList<byte[]> sendBuf;
	private ArrayList<byte[]> recvBuf;

	private GUI gui;
	private Logger logger;
	private DBSave dbsave;// 数据库保存
	private Socket socket;
	private HotKeyProcessor hkp;

	public Client() {
		start = false;
		gui = new GUI(this);
		logger = new Logger(gui);
	}

	public void start() {
		// 连接数据库，失败不影响其他功能
		dbsave = new DBSave(this);
		// 设置热键,即使出现异常也不影响其他功能
		Tool.runNoException(() -> hkp = new HotKeyProcessor());
		Tool.runNoException(() -> JIntellitype.getInstance().addHotKeyListener(hkp));

		while (true) {
			Tool.waitUntil(() -> start, 100);
			gui.disableQuery();
			start = false;
			if (connectSev() == false) {// 连接服务器
				gui.enableQuery();
				continue;
			}
			// 服务器连接成功
			String keyword = gui.getKeyword();
			try {
				String[] value = query(keyword);
				String type = value[value.length - 1];

				if (type.equals("error") || type.isEmpty()) {
					log("网站返回出错，请稍后重查");
				} else if (type.equals("null")) {
					log("查询结果为空，请确认查询关键字是否正确");
				} else {// 查询成功
					log("查询成功，请核对(小提示：按下CTRL+括号里的字母可快速复制粘贴，一般复制请按CTRL+C)：\n");
					showResult(value);
					Tool.runNoException(() -> hkp.setValue(value));// 给热键赋值
				}
			} catch (IOException e) {
				log("网络故障，请稍后重试");
			} finally {
				Tool.runNoException(() -> socket.close());
			}
			// 休息一分钟后才允许继续查
			log("休息，休息一下...");
			Tool.runNoException(() -> Thread.sleep(60000));
			gui.enableQuery();
		}
	}

	/**
	 * 根据配置文件连接服务器，等待响应时间超时也算失败
	 * 
	 * @依赖：服务器配置文件configfile
	 * @影响：连接成功则初始化好socket，启动监听线程，通过sendBuf、recvBuf与主线程通信
	 * @return 是否连接成功
	 */
	private boolean connectSev() {
		String sevIP = "66.234.40.37";
		int port = 12346;
		try {
			File f = new File(CONFIG_FILE);
			BufferedReader br = new BufferedReader(new FileReader(f));
			sevIP = br.readLine().trim();
			port = Integer.valueOf(br.readLine().trim());
			br.close();
		} catch (IOException | NumberFormatException e) {
		}

		String s = "";
		try {
			socket = new Socket(sevIP, port);
			sendBuf = new ArrayList<>();
			recvBuf = new ArrayList<>();
			new Thread(new Messager(socket, sendBuf, recvBuf)).start();

			sendString("hi");
			// 10s等待服务器回复
			long t1 = System.currentTimeMillis() + 10000;
			Tool.waitUntil(() -> System.currentTimeMillis() > t1 || recvBuf.size() > 0, 1000);
			if (recvBuf.size() > 0)
				s = recvString();
		} catch (IOException e) {
		}
		if (s == null || !s.equals("hi")) {
			Tool.runNoException(() -> socket.close());
			log("服务器忙，请稍后重试:" + sevIP + ":" + port);
			return false;
		} else {// 接受到"hi"
			log("连接服务器成功：" + sevIP + ":" + port);
			return true;
		}
	}

	/**
	 * 从返回结果中找出类型字段（第三个字段）
	 * 
	 * @param s
	 *            结果字符串
	 * @return 如果正常返回第四个字段，非正常返回空串
	 */

	private String getType(String s) {
		try {
			StringTokenizer st = new StringTokenizer(s, "|", true);
			int count = 0;
			while (count <= 2) {
				s = st.nextToken();
				if (s.equals("|"))
					count++;
			}
			return st.nextToken();
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * 查询一家企业，返回对应于数据库各字段的值的数组
	 * 
	 * @param keyword
	 * @return
	 * @throws IOException
	 */
	private String[] query(String keyword) throws IOException {
		log("开始查询，请耐心等待1-5分钟：" + keyword);
		sendString(keyword);
		while (true) {
			String msg = recvString();
			if (msg.equals("image")) {
				byte[] b = recvBytes();
				BufferedImage image = ImageIO.read(new ByteArrayInputStream(b));
				stop = true;
				gui.enableActive(image);
				Tool.waitUntil(() -> !stop, 100);// 暂停主程序，等待弹出窗口处理结束
			} else {
				String type = getType(msg);
				// 解析结果，最后一个放结果状态
				String[] value = dbsave.parser(msg);
				value[value.length - 1] = type;
				if (type.equals("normal"))
					dbsave.save(msg);
				return value;
			}
		}
	}

	/**
	 * 阻塞接受字节数据
	 * 
	 * @return 正常数据
	 * @throws IOException
	 *             网络断开异常
	 */
	private byte[] recvBytes() throws IOException {
		byte[] b = new byte[0];
		Tool.waitUntil(() -> recvBuf.size() > 0, 10);
		synchronized (recvBuf) {
			b = recvBuf.get(0);
			recvBuf.remove(0);
		}
		if (b == null)
			throw new IOException();
		else
			return b;
	}

	/**
	 * 阻塞读取一条字符串消息
	 * 
	 * @return
	 * @throws IOException
	 */
	private String recvString() throws IOException {
		byte[] b = recvBytes();
		String s = "";
		try {
			s = new String(b, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Tool.printErr(e, "字符串解码失败");
		}
		return s;
	}

	/**
	 * 发送一条字符串消息
	 * 
	 * @param s
	 */
	void sendString(String s) {
		if (s == null)
			return;
		byte[] b = new byte[0];
		try {
			b = s.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			Tool.printErr(e, "字符串编码失败");
		}
		sendBytes(b);
	}

	/**
	 * 发送字节数据
	 * 
	 * @param b
	 */
	void sendBytes(byte[] b) {
		synchronized (sendBuf) {
			sendBuf.add(b);
		}
	}

	/**
	 * 将对应于数据库字段的值展示在窗口中
	 * 
	 * @param value
	 *            对应于数据库字段的值
	 */
	private void showResult(String[] value) {
		if (value.length < 16)
			return;
		gui.addText("企业名称(M):\t" + value[0] + '\n');
		gui.addText("信用代码(X):\t" + value[1] + '\n');
		gui.addText("是否经营异常:\t" + value[2] + '\n');
		gui.addText("登记状态:\t" + value[3] + '\n');
		gui.addText("类型:\t" + value[4] + '\n');
		gui.addText("法人代表(R):\t" + value[5] + '\n');
		gui.addText("注册资本(元):\t" + value[6] + '\n');
		gui.addText("成立日期:\t" + value[7] + '\n');
		gui.addText("经营起始日:\t" + value[8] + '\n');
		gui.addText("经营期限至:\t" + value[9] + '\n');
		gui.addText("登记机关:\t" + value[10] + '\n');
		gui.addText("核准日期:\t" + value[11] + '\n');
		gui.addText("地址(D):\t" + value[12] + '\n');
		gui.addText("营业范围(F):\t" + value[13] + '\n');
		gui.addText("\n");
	}

	/**
	 * 程序退出需要处理的一些事情
	 */
	void exit() {
		Tool.runNoException(() -> socket.close());
		Tool.runNoException(() -> dbsave.exit());
		Tool.runNoException(() -> hkp.exit());
		Tool.runNoException(() -> logger.saveLog());
		System.exit(0);
	}

	void log(String s) {
		logger.log(s);
	}

	/**
	 * 按钮事件触发调用，设置信号量
	 */
	void startAction() {
		start = true;
		log("准备开始查询");
	}

	/**
	 * 使主线程继续运行
	 */
	void goon() {
		stop = false;
	}
}
