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

	public static final int COPY_MING_CHENG = 1;
	public static final int COPY_XIN_YONG_MA = 2;
	public static final int COPY_FA_REN = 3;
	public static final int COPY_DI_ZHI = 4;
	public static final int COPY_FAN_WEI = 5;

	private String[] value;// ��Ӧ��ODS���ݿ��б�DW.WQK_GSXX���ֶ�ֵ

	public HotKeyProcessor() {
		value = new String[16];
		for (int i = 0; i < value.length; i++) {
			value[i] = "";
		}
		registerKey();
	}

	public void registerKey() {
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
		String s = "";
		if (key == COPY_MING_CHENG) {
			s = value[0];
		} else if (key == COPY_XIN_YONG_MA) {
			s = value[1];
		} else if (key == COPY_FA_REN) {
			s = value[5];
		} else if (key == COPY_DI_ZHI) {
			s = value[12];
		} else if (key == COPY_FAN_WEI) {
			s = value[13];
		}

		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(s), null);
		try {
			Robot robot = new Robot();
			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_V);
			robot.keyRelease(KeyEvent.VK_V);
			robot.keyRelease(KeyEvent.VK_CONTROL);
		} catch (AWTException e) {
			Tool.printErr(e, "�ȼ��Զ�ճ��ʧ��");
		}
	}

	public void exit() {
		JIntellitype.getInstance().cleanUp();
	}
}
