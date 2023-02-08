package edu.ufl.cise.plcsp23;
import java.util.Arrays;
import java.util.HashMap;

import edu.ufl.cise.plcsp23.IToken.Kind;

public class Scanner implements IScanner {
	final String input;
    final char[] inputChars;
    int pos;
    char ch;
    HashMap<String, Kind> reservedWords = new HashMap<String, Kind>();
    HashMap<String, Kind> ops = new HashMap<String, Kind>();

    public Scanner (String input) {
        this.input = input;
        this.inputChars = Arrays.copyOf(input.toCharArray(), input.length()+1);
        pos = 0;
        ch = inputChars[pos];
        reservedWords.put("image", Kind.RES_image);
        reservedWords.put("pixel", Kind.RES_pixel);
        reservedWords.put("int", Kind.RES_int);
        reservedWords.put("string", Kind.RES_string);
        reservedWords.put("void", Kind.RES_void);
        reservedWords.put("nil", Kind.RES_nil);
        reservedWords.put("load", Kind.RES_load);
        reservedWords.put("display", Kind.RES_display);
        reservedWords.put("write", Kind.RES_write);
        reservedWords.put("x", Kind.RES_x);
        reservedWords.put("y", Kind.RES_y);
        reservedWords.put("a", Kind.RES_a);
        reservedWords.put("r", Kind.RES_r);
        reservedWords.put("X", Kind.RES_X);
        reservedWords.put("Y", Kind.RES_Y);
        reservedWords.put("Z", Kind.RES_Z);
        reservedWords.put("x_cart", Kind.RES_x_cart);
        reservedWords.put("y_cart", Kind.RES_y_cart);
        reservedWords.put("a_polar", Kind.RES_a_polar);
        reservedWords.put("r_polar", Kind.RES_r_polar);
        reservedWords.put("rand", Kind.RES_rand);
        reservedWords.put("sin", Kind.RES_sin);
        reservedWords.put("cos", Kind.RES_cos);
        reservedWords.put("atan", Kind.RES_atan);
        reservedWords.put("if", Kind.RES_if);
        reservedWords.put("while", Kind.RES_while);
        ops.put(".", Kind.DOT);
        ops.put(",", Kind.COMMA);
        ops.put("?", Kind.QUESTION);
        ops.put(":", Kind.COLON);
        ops.put("(", Kind.LPAREN);
        ops.put(")", Kind.RPAREN);
        ops.put("<", Kind.LT);
        ops.put(">", Kind.GT);
        ops.put("[", Kind.LSQUARE);
        ops.put("]", Kind.RSQUARE);
        ops.put("{", Kind.LCURLY);
        ops.put("}", Kind.RCURLY);
        ops.put("=", Kind.ASSIGN);
        ops.put("==", Kind.EQ);
        ops.put("<->", Kind.EXCHANGE);
        ops.put("<=", Kind.LE);
        ops.put(">=", Kind.GE);
        ops.put("!", Kind.BANG);
        ops.put("&", Kind.BITAND);
        ops.put("&&", Kind.AND);
        ops.put("|", Kind.BITOR);
        ops.put("||", Kind.OR);
        ops.put("+", Kind.PLUS);
        ops.put("-", Kind.MINUS);
        ops.put("*", Kind.TIMES);
        ops.put("**", Kind.EXP);
        ops.put("/", Kind.DIV);
        ops.put("%", Kind.MOD);
    }

    @Override
    public IToken next() throws LexicalException {
        return scanToken();
    }

    private enum State {
        START,
        IN_INDENT,
        IN_NUM_LIT,
        IN_STRING_LIT,
        IN_RESERVED,
        IN_OPERATOR
    }

