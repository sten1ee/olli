package com.sten1ee.olli;

import java.io.IOException;

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
public class Str extends Atom {
    final String val;
    final String rawVal; // non-null for Str literals

    private Str(String val, String rawVal, int srcLine) {
        super(srcLine);
        this.val = val;
        this.rawVal = rawVal;
    }

    static Str  make(String val, String rawVal, int srcLine) {
        return new Str(val, rawVal, srcLine);
    }

    public static Str  make(String val) {
        return new Str(val, null, NO_SRC_LINE);
    }

    @Override
    public String val() {
        return val;
    }

    @Override
    Str eval(Env env) {
        return this;
    }

    @Override
    void appendTo(Appendable sb) throws IOException {
        sb.append('"').append(val).append('"');
    }
}
