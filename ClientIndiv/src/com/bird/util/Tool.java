package com.bird.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.BooleanSupplier;

public class Tool {
	/**
	 * �ض���ϵͳ������Ϣ���ļ�����ӵ��ļ�ĩβ���ļ����������ա�
	 * 
	 * @param path
	 *            �ļ�·��
	 */
	public static void errOutTo(String path) {
		File file = new File(path);
		if (file.exists() == false) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			boolean appand = true;
			if (file.length() > 100000) {// ����ļ������򸲸�
				appand = false;
			}
			PrintStream ps = new PrintStream(new FileOutputStream(file, appand));
			System.setErr(ps);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ��ӡ�쳣��Ϣ�������쳣�Ķ�ջ��Ϣ��������ʱ����Ϣ����ע��Ϣ
	 * 
	 * @param msg
	 *            ��ע��Ϣ
	 * @param e
	 *            �쳣����
	 */
	public static void printErr(Exception e, String msg) {
		DateFormat df = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		System.err.println(df.format(new Date()) + "  " + msg);
		e.printStackTrace();
	}

	/**
	 * ��ӡ�쳣��Ϣ�������쳣�Ķ�ջ��Ϣ��������ʱ����Ϣ
	 * 
	 * @param e
	 *            �쳣����
	 */
	public static void printErr(Exception e) {
		printErr(e, "");
	}

	/**
	 * ���й��̣����ڹ����е��쳣�����κδ���Ϊ��Щ�������쳣�Ķ��ֲ��ò���д�쳣�������Ĺ��̶�д���򻯴��롣
	 * 
	 * @param p
	 *            �����еĹ���
	 */
	public static void runNoException(RunWithExc p) {
		try {
			p.run();
		} catch (Exception e) {
		}
	}

	/**
	 * �����ȴ�ֱ��ĳ��������
	 * 
	 * @param p
	 *            ����
	 * @param slicetime
	 *            ��������Ƿ�����ļ��ʱ��(ms)��Խ�̷�ӦԽ����
	 */
	public static void waitUntil(BooleanSupplier p, long slicetime) {
		while (true) {
			Tool.runNoException(() -> Thread.sleep(slicetime));
			if (p.getAsBoolean())
				return;
		}
	}

}
