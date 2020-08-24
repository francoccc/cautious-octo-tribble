package com.octo.trans.file;

import com.octo.trans.ITrans;
import com.octo.trans.NonblockingSocket;
import com.octo.trans.handler.NonblockingSocketHandler;
import com.octo.trans.handler.SocketState;
import com.octo.trans.handler.StateEventHandler;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author franco
 */
public class FileOutTransStateHandler implements StateEventHandler {

    public FileOutTransStateHandler() {

    }

    public FileOutTransStateHandler(File file) {
        this();
    }

    public void doWhenRead(NonblockingSocket clientTrans, NonblockingSocketHandler source) throws IOException {
        source.setInputCompleted(true);
    }

    public void doWhenWrite(NonblockingSocket clientTrans, NonblockingSocketHandler source) throws IOException {

    }

    @Override
    public void handleEvent(SocketState state, ITrans trans, Object source) throws IOException {

    }
}
