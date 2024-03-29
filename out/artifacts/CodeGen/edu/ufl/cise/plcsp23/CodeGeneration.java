package edu.ufl.cise.plcsp23;

import java.util.*;

import edu.ufl.cise.plcsp23.ast.*;
import edu.ufl.cise.plcsp23.IToken.Kind;
import edu.ufl.cise.plcsp23.TypeChecker.SymbolTable;

public class CodeGeneration implements ASTVisitor {
    public Set<String> imports = new HashSet<>();
    Type returnType;
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

    SymbolTable symbolTable = new SymbolTable();

    public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws PLCException {
        LValue LV = statementAssign.getLv();
        Expr expr = statementAssign.getE();
        Type lvType = symbolTable.lookup(LV.getIdent().getName()).getType();
        Type exprType = expr.getType();
        String lvStr = LV.visit(this, arg).toString();
        String exprStr = expr.visit(this, arg).toString();
        if (lvType != exprType) {
            if (lvType == Type.STRING) {    
                exprStr = "\"" + exprStr + "\"";
            }
        }
        return lvStr + " = " + exprStr + ";\n";
    }
 
	 public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCException {
        String expr0 = binaryExpr.getLeft().visit(this, arg).toString();
        String expr1 = binaryExpr.getRight().visit(this, arg).toString();
        Kind kind = binaryExpr.getOp();
        String op = "";
        Boolean boolin = false; // op takes a boolean
        Boolean boolout = true; // op returns a boolean
        if(kind == Kind.PLUS){op += "+"; boolout = false;}
        if(kind == Kind.MINUS){op += "-"; boolout = false;}
        if(kind == Kind.TIMES){op += "*"; boolout = false;}
        if(kind == Kind.DIV){op += "/"; boolout = false;}
        if(kind == Kind.MOD){op += "%"; boolout = false;}
        if(kind == Kind.LT){op += "<";}
        if(kind == Kind.GT){op += ">";}
        if(kind == Kind.LE){op += "<=";}
        if(kind == Kind.GE){op += ">=";}
        if(kind == Kind.EQ){op += "==";}
        if(kind == Kind.BITOR){op += "|"; boolout = false;}
        if(kind == Kind.OR){op += "||"; boolin = true;}
        if(kind == Kind.AND){op += "&"; boolout = false;}
        if(kind == Kind.BITAND){op += "&&"; boolin = true;}
        if(kind == Kind.EXP){
            op += "**";
            imports.add("import java.lang.Math.*;\n");
            return "(int) Math.pow(" + expr0 + ", " + expr1 + ")";
        }
        String binStr = "(" + expr0 + op + expr1 + ")";
        if (boolin)
            binStr = "((" + expr0 + " != 0) " + op + " (" + expr1 + " != 0))";
        if (boolout) 
            binStr = "(" + binStr + " ? 1 : 0)";
        return binStr;
     }
 
