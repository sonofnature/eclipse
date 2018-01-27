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

/*���ݸ������б��Զ�����վ�ϲ�ѯ������ѯ����ҳ��������
 */
class Queryer {

	private Server owner;
	private Linker linker;
	private Robot robot;
	private ArrayList<String> tracks;
	private BufferedImage image;
	private PartImage freshimage;// ������ϱ�־��ҳ�Ƿ������ɵ�ͼ�꣬��ˢ�¡�ͼ��
	private PartImage resultimage;// ����������桰Ӫҵ������
	private PartImage okimage;// ͼƬʶ��ȷ����ͼ��
	private PartImage findimage;// ���Ϸ�С�Ŵ�ͼ��
	private PartImage tipimage;// �ؼ��ֲ�����Ҫ����ʾ��ͼ��
	private PartImage sliderimage;// �����顱ͼ��
	private PartImage titleimage;// ҳ���ϵı����б���ͼ��
	private PartImage saveimage;// ����ҳ�浯����
	private PartImage downimage;// ������ɺ󵯳��ĶԻ����С�������ɡ�����ͼƬ
	private Rectangle screenRectangle;// ��Ļ����

	public Queryer(Server owner) throws Exception {
		this.owner = owner;
		this.linker = owner.getLinker();
		robot = new Robot();
		screenRectangle = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

		// ��ʼ��СͼƬ
		freshimage = new PartImage(Server.FRESH_PNG);
		okimage = new PartImage(Server.OK_PNG);
		findimage = new PartImage(Server.FIND_PNG);
		tipimage = new PartImage(Server.TIP_PNG);
		resultimage = new PartImage(Server.RESULT_PNG);
		sliderimage = new PartImage(Server.SLIDER_PNG);
		titleimage = new PartImage(Server.TITLE_PNG);
		saveimage = new PartImage(Server.SAVE_PNG);
		downimage = new PartImage(Server.DOWN_PNG);

		// ��ʼ�����켣
		tracks = new ArrayList<>();
		BufferedReader fr = new BufferedReader(new FileReader(Server.TRACK_FILE));
		for (String s = fr.readLine(); s != null; s = fr.readLine()) {
			tracks.add(s);
		}
		fr.close();
	}

