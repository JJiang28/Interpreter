package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.ast.*;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import edu.ufl.cise.plcsp23.IToken.Kind;

import javax.naming.Name;

public class TypeChecker implements ASTVisitor{

    public static class SymbolTable {
        public HashMap<String, NameDef> globalSymbols;
        public Stack<HashMap<String, NameDef>> scope_stack;
        private HashMap<Ident, String> uniqueNames;

        void enterScope() {
            scope_stack.push(new HashMap<>());
        }
        void closeScope() {
            scope_stack.pop();
        }

        public SymbolTable() {
            globalSymbols = new HashMap<>();
            scope_stack = new Stack<>();
            uniqueNames = new HashMap<>();
            enterScope();
        }

        public boolean insert(String name, NameDef desc) {
            HashMap<String, NameDef> symbolTable = scope_stack.peek();
            boolean something = (scope_stack.peek().putIfAbsent(name, desc) == null);
            if(something == true) {
                NameDef def = symbolTable.get(name);
                def.getIdent().addInstance();
            }
            return something;
        }

        public int findScope(String name) {
            for (int i = scope_stack.size() - 1; i >= 0; i--) {
                HashMap<String, NameDef> symbolTable = scope_stack.get(i);
                if(symbolTable.containsKey(name)) {
                    return i+1;
                }
            }
            return -1; //add scope level to variable name
        }

        public NameDef lookup(String name) {
            for (int i = scope_stack.size() - 1; i >= 0; i--) {
                HashMap<String, NameDef> symbolTable = scope_stack.get(i);
                if(symbolTable.containsKey(name)) {
                    //System.out.println(name + " found!");
                    return symbolTable.get(name);
                }
            }
            return globalSymbols.get(name); //add scope level to variable name
        }
        public String getUniqueString(Ident ident) {
            String uniqueName = uniqueNames.get(ident);
            if (uniqueName == null) {
                uniqueName = ident.getName() + scope_stack.size();
                uniqueNames.put(ident, uniqueName);
            }
            return uniqueName;
        }
    }

    Type progType;
    SymbolTable symbolTable = new SymbolTable();

    private void check(boolean condition, AST node, String message) throws TypeCheckException {
        if (!condition) {throw new TypeCheckException("rawr");}
    }

