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
			owner.log("监听端口被占用：" + Server.PORT);
		}
	}

	/**
	 * 从网络端口接受一条数据，如果客户端断开则返回超时异常，在限定的时间内返回结果
	 * 
	 * @param timelimit
	 *            限定的时间(ms),小于等于0代表不限定
	 * @return 接受的数据
	 * @throws NetTimeoutException
	 *             超时异常
	 */
	public byte[] recvBytes(long timelimit) throws NetTimeoutException {
		// 设置截止时间
		long t1 = System.currentTimeMillis() + timelimit;
		if (timelimit <= 0)
			t1 = Long.MAX_VALUE;
		// 等消息
		while (System.currentTimeMillis() < t1) {
			if (recvBuf.size() > 0)
				break;
			Tool.runNoException(() -> Thread.sleep(10));
		}
		// 跳出循环
		if (recvBuf.size() == 0) {// 超时
			throw new NetTimeoutException();
		} else {
			byte[] b = getMsg();
			if (b == null) {// 网络断开了
				disconnect();
				throw new NetTimeoutException();
			} else {// 正常接受到数据
				return b;
			}
		}
	}

	/**
	 * 接受字符串
	 * 
	 * @param timelimit
	 *            限定的时间(ms),小于等于0代表不限定
	 * @return
	 * @throws NetTimeoutException
	 *             超时异常
	 */
	public String recvString(long timelimit) throws NetTimeoutException {
		byte[] b = recvBytes(timelimit);
		String s = "";
		try {
			s = new String(b, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Tool.printErr(e, "接受数据解码失败");
		}
		return s;
	}

	/**
	 * 发送一条数据
	 * 
	 * @param b
	 */
	public void sendBytes(byte[] b) {
		addMsg(b);
	}

	/**
	 * 发送一条字符串，以UTF-8格式发送
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
			Tool.printErr(e, "字符串编码失败");
		}
		sendBytes(b);
	}

	/**
	 * 阻塞等待客户端连接，连接成功后初始化消息管理器，如果当前有连接则不重连接
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
		owner.log("客户端连接成功：" + socket.getInetAddress().toString());
	}

	/**
	 * 断开当前客户端
	 */
	public void disconnect() {
		Tool.runNoException(() -> socket.close());
	}

	/**
	 * 关闭Socket,ServerSocket
	 */
	public void exit() {
		Tool.runNoException(() -> socket.close());
		Tool.runNoException(() -> serverSocket.close());
	}

	/**
	 * 非阻塞从接受队列中读取一条消息，并从队列删除
	 * 
	 * @return 消息，如果没有消息返回长度为0的数组，null标志网络通信结束
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
	 * 添加一条消息到发送队列
	 * 
	 * @param b
	 *            数据
	 */
	private void addMsg(byte[] b) {
		synchronized (sendBuf) {
			sendBuf.add(b);
		}
	}
}
