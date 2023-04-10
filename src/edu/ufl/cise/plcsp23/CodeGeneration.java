package edu.ufl.cise.plcsp23;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.ufl.cise.plcsp23.ast.*;

public class CodeGeneration implements ASTVisitor {
    public Set<String> imports = new HashSet<>();

    public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws PLCException {
        LValue LV = statementAssign.getLv();
        Expr expr = statementAssign.getE();
        String lvStr = (String) LV.visit(this, arg);
        String exprStr = (String) expr.visit(this, arg);
        return lvStr + " = " + exprStr;
    }
 
	 public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCException {

     }
 
	 public Object visitBlock(Block block, Object arg) throws PLCException {
        List<String> blockList = new ArrayList<>();
        List<Declaration> dec = block.getDecList();
        for (Declaration node: dec) {
            blockList.add((String) node.visit(this, arg));
        }
        List<Statement> state = block.getStatementList();
        for (Statement node: state) {
            blockList.add((String) node.visit(this, arg));
        }
        String blockStr = "";
        for (String str: blockList) {
            blockStr += str;
        }
        return blockStr;
     }
 
	 public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCException {

     }
 
	 public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException {
        NameDef nDef = declaration.getNameDef();
        Expr initializer = declaration.getInitializer();
        String nDefStr = (String) nDef.visit(this, arg);
        String initString;
        if (initializer != null) {
            initString = (String) initializer.visit(this, arg);
            return nDefStr + " = " + initString + ";";
        }
        return nDefStr + ";";
     }
 
	 public Object visitDimension(Dimension dimension, Object arg) throws PLCException {
        throw new UnsupportedOperationException();
     }
 
	 public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCException {
        throw new UnsupportedOperationException();
     }
 
	 Object visitIdent(Ident ident, Object arg) throws PLCException;
 
	 public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCException {
        return identExpr.getName();
     }
 
	 public Object visitLValue(LValue lValue, Object arg) throws PLCException {
        Ident ident = lValue.getIdent();
        String name = ident.getName();
        return name;
     }
 
	 public Object visitNameDef(NameDef nameDef, Object arg) throws PLCException {
        Type type = nameDef.getType();
        Ident ident = nameDef.getIdent();
        String typeStr = type.toString().toLowerCase();
        String name = ident.getName();
        return typeStr + " " + name;
     }
 
	 Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCException;
 
	 public Object visitPixelFuncExpr(PixelFuncExpr pixelFuncExpr, Object arg) throws PLCException {
        throw new UnsupportedOperationException();
     }
 
	 public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCException {
        throw new UnsupportedOperationException();
     }
 
	 public Object visitPredeclaredVarExpr(PredeclaredVarExpr predeclaredVarExpr, Object arg) throws PLCException {
        throw new UnsupportedOperationException();
     }
 
	 public Object visitProgram(Program program, Object arg) throws PLCException {
        Ident ident = program.getIdent();
        Type type = program.getType();
        List<NameDef> params = program.getParamList();
        Block block = program.getBlock();

        String name = ident.getName();
        String typeStr = type.toString().toLowerCase();
        List<String> paramStrs = new ArrayList<>();
        for (int i = 0; i < params.size(); i++) {
            paramStrs.add((String) params.get(i).visit(this, arg)); // TODO: see if casting works
        }
        String blockStr = (String) block.visit(this, arg);

        String code = "public class " + name + " {\n" +
                    "public static " + typeStr + " apply(";
        for (int i = 0; i < paramStrs.size()-1; i++) {
            code += paramStrs.get(i) + ", ";
        }
        code += paramStrs.get(paramStrs.size()-1) + ") {\n" +
                    blockStr + "}";

        return code;
     }
 
	 public Object visitRandomExpr(RandomExpr randomExpr, Object arg) throws PLCException {
        imports.add("import java.lang.Math.*;");
        return "Math.floor(Math.random() * 256)";
     }
 
	 public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCException {
        Expr expr = returnStatement.getE();
        String exprStr = (String) expr.visit(this, arg);
        return "return " + exprStr + ";";
     }
 
	 public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCException {
        return stringLitExpr.toString(); //TODO: look at this pls
     }
 
	 public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCException {
        throw new UnsupportedOperationException();
     }
 
	 public Object visitUnaryExprPostFix(UnaryExprPostfix unaryExprPostfix, Object arg) throws PLCException {
        throw new UnsupportedOperationException();
     }
 
	 public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws PLCException {
        Expr expr = whileStatement.getGuard();
        Block block = whileStatement.getBlock();
        
     }
 
	 public Object visitWriteStatement(WriteStatement statementWrite, Object arg) throws PLCException {
        Expr expr = statementWrite.getE();
        String exprStr = (String) expr.visit(this, arg);
        imports.add("import edu.ufl.cise.plcsp23.image.ConsoleIO;");
        return "ConsoleIO.write(" + exprStr + ");";
     }
 
	 public Object visitZExpr(ZExpr zExpr, Object arg) throws PLCException {
        return "255"; //TODO: not sure about this one
     }
}
