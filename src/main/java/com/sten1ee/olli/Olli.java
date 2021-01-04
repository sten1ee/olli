//#!/usr/bin/env java --source 11
package com.sten1ee.olli;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
/**
 * Created by stenlee on 4/1/14.
 */
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
class EvalError extends RuntimeException {
    final Sexp cause;

    EvalError(String msg, Sexp cause) {
        super(location(cause) + msg + cause);
        this.cause = cause;
    }

    EvalError(String msg) {
        super(msg);
        cause = null;
    }

    static String  location(Sexp o) {
        if (o != null && o instanceof Symbol)
            return "line " + ((Symbol)o).srcLine + ": ";

        return "";
    }
}


abstract class Sexp {
    final static Sexp  NIL = PredefSymbol.NIL;
    final static Sexp  TRUE= PredefSymbol.TRUE;
    final static Sexp  FALSE= PredefSymbol.FALSE;

    Sexp  error(String msg) {
        throw new EvalError(msg);
    }

    Sexp  error(String msg, Sexp cause) {
        throw new EvalError(msg, cause);
    }

    Sexp  eval(Env env) {
        return error("eval not applicable to " + getClass().getSimpleName());
    }

    Lval  lval(Env env) {
        error("lval not applicable to " + getClass().getSimpleName());
        return null;
    }

    Sexp  apply(Sexp args, Env env) {
        return error("apply not applicable to " + getClass().getSimpleName());
    }

    Lval  lapply(Sexp args, Env env) {
        error("lapply not applicable to " + getClass().getSimpleName());
        return null;
    }

    public final String  toString() {
        StringBuilder sb = new StringBuilder();
        try {
            appendTo(sb);
        }
        catch (IOException exn) {
            // Should be impossible but ...
            throw new RuntimeException(exn);
        }
        return sb.toString();
    }
    abstract void appendTo(Appendable sb) throws IOException;

    protected static abstract class Lval {
        abstract Sexp  set(Sexp val);

        /** All Lval(s) support set-ing, but not all Lval(s) support define-ing: */
        Sexp  define(Sexp val) {
            val.error("define not applicable to " + getClass().getSimpleName());
            return val;
        }
    }
}

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
class Pair extends Sexp {
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

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
abstract class Env extends Sexp {
    final Env   parentEnv;

    Env(Env parentEnv) {
        this.parentEnv = parentEnv;
    }

    abstract Sexp localEval(Symbol sym);

    abstract Lval  localLval(Symbol sym);

    abstract boolean  localSetIfDefined(Symbol sym, Sexp val);

    abstract boolean  localDefine(Symbol sym, Sexp val);

    @Override
    Sexp  apply(Sexp args, Env env) {
        return args.eval(this);
    }

    @Override
    Lval  lapply(Sexp args, Env env) {
        return args.lval(this);
    }
}

class HashMapEnv extends Env {
    private HashMap<Symbol, Sexp>  symbolMap = new HashMap<>();

    HashMapEnv(Env parentEnv) {
        super(parentEnv);
    }

    boolean  localSetIfDefined(Symbol sym, Sexp val) {
        return null != symbolMap.replace(sym, val);
    }

    boolean  localDefine(Symbol sym, Sexp val) {
        return null == symbolMap.putIfAbsent(sym, val);
    }

    Sexp  localEval(Symbol sym) {
        return symbolMap.get(sym);
    }

    Lval  localLval(Symbol sym) {
        return new Lval(sym);
    }

    private class Lval extends Sexp.Lval {
        final Symbol sym;

        Lval(Symbol sym) {
            this.sym = sym;
        }

        @Override
        Sexp set(Sexp val) {
            Env e = HashMapEnv.this;
            do
                if (e.localSetIfDefined(sym, val))
                    return val;
            while ((e = e.parentEnv) != null);
            return error("set! on undefined symbol ", sym);
        }

        @Override
        Sexp define(Sexp val) {
            if (localDefine(sym, val))
                return val;
            return error("define on already defined symbol ", sym);
        }
    }

