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
    
    private Expr powerExpr() {
        Expr e = additiveExpr();
        if (match(Kind.EXP)) {
            IToken operator = previous();
            Expr right = powerExpr();
            return new BinaryExpr(e.firstToken, e, operator.getKind(), right);
        }
        return e;
    }

    private Expr additiveExpr() {
        Expr e = multiplicativeExpr();
        while (match(Kind.PLUS, Kind.MINUS)) {
            IToken mExp = previous();
            Expr right = multiplicativeExpr();
            e = new BinaryExpr(e.firstToken, e, mExp.getKind(), right);
        }
        return e;
    }

    private Expr multiplicativeExpr() {
        Expr e = unaryExpr();
        while (match(Kind.TIMES, Kind.DIV, Kind.MOD)) {
            IToken unaryOp = previous();
            Expr right = unaryExpr();
            e = new BinaryExpr(e.firstToken, e, unaryOp.getKind(), right);
        }
        return e;
    }

    private Expr unaryExpr() {
        if(match(Kind.BANG, Kind.MINUS, Kind.RES_sin, Kind.RES_cos, Kind.RES_atan)) {
            IToken unaryOp = previous();
            Expr e = unaryExpr();
            return new UnaryExpr(unaryOp, unaryOp.getKind(), e);
        }
        return primaryExpr();
    }

    private Expr primaryExpr() {
        if(match(Kind.STRING_LIT)) return new StringLitExpr(previous());
        if(match(Kind.NUM_LIT)) return new NumLitExpr(previous());
        if(match(Kind.IDENT)) return new IdentExpr(previous());
        if(match(Kind.RES_Z)) return new ZExpr(previous());
        if(match(Kind.RES_rand)) return new RandomExpr(previous());
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
