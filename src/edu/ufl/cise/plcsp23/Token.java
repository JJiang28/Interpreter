package edu.ufl.cise.plcsp23;

import java.util.Arrays;

public class Token implements IToken {
    final Kind kind;
    final int pos;
    final int length;
    final char[] source;

    public Token(Kind kind_, int pos_, int length_, char[] source_) {
        super();
        this.kind = kind_;
        this.pos = pos_;
        this.length = length_;
        this.source = source_;
    }

    @Override
    public Kind getKind() {
        return this.kind;
    }

    @Override
    public String getTokenString() {
		return new String(Arrays.copyOfRange(source, pos, length));
    }

    @Override
    public SourceLocation getSourceLocation() {
        return this.getSourceLocation();
    }
}