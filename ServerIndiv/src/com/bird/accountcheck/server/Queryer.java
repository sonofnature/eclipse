package com.bird.accountcheck.server;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.function.BooleanSupplier;
import javax.imageio.ImageIO;

import com.bird.util.Tool;

/*根据给定的列表自动在网站上查询并将查询的网页保存下来
 */
class Queryer {

	private Server owner;
	private Linker linker;
	private Robot robot;
	private ArrayList<String> tracks;
	private BufferedImage image;
	private PartImage freshimage;// 浏览器上标志网页是否加载完成的图标，“刷新”图标
	private PartImage resultimage;// 最后出结果界面“营业”字样
	private PartImage okimage;// 图片识别“确定”图标
	private PartImage findimage;// 右上方小放大镜图标
	private PartImage tipimage;// 关键字不符合要求“提示框”图标
	private PartImage sliderimage;// “滑块”图标
	private PartImage titleimage;// 页面上的标题行背景图标
	private PartImage saveimage;// 保存页面弹出框
	private PartImage downimage;// 下载完成后弹出的对话框中“下载完成”字样图片
	private Rectangle screenRectangle;// 屏幕矩形

	public Queryer(Server owner) throws Exception {
		this.owner = owner;
		this.linker = owner.getLinker();
		robot = new Robot();
		screenRectangle = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

		// 初始化小图片
		freshimage = new PartImage(Server.FRESH_PNG);
		okimage = new PartImage(Server.OK_PNG);
		findimage = new PartImage(Server.FIND_PNG);
		tipimage = new PartImage(Server.TIP_PNG);
		resultimage = new PartImage(Server.RESULT_PNG);
		sliderimage = new PartImage(Server.SLIDER_PNG);
		titleimage = new PartImage(Server.TITLE_PNG);
		saveimage = new PartImage(Server.SAVE_PNG);
		downimage = new PartImage(Server.DOWN_PNG);

		// 初始化鼠标轨迹
		tracks = new ArrayList<>();
		BufferedReader fr = new BufferedReader(new FileReader(Server.TRACK_FILE));
		for (String s = fr.readLine(); s != null; s = fr.readLine()) {
			tracks.add(s);
		}
		fr.close();
	}

	/**
	 * 查询一个企业，将结果网页保存在download文件夹中，返回保存的网页名。若果关键字不符合要求则只返回文件名而实际没有网页下载。
	 * 
	 * @param keyword
	 *            关键字
	 * @return
	 * @throws NetTimeoutException
	 *             客户端响应超时异常
	 */
	public String query(String keyword) throws NetTimeoutException {
		// 使浏览器获取焦点，以防应系统自动弹出窗口而使浏览器失去焦点
		try {
			robot.mouseMove(0, 0);
			waitHtml(() -> exist(freshimage));
			click(freshimage.x + 6, freshimage.y + 6);
			robot.mouseMove(0, 0);
		} catch (Exception e) {
			Tool.printErr(e, "浏览器获取焦点失败");
			return keyword + "@20171225180000.html";
		}

		int times = 0, i;// 超时重来次数
		while (true) {
			try {
				// 打开首页
				pressCombKey(KeyEvent.VK_CONTROL, KeyEvent.VK_F4);// ctrl+F4
				pressCombKey(KeyEvent.VK_CONTROL, KeyEvent.VK_L);// ctrl+L,定位到地址栏
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(Server.URL), null);
				pressCombKey(KeyEvent.VK_CONTROL, KeyEvent.VK_V);
				robot.mouseMove(0, 0);
				pressKey(KeyEvent.VK_ENTER);

				// 输入关键字
				for (i = 0; i < 3; i++) {
					waitHtml(() -> exist(freshimage));
					if (!exist(titleimage)) {
						click(freshimage.x + 6, freshimage.y + 6);// 刷新
						robot.mouseMove(0, 0);
					} else
						break;
				}
				if (i >= 3)
					throw new HtmlTimeoutException();
				owner.log("首页打开");
				int x = titleimage.x + 419 + (int) (Math.random() * 400);
				int y = titleimage.y + 331 + (int) (Math.random() * 30);
				moveTo(x, y);
				click(x, y);
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(keyword), null);
				pressCombKey(KeyEvent.VK_CONTROL, KeyEvent.VK_V);
				pressKey(KeyEvent.VK_ENTER);
				robot.delay(2000);

				waitHtml(() -> exist(okimage) || exist(freshimage) && exist(findimage) || exist(sliderimage)
						|| exist(tipimage));

				if (exist(tipimage)) {// 关键字不合要求
					owner.log("关键字不合要求");
					return keyword + "@20171225180000.html";
				} else if (exist(sliderimage)) {// 滑块验证
					owner.log("滑块验证");
					continue;
				} else if (exist(okimage)) {// 文字验证
					owner.log("文字验证");
					charUnlock();
				}

				// 打开并保存结果,此处对无结果返回的网页照常保存，后期分析网页是在行判断结果是否正确
				owner.log("结果列表出现");
				click(titleimage.x + 20, titleimage.y + 585);
				pressKey(KeyEvent.VK_TAB);
				pressKey(KeyEvent.VK_ENTER);
				for (i = 0; i < 5; i++) {
					waitHtml(() -> exist(resultimage) || exist(freshimage));
					if (isBlank()) {// 空白网页
						click(freshimage.x + 6, freshimage.y + 6);// 刷新
						robot.mouseMove(0, 0);
					} else {
						break;
					}
				}
				if (i >= 5)
					throw new HtmlTimeoutException();
				owner.log("详细结果显示");
				pressCombKey(KeyEvent.VK_CONTROL, KeyEvent.VK_S);// ctrl+s

				// 保存的文件名加上时间戳
				waitHtml(() -> exist(saveimage));
				SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
				String date = df.format(new Date());
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(keyword + "@" + date),
						null);
				pressCombKey(KeyEvent.VK_CONTROL, KeyEvent.VK_V);// ctrl+v
				pressKey(KeyEvent.VK_ENTER);
				waitHtml(() -> exist(downimage));

