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

	// ������������̲߳���
	private boolean start;
	private boolean stop;
	private ArrayList<byte[]> sendBuf;
	private ArrayList<byte[]> recvBuf;

	private GUI gui;
	private Logger logger;
	private DBSave dbsave;// ���ݿⱣ��
	private Socket socket;
	private HotKeyProcessor hkp;

	public Client() {
		start = false;
		gui = new GUI(this);
		logger = new Logger(gui);
	}

	public void start() {
		// �������ݿ⣬ʧ�ܲ�Ӱ����������
		dbsave = new DBSave(this);
		// �����ȼ�,��ʹ�����쳣Ҳ��Ӱ����������
		Tool.runNoException(() -> hkp = new HotKeyProcessor());
		Tool.runNoException(() -> JIntellitype.getInstance().addHotKeyListener(hkp));

		while (true) {
			Tool.waitUntil(() -> start, 100);
			gui.disableQuery();
			start = false;
			if (connectSev() == false) {// ���ӷ�����
				gui.enableQuery();
				continue;
			}
			// ���������ӳɹ�
			String keyword = gui.getKeyword();
			try {
				String[] value = query(keyword);
				String type = value[value.length - 1];

				if (type.equals("error") || type.isEmpty()) {
					log("��վ���س������Ժ��ز�");
				} else if (type.equals("null")) {
					log("��ѯ���Ϊ�գ���ȷ�ϲ�ѯ�ؼ����Ƿ���ȷ");
				} else {// ��ѯ�ɹ�
					log("��ѯ�ɹ�����˶�(С��ʾ������CTRL+���������ĸ�ɿ��ٸ���ճ����һ�㸴���밴CTRL+C)��\n");
					showResult(value);
					Tool.runNoException(() -> hkp.setValue(value));// ���ȼ���ֵ
				}
			} catch (IOException e) {
				log("������ϣ����Ժ�����");
			} finally {
				Tool.runNoException(() -> socket.close());
			}
			// ��Ϣһ���Ӻ�����������
			log("��Ϣ����Ϣһ��...");
			Tool.runNoException(() -> Thread.sleep(60000));
			gui.enableQuery();
		}
	}

	/**
	 * ���������ļ����ӷ��������ȴ���Ӧʱ�䳬ʱҲ��ʧ��
	 * 
	 * @�����������������ļ�configfile
	 * @Ӱ�죺���ӳɹ����ʼ����socket�����������̣߳�ͨ��sendBuf��recvBuf�����߳�ͨ��
	 * @return �Ƿ����ӳɹ�
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
			// 10s�ȴ��������ظ�
			long t1 = System.currentTimeMillis() + 10000;
			Tool.waitUntil(() -> System.currentTimeMillis() > t1 || recvBuf.size() > 0, 1000);
			if (recvBuf.size() > 0)
				s = recvString();
		} catch (IOException e) {
		}
		if (s == null || !s.equals("hi")) {
			Tool.runNoException(() -> socket.close());
			log("������æ�����Ժ�����:" + sevIP + ":" + port);
			return false;
		} else {// ���ܵ�"hi"
			log("���ӷ������ɹ���" + sevIP + ":" + port);
			return true;
		}
	}

	/**
	 * �ӷ��ؽ�����ҳ������ֶΣ��������ֶΣ�
	 * 
	 * @param s
	 *            ����ַ���
	 * @return ����������ص��ĸ��ֶΣ����������ؿմ�
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
	 * ��ѯһ����ҵ�����ض�Ӧ�����ݿ���ֶε�ֵ������
	 * 
	 * @param keyword
	 * @return
	 * @throws IOException
	 */
	private String[] query(String keyword) throws IOException {
		log("��ʼ��ѯ�������ĵȴ�1-5���ӣ�" + keyword);
		sendString(keyword);
		while (true) {
			String msg = recvString();
			if (msg.equals("image")) {
				byte[] b = recvBytes();
				BufferedImage image = ImageIO.read(new ByteArrayInputStream(b));
				stop = true;
				gui.enableActive(image);
				Tool.waitUntil(() -> !stop, 100);// ��ͣ�����򣬵ȴ��������ڴ������
			} else {
				String type = getType(msg);
				// ������������һ���Ž��״̬
				String[] value = dbsave.parser(msg);
				value[value.length - 1] = type;
				if (type.equals("normal"))
					dbsave.save(msg);
				return value;
			}
		}
	}

	/**
	 * ���������ֽ�����
	 * 
	 * @return ��������
	 * @throws IOException
	 *             ����Ͽ��쳣
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
	 * ������ȡһ���ַ�����Ϣ
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
			Tool.printErr(e, "�ַ�������ʧ��");
		}
		return s;
	}

	/**
	 * ����һ���ַ�����Ϣ
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
			Tool.printErr(e, "�ַ�������ʧ��");
		}
		sendBytes(b);
	}

	/**
	 * �����ֽ�����
	 * 
	 * @param b
	 */
	void sendBytes(byte[] b) {
		synchronized (sendBuf) {
			sendBuf.add(b);
		}
	}

	/**
	 * ����Ӧ�����ݿ��ֶε�ֵչʾ�ڴ�����
	 * 
	 * @param value
	 *            ��Ӧ�����ݿ��ֶε�ֵ
	 */
	private void showResult(String[] value) {
		if (value.length < 16)
			return;
		gui.addText("��ҵ����(M):\t" + value[0] + '\n');
		gui.addText("���ô���(X):\t" + value[1] + '\n');
		gui.addText("�Ƿ�Ӫ�쳣:\t" + value[2] + '\n');
		gui.addText("�Ǽ�״̬:\t" + value[3] + '\n');
		gui.addText("����:\t" + value[4] + '\n');
		gui.addText("���˴���(R):\t" + value[5] + '\n');
		gui.addText("ע���ʱ�(Ԫ):\t" + value[6] + '\n');
		gui.addText("��������:\t" + value[7] + '\n');
		gui.addText("��Ӫ��ʼ��:\t" + value[8] + '\n');
		gui.addText("��Ӫ������:\t" + value[9] + '\n');
		gui.addText("�Ǽǻ���:\t" + value[10] + '\n');
		gui.addText("��׼����:\t" + value[11] + '\n');
		gui.addText("��ַ(D):\t" + value[12] + '\n');
		gui.addText("Ӫҵ��Χ(F):\t" + value[13] + '\n');
		gui.addText("\n");
	}

	/**
	 * �����˳���Ҫ�����һЩ����
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
	 * ��ť�¼��������ã������ź���
	 */
	void startAction() {
		start = true;
		log("׼����ʼ��ѯ");
	}

	/**
	 * ʹ���̼߳�������
	 */
	void goon() {
		stop = false;
	}
}
