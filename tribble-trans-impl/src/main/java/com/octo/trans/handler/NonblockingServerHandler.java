package com.octo.trans.handler;

import java.nio.channels.Selector;

/**
 * 非阻塞服务的逻辑处理部分
 *
 * @author franco
 */
public class NonblockingServerHandler implements Runnable {

    private State state;
    private Selector selector;

    public NonblockingServerHandler(Selector selector) {
        this.state = State.READ_AWAITING;

    }

    class Factory implements NonblockingServerHandlerFactory {

        @Override
        public NonblockingServerHandler createHandler() {
            return null;
        }
    }

    enum State {
        READ_AWAITING,
        READ,
        READ_COMPLETE,
        WRITE_AWAITING,
        WRITE,
        WRITE_COMPLETE,
        CLOSE
    }

    /**
     * 轮询SelectionKey的时候会调用此方法，连接已经被accept(),执行逻辑部分
     */
    @Override
    public void run() {
        doStateChange();
        switch (state) {

        }
    }

    private void doStateChange() {
        switch (state) {
            case READ_AWAITING:
                state = State.READ;
            case WRITE_AWAITING:
                state = State.WRITE;
            case READ_COMPLETE:
                if (isInputCompleted()) {
                    state = State.WRITE;
                }
            case WRITE:
                if (isOutputCompleted()) {
                    state = State.WRITE_COMPLETE;
                }
        }
    }

    private boolean isInputCompleted() {
        return false;
    }

    private boolean isOutputCompleted() {
        return false;
    }
}
