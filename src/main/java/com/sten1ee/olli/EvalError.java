package com.sten1ee.olli;

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
public class EvalError extends RuntimeException {
    final Sexp cause;

    EvalError(String msg, Sexp cause) {
        super(location(cause) + msg + cause);
        this.cause = cause;
    }

    EvalError(String msg) {
        super(msg);
        cause = null;
    }

    static String  location(Sexp o) {
        if (o != null && o instanceof Symbol)
            return "line " + ((Symbol)o).srcLine + ": ";

        return "";
    }
}
