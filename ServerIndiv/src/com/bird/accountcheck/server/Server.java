package com.bird.accountcheck.server;

import java.io.File;
import com.bird.util.Tool;

public class Server {

	// ��ز���
	public static final String DOWN_FOLD = "download";
	public static final String LOG_FOLD = "log";
	public static final String ERROR_FILE = "error.txt";
	public static final String FRESH_PNG = "fresh.png";
	public static final String OK_PNG = "ok.png";
	public static final String FIND_PNG = "find.png";
	public static final String RESULT_PNG = "result.png";
	public static final String TIP_PNG = "tip.png";
	public static final String SLIDER_PNG = "slider.png";
	public static final String TITLE_PNG = "title.png";
	public static final String DOWN_PNG = "down.png";
	public static final String SAVE_PNG="save.png";
	public static final String TRACK_FILE = "track.txt";
	public static final String README_FILE = "readme.txt";
	public static final String URL = "http://www.gsxt.gov.cn/corp-query-homepage.html";// ��ַ��ҳ��ַ
	public static final int TAB_COUNT = 3;// ����վ��ҳ�ĵ�ַ������������Ҫ����Tab������
	public static final int TIME_LIMIT = 60000;// ��ĳһ״̬�ȴ����ʱ������
	public static final int PORT = 12346;// �����˿ڣ�������������ѯ

	private GUI gui;
	private Logger logger;
	private Linker linker;
	private Queryer queryer;
	private Parser parser;

	public static void main(String[] args) {
		Tool.errOutTo(ERROR_FILE);
		try {
			new Server().start();
		} catch (Exception e) {
			Tool.printErr(e);
		}
	}

	public Server() throws Exception {
		gui = new GUI(this);
		logger = new Logger(gui);
		linker = new Linker(this);
		queryer = new Queryer(this);
		parser = new Parser();
	}

	public void start() {
		while (true) {
			try {
				// ��һ���ͻ��˽�������
				linker.accept();
				String msg = linker.recvString(60 * 1000);
				log(msg);
				// ���к�
				if (msg.equals("hi")) {
					linker.sendString("hi");
					logger.checkLog();
				} else {
					throw new NetTimeoutException();
				}

				while (true) {
					msg = linker.recvString(60 * 1000);
					// ��ѯ
					log("��ѯ��ʼ:" + msg);
					String htmlname = queryer.query(msg);
					// ����
					String result = parser.parse(DOWN_FOLD + "/" + htmlname);
					delHtml(htmlname);
					// ���ͽ��
					linker.sendString(result);

					log("��ѯ���:" + msg);
					log("���ؽ��:" + result);
				}
			} catch (NetTimeoutException e) {
				linker.disconnect();
				log("�ͻ��˶Ͽ�\n");
				continue;
			}
		}
	}

	public Linker getLinker() {
		return linker;
	}

	public void log(String s) {
		logger.log(s);
	}

	/**
	 * �ͷ���Դ
	 */
	void exit() {
		Tool.runNoException(() -> linker.exit());
		Tool.runNoException(() -> logger.saveLog());
		System.exit(0);
	}

	/*
	 * ��html�ļ�������Դ�ļ�ɾ��
	 */
	private void delHtml(String name) {
		new File(DOWN_FOLD + "/" + name).delete();
		// ɾ����Դ�ļ���
		File fold = new File(DOWN_FOLD + "/" + name.substring(0, name.indexOf('.')) + "_files");
		File[] files = fold.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				files[i].delete();
			}
			fold.delete();
		}
	}
}
