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
	 * 重定向系统出错信息到文件，添加到文件末尾，文件过大则会清空。
	 * 
	 * @param path
	 *            文件路径
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
			if (file.length() > 100000) {// 如果文件过大，则覆盖
				appand = false;
			}
			PrintStream ps = new PrintStream(new FileOutputStream(file, appand));
			System.setErr(ps);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 打印异常信息，除了异常的堆栈信息，加上了时间信息，备注信息
	 * 
	 * @param msg
	 *            备注信息
	 * @param e
	 *            异常对象
	 */
	public static void printErr(Exception e, String msg) {
		DateFormat df = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		System.err.println(df.format(new Date()) + "  " + msg);
		e.printStackTrace();
	}

	/**
	 * 打印异常信息，除了异常的堆栈信息，加上了时间信息
	 * 
	 * @param e
	 *            异常对象
	 */
	public static void printErr(Exception e) {
		printErr(e, "");
	}

	/**
	 * 运行过程，对于过程中的异常不作任何处理，为那些不关心异常的而又不得不编写异常处理代码的过程而写，简化代码。
	 * 
	 * @param p
	 *            待运行的过程
	 */
	public static void runNoException(RunWithExc p) {
		try {
			p.run();
		} catch (Exception e) {
		}
	}

	/**
	 * 阻塞等待直到某条件满足
	 * 
	 * @param p
	 *            条件
	 * @param slicetime
	 *            检查条件是否满足的间隔时间(ms)，越短反应越灵敏
	 */
	public static void waitUntil(BooleanSupplier p, long slicetime) {
		while (true) {
			Tool.runNoException(() -> Thread.sleep(slicetime));
			if (p.getAsBoolean())
				return;
		}
	}

}
