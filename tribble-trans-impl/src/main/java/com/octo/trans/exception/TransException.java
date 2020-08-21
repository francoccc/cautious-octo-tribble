package com.octo.trans.exception;

import java.io.IOException;

public class TransException extends IOException {

    public TransException() {
    }

    public TransException(String message) {
        super(message);
    }

    public TransException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransException(Throwable cause) {
        super(cause);
    }
}
