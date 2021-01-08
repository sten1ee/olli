package com.sten1ee.olli;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
class HashMapEnv extends Env {
    private HashMap<Symbol, Sexp> symbolMap = new HashMap<>();

    HashMapEnv(Env parentEnv) {
        super(parentEnv);
    }

    boolean  localSetIfDefined(Symbol sym, Sexp val) {
        return null != symbolMap.replace(sym, val);
    }

    boolean  localDefine(Symbol sym, Sexp val) {
        return null == symbolMap.putIfAbsent(sym, val);
    }

    Sexp  localEval(Symbol sym) {
        return symbolMap.get(sym);
    }

    Lval  localLval(Symbol sym) {
        return new Lval(sym);
    }

    private class Lval extends Sexp.Lval {
        final Symbol sym;

        Lval(Symbol sym) {
            this.sym = sym;
        }

        @Override
        Sexp set(Sexp val) {
            Env e = HashMapEnv.this;
            do
                if (e.localSetIfDefined(sym, val))
                    return val;
            while ((e = e.parentEnv) != null);
            return error("set! on undefined symbol ", sym);
        }

        @Override
        Sexp define(Sexp val) {
            if (localDefine(sym, val))
                return val;
            return error("define on defined symbol ", sym);
        }
    }

    @Override
    void appendTo(Appendable sb) throws IOException {
        sb.append('{');
        if (symbolMap.size() <= 5) {
            boolean first = true;
            for (Map.Entry<Symbol, Sexp> e : symbolMap.entrySet()) {
                if (first)
                    first = false;
                else
                    sb.append(", ");
                e.getKey().appendTo(sb);
                sb.append(": ");
                e.getValue().appendTo(sb);
            }
            sb.append('}');
        }
        else {
            for (Map.Entry<Symbol, Sexp> e : symbolMap.entrySet()) {
                sb.append("\n\t");
                e.getKey().appendTo(sb);
                sb.append(": ");
                e.getValue().appendTo(sb);
            }
            sb.append("\n\t}");
        }
    }
}
