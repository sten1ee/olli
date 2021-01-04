package com.sten1ee.olli;

import java.io.IOException;

/**
 * Builtins;
 *
 */
abstract class Builtin extends Atom {

    @Override
    void  appendTo(Appendable sb) throws IOException {
        sb.append('#').append(getClass().getSimpleName());
    }

    @Override
    Sexp eval(Env env) {
        return this;
    }
}


class BuiltinCurEnv extends Builtin {

    @Override
    Sexp apply(Sexp args, Env env) {
        if (args == NIL)
            return env;
        return error("cur-env: bad arguments: ", args);
    }
}


class BuiltinNewEnv extends Builtin {

    @Override
    Sexp apply(Sexp args, Env env) {
        if (args == NIL)
            return new HashMapEnv(env);
        return error("new-env: bad arguments: ", args);
    }
}


class BuiltinEval extends Builtin {

    @Override
    Sexp apply(Sexp args, Env env) {
        try {
            Pair argl = (Pair) args;
            if (argl.rest != NIL)
                return error("eval: too many arguments: ", argl);
            return argl.head.eval(env).eval(env);
        }
        catch (ClassCastException exn) {
            return error("eval: bad arguments: ", args);
        }
    }
}


class BuiltinQuote extends Builtin {

    @Override
    Sexp apply(Sexp args, Env env) {
        try {
            Pair argl = (Pair) args;
            if (argl.rest != NIL)
                return error("quote: too many arguments: ", argl);
            return argl.head;
        }
        catch (ClassCastException exn) {
            return error("quote: bad args: ", args);
        }
    }

    @Override
    void  appendTo(Appendable sb) throws IOException {
        sb.append("quote");
    }
}


class BuiltinSet extends Builtin {

    @Override
    Sexp apply(Sexp args, Env env) {
        try {
            Pair lst = (Pair) args;
            Lval lval = lst.head.lval(env);
            Sexp  val = ((Pair)lst.rest).head.eval(env);
            return lval.set(val);
        }
        catch (ClassCastException exn) {
            return error("set!: bad args: ", args);
        }
    }
}


class BuiltinDefine extends Builtin {

    @Override
    Sexp apply(Sexp args, Env env) {
        try {
            Pair lst = (Pair) args;
            Lval lval = lst.head.lval(env);
            Sexp  val = ((Pair)lst.rest).head.eval(env);
            return lval.define(val);
        }
        catch (ClassCastException exn) {
            return error("define: bad args: ", args);
        }
    }
}


class BuiltinEq extends Builtin {

    @Override
    Sexp apply(Sexp args, Env env) {
        try {
            Pair xp = (Pair) args;
            Pair yp = (Pair) xp.rest;
            if (yp.rest != NIL)
                throw new ClassCastException();

            Sexp x = xp.head.eval(env);
            Sexp y = yp.head.eval(env);

            if (x instanceof Num)
                return (y instanceof Num && ((Num)x).val == ((Num)y).val ? TRUE : FALSE);
            if (x instanceof Symbol)
                return (y instanceof Symbol && ((Symbol)x).sym == ((Symbol)y).sym ? TRUE : FALSE);

            // in all other cases it should have the semantics of same-object?
            return (x == y ? TRUE : FALSE);
        }
        catch (ClassCastException exn) {
            return error("eq?: bad args: ", args);
        }
    }
}


class BuiltinCar extends Builtin {

    @Override
    Sexp apply(Sexp args, Env env) {
        try {
            Pair argl = (Pair) args;
            Sexp arg  = argl.head.eval(env);
            return ((Pair)arg).head;
        }
        catch (ClassCastException exn) {
            return error("car: bad args: ", args);
        }
    }

    static class Lval extends Sexp.Lval {
        final Pair targetPair;

        Lval(Pair targetPair) {
            this.targetPair = targetPair;
        }

        @Override
        Sexp set(Sexp val) {
            return targetPair.head = val;
        }
    }

    @Override
    Lval lapply(Sexp args, Env env) {
        try {
            Pair argl = (Pair) args;
            Sexp arg  = argl.head.eval(env);
            return new Lval((Pair)arg);
        }
        catch (ClassCastException exn) {
            error("car: bad args: ", args);
            return null;
        }
    }
}


class BuiltinCdr extends Builtin {

    @Override
    Sexp apply(Sexp args, Env env) {
        try {
            Pair argl = (Pair) args;
            Sexp arg  = argl.head.eval(env);
            return ((Pair)arg).rest;
        }
        catch (ClassCastException exn) {
            return error("cdr: bad args: ", args);
        }
    }

