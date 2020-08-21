package com.octo.trans.handler;

/**
 * SocketState
 *
 * @author chenxy
 */
public enum SocketState {

    READ_AWAITING,
    READ,
    READ_COMPLETE,
    WRITE_AWAITING,
    WRITE,
    WRITE_COMPLETE,
    CLOSE
}
