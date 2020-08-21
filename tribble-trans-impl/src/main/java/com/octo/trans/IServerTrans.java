package com.octo.trans;

import com.octo.trans.exception.TransException;
import com.octo.trans.server.IServer;

import java.io.Closeable;
import java.net.InetSocketAddress;

/**
 * 包装服务器的传输层，可提供客户端的连接
 *
 * @author franco
 */
public abstract class IServerTrans implements Closeable {

    // 服务器参数
    public static abstract class AbstractServerTransArgs<T extends AbstractServerTransArgs<T>> {
        int backlog = 0;
        int clientTimeout = 0;
        InetSocketAddress bindAddr;

        public T backlog(int backlog) {
            this.backlog = backlog;
            return (T) this;
        }

        public T clientTimeout(int clientTimeout) {
            this.clientTimeout = clientTimeout;
            return (T) this;
        }

        public T port(int port) {
            this.bindAddr = new InetSocketAddress(port);
            return (T) this;
        }

        public T bindAddr(InetSocketAddress bindAddr) {
            this.bindAddr = bindAddr;
            return (T) this;
        }
    }

    /**
     * 调用listen方法
     */
    public abstract void listen() throws TransException;

    /**
     * 在底层serverSocket接受一个将要连接的客户端socket。当阻塞传输时，此方法将会阻塞线程，
     * 如果在非阻塞传输中，此方法将会返回null。
     * @return
     */
    public abstract ITrans accept() throws TransException;

    public abstract void close();
}
