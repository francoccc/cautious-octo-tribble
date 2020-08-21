package com.octo.trans.server;

import com.octo.trans.IServerTrans;

/**
 * 通用服务器接口
 *
 * @author franco
 * @Date
 */
public abstract class IServer {

    public static abstract class AbstractServerArgs<T extends AbstractServerArgs<T>> {
        final IServerTrans serverTrans;

        public AbstractServerArgs(IServerTrans serverTrans) {
            this.serverTrans = serverTrans;
        }
    }

    // 定义服务器的传输层
    protected IServerTrans serverTrans;

    protected volatile boolean isServing;

    protected volatile boolean stopped;

    public IServer(AbstractServerArgs args) {
        this.serverTrans = args.serverTrans;
    }

    /**
     * 此方法是服务开始的入口点，调用之后将激活服务
     */
    public abstract void serve();

    /**
     * 调用此方法将结束服务
     */
    public void stop() {

    }

    public void setServing(boolean isServing) {
        this.isServing = isServing;
    }
}
