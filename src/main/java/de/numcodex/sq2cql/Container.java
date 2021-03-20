package de.numcodex.sq2cql;

import de.numcodex.sq2cql.model.cql.AndExpression;
import de.numcodex.sq2cql.model.cql.BooleanExpression;
import de.numcodex.sq2cql.model.cql.CodeSystemDefinition;
import de.numcodex.sq2cql.model.cql.OrExpression;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;

/**
 * A container holds an expression together with the code system definitions it uses.
 * <p>
 * Containers can be {@link #combiner combined}, collecting all code system definitions the individual contains use.
 * <p>
 * Instances are immutable.
 *
 * @author Alexander Kiel
 */
public final class Container<T> {

    private static final Container<?> EMPTY = new Container<>(null, Set.of());
    public static final BinaryOperator<Container<BooleanExpression>> AND = combiner(AndExpression::of);
    public static final BinaryOperator<Container<BooleanExpression>> OR = combiner(OrExpression::of);
    private final T expression;
    private final Set<CodeSystemDefinition> codeSystemDefinitions;

    private Container(T expression, Set<CodeSystemDefinition> codeSystemDefinitions) {
        this.expression = expression;
        this.codeSystemDefinitions = codeSystemDefinitions;
    }

    /**
     * Returns the empty container that contains no expression and no code system definitions.
     * <p>
     * The empty container is the identity element of the binary {@link #combiner combine} operation.
     *
     * @param <T> the type of the expression
     * @return the empty container
     */
    public static <T> Container<T> empty() {
        @SuppressWarnings("unchecked")
        Container<T> empty = (Container<T>) EMPTY;
        return empty;
    }

    /**
     * Returns a container holding {@code expression} and {@code codeSystemDefinitions}.
     *
     * @param expression            the expression being hold
     * @param codeSystemDefinitions the code system definition being hold
     * @param <T>                   the type of the expression
     * @return a container
     * @throws NullPointerException if {@code expression} is null
     */
    public static <T> Container<T> of(T expression, CodeSystemDefinition... codeSystemDefinitions) {
        return new Container<>(Objects.requireNonNull(expression), Set.of(codeSystemDefinitions));
    }

    /**
     * Returns a binary operator that combines expressions using {@code combiner} and the code system definitions with
     * set union.
     * <p>
     * Because the {@link #empty() empty conatainer} is the identity element of the returned operator {@code op}, the
     * following holds: {@code op.apply(empty, x) == x} and {@code op.apply(x, empty) == x}.
     *
     * @param combiner the binary operator to combine the expressions of both containers
     * @param <T>      the type of both expressions
     * @return a container holding the combined expression and the union of the code system definitions of both
     * containers
     * @throws NullPointerException if {@code combiner} is null
     */
    public static <T> BinaryOperator<Container<T>> combiner(BinaryOperator<T> combiner) {
        Objects.requireNonNull(combiner);
        return (a, b) -> a == EMPTY
                ? b : b == EMPTY ? a : new Container<>(combiner.apply(a.expression, b.expression),
                Sets.union(a.codeSystemDefinitions, b.codeSystemDefinitions));
    }

    /**
     * Returns the expression the container holds.
     *
     * @return the expression or {@link Optional#empty()} iff this container is {@link #isEmpty() empty}.
     */
    public Optional<T> getExpression() {
        return Optional.ofNullable(expression);
    }

    /**
     * Returns the code system definitions the container holds.
     *
     * @return the code system definitions the container holds
     */
    public Set<CodeSystemDefinition> getCodeSystemDefinitions() {
        return codeSystemDefinitions;
    }

    /**
     * Returns {@code true} iff this container is empty.
     *
     * @return {@code true} iff this container is empty
     */
    public boolean isEmpty() {
        return expression == null;
    }

    public <U> Container<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        return isEmpty() ? empty() : new Container<>(Objects.requireNonNull(mapper.apply(expression)),
                codeSystemDefinitions);
    }

    public <U> Container<U> flatMap(Function<? super T, ? extends Container<? extends U>> mapper) {
        Objects.requireNonNull(mapper);
        if (isEmpty()) {
            return empty();
        }
        Container<? extends U> container = mapper.apply(expression);
        if (container.isEmpty()) {
            return empty();
        }
        assert container.getExpression().isPresent();
        return new Container<>(Objects.requireNonNull(container.getExpression().get()),
                Sets.union(codeSystemDefinitions, container.getCodeSystemDefinitions()));
    }
}
