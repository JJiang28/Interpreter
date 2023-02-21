package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.ast.AST;
import edu.ufl.cise.plcsp23.ast.BinaryExpr;
import edu.ufl.cise.plcsp23.ast.Expr;
import edu.ufl.cise.plcsp23.ast.IdentExpr;
import edu.ufl.cise.plcsp23.ast.NumLitExpr;
import edu.ufl.cise.plcsp23.ast.RandomExpr;
import edu.ufl.cise.plcsp23.ast.StringLitExpr;
import edu.ufl.cise.plcsp23.ast.UnaryExpr;
import edu.ufl.cise.plcsp23.ast.ZExpr;
import edu.ufl.cise.plcsp23.ast.ConditionalExpr;
import edu.ufl.cise.plcsp23.IToken.Kind;
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
        if (tokenList.size() == 1) throw new SyntaxException("Empty string");
        return expr();
    }

    private Expr expr() throws PLCException{
        if (match(Kind.RES_if)) 
            return conditional_expr();
        return or_expr();
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

    private Expr or_expr() throws PLCException {
        IToken firstToken = previous();
        Expr left = and_expr();
        while (match(Kind.OR, Kind.BITOR)) {
            Kind op = previous().getKind();
            Expr right = and_expr();
            left = new BinaryExpr(firstToken, left, op, right);
        }
        return left;
    }

    private Expr and_expr() throws PLCException{
        IToken firstToken = previous();
        Expr left = comparison_expr();
        while (match(Kind.AND, Kind.BITAND)) {
            Kind op = previous().getKind();
            Expr right = comparison_expr();
            left = new BinaryExpr(firstToken, left, op, right);
        }
        return left;
    }

    private Expr comparison_expr() throws PLCException{
        Expr e = powerExpr();
        while (match(Kind.LT, Kind.GT, Kind.EQ, Kind.LE, Kind.GE)) {
            IToken pExp = previous();
            Expr right = powerExpr();
            e = new BinaryExpr(e.firstToken, e, pExp.getKind(), right);
        }
        return e;
    }
    
    private Expr powerExpr() throws PLCException{
        Expr e = additiveExpr();
        if (match(Kind.EXP)) {
            IToken operator = previous();
            Expr right = powerExpr();
            return new BinaryExpr(e.firstToken, e, operator.getKind(), right);
        }
        return e;
    }

    private Expr additiveExpr() throws PLCException{
        Expr e = multiplicativeExpr();
        while (match(Kind.PLUS, Kind.MINUS)) {
            IToken mExp = previous();
            Expr right = multiplicativeExpr();
            e = new BinaryExpr(e.firstToken, e, mExp.getKind(), right);
        }
        return e;
    }

    private Expr multiplicativeExpr() throws PLCException {
        Expr e = unaryExpr();
        while (match(Kind.TIMES, Kind.DIV, Kind.MOD)) {
            IToken unaryOp = previous();
            Expr right = unaryExpr();
            e = new BinaryExpr(e.firstToken, e, unaryOp.getKind(), right);
        }
        return e;
    }

    private Expr unaryExpr() throws PLCException{
        if(match(Kind.BANG, Kind.MINUS, Kind.RES_sin, Kind.RES_cos, Kind.RES_atan)) {
            IToken unaryOp = previous();
            Expr e = unaryExpr();
            return new UnaryExpr(unaryOp, unaryOp.getKind(), e);
        }
        return primaryExpr();
    }

    private Expr primaryExpr() throws PLCException{
        if(match(Kind.STRING_LIT)) return new StringLitExpr(previous());
        if(match(Kind.NUM_LIT)) return new NumLitExpr(previous());
        if(match(Kind.IDENT)) return new IdentExpr(previous());
        if(match(Kind.RES_Z)) return new ZExpr(previous());
        if(match(Kind.RES_rand)) return new RandomExpr(previous());
        if(match(Kind.LPAREN)) {
            Expr expr1 = expr();
            match(Kind.RPAREN);
            return expr1;
        }
        return null;
    }

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
