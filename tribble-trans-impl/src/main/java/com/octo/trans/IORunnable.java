package com.octo.trans;

import java.io.IOException;

/**
 * Runnable may throw IOException
 *
 * @author chenxy
 */
@FunctionalInterface
public interface IORunnable {

    void run() throws IOException;
}
