package com.sten1ee.olli;

import java.io.IOException;

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
public class Num extends Atom {
    final double val;

    private Num(double val) {
        this.val = val;
    }

    static Num  make(double val) {
        return new Num(val);
    }

    @Override
    public Number val() {
        if ((double)(long)val == val)
            return (long) val;
        else
            return val;
    }

    @Override
    Num eval(Env env) {
        return this;
    }

    @Override
    void appendTo(Appendable sb) throws IOException {
        if ((double)(long)val == val)
            sb.append(String.valueOf((long)val));
        else
            sb.append(String.valueOf(val));
    }
}