	/**
	 * ��ѯһ����ҵ���������ҳ������download�ļ����У����ر������ҳ���������ؼ��ֲ�����Ҫ����ֻ�����ļ�����ʵ��û����ҳ���ء�
	 * 
	 * @param keyword
	 *            �ؼ���
	 * @return
	 * @throws NetTimeoutException
	 *             �ͻ�����Ӧ��ʱ�쳣
	 */
	public String query(String keyword) throws NetTimeoutException {
		// ʹ�������ȡ���㣬�Է�Ӧϵͳ�Զ��������ڶ�ʹ�����ʧȥ����
		try {
			robot.mouseMove(0, 0);
			waitHtml(() -> exist(freshimage));
			click(freshimage.x + 6, freshimage.y + 6);
			robot.mouseMove(0, 0);
		} catch (Exception e) {
			Tool.printErr(e, "�������ȡ����ʧ��");
			return keyword + "@20171225180000.html";
		}

		int times = 0, i;// ��ʱ��������
		while (true) {
			try {
				// ����ҳ
				pressCombKey(KeyEvent.VK_CONTROL, KeyEvent.VK_F4);// ctrl+F4
				pressCombKey(KeyEvent.VK_CONTROL, KeyEvent.VK_L);// ctrl+L,��λ����ַ��
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(Server.URL), null);
				pressCombKey(KeyEvent.VK_CONTROL, KeyEvent.VK_V);
				robot.mouseMove(0, 0);
				pressKey(KeyEvent.VK_ENTER);

				// ����ؼ���
				for (i = 0; i < 3; i++) {
					waitHtml(() -> exist(freshimage));
					if (!exist(titleimage)) {
						click(freshimage.x + 6, freshimage.y + 6);// ˢ��
						robot.mouseMove(0, 0);
					} else
						break;
				}
				if (i >= 3)
					throw new HtmlTimeoutException();
				owner.log("��ҳ��");
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

				if (exist(tipimage)) {// �ؼ��ֲ���Ҫ��
					owner.log("�ؼ��ֲ���Ҫ��");
					return keyword + "@20171225180000.html";
				} else if (exist(sliderimage)) {// ������֤
					owner.log("������֤");
					continue;
				} else if (exist(okimage)) {// ������֤
					owner.log("������֤");
					charUnlock();
				}

				// �򿪲�������,�˴����޽�����ص���ҳ�ճ����棬���ڷ�����ҳ�������жϽ���Ƿ���ȷ
				owner.log("����б����");
				click(titleimage.x + 20, titleimage.y + 585);
				pressKey(KeyEvent.VK_TAB);
				pressKey(KeyEvent.VK_ENTER);
				for (i = 0; i < 5; i++) {
					waitHtml(() -> exist(resultimage) || exist(freshimage));
					if (isBlank()) {// �հ���ҳ
						click(freshimage.x + 6, freshimage.y + 6);// ˢ��
						robot.mouseMove(0, 0);
					} else {
						break;
					}
				}
				if (i >= 5)
					throw new HtmlTimeoutException();
				owner.log("��ϸ�����ʾ");
				pressCombKey(KeyEvent.VK_CONTROL, KeyEvent.VK_S);// ctrl+s

				// ������ļ�������ʱ���
				waitHtml(() -> exist(saveimage));
				SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
				String date = df.format(new Date());
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(keyword + "@" + date),
						null);
				pressCombKey(KeyEvent.VK_CONTROL, KeyEvent.VK_V);// ctrl+v
				pressKey(KeyEvent.VK_ENTER);
				waitHtml(() -> exist(downimage));

				// ��ѯһ����λ�ɹ�
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
	 * �ȴ�ĳ����ҳ����
	 * 
	 * @param p
	 *            ��־��ҳ���ֵ��߼����ʽ
	 * @throws HtmlTimeoutException
	 *             ��Server.TIME_LIMITʱ����δ�����׳���ʱ�쳣
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
	 * ��image��ƥ��СͼƬ����֮ǰƥ�����ֻ����֮ǰƥ���Ӧ��λ����ƥ�䣬�����ȫ��ƥ��
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
					if (compareImage(partimage.image, image, x, y) == true) {// ƥ��ɹ�
						partimage.x = x;
						partimage.y = y;
						return true;
					}
				}
			}
			return false;
		}
	}

	// ����Χ�ڱȽ�ͼƬ
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
	 * �����ж���ҳ�Ƿ���ȫ�ף�ʲô��û�У�ͨ��ȡˢ��ͼ�������һ���߿����Ƿ�ȫ�ǰ�ɫ������ȷ��
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

	// ������ϼ�
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

	// ����
	private void pressKey(int key) {
		pressKey(key, 1);
	}

	// �ظ�����
	private void pressKey(int key, int times) {
		for (int i = 0; i < times; i++) {
			robot.keyPress(key);
			robot.delay(50 + (int) (Math.random() * 100));
			robot.keyRelease(key);
			robot.delay(310 + (int) (Math.random() * 100));
		}
	}

	// ����(x,y)
	private void click(int x, int y) {
		robot.mouseMove(x, y);
		robot.delay(300 + (int) (Math.random() * 100));
		robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
		robot.delay(200 + (int) (Math.random() * 100));
		robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
	}

	/**
	 * ����������֤��ֱ���ɹ����쳣
	 * 
	 * @throws NetTimeoutException
	 *             �ͻ�����Ӧ�쳣
	 * @throws HtmlTimeoutException
	 *             ��ҳ��ʱ�쳣
	 */
	private void charUnlock() throws NetTimeoutException, HtmlTimeoutException {
		while (true) {

			BufferedImage p = image.getSubimage(okimage.x - 201, okimage.y - 362, 317, 358);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Tool.runNoException(() -> ImageIO.write(p, "png", out));
			byte[] b = out.toByteArray();
			linker.sendString("image");
			linker.sendBytes(b);

			// ���շ��ؽ��
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
			// ����֤�ɹ�������֤ʧ��
			waitHtml(() -> exist(freshimage) && exist(findimage) || exist(okimage));
			if (exist(freshimage) && exist(findimage)) {
				break;
			}
		}
	}

	/**
	 * ģ���˹��ƶ���Ŀ���
	 * 
	 * @param x
	 *            Ŀ���x
	 * @param y
	 *            Ŀ���y
	 */
	private void moveTo(int x, int y) {
		// ���ѡһ���켣
		int select = (int) (tracks.size() * Math.random());
		String s = tracks.get(select);
		StringTokenizer st = new StringTokenizer(s, ",|");
		ArrayList<Integer> ints = new ArrayList<>();
		while (st.hasMoreTokens()) {
			ints.add(Integer.valueOf(st.nextToken()));
		}
		// ���յ���������������
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
// һ��С�Ĳ���ͼƬ����ȫ���е�λ��
class PartImage {
	public BufferedImage image;
	public int x, y;// ����

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
