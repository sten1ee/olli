package com.sten1ee.olli;

import java.io.PrintStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private final Map<String, ? extends Symbol> predefSymbols;
    private final PrintStream errorTo;
    private boolean throwOnError = true; // on error - throw ParseError instead of logging err msg and recovering
    private Matcher matcher;
            int     tokenType;
    private int     line = 1;
    private int     tokenBeg, tokenEnd;

    int  line() {
        return line;
    }

    void throwOnError(boolean throwOnError) {
        this.throwOnError = throwOnError;
    }

    private String  getRawToken() {
        return matcher.group();
    }

    Num  getNum() {
        assert tokenType == NUM : (char)tokenType;
        return Num.make(Double.parseDouble(matcher.group()), line);
    }

    Str  getStr() {
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
        return Str.make(val, rawVal, line);
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

    Symbol  getSymbol() {
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

    private void  error(int lineNo, String msg) {
        if (throwOnError)
            throw new ParseError(lineNo, msg);
        if (errorTo != null)
            errorTo.println("## parse error on line " + lineNo + ": " + msg);
    }

    void  error(String msg) {
        error(tokenBeg, msg);
    }

    SexpLexer(CharSequence source, Map<String, ? extends Symbol> predefSymbols, PrintStream err) {
        this.source = source;
        this.predefSymbols = predefSymbols;
        this.errorTo = err;
        matcher = pattern.matcher(source);
    }

    SexpLexer(CharSequence cs) {
        this(cs, null, System.err);
    }

    static final int
            NUM = '1',
            STR = '"',
            SYM = 'a',
            LPA = '(',
            RPA = ')',
            DOT = '.',
            QUO = '\'',
            EOF = -1;

    int  scanNextToken() {
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

    void  listAllTokens(PrintStream ps) {
        do {
            scanNextToken();
            ps.println((char)tokenType + " :> " + (tokenType != EOF ? getRawToken() : "<EOF>"));
        } while (tokenType != EOF);
    }
}
