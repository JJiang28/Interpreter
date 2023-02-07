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
                    return new NumLitToken(originalIndex, counter, inputChars);
                }
                case IN_STRING_LIT -> {}
                case IN_RESERVED -> {}
                case IN_OPERATOR -> {}
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
