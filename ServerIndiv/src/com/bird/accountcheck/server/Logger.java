package com.bird.accountcheck.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.bird.util.Tool;

class Logger {
	private GUI gui;

	public Logger(GUI gui) {
		this.gui = gui;
	}

	/**
	 * 在界面上增加一行日志，自动添加了时间信息
	 * 
	 * @param s
	 */
	public void log(String s) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		String time = formatter.format(new Date());
		gui.addText(time + " " + s + "\n");
	}

	/**
	 * 将界面上显示的日志保存到日志文件夹下
	 */
	public void saveLog() {
		String log = gui.getText();
		if (log.isEmpty())
			return;
		File f = new File(Server.LOG_FOLD);
		if (!f.exists())
			f.mkdir();

		try {
			File[] fs = f.listFiles();
			// 少于10个日志文件直接创建新的日志文件
			if (fs.length < 10) {
				f = new File(Server.LOG_FOLD + "/" + fs.length + ".txt");
				f.createNewFile();
			} else {
				// 有10个文件则找出最早的日志文件覆盖
				long min = Long.MAX_VALUE;
				for (int i = 0; i < fs.length; i++) {
					if (fs[i].lastModified() < min) {
						f = fs[i];
						min = fs[i].lastModified();
					}
				}
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			bw.write(log);
			bw.close();
		} catch (IOException e) {
			log("保存日志失败");
			Tool.printErr(e, "保存日志失败");
		}
	}

	/**
	 * 检查界面日志是否过多，如果过多就保存并清空
	 */
	public void checkLog() {
		String s = gui.getText();
		if (s.length() > 100000) {
			saveLog();
			gui.setText("");
		}
	}

}
