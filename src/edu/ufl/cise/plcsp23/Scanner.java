package edu.ufl.cise.plcsp23;
import java.util.Arrays;

import edu.ufl.cise.plcsp23.IToken.Kind;

public class Scanner implements IScanner {
	final String input;
    final char[] inputChars;
    int pos;
    char ch;

    public Scanner (String input) {
        this.input = input;
        this.inputChars = Arrays.copyOf(input.toCharArray(), input.length()+1);
        pos = 0;
        ch = inputChars[pos];
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

    private Token scanToken() throws LexicalException {
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
                        break;
                    }
                    if(isIdent == true) {
                        state = State.IN_INDENT;
                        continue;
                    }
                    if (ch == '0') {
                        pos++;
                        return new Token(Kind.NUM_LIT, pos-1, 1, inputChars);
                    }
                    if (checkDig == true) {
                        state = State.IN_NUM_LIT;
                        continue;
                    }
                }
                case IN_INDENT -> {
                    int counter = 0;
                    while(isIdentStart(ch) || isDigit(ch)) {
                        pos++;
                        counter++;
                    }
                    return new Token(Kind.IDENT, pos, counter, inputChars);
                }
                case IN_NUM_LIT -> {
                    int counter = 0;
                    while(isDigit(ch)) {
                        pos++;
                        counter++;
                    }
                    return new Token(Kind.NUM_LIT, pos, counter, inputChars);
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
            case ' ', '\b', '\t', '\n', '\r', '\"', '\f' -> {
                return true;}
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
