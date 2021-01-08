package com.sten1ee.olli;

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
public abstract class Atom extends Sexp {
    final int srcLine;

    final static int NO_SRC_LINE = -1;

    Atom(int srcLine) {
        this.srcLine = srcLine;
    }

    public abstract Object val();

    public boolean equals(Object other) {
        return this.getClass().equals(other.getClass())
            && this.val().equals(((Atom) other).val());
    }
}
