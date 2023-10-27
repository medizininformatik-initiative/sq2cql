package de.numcodex.sq2cql;

import de.numcodex.sq2cql.model.cql.Clause;
import de.numcodex.sq2cql.model.cql.Expression;

/**
 * @author Alexander Kiel
 */
public record PrintContext(int indent, int precedence) {

    public static final PrintContext ZERO = new PrintContext(0, 0);

    public String getIndent() {
        return " ".repeat(indent);
    }

    public String parenthesizeZero(String s) {
        return parenthesize(0, s);
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

    /**
     * Sets the {@link #precedence} to zero and keeps the {@link #indent}.
     *
     * @return a new {@code PrintContext} with a {@code precedence} of zero and an {@code indent} of this {@code PrintContext}
     */
    public PrintContext resetPrecedence() {
        return new PrintContext(indent, 0);
    }

    public String print(Expression expression) {
        return expression.print(this);
    }

    public String print(Clause clause) {
        return clause.print(this);
    }

    public String print(Container<? extends Expression> container) {
        return container.getExpression().map(this::print).orElse("");
    }
}
