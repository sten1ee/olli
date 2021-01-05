//#!/usr/bin/env java --source 11
package com.sten1ee.olli;

import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
public class Olli {

    static final String version = "1.0.1";

    static void  testParseAndPrint(String source) {
        new SexpLexer(source).listAllTokens(System.err);
        Sexp sexp = new SexpParser(source, System.err).parse();
        System.out.println(sexp);
    }

    private final Env topEnv;
    private String inputPrompt  = "<< ";
    private String outputPrompt = "[${line}] => ";

    public Olli inputPrompt(String inputPrompt) {
        this.inputPrompt = inputPrompt;
        return this;
    }

    public Olli outputPrompt(String outputPrompt) {
        this.outputPrompt = outputPrompt;
        return this;
    }

    public Olli(Env topEnv) {
        this.topEnv = topEnv;
    }

    public Olli() {
        this(new TopEnv());
    }

    public Sexp  repl(CharSequence source, PrintStream out, PrintStream err) {
        SexpParser parser = new SexpParser(source, err);
        Sexp resExp = null;
        for (;;) {
            try {
                if (out != null && inputPrompt != null) {
                    out.print(inputPrompt.replace("${line}", String.valueOf(parser.line())));
                }
                Sexp inputExp = parser.parse();
                if (inputExp == null) {
                    return resExp;
                }
                resExp = inputExp.eval(topEnv);
                if (out != null) {
                    if (outputPrompt != null) {
                        out.print(outputPrompt.replace("${line}", String.valueOf(parser.line())));
                    }
                    out.println(resExp);
                }
            }
            catch (EvalError ee) {
                if (err != null)
                    err.println("## " + ee.getMessage());
            }
        }
    }

    public Sexp  repl(Reader input, PrintStream out, PrintStream err) {
        return repl(new ReaderCharSequence(input), out, err);
    }

    /** Start an Olli REPL */
    public static void  main(String[] args) {
        System.out.println("Welcome to Olli's Read-Eval-Print-Loop");
        new Olli().repl(new InputStreamReader(System.in), System.out, System.err);
    }
}
