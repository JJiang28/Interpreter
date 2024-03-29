package edu.ufl.cise.plcsp23;

import java.awt.image.BufferedImage;
import java.util.*;

import edu.ufl.cise.plcsp23.ast.*;
import edu.ufl.cise.plcsp23.runtime.FileURLIO;
import edu.ufl.cise.plcsp23.runtime.ImageOps;
import edu.ufl.cise.plcsp23.IToken.Kind;
import edu.ufl.cise.plcsp23.TypeChecker.SymbolTable;

public class CodeGeneration implements ASTVisitor {
    public Set<String> imports = new HashSet<>();
    boolean hasX, hasY;
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
            case PIXEL -> {
                return "int";
            }
            case IMAGE -> {
                imports.add("import java.awt.image.BufferedImage;\n");
                return "BufferedImage";
            }
            default -> {
                return null;
            }
        }
    }

    SymbolTable symbolTable = new SymbolTable();

    public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws PLCException {
        hasX = false;
        hasY = false;
        LValue LV = statementAssign.getLv();
        Expr expr = statementAssign.getE();
        Type lvType = symbolTable.lookup(LV.getIdent().getName()).getType();
        Type exprType = expr.getType();
        String lvStr = LV.visit(this, arg).toString();
        String exprStr = expr.visit(this, arg).toString();
        String res = "";

        if (lvType == Type.IMAGE && exprType == Type.STRING) {
            if (LV.getPixelSelector() == null && LV.getColor() == null) {
                imports.add("import edu.ufl.cise.plcsp23.runtime.ImageOps;\n");
                imports.add("import edu.ufl.cise.plcsp23.runtime.FileURLIO;\n");
                res = "ImageOps.copyInto(FileURLIO.readImage(" + exprStr + "), " + lvStr + ");\n";
                return res;
            }
        }
        
        if (lvType == Type.IMAGE && exprType == Type.IMAGE) {
            if (LV.getPixelSelector() == null && LV.getColor() == null) {
                imports.add("import edu.ufl.cise.plcsp23.runtime.ImageOps;\n");
                res = "ImageOps.copyInto(" + exprStr + ", " + lvStr + ")" + ";\n";
                return res;
            }
        }
        else if (lvType == Type.IMAGE && (exprType == Type.PIXEL || exprType == Type.INT)) {
            if (LV.getPixelSelector() == null && LV.getColor() == null) {
                System.out.println(lvType);
                System.out.println(exprType);
                imports.add("import edu.ufl.cise.plcsp23.runtime.ImageOps;\n");
                res = "ImageOps.setAllPixels(" + lvStr + ", " + exprStr + ")" + ";\n";
                return res;
            } 
            else if (LV.getPixelSelector() != null && LV.getColor() == null) {
                PixelSelector pix = LV.getPixelSelector();
                String lx = pix.getX().visit(this, arg).toString();
                String ly = pix.getY().visit(this, arg).toString();
                imports.add("import edu.ufl.cise.plcsp23.runtime.ImageOps;\n");
                res = "ImageOps.setRGB(" + lvStr + ", " + lx + ", " + ly + ", " +
                        exprStr + ");\n";
                if (hasX & hasY) {
                    res = "for (int x = 0; x < " + lvStr + ".getWidth(); x++) {\n" +
                    "\tfor (int y = 0; y < " + lvStr + ".getHeight(); y++) {\n" +
                    "\t\t" + res +
                    "\t}\n" +
                    "}\n";
                } if (hasX & !hasY) {
                    res = "for (int x = 0; x < " + lvStr + ".getWidth(); x++) {\n" +
                    "\t" + res +
                    "}\n";
                } if (!hasX & hasY) {
                    res = "for (int y = 0; y < " + lvStr + ".getHeight(); y++) {\n" +
                    "\t" + res +
                    "}\n";
                }
                return res;
            }
            else if (LV.getPixelSelector() != null && LV.getColor() != null) {
                PixelSelector pix = LV.getPixelSelector();
                ColorChannel col = LV.getColor();
                String lx = pix.getX().visit(this, arg).toString();
                String ly = pix.getY().visit(this, arg).toString();
                String color = col.name().substring(0, 1) + col.name().substring(1);
                color = color.substring(0, 1).toUpperCase() + color.substring(1);
                imports.add("import edu.ufl.cise.plcsp23.runtime.ImageOps;\n");
                imports.add("import edu.ufl.cise.plcsp23.runtime.PixelOps;\n");
                res = "ImageOps.setRGB(" + lvStr + ", " + lx + ", " + ly + ", PixelOps.set" + color + "(ImageOps.getRGB(" + lvStr + ", " + lx + ", " + ly + "), " + exprStr + "));\n";
                if (hasX & hasY) {
                    res = "for (int x = 0; x < " + lvStr + ".getWidth(); x++) {\n" +
                    "\tfor (int y = 0; y < " + lvStr + ".getHeight(); y++) {\n" +
                    "\t\t" + res +
                    "\t}\n" +
                    "}\n";
                } if (hasX & !hasY) {
                    res = "for (int x = 0; x < " + lvStr + ".getWidth(); x++) {\n" +
                    "\t" + res +
                    "}\n";
                } if (!hasX & hasY) {
                    res = "for (int y = 0; y < " + lvStr + ".getHeight(); y++) {\n" +
                    "\t" + res +
                    "}\n";
                }
                return res;
            }
            else if(LV.getPixelSelector() == null && LV.getColor() != null) {
                ColorChannel col = LV.getColor();
                String color = col.name();
                color = color.substring(0, 1).toUpperCase() + color.substring(1);
                imports.add("import edu.ufl.cise.plcsp23.runtime.ImageOps;\n");
                imports.add("import edu.ufl.cise.plcsp23.runtime.PixelOps;\n");
                res = "ImageOps.setRGB(" + lvStr + ", " + "x" + ", " + "y" + ", " +
                        exprStr + ");\n";
                res = "for (int x = 0; x < " + lvStr + ".getWidth(); x++) {\n" +
                "\tfor (int y = 0; y < " + lvStr + ".getHeight(); y++) {\n" +
                "\t\t" + res +
                "\t}\n" +
                "}\n";
                return res;
            }
        }
        else if (lvType == Type.IMAGE && exprType == Type.STRING) {
            imports.add("import edu.ufl.cise.plcsp23.runtime.FileURLIO;\n");
            String iString = "FileURLIO.readImage(" + exprStr + ")";
            return "ImageOps.copyInto(" + iString + ", " + lvStr + ")" + ";\n";
        }
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
        if(kind == Kind.AND){op += "&&"; boolin = true;}
        if(kind == Kind.BITAND){op += "&"; boolout = false;}
        if(kind == Kind.EXP){
            op += "**";
            imports.add("import java.lang.Math.*;\n");
            return "(int) Math.pow(" + expr0 + ", " + expr1 + ")";
        }
        if (binaryExpr.getLeft().getType() == Type.IMAGE) {
            imports.add("import edu.ufl.cise.plcsp23.runtime.ImageOps;\n");
            if (binaryExpr.getRight().getType() == Type.IMAGE) {
                switch (kind) {
                    case PLUS, MINUS, TIMES, DIV, MOD -> {
                        return "ImageOps.binaryImageImageOp(ImageOps.OP." + kind.name() + ", " + expr0 + ", " + expr1 +")";
                    }
                }
            }
            if (binaryExpr.getRight().getType() == Type.INT) {
                switch (kind) {
                    case PLUS, MINUS, TIMES, DIV, MOD -> {
                        return "ImageOps.binaryImageScalarOp(ImageOps.OP." + kind.name() + ", " + expr0 + ", " + expr1 +")";
                    }
                }
            }
            throw new UnsupportedOperationException();
        }
        if (binaryExpr.getLeft().getType() == Type.PIXEL) {
            if (binaryExpr.getRight().getType() == Type.PIXEL) {
                if(kind == Kind.BITAND) {
                    return expr0 + " & " + expr1;
                }
                if(kind == Kind.BITOR) {
                    return expr0 + " | " + expr1;
                }
                imports.add("import edu.ufl.cise.plcsp23.runtime.ImageOps;\n");
                return "ImageOps.binaryPackedPixelPixelOp(ImageOps.OP." + kind.name() + ", " + expr0 + ", " + expr1 +")";
            }
            imports.add("import edu.ufl.cise.plcsp23.runtime.ImageOps;\n");
            return "ImageOps.binaryPackedPixelIntOp(ImageOps.OP." + kind.name() + ", " + expr0 + ", " + expr1 +")";
            //throw new UnsupportedOperationException();
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
        hasX = false;
        hasY = false;
        NameDef nDef = declaration.getNameDef();
        Expr initializer = declaration.getInitializer();
        //symbolTable.lookup(nDef.getIdent().getName());
        String nDefStr = nDef.visit(this, arg).toString();
        String name = nDef.getIdent().getName();
        name = name + "_" + symbolTable.findScope(name);
        String iString = ""; 
        if (initializer != null) {
            iString = initializer.visit(this, arg).toString();
        }
        String initString = "";
        if (nDef.getType() == Type.IMAGE) {
            if (nDef.getDimension() == null) {
                if (initializer == null) {
                    throw new UnsupportedOperationException("invalid image");
                } 
                else if (initializer.getType() == Type.STRING) {
                    imports.add("import edu.ufl.cise.plcsp23.runtime.FileURLIO;\n");
                    initString = "FileURLIO.readImage(" + iString + ");";
                } 
                else if (initializer.getType() == Type.IMAGE) {
                    imports.add("import edu.ufl.cise.plcsp23.runtime.ImageOps;\n");
                    initString = "ImageOps.cloneImage(" + iString + ");";
                }
            } else {
                Dimension dim = nDef.getDimension();
                String w = nDef.getDimension().getWidth().visit(this, arg).toString();
                String h = nDef.getDimension().getHeight().visit(this, arg).toString();
                if (initializer == null) {
                    imports.add("import edu.ufl.cise.plcsp23.runtime.ImageOps;\n");
                    initString = "ImageOps.makeImage(" + w + ", " + h + ");";
                }
                else if (initializer.getType() == Type.STRING) {
                    imports.add("import edu.ufl.cise.plcsp23.runtime.FileURLIO;\n");
                    initString = "FileURLIO.readImage(" + iString + ", " + w + ", " + h + ");";
                }
                else if (initializer.getType() == Type.IMAGE) {
                    imports.add("import edu.ufl.cise.plcsp23.runtime.ImageOps;\n");
                    initString = "ImageOps.copyAndResize(" + iString + ", " + w + ", " + h + ");";
                }
                else if(initializer.getType() == Type.PIXEL) {
                    imports.add("import edu.ufl.cise.plcsp23.runtime.ImageOps;\n");
                    initString = "ImageOps.setAllPixels(" + "ImageOps.makeImage(" + w + ", " + h + ")" + ", " +  iString + ");"; 
                }
            }
            nDefStr = nDefStr + " = " + initString + ";\n";
            if (hasX && hasY) {
                nDefStr = "for (int x = 0; x < " + name + ".getWidth(); x++) {\n" +
                            "\tfor (int y = 0; y < " + name + ".getHeight(); y++) {\n" +
                            "\t\t" + nDefStr +
                            "\t}\n" +
                            "}\n";
            } else if (hasX && !hasY) {
                nDefStr = "for (int x = 0; x < " + name + ".getWidth(); x++) {\n" +
                            "\t" + nDefStr +
                            "}\n";
            } else if (!hasX && hasY) {
                nDefStr = "for (int y = 0; y < " + name + ".getHeight(); y++) {\n" +
                            "\t" + nDefStr +
                            "}\n";
            }
            return nDefStr;
        }
        // else if (nDef.getType() == Type.PIXEL) {
        //     String r;
        //     imports.add("import edu.ufl.cise.plcsp23.runtime.PixelOps;\n");
        //     initString = "PixelOps.pack(" + r + ", " + g + ", " + b + ");";
        // }
        else {
            if (initializer != null) {
                initString = initializer.visit(this, arg).toString();
                if (nDef.getType() != initializer.getType()) {
                    if (nDef.getType() == Type.STRING) {
                        initString = initString + " + \"\"";
                    }
                }
                return nDefStr + " = " + initString + ";\n";
            }
        }
        return nDefStr + ";\n";
     }
 
	 public Object visitDimension(Dimension dimension, Object arg) throws PLCException {
        throw new UnsupportedOperationException();
     }
 
	 public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCException {
        String r = expandedPixelExpr.getRedExpr().visit(this, arg).toString();
        String g = expandedPixelExpr.getGrnExpr().visit(this, arg).toString();
        String b = expandedPixelExpr.getBluExpr().visit(this, arg).toString();
        System.out.println(r + g + b);
        imports.add("import edu.ufl.cise.plcsp23.runtime.PixelOps;\n");
        return "PixelOps.pack(" + r + ", " + g + ", " + b + ")";
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
        String x = pixelSelector.getX().visit(this, arg).toString();
        String y = pixelSelector.getY().visit(this, arg).toString();
        return x + ", " + y;
     }
 
	 public Object visitPredeclaredVarExpr(PredeclaredVarExpr predeclaredVarExpr, Object arg) throws PLCException {
        if (predeclaredVarExpr.getKind() == Kind.RES_y) {
            hasY = true;
            return "y";
        }
        else if (predeclaredVarExpr.getKind() == Kind.RES_x) {
            hasX = true;
            return "x";
        }
        else {
            System.out.println(predeclaredVarExpr.getKind());
            throw new UnsupportedOperationException();
        }
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
                if (expr.getType() == Type.INT){
                    //exprStr = "Integer.toString(" + exprStr + ")";
                    exprStr = exprStr + " + \"\"";
                }
                if (expr.getType() == Type.PIXEL) {
                    exprStr = "PixelOps.packedToString(" + exprStr + ")";
                }
            }
        }
        return "return " + exprStr + ";\n";
     }
 
	 public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCException {
        String strStr = "\""+ stringLitExpr.getValue() + "\"";
        return strStr;
     }
 
	 public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCException {
        Kind oper = unaryExpr.getOp();
        String op = "";
        String expr = unaryExpr.getE().visit(this, arg).toString();
        if (oper == Kind.BANG) {
            op = "!";
            if (unaryExpr.getE().getType() == Type.INT) {
                return "(" + expr + " == 0 ? 1 : 0)";
            } 
            else if (unaryExpr.getE().getType() == Type.STRING) {
                return "!(" + expr + ")";
            }
        }
        if (oper == Kind.MINUS) {
            op = "-";
            if (unaryExpr.getE().getType() == Type.INT) {
                return "( -1 * " + expr + ")"; 
            }
        }
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
        Expr primary = unaryExprPostfix.getPrimary();
        PixelSelector pixel = unaryExprPostfix.getPixel();
        ColorChannel color = unaryExprPostfix.getColor();
        String primaryStr = primary.visit(this, arg).toString();
        if (primary.getType() == Type.IMAGE) {
            if (pixel != null && color == null) {
                String x = pixel.getX().visit(this, arg).toString();
                String y = pixel.getY().visit(this, arg).toString();
                imports.add("import edu.ufl.cise.plcsp23.runtime.ImageOps;\n");
                return "ImageOps.getRGB(" + primaryStr + ", " + x + ", " + y + ")"; 
            }
            if (pixel != null && color != null) {
                String x = pixel.getX().visit(this, arg).toString();
                String y = pixel.getY().visit(this, arg).toString();
                String col = color.toString();
                imports.add("import edu.ufl.cise.plcsp23.runtime.PixelOps;\n");
                imports.add("import edu.ufl.cise.plcsp23.runtime.ImageOps;\n");
                return "PixelOps." + col + "(ImageOps.getRGB(" + primaryStr + ", " + x + ", " + y + "))";
            }
            if (pixel == null && color != null) {
                imports.add("import edu.ufl.cise.plcsp23.runtime.ImageOps;\n");
                switch (color) {
                    case red -> {
                        return "ImageOps.extractRed(" + primaryStr + ")";
                    }
                    case grn -> {
                        return "ImageOps.extractGrn(" + primaryStr + ")";

                    }
                    case blu -> {
                        return "ImageOps.extractBlu(" + primaryStr + ")";
                    }
                }
            }
        }
        else if (primary.getType() == Type.PIXEL) {
            if (pixel == null && color != null) {
                imports.add("import edu.ufl.cise.plcsp23.runtime.ImageOps;\n");
                imports.add("import edu.ufl.cise.plcsp23.runtime.PixelOps;\n");
                return "PixelOps." + color.toString() + "(" + primaryStr + ")";
            }
        }
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
        if (expr.getType() == Type.PIXEL) {
            return "ConsoleIO.writePixel(" + exprStr + ");\n";
        }
        return "ConsoleIO.write(" + exprStr + ");\n";
     }
 
	 public Object visitZExpr(ZExpr zExpr, Object arg) throws PLCException {
        return "255";
     }
}
