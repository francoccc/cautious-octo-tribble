package com.octo.trans.handler;

import com.octo.trans.INonblockingTrans;

/**
 *
 * @author franco
 */
public interface NonblockingSocketHandlerFactory {

    /**
     *
     * @param clientTrans
     * @param handler
     * @return
     */
    NonblockingSocketHandler createHandler(INonblockingTrans clientTrans, StateEventHandler handler);
}
