package com.octo.trans;

import com.octo.trans.exception.TransException;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

/**
 * 传输层：非阻塞I/O
 *
 * @author franco
 */
public abstract class INonblockingServerTrans extends IServerTrans {

    /**
     * 使用Selector轮询的方式，所以通过此方法注册selector
     * @param selector
     */
    public abstract SelectionKey registerSelector(Selector selector) throws IOException;

    /**
     * Override upper method
     * @return
     * @throws TransException
     */
    @Override
    public abstract INonblockingTrans accept() throws TransException;
}
