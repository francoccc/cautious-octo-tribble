package com.octo.trans;

import com.octo.trans.exception.TransException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * NonblockingSocket
 * 包装SocketChannel
 *
 * @author franco
 */
public class NonblockingSocket extends INonblockingTrans {

    private static final Logger LOGGER = LoggerFactory.getLogger(NonblockingSocket.class.getName());

    /**
     * socket需要连接的地址
     */
    private final SocketAddress socketAddress;
    private final SocketChannel socketChannel;

    public NonblockingSocket(String host, int port) throws IOException {
        this(host, port, 0);
    }

    public NonblockingSocket(String host, int port, int timeout) throws IOException {
        this(new InetSocketAddress(host, port), SocketChannel.open(), timeout);
    }

    public NonblockingSocket(SocketAddress socketAddress, SocketChannel socketChannel, int timeout) throws IOException {
        this.socketAddress = socketAddress;
        this.socketChannel = socketChannel;
        socketChannel.configureBlocking(false);

        Socket socket = socketChannel.socket();
        socket.setSoLinger(false, 0);
        socket.setTcpNoDelay(true);
        socket.setKeepAlive(true);
        socket.setSoTimeout(timeout);
    }

    @Override
    public void open() throws TransException {
        throw new RuntimeException("open() is not supported by nonblocking socket");
    }

    @Override
    public void close() {
        if (null != socketChannel) {
            try {
                socketChannel.close();
            } catch (IOException e) {
                LOGGER.warn("Could not close socketChannel", e);
            }
        }
    }

    @Override
    public int read(byte[] buf, int off, int len) throws TransException {
        if ((socketChannel.validOps() & SelectionKey.OP_READ) != SelectionKey.OP_READ) {
            throw new TransException("Cannot read from write-only socket channel");
        }
        try {
            return socketChannel.read(ByteBuffer.wrap(buf, off, len));
        } catch (IOException iox) {
            throw new TransException("IO Exception", iox);
        }
    }

    @Override
    public void write(byte[] buf, int off, int len) throws TransException {
        if ((socketChannel.validOps() & SelectionKey.OP_WRITE) != SelectionKey.OP_WRITE) {
            throw new TransException("Cannot read from write-only socket channel");
        }
        try {
            socketChannel.write(ByteBuffer.wrap(buf, off, len));
        } catch (IOException iox) {
            throw new TransException("IO Exception", iox);
        }
    }

    @Override
    public boolean startConnect() throws IOException {
        return socketChannel.connect(socketAddress);
    }

    @Override
    public boolean finishConnect() throws IOException {
        return socketChannel.finishConnect();
    }

    @Override
    public SelectionKey registerSelector(Selector selector, int interestsOps) throws IOException {
        return socketChannel.register(selector, interestsOps);
    }

    @Override
    public int read(ByteBuffer buffer) throws IOException {
        return socketChannel.read(buffer);
    }

    @Override
    public int write(ByteBuffer buffer) throws IOException {
        return socketChannel.write(buffer);
    }

    /**
     * 获取非阻塞连接的Channel
     */
    public SocketChannel getSocketChannel() {
        return this.socketChannel;
    }
}
