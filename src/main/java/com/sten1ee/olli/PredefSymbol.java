package com.sten1ee.olli;

import java.util.LinkedHashMap;

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
class PredefSymbol extends Symbol {
    final Sexp val;

    private PredefSymbol(String str, Sexp val) {
        super(str, 0); // we set srcLine to 0, as those are predefined symbols
        this.val = (val != null ? val : this); // val == null means autoreferencing symbol, like #t and #f
        Sexp prev = predefSymbols.put(str, this);
        assert prev == null : "vm init: duplicate predef symbol " + str;
    }

    @Override
    Sexp  eval(final Env env) {
        return val;
    }

    Lval  lval(Env env) {
        error("can not change predefined symbol ", this);
        return null;
    }

    final static LinkedHashMap<String, PredefSymbol> predefSymbols = new LinkedHashMap<>();

    private static final Symbol l = new Symbol("l", 0);

    public static final Symbol
            NIL   = predefineConst("()"),
            FALSE = predefineConst("#f"),
            TRUE  = predefineConst("#t"),
            ANON = predefineConst("#anon");

    public static final Sexp
            DEFINE = predefine("define", new BuiltinDefine()),
            CAR = predefine("car", new BuiltinCar()),
            CDR = predefine("cdr", new BuiltinCdr()),
            CONS = predefine("cons", new BuiltinCons()),
            EVAL= predefine("eval",new BuiltinEval()),
            LIST= predefine("list", Lambda.make(ANON, l, l, null)),
            EQ  = predefine("eq?", new BuiltinEq()),
            NULL  = predefine("null?", new BuiltinNull()),
            IF  = predefine("if", new BuiltinIf()),
            QUOTE=predefine("quote", new BuiltinQuote()),
            SET = predefine("set!", new BuiltinSet()),
            LAMBDA = predefine("lambda", new BuiltinLambda()),
            CUR_ENV = predefine("cur-env", new BuiltinCurEnv()),
            NEW_ENV = predefine("new-env", new BuiltinNewEnv()),

            SUM = predefine("+", new BuiltinSum()),
            SUB = predefine("-", new BuiltinSub()),
            MUL = predefine("*", new BuiltinMul()),
            DIV = predefine("/", new BuiltinDiv()),

            LESS= predefine("<",  new BuiltinLess()),
            LSEQ= predefine("<=", new BuiltinLessEq()),

            AND = predefine("and", new BuiltinAnd()),
            OR  = predefine("or",  new BuiltinOr()),
            NOT = predefine("not", new BuiltinNot()),

            FORMAT = predefine("format", new BuiltinFormat());


    private final static Sexp predefine(String id, Sexp val) {
        return  new PredefSymbol(id, val).val;
    }

    private final static Symbol predefineConst(String id) {
        return new PredefSymbol(id, null);
    }
}
