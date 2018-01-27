package com.bird.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

/**
 * 网络通信接口，负责一个socket的通信，通过两个共享区与其他线程联系，当其他线程想要发送消息时，只要将消息添加到sendBuf中，而从recvBuf中接受消息；
 * 因为通过共享区实现进程间通信，故需要同步共享区；
 * 它提供稳健的网络通讯，当socket出现不通时，不论是主动断开，还是硬件断网，总会被发现，并以返回null标志断开；timeout参数是设置硬件断网发现的时间；该程序不负责关闭socket。
 * 
 * @author bird
 *
 */
public class Messager implements Runnable {
	private final int respond = 10;// 响应任务时间（毫秒）
	private final int checktime = 60000;// 最长发现网络异常的时间，读等待到达这个时间会发送报文确认网络状况
	private static final int ENQ = -1;// 询问头
	private static final int ACK = -2;// 响应头

	private Socket socket;
	private InputStream is;
	private OutputStream os;
	private ArrayList<byte[]> sendBuf;// 待发送消息列表，与其他线程共享
	private ArrayList<byte[]> recvBuf;// 接受的消息列表，与其他线程共享

	/**
	 * 
	 * @param socket
	 *            通信端口
	 * @param sendBuf
	 *            待发送消息列表区,不能为null
	 * @param recvBuf
	 *            接受的消息列表区,不能为null
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

			boolean waitAck = false;// 标志是否是在等应答
			long t1 = System.currentTimeMillis() + checktime;// 超时时间点
			while (true) {
				// 检查写任务
				if (sendBuf.size() > 0) {
					byte[] b = getMsg();
					if (b != null) {
						writeHead(b.length);
						os.write(b);
						os.flush();
					}
				}
				// 检查读任务
				try {
					int n = readHead();
					// 成功读取
					waitAck = false;
					t1 = System.currentTimeMillis() + checktime;
					if (n == ENQ) {// 对方询问
						writeHead(ACK);
					} else if (n == ACK) {// 对方应答
						waitAck = false;
					} else if (n > 0) {// 正常数据
						byte[] b = new byte[n];
						readBody(b);
						addMsg(b);
					} else {// 异常数据
						break;
					}
				} catch (SocketTimeoutException e) {
					if (System.currentTimeMillis() > t1) {// 超时
						if (waitAck) {
							throw new IOException();
						} else {// 询问
							writeHead(ENQ);
							waitAck = true;
							t1 = System.currentTimeMillis() + 30000;// 响应等待时间30s
						}
					}
				}
			}
		} catch (IOException e) {
		} finally {
			addMsg(null);// 给消息队列结尾
		}
	}

	/**
	 * 从待发送消息中取第一条数据，并将其从队列中删除
	 * 
	 * @return 返回待发的第一条数据
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
	 * 将数据添加到接受队列中
	 * 
	 * @param b
	 *            要添加的数据
	 */
	private void addMsg(byte[] b) {
		synchronized (recvBuf) {
			recvBuf.add(b);
		}
	}

	/**
	 * 读取4字节头部，并转为整数
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
	 * 发送4字节头部信息
	 * 
	 * @param n
	 *            数据长度，enq代表询问，ack代表应答，这两种清空下均无数据体
	 * @throws IOException
	 */
	private void writeHead(int n) throws IOException {
		byte[] b = new byte[] { (byte) ((n >> 24) & 0xFF), (byte) ((n >> 16) & 0xFF), (byte) ((n >> 8) & 0xFF),
				(byte) (n & 0xFF) };
		os.write(b);
		os.flush();
	}

	/**
	 * 读数据体直到将数组填满，要注意SocketTimeoutException，将SoTimeout时间设置在一个合理的值，因为数据体是必须紧跟后面的，
	 * 因此超出一定的时间没有接收到就认为网络断开
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
