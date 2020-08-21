package com.octo.trans;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

/**
 * TNonblockingTrans
 *
 * @author franco
 */
public abstract class INonblockingTrans extends ITrans {

    public abstract boolean startConnect() throws IOException;

    public abstract boolean finishConnect() throws IOException;

    public abstract SelectionKey registerSelector(Selector selector, int interestsOps) throws IOException;

    public abstract int read(ByteBuffer buffer) throws IOException;

    public abstract int write(ByteBuffer buffer) throws IOException;
}
