package com.sten1ee.olli;

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
public class ParseError extends OlliError {

    ParseError(int srcLine, String msg) {
        super(srcLine, msg);
    }

    @Override
    String errorPrompt() {
        return "## parse error ";
    }
}
