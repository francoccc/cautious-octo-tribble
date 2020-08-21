package com.octo.trans.handler;

import com.octo.trans.INonblockingTrans;
import com.octo.trans.IORunnable;
import com.octo.trans.NonblockingSocket;

import java.io.IOException;

/**
 * 非阻塞服务的逻辑处理部分
 *
 * @author franco
 */
public class NonblockingSocketHandler implements IORunnable {

    private SocketState state;
    private boolean isInputCompleted;
    private boolean isOutputCompleted;
    private NonblockingSocket clientTrans;
    private StateEventHandler handler;

    public NonblockingSocketHandler(NonblockingSocket clientTrans, StateEventHandler handler) {
        this(clientTrans, SocketState.READ_AWAITING, handler);
    }

    public NonblockingSocketHandler(NonblockingSocket clientTrans, SocketState state, StateEventHandler handler) {
        this.state = SocketState.READ_AWAITING;
        this.isInputCompleted = false;
        this.isOutputCompleted = false;
        this.clientTrans = clientTrans;
        this.handler = handler;
    }

    public static class Factory implements NonblockingSocketHandlerFactory {

        @Override
        public NonblockingSocketHandler createHandler(INonblockingTrans clientTrans, StateEventHandler handler) {
            return new NonblockingSocketHandler((NonblockingSocket) clientTrans, handler);
        }
    }

    /**
     * 轮询SelectionKey的时候会调用此方法，连接已经被accept(),执行逻辑部分
     */
    @Override
    public void run() throws IOException {
        SocketState preState = state;
        doStateChange();
        if (preState != state) {
            if (handler != null) {
                handler.handleEvent(preState, clientTrans, this);
            }
        }
        switch (state) {
            case READ:
                if (handler != null) {
                    handler.handleEvent(state, clientTrans, this);
                }
            case WRITE:
                if (handler != null) {
                    handler.handleEvent(state, clientTrans, this);
                }
        }
    }

    private void doStateChange() {
        switch (state) {
            case READ_AWAITING:
                state = SocketState.READ;
            case WRITE_AWAITING:
                state = SocketState.WRITE;
            case READ_COMPLETE:
                if (isInputCompleted()) {
                    state = SocketState.WRITE;
                }
            case WRITE:
                if (isOutputCompleted()) {
                    state = SocketState.WRITE_COMPLETE;
                }
        }
    }

    private boolean isInputCompleted() {
        return isInputCompleted;
    }

    private boolean isOutputCompleted() {
        return isOutputCompleted;
    }

    public void setInputCompleted(boolean inputCompleted) {
        if (isInputCompleted) {
            this.state = SocketState.WRITE_AWAITING;
        }
        this.isInputCompleted = inputCompleted;
    }

    public void setOutputCompleted(boolean outputCompleted) {
        if (outputCompleted) {
            this.state = SocketState.READ_AWAITING;
        }
        this.isOutputCompleted = outputCompleted;
    }
}
