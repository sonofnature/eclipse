package com.bird.accountcheck.server;

import java.io.File;
import com.bird.util.Tool;

public class Server {

	// 相关参数
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
	public static final String URL = "http://www.gsxt.gov.cn/corp-query-homepage.html";// 网址首页地址
	public static final int TAB_COUNT = 3;// 从网站首页的地址栏到输入栏需要按的Tab键次数
	public static final int TIME_LIMIT = 60000;// 在某一状态等待的最长时间限制
	public static final int PORT = 12346;// 监听端口，区别于批量查询

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
				// 与一个客户端建立连接
				linker.accept();
				String msg = linker.recvString(60 * 1000);
				log(msg);
				// 打招呼
				if (msg.equals("hi")) {
					linker.sendString("hi");
					logger.checkLog();
				} else {
					throw new NetTimeoutException();
				}

				while (true) {
					msg = linker.recvString(60 * 1000);
					// 查询
					log("查询开始:" + msg);
					String htmlname = queryer.query(msg);
					// 解析
					String result = parser.parse(DOWN_FOLD + "/" + htmlname);
					delHtml(htmlname);
					// 发送结果
					linker.sendString(result);

					log("查询完成:" + msg);
					log("返回结果:" + result);
				}
			} catch (NetTimeoutException e) {
				linker.disconnect();
				log("客户端断开\n");
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
	 * 释放资源
	 */
	void exit() {
		Tool.runNoException(() -> linker.exit());
		Tool.runNoException(() -> logger.saveLog());
		System.exit(0);
	}

	/*
	 * 将html文件及其资源文件删除
	 */
	private void delHtml(String name) {
		new File(DOWN_FOLD + "/" + name).delete();
		// 删除资源文件夹
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
