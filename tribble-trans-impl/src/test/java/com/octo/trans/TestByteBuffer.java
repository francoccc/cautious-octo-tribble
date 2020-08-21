package com.octo.trans;

import org.junit.Test;

import java.nio.ByteBuffer;
import static org.junit.Assert.*;

public class TestByteBuffer {

    @Test
    public void testByteBuffer() throws Exception {
        // allocate 4 bytes
        ByteBuffer intBuffer = ByteBuffer.allocate(4);
        byte[] src = new byte[]{0, 0, 0, 4};
        intBuffer.put(src);
        intBuffer.flip();
        byte[] dst = new byte[4];
        intBuffer.get(dst, 0, 4);
        assertArrayEquals(src, dst);
        // reuse 4 bytes
        intBuffer.flip();
        byte[] newSrc = new byte[]{0, 0, 0, 5};
        intBuffer.put(newSrc);
        intBuffer.flip();
        intBuffer.get(dst, 0, 4);
        intBuffer.flip();
        assertArrayEquals(newSrc, dst);
        assertEquals(5, intBuffer.getInt());
    }
}
