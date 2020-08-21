package com.octo.trans;

import com.octo.trans.exception.TransException;

import java.io.Closeable;

/**
 * 客户端传输连接
 *
 * @author franco
 */
public abstract class ITrans implements Closeable {

    public abstract void open() throws TransException;

    public abstract void close();

    public abstract int read(byte[] buf, int off, int len) throws TransException;

    public abstract void write(byte[] buf, int off, int len) throws TransException;

    public int readAll(byte[] buf, int off, int len) {
        return 0;
    }

    public void write(byte[] buf) throws TransException {
        write(buf, 0, buf.length);
    }

    public void flush() {

    }
}
