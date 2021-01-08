//#!/usr/bin/env java --source 11
package com.sten1ee.olli;

import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
public class Olli {

    public static final String version = "1.0.1";

    private final Env topEnv;

    /**
     * (optional) Input prompt that is rendered to `outputTo` stream.
     * Makes sense for interactive mode only.
     */
    private String inputPrompt  = "<< ";

    /**
     * (optional) Output stream that result(s) of evaluation are rendered to.
     * Makes sense for interactive mode only.
     */
    private PrintStream outputTo = System.out;
    private String      outputPrompt = "[${line}] => ";

    /**
     * (optional) Error stream that EvalError(s) are written to.
     * Makes sense for interactive mode only.
     */
    private PrintStream errorTo = System.err;

    /**
     * Allow EvalError(s) to escape (i.e. to be thrown out of) Olli.eval() and Olli.repl()
     * If set to false, EvalError(s) are caught, reported to errorTo and dismissed.
     */
    private boolean throwEvalErrors = true;

    public Olli(Env topEnv) {
        this.topEnv = topEnv;
    }

    public Olli() {
        this(new TopEnv());
    }

    public static Str Str(String val) { return Str.make(val); }
    public static Num Num(double val) { return Num.make(val, Atom.NO_SRC_LINE); }
    public static Num Num(int val) { return Num.make(val, Atom.NO_SRC_LINE); }

    public Olli inputPrompt(String inputPrompt) {
        this.inputPrompt = inputPrompt;
        return this;
    }

    public Olli outputPrompt(String outputPrompt) {
        this.outputPrompt = outputPrompt;
        return this;
    }

    public Olli outputTo(PrintStream outputTo) {
        this.outputTo = outputTo;
        return this;
    }

    public Olli noOutput() {
        this.outputTo = null;
        return this;
    }

    public Olli errorTo(PrintStream errorTo) {
        this.errorTo = errorTo;
        return this;
    }

    public Olli noError() {
        this.errorTo = null;
        return this;
    }

    public Olli throwEvalErrors(boolean throwEvalErrors) {
        this.throwEvalErrors = throwEvalErrors;
        return this;
    }

    public Sexp  eval(CharSequence source) throws EvalError {
        SexpParser parser = new SexpParser(source, errorTo);
        Sexp resExp = null;
        for (;;) {
            try {
                if (outputTo != null && inputPrompt != null) {
                    outputTo.print(inputPrompt.replace("${line}", String.valueOf(parser.line())));
                }
                Sexp inputExp = parser.parse();
                if (inputExp == null) {
                    return resExp;
                }
                resExp = inputExp.eval(topEnv);
                if (outputTo != null) {
                    if (outputPrompt != null) {
                        outputTo.print(outputPrompt.replace("${line}", String.valueOf(parser.line())));
                    }
                    outputTo.println(resExp);
                }
            }
            catch (OlliError exn) {
                if (errorTo != null) {
                    errorTo.println(exn.errorPrompt() + exn.getMessage());
                }
                if (throwEvalErrors)
                    throw exn;
            }
        }
    }

    public Sexp  repl(Reader input, PrintStream out, PrintStream err) {
        return this.outputTo(out)
                   .errorTo(err)
                   .throwEvalErrors(false)
                   .eval(new ReaderCharSequence(input));
    }

    static void  testParseAndPrint(String source) {
        new SexpLexer(source).listAllTokens(System.err);
        Sexp sexp = new SexpParser(source, System.err).parse();
        System.out.println(sexp);
    }

    /** Start an Olli REPL */
    public static void  main(String[] args) {
        System.out.println("Welcome to Olli's Read-Eval-Print-Loop");
        new Olli().repl(new InputStreamReader(System.in), System.out, System.err);
    }
}