    @Override
    void appendTo(Appendable sb) throws IOException {
        sb.append('{');
        if (symbolMap.size() <= 5) {
            boolean first = true;
            for (Map.Entry<Symbol, Sexp> e : symbolMap.entrySet()) {
                if (first)
                    first = false;
                else
                    sb.append(", ");
                e.getKey().appendTo(sb);
                sb.append(": ");
                e.getValue().appendTo(sb);
            }
            sb.append('}');
        }
        else {
            for (Map.Entry<Symbol, Sexp> e : symbolMap.entrySet()) {
                sb.append("\n\t");
                e.getKey().appendTo(sb);
                sb.append(": ");
                e.getValue().appendTo(sb);
            }
            sb.append("\n\t}");
        }
    }
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
abstract class Atom extends Sexp {
}

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
class Num extends Atom {
    final double val;

    private Num(double val) {
        this.val = val;
    }

    static Num  make(double val) {
        return new Num(val);
    }

    @Override
    Num eval(Env env) {
        return this;
    }

    @Override
    void appendTo(Appendable sb) throws IOException {
        if ((double)(long)val == val)
            sb.append(String.valueOf((long)val));
        else
            sb.append(String.valueOf(val));
    }
}

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
class Str extends Atom {
    final String val;
    final String rawVal; // non-null for Str literals

    private Str(String val, String rawVal) {
        this.val = val;
        this.rawVal = rawVal;
    }

    static Str  make(String val, String rawVal) {
        return new Str(val, rawVal);
    }

    static Str  make(String val) {
        return new Str(val, null);
    }

    @Override
    Str eval(Env env) {
        return this;
    }

    @Override
    void appendTo(Appendable sb) throws IOException {
        sb.append('"').append(val).append('"');
    }
}

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
class Symbol extends Atom {
    final String sym;
    final int    sym_hash;
    final int    srcLine;

    Symbol(String sym, int srcLine) {
        this.sym_hash = (this.sym = sym.intern()).hashCode();
        this.srcLine = srcLine;
    }

    @Override
    public int  hashCode() {
        return sym_hash;
    }

    @Override
    public boolean  equals(Object o) {
        return sym == ((Symbol)o).sym;
    }

    @Override
    Sexp  eval(final Env env) {
        Sexp res;
        Env  e = env;
        do
            if ((res = e.localEval(this)) != null)
                return res;
        while ((e = e.parentEnv) != null);
        return error("undefined symbol ", this);
    }

    @Override
    Lval  lval(final Env env) {
        return env.localLval(this);
    }

    @Override
    void appendTo(Appendable sb) throws IOException {
        sb.append(sym);
    }
}

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
class Closure extends Atom {
    final Env  env;
    final Sexp params;
    final Sexp body;

    private Closure(Sexp params, Sexp body, Env env) {
        this.params = params;
        this.body = body;
        this.env = env;
    }

    static Closure  make(Sexp params, Sexp body, Env env) {
        validateParams(params);
        return new Closure(params, body, env);
    }
    
    static void  validateParams(final Sexp allParams) {
        try {
            for (Sexp params = allParams; params != NIL; ) {
                Symbol sym;
                Sexp   rest;
                if (params instanceof Pair) {
                    Pair p = (Pair) params;
                    sym = (Symbol) p.head;
                    rest = p.rest;
                }
                else {
                    sym = (Symbol) params;
                    rest = NIL;
                }

                if (sym.getClass() != Symbol.class) {
                    assert sym.getClass() == PredefSymbol.class;
                    allParams.error("lambda: predefined symbol in params list: ", allParams);
                }
                params = rest;
            }
        }
        catch (ClassCastException exn) {
            allParams.error("lambda: non-symbol in params list: ", allParams);
        }
    }

    @Override
    Sexp  apply(Sexp args, Env argEvalEnv) {
        try {
            Env newEnv = new HashMapEnv(env);
            for (Sexp params = this.params; ; ) {
                if (params instanceof Pair) {
                    Pair p = (Pair) params;
                    Symbol name = (Symbol) p.head;

                    Pair a = (Pair) args;
                    Sexp value = a.head.eval(argEvalEnv);

                    boolean defined = newEnv.localDefine(name, value);
                    assert defined : name;

                    params = p.rest;
                    args = a.rest;
                }
                else if (params == NIL) {
                    if (args != NIL)
                        return error("Extra arguments: ", args);
                    break;
                }
                else {
                    assert params instanceof Symbol : params;
                    Symbol name = (Symbol) params;
                    boolean defined = newEnv.localDefine(name, evalVarargsList(args, argEvalEnv));
                    assert defined : name;
                    break;
                }
            }
            return body.eval(newEnv);
        }
        catch (ClassCastException exn) {
            return error("Bad arguments: ", args);
        }
    }