	 public Object visitBlock(Block block, Object arg) throws PLCException {
        List<String> blockList = new ArrayList<>();
        List<Declaration> dec = block.getDecList();
        for (Declaration node: dec) {
            //symbolTable.lookup(node.getNameDef().getIdent().getName());
            blockList.add( node.visit(this, arg).toString());
        }
        List<Statement> state = block.getStatementList();
        for (Statement node: state) {
            //symbolTable.lookup(node.firstToken.getTokenString());
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
        String conditionalStr = "((" + guard + "!= 0) ?" + trueCase + ":" + falseCase + ")";
        return conditionalStr;
	}
 
	 public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException {
        NameDef nDef = declaration.getNameDef();
        Expr initializer = declaration.getInitializer();
        //symbolTable.lookup(nDef.getIdent().getName());
        String nDefStr = nDef.visit(this, arg).toString();
        String initString;
        if (initializer != null) {
            initString = initializer.visit(this, arg).toString();
            if (nDef.getType() != initializer.getType()) {
                if (nDef.getType() == Type.STRING) {
                    initString = "\"" + initString + "\"";
                }
            }
            return nDefStr + " = " + initString + ";\n";
        }
        return nDefStr + ";\n";
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
        String name = identExpr.getName();
        return name + "_" + symbolTable.findScope(name);
     }
 
	 public Object visitLValue(LValue lValue, Object arg) throws PLCException {
        Ident ident = lValue.getIdent();
        String name = ident.getName();
        return name + "_" + symbolTable.findScope(name);
     }
 
	 public Object visitNameDef(NameDef nameDef, Object arg) throws PLCException {
        Type type = nameDef.getType();
        Ident ident = nameDef.getIdent();
        String typeStr = typeToString(type);
        String name = ident.getName();
        symbolTable.insert(name, nameDef);
        return typeStr + " " + name + "_" + symbolTable.findScope(name);
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
        returnType = type;
        List<NameDef> params = program.getParamList();
        Block block = program.getBlock();
        //System.out.println(symbolTable.scope_stack.size());

        String name = ident.getName();
        String typeStr = typeToString(type);
        List<String> paramStrs = new ArrayList<>();
        for (int i = 0; i < params.size(); i++) {
            // paramStrs.add(symbolTable.getUniqueString(params.get(i).getIdent())); // TODO: see if casting works
            paramStrs.add(params.get(i).visit(this, arg).toString());
        }
        String blockStr = block.visit(this, arg).toString();

        //System.out.println(typeStr);

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

        String importStr = "";
        for (String imp: imports) {
            importStr += imp;
        }
        code = importStr + code;
        symbolTable.closeScope();
        return code;
     }
 
	 public Object visitRandomExpr(RandomExpr randomExpr, Object arg) throws PLCException {
        imports.add("import java.lang.Math.*;\n");
        return "Math.floor(Math.random() * 256)";
     }
 
	 public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCException {
        Expr expr = returnStatement.getE();
        String exprStr = expr.visit(this, arg).toString();
        if (expr.getType() != returnType) {
            if (returnType == Type.STRING) {
                exprStr = "\"" + exprStr + "\"";
            }
        }
        return "return " + exprStr + ";\n";
     }
 
	 public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCException {
        String strStr = "\""+ stringLitExpr.getValue() + "\""; //TODO: look at this pls
        return strStr;
     }
 
	 public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCException {
        Kind oper = unaryExpr.getOp();
        String op = "";
        String expr = unaryExpr.getE().visit(this, arg).toString();
        if (oper == Kind.BANG) op = "!";
        if (oper == Kind.MINUS) op = "-";
        if (oper == Kind.RES_sin) {
            op = "sin";
            imports.add("import java.lang.Math.*;\n");
            return "Math.sin(" + expr + ")";
        }
        if (oper == Kind.RES_cos) {
            op = "cos";
            imports.add("import java.lang.Math.*;\n");
            return "Math.cos(" + expr + ")";
        }
        if (oper == Kind.RES_atan) {
            op = "atan";
            imports.add("import java.lang.Math.*;\n");
            return "Math.atan(" + expr + ")";
        }
        return op + "(" + expr + ")";
     }
 
	 public Object visitUnaryExprPostFix(UnaryExprPostfix unaryExprPostfix, Object arg) throws PLCException {
        throw new UnsupportedOperationException();
     }
 
	 public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws PLCException {
        String expr = whileStatement.getGuard().visit(this, arg).toString();
        symbolTable.enterScope();
        String block = whileStatement.getBlock().visit(this, arg).toString();
        symbolTable.closeScope();
        
        if(whileStatement.getGuard().getType() == Type.INT) {
            expr = "(" + expr + " != 0)";
        }
        String whileStr = "while (" + expr + ") {\n" +
                        block + "\n" +
                        "}\n";
        return whileStr;
     }
 
	 public Object visitWriteStatement(WriteStatement statementWrite, Object arg) throws PLCException {
        Expr expr = statementWrite.getE();
        String exprStr = expr.visit(this, arg).toString();
        imports.add("import edu.ufl.cise.plcsp23.runtime.ConsoleIO;\n");
        return "ConsoleIO.write(" + exprStr + ");\n";
     }
 
	 public Object visitZExpr(ZExpr zExpr, Object arg) throws PLCException {
        return "255"; //TODO: not sure about this one
     }
}
