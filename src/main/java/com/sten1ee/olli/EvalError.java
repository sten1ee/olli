package com.sten1ee.olli;

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
public class EvalError extends OlliError {
    final Sexp cause;

    EvalError(String msg, Sexp cause) {
        super(srcLine(cause), msg + cause);
        this.cause = cause;
    }

    EvalError(String msg) {
        super(Atom.NO_SRC_LINE, msg);
        cause = null;
    }

    static int  srcLine(Sexp o) {
        if (o != null && o instanceof Symbol)
            return ((Atom) o).srcLine;
        else
            return Atom.NO_SRC_LINE;
    }

    @Override
    String errorPrompt() {
        return "## eval error ";
    }
}