    Sexp  evalVarargsList(Sexp args, Env argEvalEnv) {
        try {
            Sexp res = NIL;
            Pair last = null;
            while (args != NIL) {
                Pair p = (Pair) args;
                args = p.rest;
                Pair a = new Pair(p.head.eval(argEvalEnv), NIL);
                if (last != null) {
                    last.rest = a;
                    last = a;
                }
                else
                    res = last = a;
            }
            return res;
        }
        catch (ClassCastException exn) {
            return error("Bad argument list: ", args);
        }
    }

    @Override
    void appendTo(Appendable sb) throws IOException {
        sb.append("#Closure");
    }
}


//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
class PredefSymbol extends Symbol {
    final Sexp val;

    private PredefSymbol(String str, Sexp val) {
        super(str, 0); // we set srcLine to 0, as those are predefined symbols
        this.val = (val != null ? val : this); // val == null means autoreferencing symbol, like #t and #f
        Sexp prev = predefSymbols.put(str, this);
        assert prev == null : "vm init: duplicate predef symbol " + str;
    }

    @Override
    Sexp  eval(final Env env) {
        return val;
    }

    Lval  lval(Env env) {
        error("can not change predefined symbol ", this);
        return null;
    }

    final static HashMap<String, PredefSymbol> predefSymbols = new HashMap<>();

    private static final Symbol l = new Symbol("l", 0);

    public static final Sexp
            DEFINE = predefine("define", new BuiltinDefine()),
            CAR = predefine("car", new BuiltinCar()),
            CDR = predefine("cdr", new BuiltinCdr()),
            EVAL= predefine("eval",new BuiltinEval()),
            LIST= predefine("list",Closure.make(l, l, null)),
            EQ  = predefine("eq?", new BuiltinEq()),
            IF  = predefine("if", new BuiltinIf()),
            QUOTE=predefine("quote", new BuiltinQuote()),
            SET = predefine("set!", new BuiltinSet()),
            LAMBDA = predefine("lambda", new BuiltinLambda()),
            CUR_ENV = predefine("cur-env", new BuiltinCurEnv()),
            NEW_ENV = predefine("new-env", new BuiltinNewEnv()),

            SUM = predefine("+", new BuiltinSum()),
            SUB = predefine("-", new BuiltinSub()),
            MUL = predefine("*", new BuiltinMul()),
            DIV = predefine("/", new BuiltinDiv()),

            LESS= predefine("<",  new BuiltinLess()),
            LSEQ= predefine("<=", new BuiltinLessEq()),
            GRTR= predefine(">",  new BuiltinGrtr()),
            GREQ= predefine(">=", new BuiltinGrtrEq()),

            AND = predefine("and", new BuiltinAnd()),
            OR  = predefine("or",  new BuiltinOr()),
            NOT = predefine("not", new BuiltinNot()),

            FORMAT = predefine("format", new BuiltinFormat()),

            NIL   = predefineConst("()"),
            FALSE = predefineConst("#f"),
            TRUE  = predefineConst("#t");


    private final static Sexp predefine(String id, Sexp val) {
        return  new PredefSymbol(id, val).val;
    }

    private final static Sexp predefineConst(String id) {
        return new PredefSymbol(id, null);
    }
}

class TopEnv extends HashMapEnv {

    TopEnv() {
        super(null); // null is for 'no parentEnv'

        defineAll(PredefSymbol.predefSymbols.values());
    }

