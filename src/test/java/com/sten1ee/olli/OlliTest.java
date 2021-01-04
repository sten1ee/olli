package com.sten1ee.olli;

import org.junit.Test;
import static org.junit.Assert.*;

public class OlliTest {
    final static String ENDL = "\n";
    final static String input1 =
            "(define fib (lambda (n) (if (<= n 1) 1 (+ (fib (- n 1)) (fib (- n 2))))))" + ENDL
          + "(define describe-fib (lambda (n) (format \"fib(%n) = %n\" n (fib n))))" + ENDL
          + "(describe-fib 7)" + ENDL;

    @Test
    public void  testInput1() {
        Sexp res = new Olli().repl(input1, System.out, System.err);
        assertTrue(res instanceof Str);
        assertEquals("fib(7) = 21", ((Str) res).val);
    }
}
