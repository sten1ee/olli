package com.sten1ee.olli;

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
public abstract class OlliError extends RuntimeException {
    final int srcLine;

    OlliError(int srcLine, String msg) {
        super("line " + srcLine + ": " + msg);
        this.srcLine = srcLine;
    }

    abstract String errorPrompt();
}