    public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws PLCException {
        Type type = (Type)statementAssign.getLv().visit(this, arg);
        Expr expr = statementAssign.getE();
        Type returnType = (Type)expr.visit(this, arg);
        boolean xd = assignmentCompatability(type, returnType);
        check(xd, statementAssign, "type of exp and dec type don't match");
        return returnType;
    }

    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCException {
        Type leftType = (Type)binaryExpr.getLeft().visit(this, arg);
        Type rightType = (Type)binaryExpr.getRight().visit(this,arg);
        binaryExpr.getRight().visit(this, arg);
        switch(binaryExpr.getOp()) {
            case BITOR, BITAND -> {
                if (leftType == Type.PIXEL) {
                    if (rightType == Type.PIXEL) {
                        binaryExpr.setType(Type.PIXEL);
                        return binaryExpr.getType();
                    }
                }
                throw new TypeCheckException("invalid type");
            }
            case AND, OR -> {
                if (leftType == Type.INT) {
                    if (rightType == Type.INT) {
                        binaryExpr.setType(Type.INT);
                        return binaryExpr.getType();
                    }
                }
                throw new TypeCheckException("invalid type");
            }
            case LT, GT, LE, GE -> {
                if (leftType == Type.INT) {
                    if (rightType == Type.INT) {
                        binaryExpr.setType(Type.INT);
                        return binaryExpr.getType();
                    }
                }
                throw new TypeCheckException("invalid type");
            }
            case EQ -> {
                switch (leftType) {
                    case INT, PIXEL, IMAGE, STRING -> {
                        if (rightType == leftType) {
                            binaryExpr.setType(Type.INT);
                            return binaryExpr.getType();
                        }
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
                        binaryExpr.setType(Type.INT);
                        return binaryExpr.getType();
                    } else if (leftType == Type.PIXEL) {
                        binaryExpr.setType(Type.PIXEL);
                        return binaryExpr.getType();
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
                        if (rightType == leftType){
                            binaryExpr.setType(leftType);
                            return leftType;
                        }
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
                        if (rightType == leftType) {
                            binaryExpr.setType(leftType);
                            return leftType;
                        }
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
                            binaryExpr.setType(rightType);
                            return binaryExpr.getType();
                        } else if (leftType == Type.PIXEL && rightType == Type.INT) {
                            binaryExpr.setType(leftType);
                            return binaryExpr.getType();
                        } else if (leftType == Type.IMAGE && rightType == Type.INT) {
                            binaryExpr.setType(leftType);
                            return binaryExpr.getType();
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
        for (Declaration node: dec) {
            node.visit(this, arg);
        }
        List<Statement> state = block.getStatementList();
        for (Statement node: state) {
            node.visit(this, arg);
        }
        return block;
    }

    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCException {
        Type zero = (Type)conditionalExpr.getGuard().visit(this, arg);
        Type one = (Type)conditionalExpr.getTrueCase().visit(this, arg);
        Type two = (Type)conditionalExpr.getFalseCase().visit(this,arg);
        if(zero != Type.INT) {
            check(false, conditionalExpr, "Not an int");
        }
        if(one != two) {
            check(false, conditionalExpr, "the true and false cases are not the same");
        }
        conditionalExpr.setType(one);
        return conditionalExpr.getType();
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException {
        NameDef name = declaration.getNameDef();
        Expr initializer = declaration.getInitializer();
        if(initializer != null) {
            Type initializerType = (Type)initializer.visit(this, arg);
            boolean typeCompat = assignmentCompatability(name.getType(), initializerType);
            check(typeCompat, declaration, "type of exp and type do not match");
        }
        name.visit(this, arg);
        if(name.getType() == Type.IMAGE) {
            if(initializer == null && name.getDimension() == null) {
                throw new TypeCheckException("not working");
            }
        }
        return declaration;
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws PLCException {
        Type width = (Type)dimension.getWidth().visit(this, arg);
        Type height = (Type)dimension.getHeight().visit(this, arg);
        if (width == Type.INT && height == Type.INT) {
            return dimension;
        }
        throw new TypeCheckException("Dimensions are not integers");
    }

    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCException {
        Type r = (Type)expandedPixelExpr.getRedExpr().visit(this, arg);
        Type g = (Type)expandedPixelExpr.getGrnExpr().visit(this, arg);
        Type b = (Type)expandedPixelExpr.getBluExpr().visit(this, arg);
        if (r == Type.INT && g == Type.INT && b == Type.INT) {
            expandedPixelExpr.setType(Type.PIXEL);
            return expandedPixelExpr.getType();
        }
        throw new TypeCheckException("invalid pixel selector");
    }

    public Object visitIdent(Ident ident, Object arg) throws PLCException {
        return ident;
    }

    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCException {
        if(symbolTable.lookup(identExpr.getName()) == null) {
            throw new TypeCheckException("ident needs to be defined");
        }
        identExpr.setType(symbolTable.lookup(identExpr.getName()).getType());
        return identExpr.getType();
    }

    public Object visitLValue(LValue lValue, Object arg) throws PLCException {
        Ident ident = lValue.getIdent();
        String name = ident.getName();
        PixelSelector px = lValue.getPixelSelector();
        // System.out.println(lValue.getLine());
        // System.out.println(name);
        if(px != null) {
            px.visit(this, arg);
            return Type.PIXEL;
        }
        NameDef def = symbolTable.lookup(name);
        check(def != null, lValue, "no work");
        return def.getType();
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCException {
        Type type = nameDef.getType();
        nameDef.getIdent().visit(this, arg);
        Dimension dim = nameDef.getDimension();
        if(dim != null) {
            if(nameDef.getType() != Type.IMAGE) {
                check(false, nameDef, "Not an image");
            }
            nameDef.getDimension().visit(this, arg);
        }
        String name = nameDef.getIdent().getName();
        //name = name + "_" + symbolTable.scope_stack.size();
        boolean inserted = symbolTable.insert(name, nameDef);
        check(inserted, nameDef, "already in table");
        if(nameDef.getType() == Type.VOID) {
            check(false, nameDef, "namedef is void");
        }
        return type;
    }

    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCException {
        numLitExpr.setType(Type.INT);
        return numLitExpr.getType();
    }

    public Object visitPixelFuncExpr(PixelFuncExpr pixelFuncExpr, Object arg) throws PLCException {
        // System.out.println("Running");
        PixelSelector px = pixelFuncExpr.getSelector();
        px.visit(this, arg);
        pixelFuncExpr.setType(Type.INT);
        return pixelFuncExpr.getType();
    }

    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCException {
        // System.out.println(pixelSelector.getLine());
        // System.out.println(pixelSelector.getFirstToken().getTokenString());
        Type x = (Type)pixelSelector.getX().visit(this, arg);
        Type y = (Type)pixelSelector.getY().visit(this, arg);
        if (x == Type.INT && y == Type.INT) {
            return null;
        }
        throw new TypeCheckException("invalid pixel selector");
    }

    public Object visitPredeclaredVarExpr(PredeclaredVarExpr predeclaredVarExpr, Object arg) throws PLCException {
        predeclaredVarExpr.setType(Type.INT);
        return predeclaredVarExpr.getType();
    }

    Type root;

    @Override
    public Object visitProgram(Program program, Object arg) throws PLCException {
        root = program.getType();
        symbolTable.enterScope();
        List<NameDef> paramList = program.getParamList();
        for (NameDef param : paramList) {
            param.visit(this, arg);
        }
        // Visit the block
        Block block = program.getBlock();
        block.visit(this, arg);
        symbolTable.closeScope();
        return program.getType();
    }

    public Object visitRandomExpr(RandomExpr randomExpr, Object arg) throws PLCException {
        randomExpr.setType(Type.INT);
        return randomExpr.getType();
    }

    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCException {
        // System.out.println("typecheck return");
        Type expr = (Type)returnStatement.getE().visit(this, arg);
        check(assignmentCompatability(root, expr), returnStatement, "type of expr and declared type don't match");
        return expr;
    }

    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCException {
        stringLitExpr.setType(Type.STRING);
        return stringLitExpr.getType();
    }

    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCException {
        switch(unaryExpr.getOp()) {
            case BANG -> {
                if ((Type)unaryExpr.getE().visit(this, arg) == Type.INT) {
                    unaryExpr.setType(Type.INT);
                    return Type.INT;
                } else if ((Type)unaryExpr.getE().visit(this, arg) == Type.PIXEL) {
                    unaryExpr.setType(Type.PIXEL);
                    return Type.PIXEL;
                }
                throw new TypeCheckException("invalid unary type");
            }
            case MINUS, RES_cos, RES_sin, RES_atan -> {
                if ((Type)unaryExpr.getE().visit(this, arg) == Type.INT) {
                    unaryExpr.setType(Type.INT);
                    return Type.INT;
                }
                throw new TypeCheckException("invalid unary type");
            }
            default -> {
                throw new TypeCheckException("invalid unary type");
            }
        }
    }

    public Object visitUnaryExprPostFix(UnaryExprPostfix unaryExprPostfix, Object arg) throws PLCException {
        //System.out.println("running");
        // System.out.println(unaryExprPostfix.getLine());
        Type primary = (Type)unaryExprPostfix.getPrimary().visit(this, arg);
        // System.out.println(unaryExprPostfix.getFirstToken().getTokenString());
        // System.out.println(unaryExprPostfix.getType());
        boolean hasPix = (unaryExprPostfix.getPixel() != null);
        if(hasPix) {
            unaryExprPostfix.getPixel().visit(this, arg);
        }
        boolean hasChan = (unaryExprPostfix.getColor() != null);
        if (primary == Type.PIXEL) {
            if (!hasPix && hasChan) {
                unaryExprPostfix.setType(Type.INT);
                return Type.INT;
            } else {
                throw new TypeCheckException("invalid pixel");
            }
        } else if (primary == Type.IMAGE) {
            if (!hasPix && hasChan) {
                unaryExprPostfix.setType(Type.IMAGE);
                return Type.IMAGE;
            } else if (hasPix && !hasChan) {
                unaryExprPostfix.getPixel().visit(this, arg);
                unaryExprPostfix.setType(Type.PIXEL);
                return Type.PIXEL;
            } else if (hasPix && hasChan) {
                unaryExprPostfix.getPixel().visit(this, arg);
                unaryExprPostfix.setType(Type.INT);
                return Type.INT;
            } else {
                throw new TypeCheckException("invalid image");
            }
        }
        throw new TypeCheckException("invalid primary type");
    }

    public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws PLCException {
         Expr expr = whileStatement.getGuard();
         Type exprType = (Type)expr.visit(this, arg);
         check(expr.getType() == Type.INT, whileStatement, "Not an int");
         if(exprType != Type.INT) {
             throw new TypeCheckException("while loop isn't equal to an integer");
         }
         symbolTable.enterScope();
         Block block = whileStatement.getBlock();
         block.visit(this, arg);
         symbolTable.closeScope();
        return whileStatement.getGuard().getType();
    }

    public Object visitWriteStatement(WriteStatement statementWrite, Object arg) throws PLCException {
        Type expr = (Type) statementWrite.getE().visit(this, arg);
        return expr;
    }

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