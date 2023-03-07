/*Copyright 2023 by Beverly A Sanders
 * 
 * This code is provided for solely for use of students in COP4020 Programming Language Concepts at the 
 * University of Florida during the spring semester 2023 as part of the course project.  
 * 
 * No other use is authorized. 
 * 
 * This code may not be posted on a public web site either during or after the course.  
 */

package edu.ufl.cise.plcsp23;
import java.util.List;
import java.util.ArrayList;

import edu.ufl.cise.plcsp23.IToken.Kind;


public class CompilerComponentFactory {
	public static IScanner makeScanner(String input) {
		//Add statement to return an instance of your scanner
		return new Scanner(input);
	}
	public static IParser makeParser(String input) throws LexicalException {
		//add code to create a scanner and parser and return the parser
		Scanner temp = new Scanner(input);
		List<IToken> tokens = new ArrayList<>();
		IToken token = temp.next();
		while (token.getKind() != Kind.EOF) {
			tokens.add(token);
			token = temp.next();
		}
		tokens.add(token);
		return new Parser(tokens);
	}

}
