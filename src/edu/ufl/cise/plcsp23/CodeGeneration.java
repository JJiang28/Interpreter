// package edu.ufl.cise.plcsp23;

// import java.util.ArrayList;
// import java.util.List;

// import edu.ufl.cise.plcsp23.ast.*;

// public class CodeGeneration implements ASTVisitor {
//     Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws PLCException;
 
// 	 public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCException {

//      }
 
// 	 public Object visitBlock(Block block, Object arg) throws PLCException {
//         List<String> blockList = new ArrayList<>();
//         List<Declaration> dec = block.getDecList();
//         for (Declaration node: dec) {
//             blockList.add((String) node.visit(this, arg));
//         }
//         List<Statement> state = block.getStatementList();
//         for (Statement node: state) {
//             blockList.add((String) node.visit(this, arg));
//         }
//         String blockStr = "";
//         for (String str: blockList) {
//             blockStr += str;
//         }
//         return blockStr;
//      }
 
// 	 public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCException {

//      }
 
// 	 public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException {
//         NameDef nDef = declaration.getNameDef();
//         Expr initializer = declaration.getInitializer();
//         String nDefStr = (String) nDef.visit(this, arg);
//         String initString;
//         if (initializer != null) {
//             initString = (String) initializer.visit(this, arg);
//             return nDefStr + " = " + initString;
//         }
//         return nDefStr;
//      }
 
// 	 public Object visitDimension(Dimension dimension, Object arg) throws PLCException {
//         throw new UnsupportedOperationException();
//      }
 
// 	 public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCException {
//         throw new UnsupportedOperationException();
//      }
 
// 	 Object visitIdent(Ident ident, Object arg) throws PLCException;
 
// 	 Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCException;
 
// 	 Object visitLValue(LValue lValue, Object arg) throws PLCException;
 
// 	 public Object visitNameDef(NameDef nameDef, Object arg) throws PLCException {

//      }
 
// 	 Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCException;
 
// 	 public Object visitPixelFuncExpr(PixelFuncExpr pixelFuncExpr, Object arg) throws PLCException {
//         throw new UnsupportedOperationException();
//      }
 
// 	 public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCException {
//         throw new UnsupportedOperationException();
//      }
 
// 	 public Object visitPredeclaredVarExpr(PredeclaredVarExpr predeclaredVarExpr, Object arg) throws PLCException {
//         throw new UnsupportedOperationException();
//      }
 
// 	 public Object visitProgram(Program program, Object arg) throws PLCException {
//         Ident ident = program.getIdent();
//         Type type = program.getType();
//         List<NameDef> params = program.getParamList();
//         Block block = program.getBlock();

//         String name = ident.getName();
//         String typeStr = type.toString().toLowerCase();
//         List<String> paramStrs = new ArrayList<>();
//         for (int i = 0; i < params.size(); i++) {
//             paramStrs.add((String) params.get(i).visit(this, arg)); // TODO: see if casting works
//         }
//         String blockStr = (String) block.visit(this, arg);

//         String code = "public class " + name + " {\n" +
//                     "public static " + typeStr + " apply(";
//         for (int i = 0; i < paramStrs.size()-1; i++) {
//             code += paramStrs.get(i) + ", ";
//         }
//         code += paramStrs.get(paramStrs.size()-1) + ") {\n" +
//                     blockStr + "}";

//         return code;
//      }
 
// 	 Object visitRandomExpr(RandomExpr randomExpr, Object arg) throws PLCException;
 
// 	 Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCException;
 
// 	 Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCException;
 
// 	 public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCException {
//         throw new UnsupportedOperationException();
//      }
 
// 	 public Object visitUnaryExprPostFix(UnaryExprPostfix unaryExprPostfix, Object arg) throws PLCException {
//         throw new UnsupportedOperationException();
//      }
 
// 	 Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws PLCException;
 
// 	 Object visitWriteStatement(WriteStatement statementWrite, Object arg) throws PLCException;
 
// 	 Object visitZExpr(ZExpr zExpr, Object arg) throws PLCException;
// }
