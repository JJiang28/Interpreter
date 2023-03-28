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
        HashMap<String,Declaration>entries = new HashMap<>();

        void enterScope() {
            currentNum++;
            scope_stack.push(currentNum);
        }
        void closeScope() {
            currentNum = scope_stack.pop();
        }

        public boolean insert(String name, Declaration declaration) {
            return (entries.putIfAbsent(name, declaration) == null);
        }

        public Declaration lookup(String name) {
            //Declaration dec = entries.get(name);
            //if (dec)
            return entries.get(name);
        }
    }

    SymbolTable symbolTable = new SymbolTable();

    private void check(boolean condition, AST node, String message) throws TypeCheckException {
        if (!condition) {throw new TypeCheckException("rawr");}
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

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException {
        NameDef name = declaration.getNameDef();
        Expr initializer = declaration.getInitializer();

        Type type = name.getType();
        if(initializer != null) {
            Type initType = initializer.getType();
        }
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

    Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws PLCException;

    Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCException;

    Object visitDimension(Dimension dimension, Object arg) throws PLCException;

    Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCException;

    Object visitIdent(Ident ident, Object arg) throws PLCException;

    Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCException;

    Object visitLValue(LValue lValue, Object arg) throws PLCException;

    Object visitNameDef(NameDef nameDef, Object arg) throws PLCException;

    Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCException;

    Object visitPixelFuncExpr(PixelFuncExpr pixelFuncExpr, Object arg) throws PLCException;

    Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCException;

    Object visitPredeclaredVarExpr(PredeclaredVarExpr predeclaredVarExpr, Object arg) throws PLCException;

    Object visitRandomExpr(RandomExpr randomExpr, Object arg) throws PLCException;

    Object visitReturnStatement(ReturnStatement returnStatement, Object arg)throws PLCException;

    Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCException;

    Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws PLCException;

    Object visitWriteStatement(WriteStatement statementWrite, Object arg) throws PLCException;

    Object visitZExpr(ZExpr zExpr, Object arg) throws PLCException;

    private boolean assignmentCompatability(Type lhs, Type rhs) {
        return (lhs==rhs || lhs == Type.IMAGE && rhs == Type.PIXEL 
        || lhs == Type.IMAGE && rhs == Type.STRING ||
        lhs == Type.PIXEL && rhs == Type.INT || lhs== Type.INT && rhs == Type.PIXEL
        || lhs == Type.STRING && (rhs == Type.INT || rhs == Type.PIXEL || rhs == Type.IMAGE));
    }
}