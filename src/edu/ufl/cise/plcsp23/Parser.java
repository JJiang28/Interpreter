package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.ast.AST;
import edu.ufl.cise.plcsp23.IToken.Kind;
import edu.ufl.cise.plcsp23.IToken;
import edu.ufl.cise.plcsp23.Scanner;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;



public class Parser implements IParser {
    private final List<IToken> tokenList;
    private int current;
    public Parser(List<IToken> tokens) {
        this.tokenList = tokens;
        current = 0;
    }

    @Override
    public AST parse() throws PLCException {
        throw new PLCException("hi");
    }

    // private AST primaryExpr() {
    //     if(match(Kind.STRING_LIT)) return new AST.
    // }

    private boolean match(Kind... kinds) {
        for (Kind k: kinds) {
          if (check(k)) {
            advance();
            return true;
          }
        }
        return false;
    }

    private IToken peek() {
        return tokenList.get(current);
    }

    private IToken previous() {
        return tokenList.get(current-1);
    }

    private boolean isAtEnd() {
        return peek().getKind() == Kind.EOF;
    }

    private boolean check (Kind kind) {
        if (isAtEnd()) return false;
        return peek().getKind() == kind;
    }

    private IToken advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

}
