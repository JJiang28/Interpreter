package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.ast.*;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import edu.ufl.cise.plcsp23.IToken.Kind;

public class TypeChecker implements ASTVisitor{

    public static class SymbolTable {
        int currentNum = 0;
        Stack<Integer> scope_stack = new Stack<>();
        HashMap<String,NameDef>entries = new HashMap<>();

        void enterScope() {
            currentNum++;
            scope_stack.push(currentNum);
        }
        void closeScope() {
            currentNum = scope_stack.pop();
        }

        public boolean insert(String name, NameDef desc) {
            return (entries.putIfAbsent(name, desc ) == null);
        }

        public NameDef lookup(String name) {
            //Declaration dec = entries.get(name);
            //if (dec)
            return entries.get(name);
        }
    }

    SymbolTable symbolTable = new SymbolTable();

    private void check(boolean condition, AST node, String message) throws TypeCheckException {
        if (!condition) {throw new TypeCheckException("rawr");}
    }

    public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws PLCException {

    }

    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCException {
        Type leftType = binaryExpr.getLeft().getType();
        Type rightType = binaryExpr.getRight().getType();
        switch(binaryExpr.getOp()) {
            case BITOR, BITAND -> {
                if (leftType == Type.PIXEL) {
                    if (rightType == Type.PIXEL) {
                        return Type.PIXEL;
                    }
                }
                throw new TypeCheckException("invalid type");
            }
            case AND, OR -> {
                if (leftType == Type.INT) {
                    if (rightType == Type.INT) {
                        return Type.INT;
                    }
                }
                throw new TypeCheckException("invalid type");
            }
            case LT, GT, LE, GE -> {
                if (leftType == Type.INT) {
                    if (rightType == Type.INT) {
                        return Type.INT;
                    }
                }
                throw new TypeCheckException("invalid type");
            }
            case EQ -> {
                switch (leftType) {
                    case INT, PIXEL, IMAGE, STRING -> {
                        if (rightType == leftType)
                            return Type.INT;
                        else
                            throw new TypeCheckException("type mismatch"); 
                    }
                    default -> {
                        throw new TypeCheckException("invalid type");
                    }
                }
            }
            case EXP -> {
                if (rightType == Type.INT) {
                    if (leftType == Type.INT) {
                        return Type.INT;
                    } else if (leftType == Type.PIXEL) {
                        return Type.PIXEL;
                    } else {
                        throw new TypeCheckException("EXP left type");
                    }
                } else {
                    throw new TypeCheckException("EXP right type");
                }
            }
            case PLUS -> {
                switch (leftType) {
                    case INT, PIXEL, IMAGE, STRING -> {
                        if (rightType == leftType)
                            return leftType;
                        else
                            throw new TypeCheckException("type mismatch"); 
                    }
                    default -> {
                        throw new TypeCheckException("invalid type");
                    }
                }
            }
            case MINUS -> {
                switch (leftType) {
                    case INT, PIXEL, IMAGE -> {
                        if (rightType == leftType)
                            return leftType;
                        else
                            throw new TypeCheckException("type mismatch"); 
                    }
                    default -> {
                        throw new TypeCheckException("invalid type");
                    }
                }
            }
            case TIMES, DIV, MOD -> {
                switch (leftType) {
                    case INT, PIXEL, IMAGE -> {
                        if (rightType == leftType) {
                            return leftType;
                        } else if (leftType == Type.PIXEL && rightType == Type.INT) {
                            return Type.PIXEL;
                        } else if (leftType == Type.IMAGE && rightType == Type.INT) {
                            return Type.IMAGE;
                        } else {
                            throw new TypeCheckException("Invalid type combination");
                        }    
                    }
                    default -> {
                        throw new TypeCheckException("invalid type");
                    }
                }
            }
            default -> {
                throw new TypeCheckException("Invalid op");
            }
        }
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLCException {
        List<Declaration> dec = block.getDecList();
        List<Statement> state = block.getStatementList();
        for (Declaration node: dec) {
            node.visit(this, arg);
        }
        for (Statement node: state) {
            node.visit(this, arg);
        } 
        return block;
    }

    Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCException;
    
    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException {
        NameDef name = declaration.getNameDef();
        Expr initializer = declaration.getInitializer();
        name.visit(this, arg);
        if(initializer != null) {
            initializer.visit(this, arg);
        }
        return declaration;
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws PLCException {
        Type width = dimension.getWidth().getType();
        Type height = dimension.getHeight().getType();
        if (width == Type.INT && height == Type.INT) {
            return null;
        }
        throw new TypeCheckException("Dimensions are not integers");
    }

    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCException {
        Type r = expandedPixelExpr.getRedExpr().getType();
        Type g = expandedPixelExpr.getGrnExpr().getType();
        Type b = expandedPixelExpr.getBluExpr().getType();
        if (r == Type.INT && g == Type.INT && b == Type.INT) {
            return null;
        }
        throw new TypeCheckException("invalid pixel selector");
    }

    Object visitIdent(Ident ident, Object arg) throws PLCException;

    Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCException;

    public Object visitLValue(LValue lValue, Object arg) throws PLCException {

    }

    @Override  
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCException {
        Dimension dim = nameDef.getDimension();
        if(dim != null) {
            dim.visit(this, arg);
        }
        String name = nameDef.getIdent().getName();
        boolean inserted = symbolTable.insert(name, nameDef);
        check(inserted, null, "already in table");
        return nameDef.getType();
    }

    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCException {
        numLitExpr.setType(Type.INT);
        return numLitExpr.getType();
    }

    Object visitPixelFuncExpr(PixelFuncExpr pixelFuncExpr, Object arg) throws PLCException;

    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCException {
        Type x = pixelSelector.getX().getType();
        Type y = pixelSelector.getY().getType();
        if (x == Type.INT && y == Type.INT) {
            return null;
        }
        throw new TypeCheckException("invalid pixel selector");
    }

    public Object visitPredeclaredVarExpr(PredeclaredVarExpr predeclaredVarExpr, Object arg) throws PLCException {
        predeclaredVarExpr.setType(Type.INT);
        return predeclaredVarExpr.getType();
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLCException {
        List<NameDef> paramList = program.getParamList();
        for (NameDef param : paramList) {
            param.visit(this, arg);
        }
        // Visit the block
        Block block = program.getBlock();
        block.visit(this, arg);
        return program;
    }

    public Object visitRandomExpr(RandomExpr randomExpr, Object arg) throws PLCException {
        randomExpr.setType(Type.INT);
        return randomExpr.getType();
    }

    Object visitReturnStatement(ReturnStatement returnStatement, Object arg)throws PLCException;

    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCException {
        stringLitExpr.setType(Type.STRING);
        return stringLitExpr.getType();
    }

    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCException {
        switch(unaryExpr.getOp()) {
            case BANG -> {
                if (unaryExpr.getE().getType() == Type.INT) {
                    return Type.INT;
                } else if (unaryExpr.getE().getType() == Type.PIXEL) {
                    return Type.PIXEL;
                }
                check(false, null, "invalid type");
            }
            case MINUS, RES_cos, RES_sin, RES_atan -> {
                if (unaryExpr.getE().getType() == Type.INT) {
                    return Type.INT;
                }
                check(false, null, "invalid type");
            }
            default -> {
                check(false, null, "invalid op");
            }
        };
        return null;
    }

    public Object visitUnaryExprPostFix(UnaryExprPostfix unaryExprPostfix, Object arg) throws PLCException {
        Type primary = unaryExprPostfix.getPrimary().getType();
        boolean hasPix = unaryExprPostfix.getPixel() != null;
        boolean hasChan = unaryExprPostfix.getColor() != null;
        if (primary == Type.PIXEL) {
            if (!hasPix && hasChan) {
                return Type.INT;
            } else {
                throw new TypeCheckException("invalid pixel");
            }
        } else if (primary == Type.IMAGE) {
            if (!hasPix && hasChan) {
                return Type.IMAGE;
            } else if (hasPix && !hasChan) {
                return Type.PIXEL;
            } else if (hasPix && hasChan) {
                return Type.INT;
            } else {
                throw new TypeCheckException("invalid image");
            }
        }
        throw new TypeCheckException("invalid primary type");
    }

    Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws PLCException;

    Object visitWriteStatement(WriteStatement statementWrite, Object arg) throws PLCException;

    public Object visitZExpr(ZExpr zExpr, Object arg) throws PLCException {
        zExpr.setType(Type.INT);
        return zExpr.getType();
    }

    private boolean assignmentCompatability(Type lhs, Type rhs) {
        return (lhs == rhs || lhs == Type.IMAGE && rhs == Type.PIXEL ||
         lhs == Type.IMAGE && rhs == Type.STRING ||
         lhs == Type.PIXEL && rhs == Type.INT || 
         lhs == Type.INT && rhs == Type.PIXEL ||
         lhs == Type.STRING && (rhs == Type.INT || rhs == Type.PIXEL || rhs == Type.IMAGE));
    }
}