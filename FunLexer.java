import java.io.InputStream;
import java.io.IOException;

/**
 * Lexer for the cacluator language.
 */
public class FunLexer implements FunTokens
{
    private InputStream stream;     // The stream we are lexing
    private char curScan;           // The current character
    private int curTok;             // The current token
    private Object curVal;          // The current value
    private StringBuilder curStr;   // The current lexeme string
    private boolean eof;            // True if we have reached the EOFj
    private int lineno;             // The current line number
    private int colno;              // The current column number
    private int bline;              // The line where the token begins
    private int bcol;               // The col where the token begins

    /**
     * Construct a lexer from an input stream.
     * @param stream The input stream to lex
     */
    public FunLexer(InputStream stream) {
        this.stream = stream;
        eof = false;
        curStr = new StringBuilder();
        lineno=1;
        colno=1;

        //read the first character
        readChar();
    }


    /**
     * Advances the lexer to the next lexeme.
     * @return The scanned token.
     */
    public int next() {
        // skip spaces and comments
        do {
            // skip the space
            skipSpace();

            // skip comments
            if(curScan == '#') {
                skipLine();
            }

        } while(Character.isWhitespace(curScan) || curScan == '#');

        // start a new lexeme string
        curStr.setLength(0);
        curVal = null;
        bline = lineno;
        bcol = colno;

        // handle eof
        if(eof) {
            curTok = EOF;
            return curTok;
        }

        //Try each group of lexing
        if(group1()) {
            return curTok;
        } else if(group2()) {
            return curTok;
        } else if(group3()) {
            return curTok;
        }

        //if we make it here, there was no token matched
        curTok = INVALID;
        consume();

        return curTok;
    }


    /**
     * Attempt to lex a group1 token.
     * @return true on success
     */
    private boolean group1()
    {
        char [] singleChar = { '+', '-', '/', '(', ')', ',', '[', ']', '='};
        int  [] singleCharTok = { PLUS, MINUS, DIVIDE, LPAREN, RPAREN, COMMA, LBRACKET, RBRACKET, EQUAL};

        // try our single character tokens
        for(int i=0; i<singleChar.length; i++) {
            if(curScan == singleChar[i]) {
                consume();
                curTok = singleCharTok[i];
                return true;
            }
        }

        // match a multi-char group1 token
        if(curScan == ':') {
            consume();
            if(curScan == '=') {
                // matched assignment
                consume();
                curTok = ASSIGN;
            } else {
                curTok = INHERITS;
            }
            return true;
        } else if (curScan == '!') {
            consume();
            if (curScan == '=')
            {
                consume();
                curTok = NOTEQUAL;
            }
            else {
                curTok = INVALID;
            }
            return true;
        } else if(curScan == '"') {
            lexString();
            return true;
        } else if(curScan == '\'') {
            lexChar();
            return true;
        } else if(Character.isDigit(curScan)) {
            lexNumber();
            return true;
        }
        return false;
    }


    /**
     * Attempt to lex a group2 token.
     * @return true on success
     */
    private boolean group2()
    {
        String [] match = { "*", "**", "<", "<=", ">", ">=", "true", "false"};
        int [] token = { TIMES, EXPONENT, LT, LTE, GT, GTE, TRUE, FALSE };

        // conintue mathcing curStr + curScan
        // until we no longer have a prefix in match
        boolean done = false;
        while(!done) {
            // form the candidate string
            String candidate = curStr.toString() + curScan;

            // assume we are done, and then verify that
            done = true;
            for(int i=0; i < match.length; i++) {
                if(match[i].startsWith(candidate)) {
                    // matching this token
                    done = false;
                    consume();
                    break;
                }
            }
        }

        //did this match fail?
        if(curStr.length() == 0) {
            return false;
        }

        // find the matching string
        curTok = INVALID;
        for(int i=0; i < match.length; i++) {
            if(match[i].equals(curStr.toString())) {
                curTok = token[i];
                break;
            }
        }
        return true;
    }