    void  defineAll(Collection<PredefSymbol> predefSymbols) {
        for (PredefSymbol ps : predefSymbols) {
            boolean res = localDefine(ps, ps.val);
            assert res : "local(pre)Define failed " + ps.sym;
        }
    }
}

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

class SexpLexer {
    private static final String  strPattern =
      /* number */     "(-?[0-9]+[.]?[0-9]*([eE][+-]?[0-9]+)?)(?=[ \t\n;()])"
      /* string */  +  "|(\"([^\n\"\\\\]|\\\\[^\n])*[\n\"])"
      /* symbol */  +  "|(?<![0-9])[a-zA-Z_#!?+*/%<>=-][a-zA-Z0-9_#!?+*/%<>=-]*"
      /* punct. */  +  "|[().'\n]"           // treat \n as separate token, as Lexer needs them for line counting
      /* w.space*/  +  "|([ \t]+|;[^\n]*)+"  // (white-space or comment)+
                    ;

    private static final Pattern pattern = Pattern.compile(strPattern, Pattern.CASE_INSENSITIVE);

    private final CharSequence  source;
    private final Map<String, ? extends Symbol>  predefSymbols;
    private final PrintStream   err;
    private Matcher matcher;
            int     tokenType;
    private int     line;
    private int     tokenBeg, tokenEnd;

    int  line() {
        return line;
    }

    private String  getRawToken() {
        return matcher.group();
    }

    protected Num  getNum() {
        assert tokenType == NUM : (char)tokenType;
        return Num.make(Double.parseDouble(matcher.group()));
    }

    protected Str  getStr() {
        assert tokenType == STR : (char) tokenType;
        String rawToken = getRawToken();
        assert rawToken.startsWith("\"") : rawToken;
        assert rawToken.length() >= 2 : rawToken;
        if (rawToken.endsWith("\n")) {
            error(line, "Unterminated Str literal");
            rawToken = rawToken.substring(0, rawToken.length() - 1) + '\"';
        }
        assert rawToken.endsWith("\"") : rawToken;

        String rawVal = rawToken.substring(1, rawToken.length() - 1);
        String val = rawValToVal(rawVal);
        return Str.make(val, rawVal);
    }

