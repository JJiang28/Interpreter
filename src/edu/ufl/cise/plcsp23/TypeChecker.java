package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.ast.*;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

import edu.ufl.cise.plcsp23.IToken.Kind;

public class TypeChecker implements ASTVisitor{

    public static class SymbolTable {
        HashMap<String,Declaration>entries = new HashMap<>();

        public boolean insert(String name, Declaration declaration) {
            return (entries.putIfAbsent(name, declaration) == null);
        }

        public Declaration lookup(String name) {
            return entries.get(name);
        }
    }

    private void check(boolean condition, AST node, String message) throws PLCException {
        if (!condition) {throw new PLCException("rawr");}
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLCException {
        List<NameDef> decAndStatements = program.getParamList();
        for (NameDef node: decAndStatements) {
            node.visit(this, arg);
        }
        return program;
    }

}