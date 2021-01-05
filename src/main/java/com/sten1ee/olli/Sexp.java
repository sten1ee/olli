package com.sten1ee.olli;

import java.io.IOException;

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
public abstract class Sexp {
    final static Sexp  NIL = PredefSymbol.NIL;
    final static Sexp  TRUE= PredefSymbol.TRUE;
    final static Sexp  FALSE= PredefSymbol.FALSE;

    Sexp  error(String msg) {
        throw new EvalError(msg);
    }

    Sexp  error(String msg, Sexp cause) {
        throw new EvalError(msg, cause);
    }

    Sexp  eval(Env env) {
        return error("eval not applicable to " + getClass().getSimpleName());
    }

    Lval  lval(Env env) {
        error("lval not applicable to " + getClass().getSimpleName());
        return null;
    }

    Sexp  apply(Sexp args, Env env) {
        return error("apply not applicable to " + getClass().getSimpleName());
    }

    Lval  lapply(Sexp args, Env env) {
        error("lapply not applicable to " + getClass().getSimpleName());
        return null;
    }

    public final String  toString() {
        StringBuilder sb = new StringBuilder();
        try {
            appendTo(sb);
        }
        catch (IOException exn) {
            // Should be impossible but ...
            throw new RuntimeException(exn);
        }
        return sb.toString();
    }

    abstract void appendTo(Appendable sb) throws IOException;

    protected static abstract class Lval {
        abstract Sexp  set(Sexp val);

        /** All Lval(s) support set-ing, but not all Lval(s) support define-ing: */
        Sexp  define(Sexp val) {
            val.error("define not applicable to " + getClass().getSimpleName());
            return val;
        }
    }
}
