package com.sten1ee.olli;

import java.io.IOException;

/**
    (define (a.z) (+ (a.x) (a.y)))

        vs.

    (a define z (+ x y))
    (a.b define z 1)
    (a.b.c define z 2)

        vs.

    (a . define z (+ x y))


    (+ (w.x) (w.y))
    (w + x y)
    (w.(+ x y))
*/


//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
public class Pair extends Sexp {
    Sexp head;
    Sexp rest;

    public Pair(Sexp head, Sexp rest) {
        this.head = head;
        this.rest = rest;
    }

    @Override
    Sexp  eval(Env env) {
        return head.eval(env).apply(rest, env);
    }

    @Override
    Lval  lval(Env env) {
        // so that things like these work:
        // (set! (car l) 5)
        // (set! (a . x) 6)
        return head.eval(env).lapply(rest, env);
    }

    void appendTo(Appendable sb) throws IOException {
        boolean first = true;
        for (Pair p = this; p != NIL; p = (Pair)p.rest) {
            char c;
            if (first) {
                first = false;
                c = '(';
            }
            else
                c = ' ';
            sb.append(c);
            p.head.appendTo(sb);
            if (!(p.rest instanceof Pair)) {
                if (p.rest != NIL) {
                    sb.append(" . ");
                    p.rest.appendTo(sb);
                }
                sb.append(')');
                break;
            }
        }
    }
}
