package com.octo.trans.handler;

import com.octo.trans.ITrans;

import java.io.IOException;

/**
 * StateEventHandler
 * @see SocketState
 *
 * @author chenxy
 */
public interface StateEventHandler {

    void handleEvent(SocketState state, ITrans trans, Object source) throws IOException;
}