    static class Lval extends Sexp.Lval {
        final Pair targetPair;

        Lval(Pair targetPair) {
            this.targetPair = targetPair;
        }

        @Override
        Sexp set(Sexp val) {
            return targetPair.rest = val;
        }
    }

    @Override
    Lval lapply(Sexp args, Env env) {
        try {
            Pair argl = (Pair) args;
            Sexp arg  = argl.head.eval(env);
            return new Lval((Pair)arg);
        }
        catch (ClassCastException exn) {
            error("cdr: bad args: ", args);
            return null;
        }
    }
}


class BuiltinIf extends Builtin {
    @Override
    Sexp apply(Sexp args, Env env) {
        try {
            Pair argl = (Pair) args;
            Sexp cond = argl.head.eval(env);
            argl = (Pair)argl.rest;
            if (cond != FALSE)
                return argl.head.eval(env);
            if (argl.rest == NIL)
                return NIL;
            argl = (Pair)argl.rest;
            return argl.head.eval(env);
        }
        catch (ClassCastException exn) {
            return error("if: bad args: ", args);
        }
    }

    Lval lapply(Sexp args, Env env) {
        try {
            Pair argl = (Pair) args;
            Sexp cond = argl.head.eval(env);
            argl = (Pair)argl.rest;
            if (cond != FALSE)
                return argl.head.lval(env);
            if (argl.rest == NIL)
                return NIL.lval(env);
            argl = (Pair)argl.rest;
            return argl.head.lval(env);
        }
        catch (ClassCastException exn) {
            error("if: bad args: ", args);
            return null;
        }
    }
}


class BuiltinSum extends Builtin {

    @Override
    Sexp  apply(final Sexp args, final Env env) {
        double sum = 0;
        for (Sexp argl = args; argl != NIL; argl = ((Pair) argl).rest) {
            try {
                Num arg = (Num) ((Pair) argl).head.eval(env);
                sum += arg.val;
            }
            catch (ClassCastException exn) {
                return error("+: bad args: ", args);
            }
        }
        return Num.make(sum);
    }
}


class BuiltinSub extends Builtin {

    @Override
    Sexp  apply(final Sexp args, final Env env) {
        try {
            double res = 0;
            Pair argl = (Pair) args;
            for (; ; argl = (Pair) argl.rest) {
                Num  n = (Num) argl.head.eval(env);
                if (argl == args)
                    res = n.val;
                else
                    res -= n.val;

                if (argl.rest == NIL)
                    break;
            }
            return Num.make(argl != args ? res : -res); // As (- x) is negation
        }
        catch (ClassCastException exn) {
            return error("-: bad args: ", args);
        }
    }
}


class BuiltinMul extends Builtin {

    @Override
    Sexp apply(Sexp args, Env env) {
        try {
            double res = 1;
            Pair argl = (Pair) args;
            for (; ; argl = (Pair) argl.rest) {
                Num  n = (Num) argl.head.eval(env);
                res *= n.val;

                if (argl.rest == NIL)
                    break;
            }
            return Num.make(res);
        }
        catch (ClassCastException exn) {
            return error("*: bad args: ", args);
        }
    }
}


class BuiltinDiv extends Builtin {

    @Override
    Sexp  apply(final Sexp args, final Env env) {
        try {
            double res = 1;
            Pair argl = (Pair) args;
            for (; ; argl = (Pair) argl.rest) {
                Num  n = (Num) argl.head.eval(env);
                if (argl == args)
                    res = n.val;
                else
                    res /= n.val;

                if (argl.rest == NIL)
                    break;
            }
            return Num.make(argl != args ? res : 1./res); // As (/ x) is reciprocal
        }
        catch (ClassCastException exn) {
            return error("/: bad args: ", args);
        }
    }
}


class BuiltinLess extends Builtin {

    @Override
    Sexp  apply(final Sexp args, final Env env) {
        try {
            double prev = 0;
            Pair argl = (Pair) args;
            for (; ; argl = (Pair) argl.rest) {
                Num  n = (Num) argl.head.eval(env);
                if (argl != args && !(prev < n.val))
                    return FALSE;

                if (argl.rest == NIL)
                    return TRUE;

                prev = n.val;
            }
        }
        catch (ClassCastException exn) {
            return error("<: bad args: ", args);
        }
    }
}


class BuiltinLessEq extends Builtin {

