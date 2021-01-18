# Olli
Minimalist "Scheme" interpreter coded in Java

### Build
To build with Maven:

`mvn clean install`

### Run
To start a REPL:

`java -jar target/olli.jar`

## Available builtins/primitives
To see the available builtins in the REPL type:

`(cur-env)`

## Sample Olli code:
You try copy-pasting this valid Olli code in the REPL:
```scheme
(define fib (lambda (n) (if (<= n 1) 1 (+ (fib (- n 1)) (fib (- n 2))))))
(define describe-fib (lambda (n) (format "fib(%n) = %n" n (fib n))))
(describe-fib 7)

(define map (lambda (f l) (if (null? l) () (cons (f (car l)) (map f (cdr l))))))
(define square (lambda (x) (* x x)))
(map square '(1 2 3 4 5))

(define reduce (lambda (f l id) (if (null? l) id (reduce f (cdr l) (f id (car l))))))
(reduce + (map square '(1 2 3 4 5)) 0)

(define append (lambda (l m) (if (null? l) m (cons (car l) (append (cdr l) m)))))
```