    /**
     * Attempt to lex a group3 token.
     * @return true on success
     */
    private boolean group3()
    {
        // check for a match failure
        if(curScan != '_' && !Character.isAlphabetic(curScan)) {
            return true;
        }

        // consume the first one
        consume();

        // consume characters, so long as they match group 3
        while(curScan == '_' ||
              Character.isAlphabetic(curScan) ||
              Character.isDigit(curScan)) {
            consume();
        }

        // if it is not a keyword, it is a reference
        curTok = REF;
        String [] keyword = { "PRINT", "BEGIN", "END", "PROC", "STRING", "CLASS", "PRIVATE", "OVERRIDE", "NUMBER", "CHARACTER", "BOOLEAN", "IF", "ELSE", "WHILE", "READ", "NEW" };
        int [] token = { PRINT, BEGIN, END, PROC, STRING, CLASS, PRIVATE, OVERRIDE, NUMBER, CHARACTER, BOOLEAN, IF, ELSE, WHILE, READ, NEW };
        for(int i=0; i<keyword.length; i++) {
            if(keyword[i].equals(curStr.toString())) {
                curTok = token[i];
                break;
            }
        }

        return true;
    }


    /**
     * Match a string.
     */
    private void lexString()
    {
        StringBuilder s=new StringBuilder();  // the string we are lexing
        boolean terminated=false;

        // consume the leading quote
        consume();

        // read either to EOF or "
        while(!eof && curScan != '"') {
            s.append(consumeEscaped());
        }

        // consume the closing quote, if there is one
        if(curScan == '"') {
            terminated = true;
            consume();
        }

        // validate the string
        if(s.length() == 0 || !terminated) {
            curTok = INVALID;
        } else {
            curTok = STRING;
            curVal = s.toString();
        }
    }

    private void lexChar()
    {
        StringBuilder s=new StringBuilder();  // the string we are lexing
        boolean terminated=false;

        // consume the leading quote
        consume();

        // read either to EOF or "
        while(!eof && curScan != '\'') {
            s.append(consumeEscaped());
        }

        // consume the closing quote, if there is one
        if(curScan == '\'') {
            terminated = true;
            consume();
        }

        // validate the string
        if(s.length() == 0 || !terminated) {
            curTok = INVALID;
        } else {
            curTok = CHARACTER;
            curVal = s.toString();
        }
    }


    private void lexNumber()
    {
        boolean f = false; // true if we have a floating point number

        do {
            if(curScan == '.') {
                f = true;
            }
            consume();
        } while(Character.isDigit(curScan) || curScan == '.');

        // get the number as a string
        String num = curStr.toString();

        //check for an error
        if(!Character.isDigit(num.charAt(num.length()-1))) {
            curTok = INVALID;
        } else {
            if(f) {
                curVal = Double.parseDouble(num);
                curTok = FLOAT;
            } else {
                curVal = Integer.parseInt(num);
                curTok = INTEGER;
            }
        }
    }


    /**
     * Consume a potentially escaped character.
     * @return The translated character
     */
    private char consumeEscaped()
    {
        if(curScan == '\\') {
            //match an escaped character
            consume();

            char c;

            // build the chracter
            switch(curScan) {
            case 'n':
                // newline
                c = '\n';
                break;
            case 't':
                // tab
                c = '\t';
                break;
            default:
                c = curScan;
            }

            //consume that second character
            consume();
            return c;
        } else {
            // non escaped character
            char c = curScan;
            consume();
            return c;
        }
    }


    /**
     * Return the current token.
     * @return The current lexeme token.
     */
    public int token() {
        return curTok;
    }