    private String  rawValToVal(String rawVal) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rawVal.length(); ) {
            char c = rawVal.charAt(i++);
            if (c == '\\') {
                // escape sequence
                switch (c = rawVal.charAt(i++)) {
                    case '"':
                    case '\\':
                        break;

                    case 'r':
                        c = '\r';
                        break;
                    case 'n':
                        c = '\n';
                        break;
                    case 't':
                        c = '\t';
                        break;
                    case 'f':
                        c = '\f';
                        break;
                    case 'b':
                        c = '\b';
                        break;

                    case 'x':
                        assert i + 2 <= rawVal.length() : rawVal;
                        c = (char) Integer.parseInt(rawVal.substring(i, i += 2), 16);
                        break;

                    case 'u':
                        assert i + 4 <= rawVal.length() : rawVal;
                        c = (char) Integer.parseInt(rawVal.substring(i, i += 4), 16);
                        break;

                    default:
                        error("Unexpected escape sequence in Str: " + rawVal.substring(i - 2));
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }

    protected Symbol  getSymbol() {
        assert tokenType == SYM : (char)tokenType;
        String id = matcher.group();
        Symbol sym;
        if (predefSymbols != null
         && (sym = predefSymbols.get(id)) != null) {
            return sym;
        }
        return new Symbol(id, line);
    }

    private int  countNewlines(String s) {
        int count = 0;
        int pos = s.indexOf('\n');
        while (0 <= pos) {
            ++count;
            pos = s.indexOf('\n', pos);
        }
        return count;
    }

    private String  location(int srcPos) {
        /*
        int nnl = 0;
        int pos = source.indexOf('\n');
        while (0 <= pos && pos <= srcPos) {
            ++nnl;
            pos = source.indexOf('\n', pos);
        }
        return "line " + (1+nnl) + ", col " + (1+srcPos - nnl) + ": ";
        */
        return "line " + line + ": ";
    }

    private void  error(int srcPos, String msg) {
        if (err != null)
            err.println("## " + location(srcPos) + msg);
    }

    protected void  error(String msg) {
        error(tokenBeg, msg);
    }

    protected SexpLexer(CharSequence source, Map<String, ? extends Symbol> predefSymbols, PrintStream  err) {
        this.source = source;
        this.predefSymbols = predefSymbols;
        this.err = err;
        line = 0;
        matcher = pattern.matcher(source);
    }

    protected SexpLexer(CharSequence cs) {
        this(cs, null, System.err);
    }

    protected static final int
            NUM = '1',
            STR = '"',
            SYM = 'a',
            LPA = '(',
            RPA = ')',
            DOT = '.',
            QUO = '\'',
            EOF = -1;

    /** If current tokenType is tt -- scanNextToken() and return true
     *  otherwise -- return false
     */
    protected final boolean  consume(int tt) {
        if (tokenType == tt) {
            scanNextToken();
            return true;
        }
        return false;
    }

    protected int  scanNextToken() {
        for (;;) {
            if (matcher.find()) {
                if (tokenEnd < matcher.start()) {
                    // report unrecognized token:
                    error("unrecognized token: " + source.subSequence(tokenEnd, matcher.start()));
                }
                tokenBeg = matcher.start();
                tokenEnd = matcher.end();
                int c = matcher.group().charAt(0);
                switch (c) {
                    case '(':
                    case ')':
                    case '.':
                    case '\'':
                        return tokenType = c;

                    case '"':
                        return tokenType = STR;

                    case '0':case '1':case '2':case '3':case '4':
                    case '5':case '6':case '7':case '8':case '9':
                        return tokenType = NUM;

                    case '\n':
                        line++;
                    case ' ':case '\t':case ';':
                        continue;

                    case 'A': case 'B': case 'C': case 'D': case 'E': case 'F':
                    case 'G': case 'H': case 'I': case 'J': case 'K': case 'L':
                    case 'M': case 'N': case 'O': case 'P': case 'Q': case 'R':
                    case 'S': case 'T': case 'U': case 'V': case 'W': case 'X':
                    case 'Y': case 'Z':

                    case 'a': case 'b': case 'c': case 'd': case 'e': case 'f':
                    case 'g': case 'h': case 'i': case 'j': case 'k': case 'l':
                    case 'm': case 'n': case 'o': case 'p': case 'q': case 'r':
                    case 's': case 't': case 'u': case 'v': case 'w': case 'x':
                    case 'y': case 'z':

                    case '_': case '+': case '*': case '/': case '%':
                    case '!': case '?': case '#': case '=': case '<': case '>':

                        return tokenType = SYM;

                    case '-': // it may be a negative number or a symbol
                        if (matcher.group(1) != null)
                            return tokenType = NUM;

                        return tokenType = SYM;

                    default:
                        assert false;
                }
            }
            else
                return tokenType = EOF;
        }
    }

    public void  listAllTokens(java.io.PrintStream ps) {
        do {
            scanNextToken();
            ps.println((char)tokenType + " :> " + (tokenType != EOF ? getRawToken() : "<EOF>"));
        } while (tokenType != EOF);
    }
}

class SexpParser extends SexpLexer {
    final static Sexp NIL = Sexp.NIL;
    final static Symbol SYNTAX_ERR = new Symbol("#SYNTAX_ERROR#", 0);

    public SexpParser(CharSequence source, Map<String, ? extends Symbol> predefSymbols, PrintStream  err) {
        super(source, predefSymbols,  err);
    }

    public SexpParser(CharSequence source, PrintStream err) {
        super(source, PredefSymbol.predefSymbols, System.err);
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

public class Olli {

    static final String version = "1.0.1";

    static void  testParseAndPrint(String source) {
        new SexpLexer(source).listAllTokens(System.err);
        Sexp sexp = new SexpParser(source, System.err).parse();
        System.out.println(sexp);
    }

    private final Env topEnv;
    private String prompt = "<< ";

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
                if (out != null && prompt != null)
                    out.print("<< ");
                Sexp inputExp = parser.parse();
                if (inputExp == null)
                    return resExp;
                resExp = inputExp.eval(topEnv);
                if (out != null)
                    out.println("[" + parser.line() +  "] => " + resExp);
            }
            catch (EvalError ee) {
                if (err != null)
                    err.println("## " + ee.getMessage());
            }
        }
    }

    /** Start an Olli REPL */
    public static void  main(String[] args) {
        CharSequence input = new ReaderCharSequence(new InputStreamReader(System.in));
        System.out.println("Welcome to Olli's Read-Eval-Print-Loop");
        new Olli().repl(input, System.out, System.err);
    }
}
