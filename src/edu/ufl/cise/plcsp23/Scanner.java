package edu.ufl.cise.plcsp23;
import java.util.Arrays;

public class Scanner implements IScanner {
	final String input;
    final char[] inputChars;
    int pos;
    char ch;

    public Scanner (String input) {
        this.input = input;
        this.inputChars = Arrays.copyOf(input.toCharArray(), input.length()+1);
        pos = 0;
        ch = inputChars[pos];
    }
}
