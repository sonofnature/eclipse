package com.bird.accountcheck.client;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import com.bird.util.Tool;
import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;

class HotKeyProcessor implements HotkeyListener {

	public static final int COPY_ALL = 0;
	public static final int COPY_MING_CHENG = 1;
	public static final int COPY_XIN_YONG_MA = 2;
	public static final int COPY_FA_REN = 3;
	public static final int COPY_DI_ZHI = 4;
	public static final int COPY_FAN_WEI = 5;

	private String[] value;// 对应于ODS数据库中表DW.WQK_GSXX各字段值

	public HotKeyProcessor() {
		value = new String[16];
		for (int i = 0; i < value.length; i++) {
			value[i] = "";
		}
		registerKey();
	}

	public void registerKey() {
		JIntellitype.getInstance().registerHotKey(COPY_ALL, JIntellitype.MOD_CONTROL, 'Q');
		JIntellitype.getInstance().registerHotKey(COPY_MING_CHENG, JIntellitype.MOD_CONTROL, 'M');
		JIntellitype.getInstance().registerHotKey(COPY_XIN_YONG_MA, JIntellitype.MOD_CONTROL, 'X');
		JIntellitype.getInstance().registerHotKey(COPY_FA_REN, JIntellitype.MOD_CONTROL, 'R');
		JIntellitype.getInstance().registerHotKey(COPY_DI_ZHI, JIntellitype.MOD_CONTROL, 'D');
		JIntellitype.getInstance().registerHotKey(COPY_FAN_WEI, JIntellitype.MOD_CONTROL, 'F');
	}

	public void setValue(String[] value) {
		this.value = value;
	}

	public void onHotKey(int key) {
		if (key == COPY_ALL) {
			paste(value[0]);
			pressEnter(1);
			paste(value[1]);
			pressEnter(2);
			paste(value[12]);
			pressEnter(4);
			paste(value[13]);
			pressEnter(2);
			paste(value[5]);
		} else if (key == COPY_MING_CHENG) {
			paste(value[0]);
		} else if (key == COPY_XIN_YONG_MA) {
			paste(value[1]);
		} else if (key == COPY_FA_REN) {
			paste(value[5]);
		} else if (key == COPY_DI_ZHI) {
			paste(value[12]);
		} else if (key == COPY_FAN_WEI) {
			paste(value[13]);
		}

	}

	public void exit() {
		JIntellitype.getInstance().cleanUp();
	}

	/**
	 * 将字符串s粘贴到当前位置
	 * 
	 * @param s
	 */
	private void paste(String s) {
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(s), null);
		try {
			Robot robot = new Robot();
			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_V);
			robot.keyRelease(KeyEvent.VK_V);
			robot.keyRelease(KeyEvent.VK_CONTROL);
			robot.delay(100);
		} catch (AWTException e) {
			Tool.printErr(e, "热键自动粘贴失败");
		}
	}

	/**
	 * 按回车键n次
	 * 
	 * @param n
	 */
	private void pressEnter(int n) {
		for (int i = 0; i < n; i++) {
			try {
				Robot robot = new Robot();
				robot.keyPress(KeyEvent.VK_ENTER);
				robot.keyRelease(KeyEvent.VK_ENTER);
				robot.delay(100);
			} catch (AWTException e) {
				Tool.printErr(e);
			}
		}
	}
}
