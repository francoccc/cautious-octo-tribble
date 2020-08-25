package com.octo.trans;

import com.octo.trans.file.FileInTransStateHandler;
import com.octo.trans.server.AbstractNonblockingServer;
import com.octo.trans.server.SimpleNonblockingServer;
import com.octo.util.FileUtils;
import org.junit.Test;

public class TestNioFileServer {

    @Test
    public void testNioFileServer() throws Exception {
        // create server socket wrapper
        NonblockingServerSocket.NonblockingServerSocketArgs socketArgs = new NonblockingServerSocket.NonblockingServerSocketArgs();
        socketArgs.port(6888);
        IServerTrans serverTrans = new NonblockingServerSocket(socketArgs);
        // use FileInTransStateHandler
        String destPath = FileUtils.getJoinPath(System.getProperty("user.dir"), "dist");
        FileInTransStateHandler handler = new FileInTransStateHandler(destPath);
        AbstractNonblockingServer.NonblockingServerArgs args = new AbstractNonblockingServer.NonblockingServerArgs(serverTrans, handler);
        SimpleNonblockingServer server = new SimpleNonblockingServer(args);
        server.serve();
    }
}
