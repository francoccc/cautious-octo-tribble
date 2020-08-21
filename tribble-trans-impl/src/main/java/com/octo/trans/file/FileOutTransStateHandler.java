package com.octo.trans.file;

import com.octo.trans.NonblockingSocket;
import com.octo.trans.handler.NonblockingSocketHandler;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author franco
 */
public class FileOutTransStateHandler {

    public FileOutTransStateHandler() { }

    public FileOutTransStateHandler(File file) {

    }

    public void doWhenRead(NonblockingSocket clientTrans, NonblockingSocketHandler source) throws IOException {

    }

    public void doWhenWrite(NonblockingSocket clientTrans, NonblockingSocketHandler source) throws IOException {

    }
}
