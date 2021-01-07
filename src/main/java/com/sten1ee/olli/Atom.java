package com.sten1ee.olli;

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
public abstract class Atom extends Sexp {
    public abstract Object val();

    public boolean equals(Object other) {
        return this.getClass().equals(other.getClass())
            && this.val().equals(((Atom) other).val());
    }
}
