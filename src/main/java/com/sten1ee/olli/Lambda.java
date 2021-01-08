package com.sten1ee.olli;

import java.io.IOException;

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
public class Lambda extends Atom {
    /**
     * Name of the Lambda if it was created through (define (f (x y) ...)
     * Or symbol 'anonymous' if this is anonymous lambda
     */
    final Symbol name;
    final Env  env;
    final Sexp params;
    final Sexp body;

    private Lambda(Symbol name, Sexp params, Sexp body, Env env) {
        super(name.srcLine);
        this.name = name;
        this.params = params;
        this.body = body;
        this.env = env;
    }

    static Lambda make(Symbol name, Sexp params, Sexp body, Env env) {
        validateParams(params);
        return new Lambda(name, params, body, env);
    }

    @Override
    public Lambda val() {
        return this;
    }

    static void  validateParams(final Sexp allParams) {
        try {
            for (Sexp params = allParams; params != NIL; ) {
                Symbol sym;
                Sexp   rest;
                if (params instanceof Pair) {
                    Pair p = (Pair) params;
                    sym = (Symbol) p.head;
                    rest = p.rest;
                }
                else {
                    sym = (Symbol) params;
                    rest = NIL;
                }

                if (sym.getClass() != Symbol.class) {
                    assert sym.getClass() == PredefSymbol.class;
                    allParams.error("lambda: predefined symbol in params list: ", allParams);
                }
                params = rest;
            }
        }
        catch (ClassCastException exn) {
            allParams.error("lambda: non-symbol in params list: ", allParams);
        }
    }

    @Override
    Sexp  apply(Sexp args, Env argEvalEnv) {
        try {
            Env newEnv = new HashMapEnv(env);
            for (Sexp params = this.params; ; ) {
                if (params instanceof Pair) {
                    Pair p = (Pair) params;
                    Symbol name = (Symbol) p.head;

                    Pair a = (Pair) args;
                    Sexp value = a.head.eval(argEvalEnv);

                    boolean defined = newEnv.localDefine(name, value);
                    assert defined : name;

                    params = p.rest;
                    args = a.rest;
                }
                else if (params == NIL) {
                    if (args != NIL)
                        return error("Extra arguments: ", args);
                    break;
                }
                else {
                    assert params instanceof Symbol : params;
                    Symbol name = (Symbol) params;
                    boolean defined = newEnv.localDefine(name, evalVarargsList(args, argEvalEnv));
                    assert defined : name;
                    break;
                }
            }
            return body.eval(newEnv);
        }
        catch (ClassCastException exn) {
            return error("Bad arguments: ", args);
        }
    }

    Sexp  evalVarargsList(Sexp args, Env argEvalEnv) {
        try {
            Sexp res = NIL;
            Pair last = null;
            while (args != NIL) {
                Pair p = (Pair) args;
                args = p.rest;
                Pair a = new Pair(p.head.eval(argEvalEnv), NIL);
                if (last != null) {
                    last.rest = a;
                    last = a;
                }
                else
                    res = last = a;
            }
            return res;
        }
        catch (ClassCastException exn) {
            return error("Bad argument list: ", args);
        }
    }

    @Override
    void appendTo(Appendable sb) throws IOException {
        sb.append("<lambda:" + name.sym + ">");
    }
}
