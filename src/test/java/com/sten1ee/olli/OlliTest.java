package com.sten1ee.olli;

import org.junit.Test;
import static org.junit.Assert.*;

public class OlliTest {
    final static String ENDL = "\n";

    final static String inputFib7 =
            "(define fib (lambda (n) (if (<= n 1) 1 (+ (fib (- n 1)) (fib (- n 2))))))" + ENDL
          + "(define describe-fib (lambda (n) (format \"fib(%n) = %n\" n (fib n))))" + ENDL
          + "(describe-fib 7)" + ENDL;

    @Test
    public void  test_fib7() {
        Sexp res = new Olli().repl(inputFib7, System.out, System.err);
        assertEquals(Olli.Str("fib(7) = 21"), res);
    }

    @Test
    public void  test_define() {
        Olli olli = new Olli();
        Sexp res = olli.repl("(define two (+ 1 1))", System.out, System.err);
        assertEquals(Olli.Num(2), res);
    }
}
