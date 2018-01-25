package com.bird.accountcheck.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import com.bird.util.Messager;
import com.bird.util.Tool;

class Linker {

	private Server owner;
	private Socket socket;
	private ServerSocket serverSocket;
	private ArrayList<byte[]> sendBuf;
	private ArrayList<byte[]> recvBuf;

	public Linker(Server owner) {
		this.owner = owner;
		try {
			serverSocket = new ServerSocket(Server.PORT, 1);
		} catch (IOException e) {
			owner.log("�����˿ڱ�ռ�ã�" + Server.PORT);
		}
	}

	/**
	 * ������˿ڽ���һ�����ݣ�����ͻ��˶Ͽ��򷵻س�ʱ�쳣�����޶���ʱ���ڷ��ؽ��
	 * 
	 * @param timelimit
	 *            �޶���ʱ��(ms),С�ڵ���0�����޶�
	 * @return ���ܵ�����
	 * @throws NetTimeoutException
	 *             ��ʱ�쳣
	 */
	public byte[] recvBytes(long timelimit) throws NetTimeoutException {
		// ���ý�ֹʱ��
		long t1 = System.currentTimeMillis() + timelimit;
		if (timelimit <= 0)
			t1 = Long.MAX_VALUE;
		// ����Ϣ
		while (System.currentTimeMillis() < t1) {
			if (recvBuf.size() > 0)
				break;
			Tool.runNoException(() -> Thread.sleep(10));
		}
		// ����ѭ��
		if (recvBuf.size() == 0) {// ��ʱ
			throw new NetTimeoutException();
		} else {
			byte[] b = getMsg();
			if (b == null) {// ����Ͽ���
				disconnect();
				throw new NetTimeoutException();
			} else {// �������ܵ�����
				return b;
			}
		}
	}

	/**
	 * �����ַ���
	 * 
	 * @param timelimit
	 *            �޶���ʱ��(ms),С�ڵ���0�����޶�
	 * @return
	 * @throws NetTimeoutException
	 *             ��ʱ�쳣
	 */
	public String recvString(long timelimit) throws NetTimeoutException {
		byte[] b = recvBytes(timelimit);
		String s = "";
		try {
			s = new String(b, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Tool.printErr(e, "�������ݽ���ʧ��");
		}
		return s;
	}

	/**
	 * ����һ������
	 * 
	 * @param b
	 */
	public void sendBytes(byte[] b) {
		addMsg(b);
	}

	/**
	 * ����һ���ַ�������UTF-8��ʽ����
	 * 
	 * @param s
	 */
	public void sendString(String s) {
		if (s == null)
			return;
		byte[] b = new byte[0];
		try {
			b = s.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			Tool.printErr(e, "�ַ�������ʧ��");
		}
		sendBytes(b);
	}

	/**
	 * �����ȴ��ͻ������ӣ����ӳɹ����ʼ����Ϣ�������������ǰ��������������
	 * 
	 * @throws IOException
	 */
	public void accept() {
		while (true) {
			try {
				socket = serverSocket.accept();
				break;
			} catch (IOException e) {
			}
		}
		sendBuf = new ArrayList<>();
		recvBuf = new ArrayList<>();
		new Thread(new Messager(socket, sendBuf, recvBuf)).start();
		owner.log("�ͻ������ӳɹ���" + socket.getInetAddress().toString());
	}

	/**
	 * �Ͽ���ǰ�ͻ���
	 */
	public void disconnect() {
		Tool.runNoException(() -> socket.close());
	}

	/**
	 * �ر�Socket,ServerSocket
	 */
	public void exit() {
		Tool.runNoException(() -> socket.close());
		Tool.runNoException(() -> serverSocket.close());
	}

	/**
	 * �������ӽ��ܶ����ж�ȡһ����Ϣ�����Ӷ���ɾ��
	 * 
	 * @return ��Ϣ�����û����Ϣ���س���Ϊ0�����飬null��־����ͨ�Ž���
	 */
	private byte[] getMsg() {
		byte[] b = new byte[0];
		if (recvBuf.size() > 0) {
			synchronized (recvBuf) {
				b = recvBuf.get(0);
				recvBuf.remove(0);
			}
		}
		return b;
	}

	/**
	 * ���һ����Ϣ�����Ͷ���
	 * 
	 * @param b
	 *            ����
	 */
	private void addMsg(byte[] b) {
		synchronized (sendBuf) {
			sendBuf.add(b);
		}
	}
}
