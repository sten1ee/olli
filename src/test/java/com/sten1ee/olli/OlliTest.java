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
        assertEquals("line 1: define on defined symbol two", error.getMessage());
    }

    @Test
    public void  test_higher_order_functions() {
        Olli olli = new Olli();
        Sexp res = olli.eval("(define map "
                + "(lambda (f l) (if (null? l) () (cons (f (car l)) (map f (cdr l))))))");
        assertTrue(res instanceof Lambda);

        res = olli.eval("(define square (lambda (x) (* x x)))");
        assertTrue(res instanceof Lambda);

        res = olli.eval("(map square '(1 2 3 4 5))");
        assertEquals("(1 4 9 16 25)", res.toString());

        res = olli.eval("(define reduce "
                + "(lambda (f l id) (if (null? l) id (reduce f (cdr l) (f id (car l))))))");
        assertTrue(res instanceof Lambda);

        res = olli.eval("(define one-to-five '(1 2 3 4 5))");
        assertEquals("(1 2 3 4 5)", res.toString());

        res = olli.eval("(reduce + (map square one-to-five) 0)");
        assertEquals(Olli.Num(55), res);
    }

    @Test
    public void test_weird_lvals_and_vals() {
        Olli olli = new Olli();
        Sexp res = olli.eval("(define a (new-env))");
        assertTrue(res instanceof Env);

        assertEquals(Olli.Num(10), olli.eval("(define (a.x) 10)"));
        assertEquals("{x: 10}", olli.eval("a").toString());
        assertEquals(Olli.Num(20), olli.eval("(a define y 20)"));
        assertEquals("{x: 10, y: 20}", olli.eval("a").toString());
        assertEquals(Olli.Num(30), olli.eval("(define (a.z) (+ (a.x) (a.y)))"));
        assertEquals("{x: 10, y: 20, z: 30}", olli.eval("a").toString());
        assertEquals(Olli.Num(40), olli.eval("(a define c 40)"));
        assertEquals("{x: 10, y: 20, z: 30, c: 40}", olli.eval("a").toString());

        assertEquals(Olli.Num(30), olli.eval("(+ (a.x) (a.y))"));
        assertEquals(Olli.Num(30), olli.eval("(a + x y)"));
        assertEquals(Olli.Num(30), olli.eval("(a.(+ x y))"));
    }
}
