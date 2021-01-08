package com.sten1ee.olli;

import java.io.PrintStream;
import java.util.Map;

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
public class SexpParser extends SexpLexer {
    final static Sexp NIL = Sexp.NIL;
    final static Symbol SYNTAX_ERR = new Symbol("#SYNTAX_ERROR#", 0);

    public SexpParser(CharSequence source, Map<String, ? extends Symbol> predefSymbols, PrintStream err) {
        super(source, predefSymbols,  err);
    }

    public SexpParser(CharSequence source, PrintStream err) {
        super(source, PredefSymbol.predefSymbols, err);
    }

    private Sexp  parseObj() {
        switch (tokenType) {
            case SYM:
                return getSymbol();

            case NUM:
                return getNum();

            case STR:
                return getStr();

            case LPA:
                return parseList();

            case QUO:
                scanNextToken();
                return new Pair(PredefSymbol.QUOTE, new Pair(parseObj(), NIL));

            case EOF:
                return null;

            default:
                error("parseObject encountered unexpected tokenType: " + (char)tokenType);
                return SYNTAX_ERR;
        }
    }

    private Sexp  parseList() {
        assert tokenType == LPA;

        Sexp head = NIL;
        Pair tail = null;
        while (scanNextToken() != EOF) {
            if (tokenType == RPA)
                return head;

            if (tokenType == DOT) {
                scanNextToken();
                if (tail == null)
                    head = tail = new Pair(head, NIL);
                if (tokenType == RPA)
                    return head;
                tail.rest = parseObj();
                tail = null;
                continue;
            }

            Sexp obj = parseObj();
            if (tail != null)
                tail = (Pair)(tail.rest = new Pair(obj, NIL));
            else
            if (head == NIL) // first list elem:
                head = tail = new Pair(obj, NIL);
            else
                head = new Pair(head, tail = new Pair(obj, NIL));
        }

        assert tokenType == EOF;
        error("unexpected EOF while parsing list");
        return head;
    }

    /**
     * @return next token parsed or null if EOF is reached for source
     */
    public Sexp  parse() {
        scanNextToken();
        return parseObj();
    }
}
