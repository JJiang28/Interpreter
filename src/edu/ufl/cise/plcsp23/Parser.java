package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.ast.AST;
import edu.ufl.cise.plcsp23.IToken.Kind;
import edu.ufl.cise.plcsp23.IToken;
import edu.ufl.cise.plcsp23.Scanner;


public class Parser implements IParser {
    private IToken token;
    public Parser(IToken t) {
        this.token = t;
    }

    @Override
    public AST parse() throws PLCException {
        throw new PLCException("hi");
    }

    // private AST expression() {

    // }

    private IToken peek() {
        return IToken.next();
    }

}
