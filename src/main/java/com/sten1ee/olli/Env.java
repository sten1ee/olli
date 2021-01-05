package com.sten1ee.olli;

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
public abstract class Env extends Sexp {
    final Env   parentEnv;

    Env(Env parentEnv) {
        this.parentEnv = parentEnv;
    }

    abstract Sexp localEval(Symbol sym);

    abstract Lval  localLval(Symbol sym);

    abstract boolean  localSetIfDefined(Symbol sym, Sexp val);

    abstract boolean  localDefine(Symbol sym, Sexp val);

    @Override
    Sexp  apply(Sexp args, Env env) {
        return args.eval(this);
    }

    @Override
    Lval  lapply(Sexp args, Env env) {
        return args.lval(this);
    }
}
