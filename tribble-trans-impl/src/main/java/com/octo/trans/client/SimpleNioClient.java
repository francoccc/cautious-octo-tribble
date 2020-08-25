package com.octo.trans.client;

import com.octo.trans.INonblockingTrans;
import com.octo.trans.NonblockingSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * SimpleNioClient
 *
 * @author chenxy
 */
public class SimpleNioClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleNioClient.class.getName());
    private static final String NAME_PREFIX = "NioClient-";
    private static final AtomicInteger count = new AtomicInteger(0);

    private INonblockingTrans clientTrans;
    private Selector selector;
    private String name;
    private ReentrantLock writeLock = new ReentrantLock(true);
    private List<ReadFutureTask<byte[]>> readQueue;
    private MainLoop loop;

    public SimpleNioClient(String host, int port) throws IOException {
        this(host, port, 0);
    }

    public SimpleNioClient(String host, int port, int clientTimeout) throws IOException {
        this(new NonblockingSocket(host, port, clientTimeout));
    }

    public SimpleNioClient(INonblockingTrans clientTrans) {
        this.clientTrans = clientTrans;
        this.name = NAME_PREFIX + count.getAndIncrement();
    }

    public void connect() throws IOException {
        boolean start = clientTrans.startConnect();
        if (!start) {
            throw new IOException("Connect fail and check your net");
        }
        // 注册connect事件
        this.selector = SelectorProvider.provider().openSelector();
        clientTrans.registerSelector(selector, SelectionKey.OP_CONNECT);
    }

    public void read(byte[] buf, int off, int len) throws IOException {
        clientTrans.read(buf, off, len);
    }

    public Future<byte[]> readAsync(byte[] buf, int off, int len) throws IOException {
        writeLock.lock();
        try {
            ReadFutureTask<byte[]> readTask = new ReadFutureTask<>(buf, off, len);
            readQueue.add(readTask);
            clientTrans.registerSelector(selector, SelectionKey.OP_READ);
            return readTask;
        } finally {
            writeLock.unlock();
        }
    }

    public void write(byte[] buf, int off, int len) throws IOException {
        clientTrans.write(buf, off, len);
    }

    class ReadFutureTask<V> implements Future<V> {

        private int off;
        private int len;
        private V buf;

        public ReadFutureTask(V buf, int off, int len) {
            this.off = off;
            this.len = len;
            this.buf = buf;
        }

        @Override
        public boolean cancel(boolean b) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return len <= 0;
        }

        @Override
        public V get() throws InterruptedException, ExecutionException {
            checkAndThrowException();
            while(!isDone()) {
                wait();
            }
            return buf;
        }

        @Override
        public V get(long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
            checkAndThrowException();
            long start = System.currentTimeMillis();
            long last = timeUnit.toMillis(l);
            while(!isDone() && System.currentTimeMillis() < start + l) {
                wait(start + l - System.currentTimeMillis());
            }
            if (!isDone()) {
                throw new TimeoutException("Client get() timeout");
            }
            return buf;
        }

        private void checkAndThrowException() throws InterruptedException {
            if (loop == null || loop.isStop()) {
                throw new InterruptedException("Client selector loop is stop");
            }
        }
    }

   class MainLoop implements Runnable {

        private volatile boolean stop;

        @Override
        public void run() {

            while(!stop) {
                select();
            }
        }

        public boolean isStop() {
            return stop;
        }

        private void select() {
            try {
                selector.select();
                Iterator<SelectionKey> selectionKeys = selector.selectedKeys().iterator();
                while(selectionKeys.hasNext()) {
                    SelectionKey sk = selectionKeys.next();

                    if (sk.isConnectable()) {
                        if (clientTrans.finishConnect()) {
                            LOGGER.debug("{} connected remote server", "[" + name + "]");
                        } else {
                            stop = true;
                            LOGGER.debug("{} did not connect remote server", "[" + name + "]");
                            break;
                        }
                        selectionKeys.remove();
                    }
                    else if(sk.isReadable()) {
                        writeLock.lock();
                        if (readQueue.size() <= 0) {
                            // 显然不可能发生
                            selectionKeys.remove();
                        }
                        try {
                            ReadFutureTask<byte[]> rft = readQueue.get(0);
                            int l = clientTrans.read(rft.buf, rft.off, rft.len);
                            rft.len -= l;
                            rft.off += l;
                            if (rft.isDone()) {
                                readQueue.remove(0);
                                selectionKeys.remove();
                            }
                        } finally {
                            writeLock.unlock();
                        }
                    }
                }
            } catch (IOException iox) {
                stop = true;
                LOGGER.error("{} is stop caused by exception", "[" + name + "]");
            }

        }
    }
}
