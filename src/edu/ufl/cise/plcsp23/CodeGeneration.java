package edu.ufl.cise.plcsp23;

import java.util.*;

import edu.ufl.cise.plcsp23.ast.*;
import edu.ufl.cise.plcsp23.IToken.Kind;

public class CodeGeneration implements ASTVisitor {
    public Set<String> imports = new HashSet<>();
    String all;

    public CodeGeneration(String pack) {
        all = pack;
    }

    public String typeToString(Type type) {
        switch(type) {
            case INT, VOID -> {
                return type.toString().toLowerCase();
            }
            case STRING -> {
                return "String";
            }
            default -> {
                return null;
            }
        }
    }

    public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws PLCException {
        LValue LV = statementAssign.getLv();
        Expr expr = statementAssign.getE();
        String lvStr = LV.visit(this, arg).toString();
        String exprStr = expr.visit(this, arg).toString();
        return lvStr + " = " + exprStr;
    }
 
	 public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCException {
        String expr0 = binaryExpr.getLeft().visit(this, arg).toString();
        String expr1 = binaryExpr.getRight().visit(this, arg).toString();
        Kind kind = binaryExpr.getOp();
        String op = "";
        if(kind == Kind.PLUS){op += "+";}
        if(kind == Kind.MINUS){op += "-";}
        if(kind == Kind.TIMES){op += "*";}
        if(kind == Kind.DIV){op += "/";}
        if(kind == Kind.MOD){op += "%";}
        if(kind == Kind.LT){op += "<";}
        if(kind == Kind.GT){op += ">";}
        if(kind == Kind.LE){op += "<=";}
        if(kind == Kind.GE){op += ">=";}
        if(kind == Kind.EQ){op += "==";}
        if(kind == Kind.BITOR){op += "|";}
        if(kind == Kind.OR){op += "||";}
        if(kind == Kind.AND){op += "&";}
        if(kind == Kind.BITAND){op += "&&";}
        if(kind == Kind.EXP){op += "**";}

        return expr0 + op + expr1;
     }
 
	 public Object visitBlock(Block block, Object arg) throws PLCException {
        List<String> blockList = new ArrayList<>();
        List<Declaration> dec = block.getDecList();
        for (Declaration node: dec) {
            blockList.add((String) node.visit(this, arg));
        }
        List<Statement> state = block.getStatementList();
        for (Statement node: state) {
            blockList.add(node.visit(this, arg).toString());
        }
        String blockStr = "";
        for (String str: blockList) {
            blockStr += str;
        }
        return blockStr;
     }
 
	public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCException {
		String guard = conditionalExpr.getGuard().visit(this, arg).toString();
		String trueCase = conditionalExpr.getTrueCase().visit(this, arg).toString();
		String falseCase = conditionalExpr.getFalseCase().visit(this, arg).toString();
        String conditionalStr = "(" + guard + "?" + trueCase + ":" + falseCase + ")";
        return conditionalStr;
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
 
	 public Object visitIdent(Ident ident, Object arg) throws PLCException {
      throw new UnsupportedOperationException();
    }
 
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
        String typeStr = typeToString(type);
        String name = ident.getName();
        return typeStr + " " + name;
     }
 
	 public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCException {
        return numLitExpr.getValue();
    }
 
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
        String typeStr = typeToString(type);
        List<String> paramStrs = new ArrayList<>();
        for (int i = 0; i < params.size(); i++) {
            paramStrs.add((String) params.get(i).visit(this, arg)); // TODO: see if casting works
        }
        String blockStr = (String) block.visit(this, arg);

        if (typeStr.equals("string")) {
            typeStr = "String";
        }

        System.out.println(typeStr);

        String code = "public class " + name + " {\n" +
                    "public static " + typeStr + " apply(";

         for (int i = 0; i < paramStrs.size(); i++) {
             String str = paramStrs.get(i);
             str = str.replaceAll("(?i)string", "String"); // (?i) makes the search case-insensitive
             paramStrs.set(i, str);
         }

        for (int i = 0; i < paramStrs.size()-1; i++) {
            code += paramStrs.get(i) + ", ";
        }
        if (paramStrs.size() == 0) {
            code += ") {\n" +
                    blockStr + "}" + "\n}";
        }

        if(paramStrs.size() > 0) {
            code += paramStrs.get(paramStrs.size() - 1) + ") {\n" +
                    blockStr + "}" + "\n}";
        }

        return code;
     }
 
	 public Object visitRandomExpr(RandomExpr randomExpr, Object arg) throws PLCException {
        imports.add("import java.lang.Math.*;");
        return "Math.floor(Math.random() * 256)";
     }
 
	 public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCException {
        Expr expr = returnStatement.getE();
        String exprStr = expr.visit(this, arg).toString();
        return "return " + exprStr + ";";
     }
 
	 public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCException {
        String strStr = "\""+ stringLitExpr.getValue() + "\""; //TODO: look at this pls
        return strStr;
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
        throw new UnsupportedOperationException();
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
