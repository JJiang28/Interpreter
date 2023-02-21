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

 import static org.junit.jupiter.api.Assertions.assertEquals;
 import static org.junit.jupiter.api.Assertions.assertThrows;
 import static org.junit.jupiter.api.Assertions.assertTrue;
 
 import java.util.ArrayList;
 
 import org.junit.jupiter.api.Test;
 
 import edu.ufl.cise.plcsp23.IToken.Kind;
 import edu.ufl.cise.plcsp23.IToken.SourceLocation;
 
 class TestScanner_starter {
 
	 // makes it easy to turn output on and off (and less typing than
	 // System.out.println)
	 static final boolean VERBOSE = true;
 
	 void show(Object obj) {
		 if (VERBOSE) {
			 System.out.println(obj);
		 }
	 }
 
	 // check that this token has the expected kind
	 void checkToken(Kind expectedKind, IToken t) {
		 assertEquals(expectedKind, t.getKind());
	 }
	 
	 void checkToken(Kind expectedKind, String expectedChars, SourceLocation expectedLocation, IToken t) {
		 assertEquals(expectedKind, t.getKind());
		 assertEquals(expectedChars, t.getTokenString());
		 assertEquals(expectedLocation, t.getSourceLocation());
		 ;
	 }
 
	 void checkIdent(String expectedChars, IToken t) {
		 checkToken(Kind.IDENT, t);
		 assertEquals(expectedChars.intern(), t.getTokenString().intern());
		 ;
	 }
 
	 void checkString(String expectedValue, IToken t) {
		 assertTrue(t instanceof IStringLitToken);
		 assertEquals(expectedValue, ((IStringLitToken) t).getValue());
	 }
 
	 void checkString(String expectedChars, String expectedValue, SourceLocation expectedLocation, IToken t) {
		 assertTrue(t instanceof IStringLitToken);
		 assertEquals(expectedValue, ((IStringLitToken) t).getValue());
		 assertEquals(expectedChars, t.getTokenString());
		 assertEquals(expectedLocation, t.getSourceLocation());
	 }
 
	 void checkNUM_LIT(int expectedValue, IToken t) {
		 checkToken(Kind.NUM_LIT, t);
		 int value = ((INumLitToken) t).getValue();
		 assertEquals(expectedValue, value);
	 }
	 
	 void checkNUM_LIT(int expectedValue, SourceLocation expectedLocation, IToken t) {
		 checkToken(Kind.NUM_LIT, t);
		 int value = ((INumLitToken) t).getValue();
		 assertEquals(expectedValue, value);
		 assertEquals(expectedLocation, t.getSourceLocation());
	 }
 
	 void checkTokens(IScanner s, Kind... kinds) throws LexicalException {
		 for (Kind kind : kinds) {
			 checkToken(kind, s.next());
		 }
	 }
 
	 void checkTokens(String input, Kind... kinds) throws LexicalException {
		 IScanner s = CompilerComponentFactory.makeScanner(input);
		 for (Kind kind : kinds) {
			 checkToken(kind, s.next());
		 }
	 }
 
	 // check that this token is the EOF token
	 void checkEOF(IToken t) {
		 checkToken(Kind.EOF, t);
	 }
 
	 //Works
	 @Test
	 void emptyProg() throws LexicalException {
		 String input = "";
		 IScanner scanner = CompilerComponentFactory.makeScanner(input);
		 checkEOF(scanner.next());
	 }
 
	 //Works
	 @Test
	 void onlyWhiteSpace() throws LexicalException {
		 String input = " \t \r\n \f \n";
		 IScanner scanner = CompilerComponentFactory.makeScanner(input);
		 checkEOF(scanner.next());
		 checkEOF(scanner.next());  //repeated invocations of next after end reached should return EOF token
	 }
 
	 //Works
	 @Test
	 void numLits1() throws LexicalException {
		 String input = """
				 123
				 05 240
				 """;
		 IScanner scanner = CompilerComponentFactory.makeScanner(input);
		 checkNUM_LIT(123, scanner.next());
		 checkNUM_LIT(0, scanner.next());
		 checkNUM_LIT(5, scanner.next());
		 checkNUM_LIT(240, scanner.next());
		 checkEOF(scanner.next());
	 }
 
	 //Works
	 @Test
	 //Too large should still throw LexicalException
	 void numLitTooBig() throws LexicalException {
		 String input = "999999999999999999999";
		 IScanner scanner = CompilerComponentFactory.makeScanner(input);
		 assertThrows(LexicalException.class, () -> {
			 scanner.next();
		 });
	 }
 
 
	 @Test
	 void identsAndReserved() throws LexicalException {
		 String input = """
				 i0
				   i1  x ~~~2 spaces at beginning and after il
				 y Y
				 """;
 
		 IScanner scanner = CompilerComponentFactory.makeScanner(input);
		 checkToken(Kind.IDENT,"i0", new SourceLocation(1,1), scanner.next());
		 checkToken(Kind.IDENT, "i1",new SourceLocation(2,3), scanner.next());
		 checkToken(Kind.RES_x, "x", new SourceLocation(2,7), scanner.next());		
		 checkToken(Kind.RES_y, "y", new SourceLocation(3,1), scanner.next());
		 checkToken(Kind.RES_Y, "Y", new SourceLocation(3,3), scanner.next());
		 checkEOF(scanner.next());
	 }
 
 
	 //Works
	 @Test
	 void operators0() throws LexicalException {
		 String input = """
				 ==
				 +
				 /
				 ====
				 =
				 ===
				 """;
		 IScanner scanner = CompilerComponentFactory.makeScanner(input);
		 checkToken(Kind.EQ, scanner.next());
		 checkToken(Kind.PLUS, scanner.next());
		 checkToken(Kind.DIV, scanner.next());
		 checkToken(Kind.EQ, scanner.next());
		 checkToken(Kind.EQ, scanner.next());
		 checkToken(Kind.ASSIGN, scanner.next());
		 checkToken(Kind.EQ, scanner.next());
		 checkToken(Kind.ASSIGN, scanner.next());
		 checkEOF(scanner.next());
	 }
 
	 //Works
	 @Test
	 void stringLiterals1() throws LexicalException {
		 String input = """
				 "hello"
				 "\t"
				 "\\""
				 """;
		 IScanner scanner = CompilerComponentFactory.makeScanner(input);
		 checkString(input.substring(0, 7),"hello", new SourceLocation(1,1), scanner.next());
		 checkString(input.substring(8, 11), "\t", new SourceLocation(2,1), scanner.next());
		 checkString(input.substring(12, 16), "\"",  new SourceLocation(3,1), scanner.next());
		 checkEOF(scanner.next());
	 }
 
	 //Works
	 @Test
	 void stringLiterals2() throws LexicalException {
		 String input = """
				 "hello"
				 "\t"
				 """;
		 IScanner scanner = CompilerComponentFactory.makeScanner(input);
		 checkString(input.substring(0, 7),"hello", new SourceLocation(1,1), scanner.next());
		 checkString(input.substring(8, 11), "\t", new SourceLocation(2,1), scanner.next());
		 checkEOF(scanner.next());
	 }
 
	 //Works
	 @Test
	 void illegalEscape() throws LexicalException {
		 String input = """
				 "\\t"
				 "\\k"
				 """;
		 IScanner scanner = CompilerComponentFactory.makeScanner(input);
		 checkString("\"\\t\"","\t", new SourceLocation(1,1), scanner.next());
		 assertThrows(LexicalException.class, () -> {
			 scanner.next();
		 });
	 }
	 
	 @Test
	 void illegalLineTermInStringLiteral() throws LexicalException {
		 String input = """
				 "\\n"  ~ this one passes the escape sequence--it is OK
				 "\n"   ~ this on passes the LF, it is illegal.
				 """;
		 IScanner scanner = CompilerComponentFactory.makeScanner(input);
		 checkString("\"\\n\"","\n", new SourceLocation(1,1), scanner.next());
		 assertThrows(LexicalException.class, () -> {
			 scanner.next();
		 });
	 }
 
	 //Works
	 @Test
	 void lessThanGreaterThanExchange() throws LexicalException {
		 String input = """
				 <->>>>=
				 <<=<
				 """;
		 checkTokens(input, Kind.EXCHANGE, Kind.GT, Kind.GT, Kind.GE, Kind.LT, Kind.LE, Kind.LT, Kind.EOF);
	 }
 
	 //Works
	 @Test
	 void lessThanGreaterThanExchange2() throws LexicalException {
		 String input = """
				 <->>>
				 """;
		 checkTokens(input, Kind.EXCHANGE, Kind.GT, Kind.GT, Kind.EOF);
	 }
 
	 @Test
	 void andOrExp() throws LexicalException {
		 String input = """
				 ||&&**
				 *&&|||
				 """;
		 checkTokens(input, Kind.OR, Kind.AND, Kind.EXP, Kind.TIMES, Kind.AND, Kind.OR, Kind.BITOR, Kind.EOF);
	 }
 
	 // Works
	 /** The Scanner should not backtrack so this input should throw an exception */
	 @Test
	 void incompleteExchangeThrowsException() throws LexicalException {
		 String input = " <- ";
		 IScanner scanner = CompilerComponentFactory.makeScanner(input);
		 assertThrows(LexicalException.class, () -> {
			 scanner.next();
		 });	
	 }
 
	 @Test
	 void illegalChar() throws LexicalException {
		 String input = """
				 abc
				 @
				 """;
		 IScanner scanner = CompilerComponentFactory.makeScanner(input);
		 checkIdent("abc", scanner.next());
		 assertThrows(LexicalException.class, () -> {
			 @SuppressWarnings("unused")
			 IToken t = scanner.next();
		 });
	 }
 
	 //Works
	 @Test
	 void singleCharTokens() throws LexicalException{
		 String input = "+*00";
		 IScanner scanner =
				 CompilerComponentFactory.makeScanner(input);
		 checkToken(Kind.PLUS,scanner.next());
		 checkToken(Kind.TIMES,scanner.next());
		 checkToken(Kind.NUM_LIT,scanner.next());
		 checkToken(Kind.NUM_LIT,scanner.next());
		 checkEOF(scanner.next());
	 }
 
	 //Works
	 @Test
	 void equals() throws LexicalException{
		 String input = """
	 ==
	 == ==
	 ==*==
	 *==+
	 """;
		 IScanner scanner = CompilerComponentFactory.makeScanner(input);
		 checkToken(Kind.EQ,scanner.next());
		 checkToken(Kind.EQ,scanner.next());
		 checkToken(Kind.EQ,scanner.next());
		 checkToken(Kind.EQ,scanner.next());
		 checkToken(Kind.TIMES,scanner.next());
		 checkToken(Kind.EQ,scanner.next());
		 checkToken(Kind.TIMES,scanner.next());
		 checkToken(Kind.EQ,scanner.next());
		 checkToken(Kind.PLUS,scanner.next());
	 }
 
	 void checkNUM_LIT(String expectedChars, IToken t) {
		 checkToken(Kind.NUM_LIT,t);
		 assertEquals(expectedChars, t.getTokenString());
	 }
 
	 //Works
	 @Test
	 void numLits() throws LexicalException{
		 String input = """
	 123
	 05
	 240
	 1+2
	 """;
		 IScanner scanner = CompilerComponentFactory.makeScanner(input);
		 checkNUM_LIT("123",scanner.next());
		 checkNUM_LIT("0",scanner.next());
		 checkNUM_LIT("5",scanner.next());
		 checkNUM_LIT("240",scanner.next());
		 checkNUM_LIT("1",scanner.next());
		 checkToken(Kind.PLUS,scanner.next());
		 checkNUM_LIT("2",scanner.next());
	 }
 
	 @Test
	 void identifierTest() throws LexicalException {
		 String input = "T6_y_MX__6NRKZt t09 pS_ __0IHtYMoJ4629qz_3 d1hlp__QV O_f2W7z ____liu9mi a__Q5x88i9ac_i8449 \nSe_CG__U_bjrt BR_44y_0Yb_r h_k_3746X _ W6xSh3 _iMu_eny__hg__j V_u__LRTm_ AX_R_8sNy9_9G0iq__ \nb1Y C6_z_ _or5 Y_G4Oj3_ay Hth2w_q43_6__1tdA yl8WUL6qn_ aO_ \nfG__O_Wsy e_S2T \nG_6l __3_6__D_Y91admK q_R0GW_BwRK _g9z jmUMrkC1 \n";
		 IScanner scanner = CompilerComponentFactory.makeScanner(input);
		 checkToken(Kind.IDENT, "T6_y_MX__6NRKZt", new SourceLocation(1, 1), scanner.next());
		 checkToken(Kind.IDENT, "t09", new SourceLocation(1, 17), scanner.next());
	 }
 
	 @Test
	 void andIllegalCarriageReturn() throws LexicalException {
		 String input = """
			 "\\r" ~ legal
			 "\r" ~ illegal
			 """;
		 IScanner scanner = CompilerComponentFactory.makeScanner(input);
		 checkString("\"\\r\"", "\r", new SourceLocation(1, 1), scanner.next());
		 assertThrows(LexicalException.class, () -> {
			 scanner.next();
		 });
	 }
 
	 @Test
	 void stringContainBSlash() throws LexicalException {
		 String input = """
			 "\\ abc"
			 """;
		 IScanner scanner = CompilerComponentFactory.makeScanner(input);
		 assertThrows(LexicalException.class, () -> {
			 scanner.next();
		 });
	 }
 
	 @Test
	 void stringContainBSlash2() throws LexicalException {
		 String input = """
			 "abc \\"
			 """;
		 IScanner scanner = CompilerComponentFactory.makeScanner(input);
		 assertThrows(LexicalException.class, () -> {
			 scanner.next();
		 });
	 }
 
	 @Test
	 void stringContainBSlash3() throws LexicalException {
		 String input = """
			 "abc \\""
			 """;
		 IScanner scanner = CompilerComponentFactory.makeScanner(input);
		 checkString("abc \"", scanner.next());
	 }
 
	 @Test
	 void stringContainBSlash4() throws LexicalException {
		 String input = """
			 "abc \\""abc"
			 """;
		 IScanner scanner = CompilerComponentFactory.makeScanner(input);
		 checkString("abc \"", scanner.next());
		 checkIdent("abc", scanner.next());
		 assertThrows(LexicalException.class, () -> {
			 scanner.next();
		 });
		 checkEOF(scanner.next());
	 }
 }