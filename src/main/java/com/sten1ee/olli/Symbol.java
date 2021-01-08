package com.sten1ee.olli;

import java.io.IOException;

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
public class Symbol extends Atom {
    final String sym;
    final int    sym_hash;

    Symbol(String sym, int srcLine) {
        super(srcLine);
        this.sym_hash = (this.sym = sym.intern()).hashCode();
    }

    @Override
    public int  hashCode() {
        return sym_hash;
    }

    @Override
    public boolean  equals(Object o) {
        return sym == ((Symbol)o).sym;
    }

    @Override
    public String val() {
        return sym;
    }

    @Override
    Sexp  eval(final Env env) {
        Sexp res;
        Env  e = env;
        do
            if ((res = e.localEval(this)) != null)
                return res;
        while ((e = e.parentEnv) != null);
        return error("undefined symbol ", this);
    }

    @Override
    Lval  lval(final Env env) {
        return env.localLval(this);
    }

    @Override
    void appendTo(Appendable sb) throws IOException {
        sb.append(sym);
    }
}
