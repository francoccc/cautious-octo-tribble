package com.octo.trans.file;

import com.octo.trans.ITrans;
import com.octo.trans.NonblockingSocket;
import com.octo.trans.handler.NonblockingSocketHandler;
import com.octo.trans.handler.SocketState;
import com.octo.trans.handler.StateEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

/**
 * 文件传输
 *
 * @author franco
 */
public class FileInTransStateHandler implements StateEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileInTransStateHandler.class.getName());
    private String destPath;
    private String fileName;
    private long size;
    private boolean init;

    public FileInTransStateHandler(String destPath) {
        this.destPath = destPath;
        this.init = false;
        LOGGER.debug("Local file path: {}", destPath);
    }

    @Override
    public void handleEvent(SocketState state, ITrans trans, Object source) throws IOException {
        switch (state) {
            case READ:
                doWhenRead((NonblockingSocket) trans, (NonblockingSocketHandler) source);
            case WRITE:
                doWhenWrite((NonblockingSocket) trans, (NonblockingSocketHandler) source);
        }
    }

    private void doWhenRead(NonblockingSocket clientTrans, NonblockingSocketHandler source) throws IOException {
        if (!init) {
            preRead(clientTrans);
        }
        if (init) {
            String filePath = destPath + File.separator + fileName;
            fastTransferFile(filePath, clientTrans.getSocketChannel());
            source.setInputCompleted(true);
        }
    }

    private void preRead(NonblockingSocket clientTrans) throws IOException {
        ByteBuffer longBuffer = ByteBuffer.allocate(8);
        ByteBuffer intBuffer = ByteBuffer.allocate(4);
        clientTrans.read(longBuffer);
        clientTrans.read(intBuffer);
        longBuffer.flip();
        this.size = longBuffer.getLong();
        intBuffer.flip();
        int fileStrLen = intBuffer.getInt();
        int pos = 0;
        byte[] buf = new byte[fileStrLen];
        while(pos < fileStrLen) {
            pos += clientTrans.read(buf, pos, fileStrLen - pos);
        }
        this.fileName = new String(buf);
        init = true;
    }

    private void fastTransferFile(String filePath, SocketChannel socketChannel) {
        File destFile = new File(filePath);
        try {
            if (!destFile.exists()) {
                destFile.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(destFile);
            FileChannel fileChannel = fos.getChannel();
            long pos = 0;
            long count;
            while (pos < size) {
                count = size - pos > 1024 ? 1024 : size - pos;
                pos += fileChannel.transferFrom(socketChannel, pos, count);
            }
            LOGGER.info("Receive file:{}, size:{}", fileName, size);
            fileChannel.force(true);
        } catch (IOException iox) {
            LOGGER.error("Write file error.", iox);
        }
    }

    public void doWhenWrite(NonblockingSocket clientTrans, NonblockingSocketHandler source) {
        // discard
        source.setOutputCompleted(true);
    }
}