    /**
     * Return the name of the current token as a string.
     * @return String name of the current token.
     */
    public String tokenLabel() {
        if(curTok == EOF) return "EOF";
        if(curTok == INVALID) return "INVALID";
        if(curTok == ASSIGN) return "ASSIGN";
        if(curTok == PLUS) return "PLUS";
        if(curTok == MINUS) return "MINUS";
        if(curTok == TIMES) return "TIMES";
        if(curTok == DIVIDE) return "DIVIDE";
        if(curTok == EXPONENT) return "EXPONENT";
        if(curTok == LPAREN) return "LPAREN";
        if(curTok == RPAREN) return "RPAREN";
        if(curTok == INTEGER) return "INTEGER";
        if(curTok == FLOAT) return "FLOAT";
        if(curTok == REF) return "REF";
        if(curTok == PRINT) return "PRINT";
        if(curTok == STRING) return "STRING";
        if(curTok == COMMA) return "COMMA";
		if(curTok == LBRACKET) return "LBRACKET";
		if(curTok == RBRACKET) return "RBRACKET";
		if(curTok == EQUAL) return "EQUAL";
		if(curTok == NOTEQUAL) return "NOTEQUAL";
		if(curTok == LT) return "LT";
		if(curTok == LTE)return "LTE";
		if(curTok == GT) return "GT";
		if(curTok == GTE) return "GTE";
		if(curTok == BEGIN) return "BEGIN";
		if(curTok == END) return "END";
		if(curTok == PROC) return "PROC";
		if(curTok == NUMBER) return "NUMBER";
		if(curTok == CHARACTER) return "CHARACTER";
		if(curTok == BOOLEAN) return "BOOLEAN";
		if(curTok == IF) return "IF";
		if(curTok == ELSE) return "ELSE";
		if(curTok == WHILE)return "WHILE";
		if(curTok == READ) return "READ";
		if(curTok == CLASS) return "CLASS";
		if(curTok == OVERRIDE) return "OVERRIDE";
		if(curTok == PRIVATE) return "PRIVATE";
		if(curTok == NEW) return "NEW";
		if(curTok == INHERITS) return "INHERITS";
		if(curTok == TRUE) return "TRUE";
		if(curTok == FALSE) return "FALSE";
		return "ERROR: UNKNOWN TOKEN!!!";
    }


    /**
     * Return the current value.
     * @return The current lexeme value.
     */
    public Object value() {
        return curVal;
    }


    /**
     * Return the current lexeme string.
     * @return The current lexeme string
     */
    public String string() {
        return curStr.toString();
    }


    /**
     * Return the current line number.
     * @return The current line number.
     */
    public int lineNumber() {
        return bline;
    }


    /**
     * Return the current column number.
     * @return The current column number.
     */
    public int columnNumber() {
        return bcol;
    }


    /**
     * Read a single character from the stream, handling EOF and exceptions.
     * This updates curScan, it returns nothing
     */
    private void readChar()
    {
        // read the first character
        try {
            int c = stream.read();
            if(c != -1) {
                curScan = (char) c;
            } else {
                curScan = 0;
                eof = true;
            }
        } catch(IOException ex) {
            eof = true;
            curScan = 0;
        }

        // update our counters
        if(!eof) {
            colno++;
            if(curScan == '\n') {
                colno = 0;
                lineno++;
            }
        }
    }


    /**
     * Consume the current character and load the next. This updates
     * the lexeme string and the eof field as well.
     */
    private void consume() {
        int c;    // The character we are going to read

        //never try to read an ended stream
        if(eof) {
            return;
        }

        // append to the current lexeme
        if(curScan != 0) {
            curStr.append(curScan);
        }

        // read the next character
        readChar();

        // place the null character on eof
        if(eof) {
            curScan = 0;
        }
    }


    /**
     * Skip whitespace characters.
     */
    private void skipSpace()
    {
        // read until we hit a non-space
        while(Character.isWhitespace(curScan)) {
            readChar();
        }
    }


    /**
     * Skip the rest of the line.
     */
    private void skipLine()
    {
        // skip to the newline
        while(curScan != '\n') {
            readChar();
        }

        //skip the newline
        readChar();
    }


    /**
     * Test the lexer by making it run on System.in.
     */
    public static void main(String [] args) {
        FunLexer lexer = new FunLexer(System.in);

        while(lexer.next() != EOF) {
            System.out.printf("Line %d, Column %d\t %s: %s %s\n", lexer.lineNumber(), lexer.columnNumber(), lexer.tokenLabel(), lexer.string(), lexer.value());
        }

        System.out.printf("Line %d, Column %d\t %s: %s %s\n", lexer.lineNumber(), lexer.columnNumber(), lexer.tokenLabel(), lexer.string(), lexer.value());
    }
}
