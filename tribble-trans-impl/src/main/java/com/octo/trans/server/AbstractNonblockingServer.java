package com.octo.trans.server;


import com.octo.trans.INonblockingServerTrans;
import com.octo.trans.INonblockingTrans;
import com.octo.trans.IORunnable;
import com.octo.trans.IServerTrans;
import com.octo.trans.exception.TransException;
import com.octo.trans.handler.NonblockingSocketHandler;
import com.octo.trans.handler.NonblockingSocketHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.function.Supplier;

/**
 * AbstractNonBlockingServer
 *
 * @author franco
 */
public abstract class AbstractNonblockingServer extends IServer {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass().getName());

    protected Constructor<? extends Acceptor> acceptorConstructor;
    private NonblockingSocketHandlerFactory factory;

    public static class NonblockingServerArgs extends AbstractServerArgs<NonblockingServerArgs> {

        private Class<? extends Acceptor> acceptorClazz = Acceptor.class;
        private NonblockingSocketHandlerFactory factory = new NonblockingSocketHandler.Factory();
        // 服务处理handler
        private NonblockingSocketHandler.StateEventHandler stateEventHandler;

        public NonblockingServerArgs(IServerTrans serverTrans, NonblockingSocketHandler.StateEventHandler stateEventHandler) {
            super(serverTrans);
        }

        public NonblockingServerArgs acceptor(Class<? extends Acceptor> acceptorClazz) {
            this.acceptorClazz = acceptorClazz;
            return this;
        }

        public NonblockingServerArgs handlerFactory(NonblockingSocketHandlerFactory factory) {
            this.factory = factory;
            return this;
        }
    }

    public AbstractNonblockingServer(NonblockingServerArgs args) {
        super(args);
        try {
            this.acceptorConstructor
                    = args.acceptorClazz.getDeclaredConstructor(INonblockingServerTrans.class, Selector.class);
        } catch (NoSuchMethodException nsme) {
            LOGGER.warn("Acceptor has no constructor.", nsme);
        }
        this.factory = args.factory;
    }


    public void serve() {
        if (!startThread()) {
            return;
        }

        if (!startListening()) {
            return;
        }

        // 启动成功
        setServing(true);

        waitForShutdown();

        // 关闭
        setServing(false);

        stopListening();
    }

    /**
     * 启动服务器线程
     */
    protected abstract boolean startThread();

    /**
     * 监听服务器端口
     */
    protected boolean startListening() {
        try {
            serverTrans.listen();
            return true;
        } catch (TransException e) {
            LOGGER.error("Failed to listen on ServerSocket", e);
            return false;
        }
    }

    protected void stopListening() {
        serverTrans.close();
    }

    protected abstract void waitForShutdown();

    protected abstract class AbstractSelectorThread extends Thread {
        protected Selector selector;

        public AbstractSelectorThread() throws IOException {
            this.selector = SelectorProvider.provider().openSelector();
        }

        public void wakeUpSelector() {
            this.selector.wakeup();
        }

        public void cleanSelectionKey(SelectionKey sk) {
            sk.cancel();
        }
    }

    public class Acceptor implements IORunnable {

        private Selector selector;

        public Acceptor(Selector selector) {
            this.selector = selector;
        }

        @Override
        public void run() throws IOException {
            INonblockingTrans clientTrans = null;
            SelectionKey clientKey = null;
            try {
                clientTrans = ((INonblockingServerTrans) serverTrans).accept();
                clientKey = clientTrans.registerSelector(selector, SelectionKey.OP_READ);
//                Runnable handler = handlerSupplier.get();
                NonblockingSocketHandler handler = factory.createHandler(clientTrans, null);
                clientKey.attach(handler);
            } catch (IOException e) {
                // TODO handler
                if (clientTrans != null) clientTrans.close();
            }
        }
    }

}
