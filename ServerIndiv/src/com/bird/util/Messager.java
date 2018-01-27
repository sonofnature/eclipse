package com.bird.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

/**
 * ����ͨ�Žӿڣ�����һ��socket��ͨ�ţ�ͨ�������������������߳���ϵ���������߳���Ҫ������Ϣʱ��ֻҪ����Ϣ��ӵ�sendBuf�У�����recvBuf�н�����Ϣ��
 * ��Ϊͨ��������ʵ�ֽ��̼�ͨ�ţ�����Ҫͬ����������
 * ���ṩ�Ƚ�������ͨѶ����socket���ֲ�ͨʱ�������������Ͽ�������Ӳ���������ܻᱻ���֣����Է���null��־�Ͽ���timeout����������Ӳ���������ֵ�ʱ�䣻�ó��򲻸���ر�socket��
 * 
 * @author bird
 *
 */
public class Messager implements Runnable {
	private final int respond = 10;// ��Ӧ����ʱ�䣨���룩
	private final int checktime = 60000;// ����������쳣��ʱ�䣬���ȴ��������ʱ��ᷢ�ͱ���ȷ������״��
	private static final int ENQ = -1;// ѯ��ͷ
	private static final int ACK = -2;// ��Ӧͷ

	private Socket socket;
	private InputStream is;
	private OutputStream os;
	private ArrayList<byte[]> sendBuf;// ��������Ϣ�б��������̹߳���
	private ArrayList<byte[]> recvBuf;// ���ܵ���Ϣ�б��������̹߳���

	/**
	 * 
	 * @param socket
	 *            ͨ�Ŷ˿�
	 * @param sendBuf
	 *            ��������Ϣ�б���,����Ϊnull
	 * @param recvBuf
	 *            ���ܵ���Ϣ�б���,����Ϊnull
	 */
	public Messager(Socket socket, ArrayList<byte[]> sendBuf, ArrayList<byte[]> recvBuf) {
		this.socket = socket;
		this.sendBuf = sendBuf;
		this.recvBuf = recvBuf;
	}

	@Override
	public void run() {
		try {
			socket.setSoTimeout(respond);
			is = socket.getInputStream();
			os = socket.getOutputStream();

			boolean waitAck = false;// ��־�Ƿ����ڵ�Ӧ��
			long t1 = System.currentTimeMillis() + checktime;// ��ʱʱ���
			while (true) {
				// ���д����
				if (sendBuf.size() > 0) {
					byte[] b = getMsg();
					if (b != null) {
						writeHead(b.length);
						os.write(b);
						os.flush();
					}
				}
				// ��������
				try {
					int n = readHead();
					// �ɹ���ȡ
					waitAck = false;
					t1 = System.currentTimeMillis() + checktime;
					if (n == ENQ) {// �Է�ѯ��
						writeHead(ACK);
					} else if (n == ACK) {// �Է�Ӧ��
						waitAck = false;
					} else if (n > 0) {// ��������
						byte[] b = new byte[n];
						readBody(b);
						addMsg(b);
					} else {// �쳣����
						break;
					}
				} catch (SocketTimeoutException e) {
					if (System.currentTimeMillis() > t1) {// ��ʱ
						if (waitAck) {
							throw new IOException();
						} else {// ѯ��
							writeHead(ENQ);
							waitAck = true;
							t1 = System.currentTimeMillis() + 30000;// ��Ӧ�ȴ�ʱ��30s
						}
					}
				}
			}
		} catch (IOException e) {
		} finally {
			addMsg(null);// ����Ϣ���н�β
		}
	}

	/**
	 * �Ӵ�������Ϣ��ȡ��һ�����ݣ�������Ӷ�����ɾ��
	 * 
	 * @return ���ش����ĵ�һ������
	 */
	private byte[] getMsg() {
		byte[] b = new byte[0];
		synchronized (sendBuf) {
			if (sendBuf.size() > 0) {
				b = sendBuf.get(0);
				sendBuf.remove(0);
			}
		}
		return b;
	}

	/**
	 * ��������ӵ����ܶ�����
	 * 
	 * @param b
	 *            Ҫ��ӵ�����
	 */
	private void addMsg(byte[] b) {
		synchronized (recvBuf) {
			recvBuf.add(b);
		}
	}

	/**
	 * ��ȡ4�ֽ�ͷ������תΪ����
	 * 
	 * @return
	 * @throws IOException
	 */
	private int readHead() throws IOException {
		byte[] b = new byte[4];
		is.read(b);
		return b[3] & 0xFF | (b[2] & 0xFF) << 8 | (b[1] & 0xFF) << 16 | (b[0] & 0xFF) << 24;
	}

	/**
	 * ����4�ֽ�ͷ����Ϣ
	 * 
	 * @param n
	 *            ���ݳ��ȣ�enq����ѯ�ʣ�ack����Ӧ������������¾���������
	 * @throws IOException
	 */
	private void writeHead(int n) throws IOException {
		byte[] b = new byte[] { (byte) ((n >> 24) & 0xFF), (byte) ((n >> 16) & 0xFF), (byte) ((n >> 8) & 0xFF),
				(byte) (n & 0xFF) };
		os.write(b);
		os.flush();
	}

	/**
	 * ��������ֱ��������������Ҫע��SocketTimeoutException����SoTimeoutʱ��������һ�������ֵ����Ϊ�������Ǳ����������ģ�
	 * ��˳���һ����ʱ��û�н��յ�����Ϊ����Ͽ�
	 * 
	 * @param b
	 * @throws IOException
	 */
	private void readBody(byte[] b) throws IOException {
		socket.setSoTimeout(30000);
		try {
			int off = 0, end = b.length;
			while (off < end) {
				int n = is.read(b, off, end - off);
				off += n;
			}
		} catch (SocketTimeoutException e) {
			throw new IOException();
		}
		socket.setSoTimeout(respond);
	}
}