    private IToken scanToken() throws LexicalException {
        State state = State.START;
        int index = -1;
        while(true) {
            switch(state) {
                case START -> {
                    index = pos;
                    if(ch == 0) {
                        return new Token(Kind.EOF, index, 1, inputChars);
                    }
                    boolean checkDig = isDigit(ch);
                    boolean checkLet = isLetter(ch);
                    boolean isIdent = isIdentStart(ch);
                    boolean isOp = isOper(ch);
                    boolean isWhite = isWhiteSpace(ch);
                    if(isWhite)
                    {
                        pos++;
                        ch = inputChars[pos];
                        state = State.START;
                        break;
                    }
                    if(isIdent == true) {
                        state = State.IN_INDENT;
                        continue;
                    }
                    if (ch == '0') {
                        pos++;
                        ch = inputChars[pos];
                        return new NumLitToken(pos-1, 1, inputChars);
                    }
                    if (checkDig == true) {
                        state = State.IN_NUM_LIT;
                        continue;
                    }
                    if (ch == '"') {
                        state = State.IN_STRING_LIT;
                        continue;
                    }
                    if (isOp) {
                        state = State.IN_OPERATOR;
                        continue;
                    }
                    return new Token(Kind.EOF, pos, 1, inputChars);
                }
                case IN_INDENT -> {
                    int counter = 0;
                    while(isIdentStart(ch) || isDigit(ch)) {
                        pos++;
                        ch = inputChars[pos];
                        counter++;
                    }
                    return new Token(Kind.IDENT, pos, counter, inputChars);
                }
                case IN_NUM_LIT -> {
                    int counter = 0;
                    int originalIndex = pos;
                    while(ch != 0 && isDigit(ch) && (inputChars[pos] != 0)) {
                        pos++;
                        ch = inputChars[pos];
                        counter++;
                    }
                    NumLitToken token = new NumLitToken(originalIndex, counter, inputChars);
                    try {
                        Integer.parseInt(token.getTokenString());
                    } catch (Exception e) {
                        throw new LexicalException("uhh");
                    }
                    return token;
                }
                case IN_STRING_LIT -> {
                    int counter = 0;
                    int originalIndex = pos;
                    while(ch != '"') {

                    }
                }
                case IN_RESERVED -> {}
                case IN_OPERATOR -> {
                    if (ch == '=' && inputChars[pos+1] == '=') {
                        pos +=2;
                        ch = inputChars[pos];
                        return new Token(Kind.EQ, pos-2, 2, inputChars);
                    }
                    if (ch == '|' && inputChars[pos+1] == '|') {
                        pos +=2;
                        ch = inputChars[pos];
                        return new Token(Kind.OR, pos-2, 2, inputChars);
                    }
                    if (ch == '&' && inputChars[pos+1] == '&') {
                        pos +=2;
                        ch = inputChars[pos];
                        return new Token(Kind.AND, pos-2, 2, inputChars);
                    }
                    if (ch == '*' && inputChars[pos+1] == '*') {
                        pos +=2;
                        ch = inputChars[pos];
                        return new Token(Kind.EXP, pos-2, 2, inputChars);
                    }
                    if (ch == '>' && inputChars[pos+1] == '=') {
                        pos +=2;
                        ch = inputChars[pos];
                        return new Token(Kind.GE, pos-2, 2, inputChars);
                    }
                    if (ch == '<') {
                        if (inputChars[pos+1] == '=') {
                            pos +=2;
                            ch = inputChars[pos];
                            return new Token(Kind.LE, pos-2, 2, inputChars);
                        }
                        if (inputChars[pos+1] == '-' && inputChars[pos+2] == '>') {
                            pos +=3;
                            ch = inputChars[pos];
                            return new Token(Kind.EXCHANGE, pos-3, 3, inputChars);
                        }
                    }
                    if (ch == '.') {
                        pos +=1;
                        ch = inputChars[pos];
                        return new Token(Kind.DOT, pos-1, 1, inputChars);
                    }if (ch == ',') {
                        pos +=1;
                        ch = inputChars[pos];
                        return new Token(Kind.COMMA, pos-1, 1, inputChars);
                    }if (ch == '?') {
                        pos +=1;
                        ch = inputChars[pos];
                        return new Token(Kind.QUESTION, pos-1, 1, inputChars);
                    }if (ch == ':') {
                        pos +=1;
                        ch = inputChars[pos];
                        return new Token(Kind.COLON, pos-1, 1, inputChars);
                    }if (ch == '(') {
                        pos +=1;
                        ch = inputChars[pos];
                        return new Token(Kind.LPAREN, pos-1, 1, inputChars);
                    }if (ch == ')') {
                        pos +=1;
                        ch = inputChars[pos];
                        return new Token(Kind.RPAREN, pos-1, 1, inputChars);
                    }if (ch == '<') {
                        pos +=1;
                        ch = inputChars[pos];
                        return new Token(Kind.LT, pos-1, 1, inputChars);
                    }if (ch == '>') {
                        pos +=1;
                        ch = inputChars[pos];
                        return new Token(Kind.GT, pos-1, 1, inputChars);
                    }if (ch == '[') {
                        pos +=1;
                        ch = inputChars[pos];
                        return new Token(Kind.LSQUARE, pos-1, 1, inputChars);
                    }if (ch == ']') {
                        pos +=1;
                        ch = inputChars[pos];
                        return new Token(Kind.RSQUARE, pos-1, 1, inputChars);
                    }if (ch == '{') {
                        pos +=1;
                        ch = inputChars[pos];
                        return new Token(Kind.LCURLY, pos-1, 1, inputChars);
                    }if (ch == '}') {
                        pos +=1;
                        ch = inputChars[pos];
                        return new Token(Kind.RCURLY, pos-1, 1, inputChars);
                    }if (ch == '=') {
                        pos +=1;
                        ch = inputChars[pos];
                        return new Token(Kind.ASSIGN, pos-1, 1, inputChars);
                    }if (ch == '!') {
                        pos +=1;
                        ch = inputChars[pos];
                        return new Token(Kind.BANG, pos-1, 1, inputChars);
                    }if (ch == '&') {
                        pos +=1;
                        ch = inputChars[pos];
                        return new Token(Kind.BITAND, pos-1, 1, inputChars);
                    }if (ch == '|') {
                        pos +=1;
                        ch = inputChars[pos];
                        return new Token(Kind.BITOR, pos-1, 1, inputChars);
                    }if (ch == '+') {
                        pos +=1;
                        ch = inputChars[pos];
                        return new Token(Kind.PLUS, pos-1, 1, inputChars);
                    }if (ch == '-') {
                        pos +=1;
                        ch = inputChars[pos];
                        return new Token(Kind.MINUS, pos-1, 1, inputChars);
                    }if (ch == '*') {
                        pos +=1;
                        ch = inputChars[pos];
                        return new Token(Kind.TIMES, pos-1, 1, inputChars);
                    }if (ch == '/') {
                        pos +=1;
                        ch = inputChars[pos];
                        return new Token(Kind.DIV, pos-1, 1, inputChars);
                    }if (ch == '%') {
                        pos +=1;
                        ch = inputChars[pos];
                        return new Token(Kind.MOD, pos-1, 1, inputChars);
                    }
                }
                default -> { throw new UnsupportedOperationException("Bug");}
            }
           
        }

    }

    private boolean isDigit(int c) {
        return '0' <= c && c <= '9';
     }
     private boolean isLetter(int c) {
        return ('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z');
     }
     private boolean isIdentStart(int c) {
        return isLetter(c) || (c == '$') || (c == '_');
     }
     private boolean isWhiteSpace(char ch) {
        switch(ch) {
            case ' ', '\b', '\t', '\n', '\r', '\"', '\f' -> {return true;}
            default -> {return false;}
        }
     }

     private void error(String message) throws LexicalException{
        throw new LexicalException("Error at pos " + pos + ": " + message); 
     }
     private boolean isOper(char c) {
        switch(c) {
            case '.', ',', '?', ':', '(', ')', '<', '>', '[', ']', '{', '}', '=', '!', '&', '|', '+', '-', '*', '/', '%' -> {return true;}
            default -> {return false;}
        }
     }
}
