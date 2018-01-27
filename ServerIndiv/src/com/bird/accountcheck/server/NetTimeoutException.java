package com.bird.accountcheck.server;

/**
 * 等待客户端响应超时异常，两种情形：一种是设定了限定时间，在限定时间内未有响应，第二种是与客户端网络断开
 * @author a
 *
 */
class NetTimeoutException extends Exception{
	private static final long serialVersionUID = 1L;
}
