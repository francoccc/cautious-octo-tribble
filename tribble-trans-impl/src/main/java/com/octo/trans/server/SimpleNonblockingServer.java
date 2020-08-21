package com.octo.trans.server;

import com.octo.trans.INonblockingServerTrans;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.SelectionKey;
import java.util.Iterator;

/**
 * SimpleNonBlockingServer
 *
 * @author franco
 */
public class SimpleNonblockingServer extends AbstractNonblockingServer {

    private SelectorThread selectorThread;

    public SimpleNonblockingServer(NonblockingServerArgs args) {
        super(args);
    }

    @Override
    protected boolean startThread() {
        try {
            selectorThread = new SelectorThread((INonblockingServerTrans) serverTrans);
            selectorThread.start();
            return true;
        } catch (IOException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            return false;
        }
    }

    @Override
    protected void waitForShutdown() {
        if (selectorThread != null) {
            try {
                selectorThread.join();
            } catch (InterruptedException e) {
                LOGGER.error("SelectorThread Interrupted", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    class SelectorThread extends AbstractSelectorThread {

        private final INonblockingServerTrans nonblockingServerTrans;

        private Acceptor acceptor;

        public SelectorThread(INonblockingServerTrans nonblockingServerTrans) throws IOException, IllegalAccessException, InvocationTargetException, InstantiationException {
            this.nonblockingServerTrans = nonblockingServerTrans;
            SelectionKey sk = nonblockingServerTrans.registerSelector(selector);
            acceptor = acceptorConstructor.newInstance(nonblockingServerTrans, sk);
        }

        @Override
        public void run() {
            try {
                while (!stopped) {
                    select();
                }
                // Selector线程结束清理所有的SelectionKey
                for (SelectionKey selectionKey : selector.keys()) {
                    cleanSelectionKey(selectionKey);
                }
            } finally {
                try {
                    selector.close();
                } catch (IOException e) {
                    LOGGER.error("Got an IOException while closing selector!", e);
                }
                stopped = true;
            }
        }

        private void select() {
            try {
                selector.select();
                // process the io events we received
                Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
                while(!stopped && selectedKeys.hasNext()) {
                    SelectionKey sk = selectedKeys.next();
                    if (sk.isValid()) {
                        cleanSelectionKey(sk);
                    }
                    Object attachment = sk.attachment();
                    if (sk.attachment() == null) {
                        cleanSelectionKey(sk);
                    }
                    ((Runnable) attachment).run();
                }
            } catch (IOException e) {
                LOGGER.warn("Got an IOException while selecting!", e);
            }
        }
    }

}