    @Override
    Sexp  apply(final Sexp args, final Env env) {
        try {
            double prev = 0;
            Pair argl = (Pair) args;
            for (; ; argl = (Pair) argl.rest) {
                Num  n = (Num) argl.head.eval(env);
                if (argl != args && !(prev <= n.val))
                    return FALSE;

                if (argl.rest == NIL)
                    return TRUE;

                prev = n.val;
            }
        }
        catch (ClassCastException exn) {
            return error("<=: bad args: ", args);
        }
    }
}


class BuiltinGrtr extends Builtin {

    @Override
    Sexp  apply(final Sexp args, final Env env) {
        try {
            double prev = 0;
            Pair argl = (Pair) args;
            for (; ; argl = (Pair) argl.rest) {
                Num  n = (Num) argl.head.eval(env);
                if (argl != args && !(prev > n.val))
                    return FALSE;

                if (argl.rest == NIL)
                    return TRUE;

                prev = n.val;
            }
        }
        catch (ClassCastException exn) {
            return error(">: bad args: ", args);
        }
    }
}


class BuiltinGrtrEq extends Builtin {

    @Override
    Sexp  apply(final Sexp args, final Env env) {
        try {
            double prev = 0;
            Pair argl = (Pair) args;
            for (; ; argl = (Pair) argl.rest) {
                Num  n = (Num) argl.head.eval(env);
                if (argl != args && !(prev >= n.val))
                    return FALSE;

                if (argl.rest == NIL)
                    return TRUE;

                prev = n.val;
            }
        }
        catch (ClassCastException exn) {
            return error(">=: bad args: ", args);
        }
    }
}


class BuiltinAnd extends Builtin {

    @Override
    Sexp  apply(final Sexp args, final Env env) {
        try {
            Pair argl = (Pair) args;
            for (; ; argl = (Pair) argl.rest) {
                Sexp res = argl.head.eval(env);
                if (res == FALSE)
                    return FALSE;

                if (argl.rest == NIL)
                    return res;
            }
        }
        catch (ClassCastException exn) {
            return error("and: bad args: ", args);
        }
    }
}


class BuiltinOr extends Builtin {

    @Override
    Sexp  apply(final Sexp args, final Env env) {
        try {
            Pair argl = (Pair) args;
            for (; ; argl = (Pair) argl.rest) {
                Sexp res = argl.head.eval(env);
                if (res != FALSE)
                    return res;

                if (argl.rest == NIL)
                    return FALSE;
            }
        }
        catch (ClassCastException exn) {
            return error("and: bad args: ", args);
        }
    }
}


class BuiltinNot extends Builtin {

    @Override
    Sexp  apply(final Sexp args, final Env env) {
        try {
            Pair argl = (Pair) args;
            if (argl.rest != NIL)
                throw new ClassCastException();
            return (argl.head.eval(env) == FALSE ? TRUE : FALSE);
        }
        catch (ClassCastException exn) {
            return error("and: bad args: ", args);
        }
    }
}


class BuiltinLambda extends Builtin {

    @Override
    Sexp  apply(final Sexp args, final Env env) {
        try {
            Pair paramsp = (Pair) args;
            Pair bodyp = (Pair) paramsp.rest;
            if (bodyp.rest != NIL)
                throw new ClassCastException();
            return Closure.make(paramsp.head, bodyp.head, env);
        }
        catch (ClassCastException exn) {
            return error("lambda: bad args: ", args);
        }
    }
}


class BuiltinFormat extends Builtin {

    @Override
    Sexp  apply(final Sexp args, final Env env) {
        try {
            Pair argl = (Pair) args;
            String formatStr = ((Str) argl.head).val;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < formatStr.length(); ) {
                char c = formatStr.charAt(i++);
                if (c != '%' || i == formatStr.length()) {
                    sb.append(c);
                    continue;
                }
                switch (c = formatStr.charAt(i++)) {
                    case '%':
                        sb.append(c);
                        continue;

                    case 'd':
                    case 'i':
                    case 'f':
                        argl = (Pair) argl.rest;
                        double numVal = ((Num) argl.head.eval(env)).val;
                        if (c == 'f')
                            sb.append(numVal);
                        else
                            sb.append((long) numVal);
                        break;

                    case 's':
                        argl = (Pair) argl.rest;
                        String strVal = ((Str) argl.head.eval(env)).val;
                        sb.append(strVal);
                        break;

                    case 'o':
                    default:
                        return error("format: bad format char: " + c);
                }
            }
            return Str.make(sb.toString(), null);
        }
        catch (ClassCastException exn) {
            return error("format: bad args: ", args);
        }
    }
}
