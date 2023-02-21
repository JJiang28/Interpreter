package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.ast.AST;
import edu.ufl.cise.plcsp23.ast.BinaryExpr;
import edu.ufl.cise.plcsp23.ast.ConditionalExpr;
import edu.ufl.cise.plcsp23.ast.Expr;
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
        if (tokenList.size() == 0) throw new SyntaxException("Empty string");
        return expr();
    }

    private Expr expr() throws PLCException{
        if (match(Kind.QUESTION)) {
            return conditional_expr();
        } else {
            return conditional_expr();
        }
    }

    private ConditionalExpr conditional_expr() throws PLCException {
        IToken firstToken = previous();
        Expr guard = expr();
        if (!match(Kind.QUESTION)) {
            throw new PLCException("Invalid conditional expr");
        }
        Expr trueCase = expr();
        if (!match(Kind.QUESTION)) {
            throw new PLCException("Invalid conditional expr");
        }
        Expr falseCase = expr();
        return new ConditionalExpr(firstToken, guard, trueCase, falseCase);
    }

    private BinaryExpr or_expr() {
        IToken firstToken = previous();
        BinaryExpr left = and_expr();
        while (match(Kind.OR, Kind.BITOR)) {
            Kind op = previous().getKind();
            BinaryExpr right = and_expr();
            left = new BinaryExpr(firstToken, left, op, right);
        }
        return left;
    }

    private BinaryExpr and_expr() {
        IToken firstToken = previous();
        BinaryExpr left = comparison_expr();
        while (match(Kind.AND, Kind.BITAND)) {
            Kind op = previous().getKind();
            BinaryExpr right = comparison_expr();
            left = new BinaryExpr(firstToken, left, op, right);
        }
        return left;
    }

    private BinaryExpr comparison_expr() {
        return null;
    }

    // private AST primaryExpr() {
    //     if(match(Kind.STRING_LIT)) return new AST.
    // }*/

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
