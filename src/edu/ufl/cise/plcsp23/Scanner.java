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
        IN_OPERATOR,
        IN_WHITESPACE
    }

    private Token scanToken() throws LexicalException {
        State state = State.START;
        int index = -1;
        while(true) {
            switch(state) {
                case START -> {
                    index = pos;
                    boolean checkDig = isDigit(ch);
                    boolean checkLet = isLetter(ch);
                    boolean isIdent = isIdentStart(ch);
                    boolean isOp = isOper(ch);
                    if(ch == 0) {
                        return new Token(Kind.EOF, pos, 1, inputChars);
                    }
                    if(isIdent == true) {
                        state = State.IN_INDENT;
                        continue;
                    }
                }
                case IN_INDENT -> {
                    int counter = 0;
                    while(isIdentStart(index) || isDigit(index)) {
                        pos++;
                        counter++;
                    }
                    return new Token(Kind.IDENT, pos, counter, inputChars);
                }
                case IN_NUM_LIT -> {}
                case IN_STRING_LIT -> {}
                case IN_RESERVED -> {}
                case IN_OPERATOR -> {}
                case IN_WHITESPACE -> {}
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
