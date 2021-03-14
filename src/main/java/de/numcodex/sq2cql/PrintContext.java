package de.numcodex.sq2cql;

/**
 * @author Alexander Kiel
 */
public final class PrintContext {

    public static final PrintContext ZERO = new PrintContext(0, 0);

    private final int indent;
    private final int precedence;

    private PrintContext(int indent, int precedence) {
        this.indent = indent;
        this.precedence = precedence;
    }

    public String getIndent() {
        return " ".repeat(indent);
    }

    public String parenthesize(int precedence, String s) {
        return precedence < this.precedence ? "(%s)".formatted(s) : s;
    }

    public PrintContext increase() {
        return new PrintContext(indent + 2, precedence);
    }

    public PrintContext withPrecedence(int precedence) {
        return new PrintContext(indent, precedence);
    }

    public PrintContext resetPrecedence() {
        return new PrintContext(indent, 0);
    }
}
