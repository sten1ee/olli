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
        Sexp res = new Olli().eval(inputFib7);
        assertEquals(Olli.Str("fib(7) = 21"), res);
    }

    @Test
    public void  test_define() {
        Olli olli = new Olli();
        Sexp res = olli.eval("(define two (+ 1 1))");
        assertEquals(Olli.Num(2), res);

        res = olli.eval("two");
        assertEquals(Olli.Num(2), res);

        Exception error = assertThrows(EvalError.class, () -> {
            olli.eval("(define two (- 4 1))");
        });
        assertEquals("line 1: define on already defined symbol two", error.getMessage());
    }
}
