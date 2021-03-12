package com.codex.sq2cql;

/**
 * @author Alexander Kiel
 */
public final class PrintContext {

    public static final PrintContext ZERO = new PrintContext(0);

    private final int indent;

    private PrintContext(int indent) {
        this.indent = indent;
    }

    public String getIndent() {
        return " ".repeat(indent);
    }

    public PrintContext increase() {
        return new PrintContext(indent + 2);
    }
}
