package com.bird.accountcheck.client;

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
	 * �ڽ���������һ����־���Զ������ʱ����Ϣ
	 * 
	 * @param s
	 */
	public void log(String s) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		String time = formatter.format(new Date());
		gui.addText(time + " " + s + "\r\n");
	}

	/**
	 * ����������ʾ����־���浽��־�ļ�����
	 */
	public void saveLog() {
		String log = gui.getText();
		if (log.isEmpty())
			return;
		File f = new File(Client.LOG_FOLD);
		if (!f.exists())
			f.mkdir();

		try {
			File[] fs = f.listFiles();
			// ����10����־�ļ�ֱ�Ӵ����µ���־�ļ�
			if (fs.length < 10) {
				f = new File(Client.LOG_FOLD + "/" + fs.length + ".txt");
				f.createNewFile();
			} else {
				// ��10���ļ����ҳ��������־�ļ�����
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
			log("������־ʧ��");
			Tool.printErr(e, "������־ʧ��");
		}
	}
}
