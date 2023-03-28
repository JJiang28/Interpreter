package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.ast.*;
import edu.ufl.cise.plcsp23.IToken.Kind;

import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes.Name;

import javax.swing.plaf.basic.BasicTreeUI.NodeDimensionsHandler;



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
        return program();
    }

    public Program program() throws PLCException {
        Type type = type();
        if (type == null) throw new SyntaxException("program issue");
        IToken firstToken = previous();
        if (match(Kind.IDENT)) {
            Ident ident = new Ident(previous());
            if (match(Kind.LPAREN)) {
                List<NameDef> params = paramList();
                if (match(Kind.RPAREN)) {
                    Block block = block();
                    if (peek().getKind() != Kind.EOF) {
                        throw new SyntaxException("something after");
                    }
                    return new Program(firstToken, type, ident, params, block);
                }
            }
        }
        throw new SyntaxException("program");
    }

    private Block block() throws PLCException {
        if (match(Kind.LCURLY)) {
            IToken firstToken = previous();
            List<Declaration> decs = decList();
            List<Statement> states = statementList();
            if (match(Kind.RCURLY)) {
                return new Block(firstToken, decs, states);
            }
            throw new SyntaxException("missing rcurly");
        }
        throw new SyntaxException("missing lcurly");
    }

    private List<Declaration> decList() throws PLCException {
        List<Declaration> decs = new ArrayList<>();
        Declaration dec = declaration();
        while (dec != null) {
            if (match(Kind.DOT))
                decs.add(dec);
            else
                throw new SyntaxException("dec list :(");
            dec = declaration();
        }
        return decs;
    }

    private List<Statement> statementList() throws PLCException {
        List<Statement> statements = new ArrayList<>();
        Statement st = statement();
        while(st != null) {
            if (match(Kind.DOT))
                statements.add(st);
            else
                throw new SyntaxException("statement list gone wrong");
            st = statement();
        }
        return statements;
    } 

    private List<NameDef> paramList() throws PLCException {
        List<NameDef> params = new ArrayList<>();
        NameDef currDef = nameDef();
        if (currDef == null) return params;
        params.add(currDef);
        while (match(Kind.COMMA)) {
            currDef = nameDef();
            params.add(currDef);
        }
        return params;
    }

    private NameDef nameDef() throws PLCException {
        IToken firstToken = peek();
        Type type = type();
        if (type == null) return null;
        int temp = current;
        Dimension dim = dimension();
        if (dim == null) current = temp;
        if (match(Kind.IDENT)) {
            Ident ident = new Ident(previous());
            return new NameDef(firstToken, type, dim, ident);
        }
        else {
            Ident ident = new Ident(previous());
            return new NameDef(firstToken, type, null, ident);
        }
    }

    private Type type() throws PLCException {
        if(match(Kind.RES_image, Kind.RES_pixel, Kind.RES_int, Kind.RES_string, Kind.RES_void)) {
            return Type.getType(previous());
        }
        return null;
    }

    private Declaration declaration() throws PLCException {
        IToken firstToken = peek();
        NameDef nd = nameDef();
        if (nd == null) return null;
        if (match(Kind.ASSIGN)) {
            Expr e = expr();
            return new Declaration(firstToken, nd, e);
        } else {
            return new Declaration(firstToken, nd, null);
        }
    }

    private Expr expr() throws PLCException {
        if (match(Kind.RES_if)) 
            return conditional_expr();
        return or_expr();
    }

    private Expr conditional_expr() throws PLCException {
        IToken firstToken = previous();
        Expr guard = expr();
        if (!match(Kind.QUESTION)) {
            throw new SyntaxException("Invalid conditional expr");
        }
        Expr trueCase = expr();
        if (!match(Kind.QUESTION)) {
            throw new SyntaxException("Invalid conditional expr");
        }
        Expr falseCase = expr();
        return new ConditionalExpr(firstToken, guard, trueCase, falseCase);
    }

    private Expr or_expr() throws PLCException{
        IToken firstToken = peek();
        Expr left = and_expr();
        while (match(Kind.OR, Kind.BITOR)) {
            Kind op = previous().getKind();
            Expr right = and_expr();
            left = new BinaryExpr(firstToken, left, op, right);
        }
        return left;
    }

    private Expr and_expr() throws PLCException{
        IToken firstToken = peek();
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
            return new BinaryExpr(operator, e, operator.getKind(), right);
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
        return unaryExprPostfix();
    }

    private Expr unaryExprPostfix() throws PLCException {
        IToken firstToken = peek();
        Expr expr = primaryExpr();
        int temp = current;
        PixelSelector ps = pixelSelector();
        if (ps == null) current = temp;
        temp = current;
        ColorChannel cc = channelSelector();
        if (cc == null) current = temp;
        if (ps == null && cc == null) return expr;
        return new UnaryExprPostfix(firstToken, expr, ps, cc);
    }

    private Expr primaryExpr() throws PLCException{
        if(match(Kind.STRING_LIT)) return new StringLitExpr(previous());
        if(match(Kind.NUM_LIT)) return new NumLitExpr(previous());
        if(match(Kind.IDENT)) { 
            return new IdentExpr(previous());
        }
        if(match(Kind.LPAREN)) {
            Expr expr1 = expr();
            if (!match(Kind.RPAREN)) {
                throw new SyntaxException("Parentheses");
            };
            return expr1;
        }
        if(match(Kind.RES_Z)) return new ZExpr(previous());
        if(match(Kind.RES_rand)) return new RandomExpr(previous());
        if(match(Kind.RES_x)) return new PredeclaredVarExpr(previous());
        if(match(Kind.RES_y)) return new PredeclaredVarExpr(previous());
        if(match(Kind.RES_a)) return new PredeclaredVarExpr(previous());
        if(match(Kind.RES_r)) return new PredeclaredVarExpr(previous());
        int temp = current;
        Expr expanded = expandedPixel();
        if (expanded == null) current = temp;
        else return expanded;
        temp = current;
        Expr pixFunc = pixelFunctionExpr();
        if (pixFunc == null) current = temp;
        else return pixFunc;
        throw new SyntaxException("not valid");
    }

    private ColorChannel channelSelector() throws PLCException {
        if(match(Kind.COLON)) {
            if(match(Kind.RES_red, Kind.RES_grn, Kind.RES_blu)) {
                return ColorChannel.getColor(previous());
            }
        }
        return null;
    }

    private PixelSelector pixelSelector() throws PLCException {
        if (match(Kind.LSQUARE)) {
            IToken firstToken = previous();
            Expr expr1 = expr();
            if (match(Kind.COMMA)) {
                Expr expr2 = expr();
                if (match(Kind.RSQUARE)) {
                    return new PixelSelector(firstToken, expr1, expr2);
                }
            }
        }
        return null;
    }

    private Expr expandedPixel() throws PLCException {
        if (match(Kind.LSQUARE)) {
            IToken firstToken = previous();
            Expr expr1 = expr();
            if (match(Kind.COMMA)) {
                Expr expr2 = expr();
                if (match(Kind.COMMA)) {
                    Expr expr3 = expr();
                    if (match(Kind.RSQUARE)) {
                        return new ExpandedPixelExpr(firstToken, expr1, expr2, expr3);
                    }
                }
            }
        }
        return null;
    }

    private Expr pixelFunctionExpr() throws PLCException {
        if (match(Kind.RES_x_cart, Kind.RES_y_cart, Kind.RES_a_polar, Kind.RES_r_polar)) {
            IToken func = previous();
            PixelSelector ps = pixelSelector();
            return new PixelFuncExpr(func, func.getKind(), ps);
        }
        throw new SyntaxException("pixel function expr did not work");
    }

    private Dimension dimension() throws PLCException {
        if (match(Kind.LSQUARE)) {
            IToken firstToken = previous();
            Expr expr1 = expr();
            if (match(Kind.COMMA)) {
                Expr expr2 = expr();
                if (match(Kind.RSQUARE)) {
                    return new Dimension(firstToken, expr1, expr2);
                }
            }
        }
        return null;
    }

    private LValue LValue() throws PLCException {
        if (match(Kind.IDENT)) {
            IToken firstToken = previous();
            Ident ident = new Ident(firstToken);
            int temp = current;
            PixelSelector ps = pixelSelector();
            if (ps == null) current = temp;
            temp = current;
            ColorChannel cc = channelSelector();
            if (cc == null) current = temp;
            return new LValue(firstToken, ident, ps, cc);
        }
        return null;
    }

    private Statement statement() throws PLCException {
        if (match(Kind.RES_write)) {
            IToken firstToken = previous();
            Expr e = expr();
            return new WriteStatement(firstToken, e);
        } else if (match(Kind.RES_while)) {
            IToken firstToken = previous();
            Expr e = expr();
            Block block = block();
            return new WhileStatement(firstToken, e, block);
        } else if (match(Kind.COLON)) {
            IToken firstToken = previous();
            Expr e = expr();
            return new ReturnStatement(firstToken, e);
        } else {
            IToken firstToken = peek();
            LValue lv = LValue();
            if (lv == null) return null;
            if (match(Kind.ASSIGN)) {
                Expr e = expr();
                return new AssignmentStatement(firstToken, lv, e);
            }
            else
                return null;
        }
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
