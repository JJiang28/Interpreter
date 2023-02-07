package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.IToken.Kind;
import java.util.Arrays;

public class StringLitToken implements IStringLitToken{
	final Kind kind;
    final int pos;
    final int length;
    final char[] source;

	public StringLitToken(int pos_, int length_, char[] source_) {
        super();
		this.kind = Kind.STRING_LIT;
        this.pos = pos_;
        this.length = length_;
        this.source = source_;
	}

    public String getValue() {
		String temp = this.getTokenString();
        return temp.substring(1, temp.length()-1);
    }
    /**
	 * Returns a SourceLocation record containing the line and column number of this token.
	 * Both counts start numbering at 1.
	 * 
	 * @return Line number and column of this token.  
	 */
	public SourceLocation getSourceLocation() {
		return this.getSourceLocation();
	}
	
	/** Returns the kind of this Token
	 * 
	 * @return kind
	 */
	public Kind getKind() {
        return Kind.STRING_LIT;
    }
	
	/**
	 * Returns a char array containing the characters of this token.
	 * 
	 * @return
	 */
	public String getTokenString() {
		return new String(Arrays.copyOfRange(source, pos, pos + length));
	}
}
