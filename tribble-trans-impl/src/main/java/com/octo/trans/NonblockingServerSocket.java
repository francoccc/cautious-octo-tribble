package com.octo.trans;

import com.octo.trans.exception.TransException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketOption;
import java.nio.channels.*;

/**
 * NonBlockingServerSocket
 *
 * @author franco
 */
public class NonblockingServerSocket extends INonblockingServerTrans {

    private static final Logger LOGGER = LoggerFactory.getLogger(NonblockingServerSocket.class.getName());

    /**
     * ServerSocketChannel
     */
    private ServerSocketChannel serverSocketChannel;

    private ServerSocket serverSocket;

    /**
     * Timeout for client socket from accept
     */
    private int clientTimeout = 0;

    public static class NonblockingServerSocketArgs extends AbstractServerTransArgs<NonblockingServerSocketArgs> {}

    public NonblockingServerSocket(NonblockingServerSocketArgs args) throws TransException {
        super(args);
        this.clientTimeout = args.clientTimeout;
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);

            serverSocket = serverSocketChannel.socket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(args.bindAddr);
        } catch (IOException e) {
            serverSocket = null;
            throw new TransException("create ServerSocket Fail address:" + args.bindAddr.toString(), e);
        }
    }

    @Override
    public void listen() throws TransException {
        if (serverSocket == null) {
            throw new TransException("Listen port but ServerSocket is null");
        }
        try {
            serverSocket.setSoTimeout(clientTimeout);
        } catch (IOException e) {
            throw new TransException("listen on ServerSocket fail", e);
        }
    }

    @Override
    public INonblockingTrans accept() throws TransException {
        if (serverSocket == null) {
            throw new TransException("Non server socket.");
        }
        // TODO
        return null;
    }

    @Override
    public SelectionKey registerSelector(Selector selector) throws IOException {
        try {
            return serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (ClosedChannelException e) {
            LOGGER.error("{} selector register fail because channel is close", this.getClass().getName());
            throw e;
        }
    }

    public void close() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException iox) {
                LOGGER.warn("WARNING: Could not close server socket: " + iox.getMessage());
            }
            serverSocket = null;
        }
    }
}