				// 查询一个单位成功
				return keyword + "@" + date + ".html";
			} catch (HtmlTimeoutException e) {
				times++;
				if (times > 5)
					return keyword + "@20171225180000.html";
				else
					continue;
			}
		}
	}

	/**
	 * 等待某个网页出现
	 * 
	 * @param p
	 *            标志网页出现的逻辑表达式
	 * @throws HtmlTimeoutException
	 *             在Server.TIME_LIMIT时间内未出现抛出超时异常
	 */
	private void waitHtml(BooleanSupplier p) throws HtmlTimeoutException {
		long t1 = System.currentTimeMillis() + Server.TIME_LIMIT;
		while (System.currentTimeMillis() < t1) {
			Tool.runNoException(() -> Thread.sleep(1000));
			image = robot.createScreenCapture(screenRectangle);
			if (p.getAsBoolean())
				return;
		}
		throw new HtmlTimeoutException();
	}

	/**
	 * 在image中匹配小图片，若之前匹配过则只会在之前匹配对应的位置上匹配，否则会全局匹配
	 * 
	 * @param partimage
	 * @return
	 */
	private boolean exist(PartImage partimage) {
		if (partimage.x >= 0 && partimage.y >= 0) {
			return compareImage(partimage.image, image, partimage.x, partimage.y);
		} else {
			int w = image.getWidth() - partimage.image.getWidth();
			int h = image.getHeight() - partimage.image.getHeight();
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					if (compareImage(partimage.image, image, x, y) == true) {// 匹配成功
						partimage.x = x;
						partimage.y = y;
						return true;
					}
				}
			}
			return false;
		}
	}

	// 在误差范围内比较图片
	private boolean compareImage(BufferedImage smallimage, BufferedImage bigimage, int sx, int sy) {
		int w = smallimage.getWidth();
		int h = smallimage.getHeight();
		if (sx + w > bigimage.getWidth() || sy + h > bigimage.getHeight())
			return false;
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int rgb1 = smallimage.getRGB(x, y);
				int rgb2 = bigimage.getRGB(sx + x, sy + y);
				int r1 = (rgb1 >> 16) & 0xFF;
				int g1 = (rgb1 >> 8) & 0xFF;
				int b1 = (rgb1) & 0xFF;
				int r2 = (rgb2 >> 16) & 0xFF;
				int g2 = (rgb2 >> 8) & 0xFF;
				int b2 = (rgb2) & 0xFF;
				if (Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2) > 10)
					return false;
			}
		}
		return true;
	}

	/*
	 * 近似判断网页是否是全白，什么都没有，通过取刷新图标下面的一条线看看是否全是白色来近似确定
	 */
	private boolean isBlank() {
		int white = Color.WHITE.getRGB();
		image = robot.createScreenCapture(screenRectangle);
		for (int dy = 80; dy < 400; dy++) {
			int rgb = image.getRGB(freshimage.x, freshimage.y + dy);
			if (rgb != white)
				return false;
		}
		return true;
	}

	// 按下组合键
	private void pressCombKey(int key1, int key2) {
		robot.keyPress(key1);
		robot.delay(280 + (int) (Math.random() * 100));
		robot.keyPress(key2);
		robot.delay(140 + (int) (Math.random() * 100));
		robot.keyRelease(key2);
		robot.delay(95 + (int) (Math.random() * 100));
		robot.keyRelease(key1);
		robot.delay(500 + (int) (Math.random() * 300));
	}

	// 按键
	private void pressKey(int key) {
		pressKey(key, 1);
	}

	// 重复按键
	private void pressKey(int key, int times) {
		for (int i = 0; i < times; i++) {
			robot.keyPress(key);
			robot.delay(50 + (int) (Math.random() * 100));
			robot.keyRelease(key);
			robot.delay(310 + (int) (Math.random() * 100));
		}
	}

	// 单击(x,y)
	private void click(int x, int y) {
		robot.mouseMove(x, y);
		robot.delay(300 + (int) (Math.random() * 100));
		robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
		robot.delay(200 + (int) (Math.random() * 100));
		robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
	}

	/**
	 * 解锁文字验证码直至成功或异常
	 * 
	 * @throws NetTimeoutException
	 *             客户端响应异常
	 * @throws HtmlTimeoutException
	 *             网页超时异常
	 */
	private void charUnlock() throws NetTimeoutException, HtmlTimeoutException {
		while (true) {

			BufferedImage p = image.getSubimage(okimage.x - 201, okimage.y - 362, 317, 358);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Tool.runNoException(() -> ImageIO.write(p, "png", out));
			byte[] b = out.toByteArray();
			linker.sendString("image");
			linker.sendBytes(b);

			// 接收返回结果
			String r = linker.recvString(60 * 1000);
			StringTokenizer st = new StringTokenizer(r, "|");
			while (st.hasMoreTokens()) {
				int x = Integer.valueOf(st.nextToken());
				int y = Integer.valueOf(st.nextToken());
				click(okimage.x - 201 + x, okimage.y - 362 + y);
			}
			click(okimage.x + 55, okimage.y + 19);
			robot.mouseMove(okimage.x - 20, okimage.y - 20);
			robot.delay(3 * 1000);
			// 等验证成功或者验证失败
			waitHtml(() -> exist(freshimage) && exist(findimage) || exist(okimage));
			if (exist(freshimage) && exist(findimage)) {
				break;
			}
		}
	}

	/**
	 * 模拟人工移动到目标点
	 * 
	 * @param x
	 *            目标点x
	 * @param y
	 *            目标点y
	 */
	private void moveTo(int x, int y) {
		// 随机选一个轨迹
		int select = (int) (tracks.size() * Math.random());
		String s = tracks.get(select);
		StringTokenizer st = new StringTokenizer(s, ",|");
		ArrayList<Integer> ints = new ArrayList<>();
		while (st.hasMoreTokens()) {
			ints.add(Integer.valueOf(st.nextToken()));
		}
		// 从终点坐标计算起点坐标
		int x0 = x - ints.get(ints.size() - 2);
		int y0 = y - ints.get(ints.size() - 1);
		robot.mouseMove(x0, y0);
		for (int i = 0; i < ints.size();) {
			robot.delay(ints.get(i++));
			robot.mouseMove(x0 + ints.get(i++), y0 + ints.get(i++));
		}
	}
}

/***********************************************************************************************/
// 一个小的部分图片及在全屏中的位置
class PartImage {
	public BufferedImage image;
	public int x, y;// 坐标

	public PartImage(String imagepath) throws IOException {
		image = ImageIO.read(new File(imagepath));
		x = -1;
		y = -1;
	}

	public void clearXY() {
		x = -1;
		y = -1;
	}
}
