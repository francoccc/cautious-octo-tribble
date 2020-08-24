package com.octo.trans.server;


import com.octo.trans.INonblockingServerTrans;
import com.octo.trans.INonblockingTrans;
import com.octo.trans.IORunnable;
import com.octo.trans.IServerTrans;
import com.octo.trans.exception.TransException;
import com.octo.trans.handler.NonblockingSocketHandler;
import com.octo.trans.handler.NonblockingSocketHandlerFactory;
import com.octo.trans.handler.StateEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;

/**
 * AbstractNonBlockingServer
 *
 * @author franco
 */
public abstract class AbstractNonblockingServer extends IServer {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass().getName());

    protected Constructor<? extends Acceptor> acceptorConstructor;
    private NonblockingSocketHandlerFactory factory;
    private StateEventHandler stateEventHandler;

    public static class NonblockingServerArgs extends AbstractServerArgs<NonblockingServerArgs> {

        private Class<? extends Acceptor> acceptorClazz = Acceptor.class;
        private NonblockingSocketHandlerFactory factory = new NonblockingSocketHandler.Factory();
        // 服务处理handler
        private StateEventHandler stateEventHandler;

        public NonblockingServerArgs(IServerTrans serverTrans, StateEventHandler stateEventHandler) {
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
        this.factory = args.factory;
        this.stateEventHandler = args.stateEventHandler;
        try {
            this.acceptorConstructor = args.acceptorClazz.getDeclaredConstructor(AbstractNonblockingServer.class, Selector.class);
        } catch (NoSuchMethodException ignored) { }
        if (this.acceptorConstructor == null) {
            this.acceptorConstructor = (Constructor<? extends Acceptor>) args.acceptorClazz.getConstructors()[0];
        }
    }


    public void serve() {
        if (!startThread()) {
            LOGGER.warn("Select thread did not start.");
            return;
        }
        LOGGER.debug("Select thread started.");

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
            LOGGER.info("Server listen on port: {}", serverTrans.getPort());
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
                NonblockingSocketHandler handler = factory.createHandler(clientTrans, stateEventHandler);
                clientKey.attach(handler);
            } catch (IOException e) {
                // TODO handler
                if (clientTrans != null) clientTrans.close();
            }
        }
    }

    public static void main(String[] args) {
        try {
            Constructor<Acceptor> acceptorConstructor = Acceptor.class.getConstructor(AbstractNonblockingServer.class, Selector.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}
