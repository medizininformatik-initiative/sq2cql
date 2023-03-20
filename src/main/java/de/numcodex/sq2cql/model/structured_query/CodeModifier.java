package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.Container;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.cql.BooleanExpression;
import de.numcodex.sq2cql.model.cql.ComparatorExpression;
import de.numcodex.sq2cql.model.cql.IdentifierExpression;
import de.numcodex.sq2cql.model.cql.InvocationExpression;
import de.numcodex.sq2cql.model.cql.ListSelector;
import de.numcodex.sq2cql.model.cql.MembershipExpression;
import de.numcodex.sq2cql.model.cql.StringLiteralExpression;

import java.util.List;
import java.util.Objects;

import static de.numcodex.sq2cql.model.common.Comparator.EQUAL;

/**
 * @author Alexander Kiel
 */
public final class CodeModifier extends AbstractModifier {

    private final List<String> codes;

    private CodeModifier(String path, List<String> codes) {
        super(path);
        this.codes = codes;
    }

    public static CodeModifier of(String path, String... codes) {
        return new CodeModifier(path, codes == null ? List.of() : List.of(codes));
    }

    @Override
    public Container<BooleanExpression> expression(MappingContext mappingContext, IdentifierExpression identifier) {
        var propertyExpr = InvocationExpression.of(identifier, path);
        if (codes.size() == 1) {
            return Container.of(ComparatorExpression.of(propertyExpr, EQUAL, StringLiteralExpression.of(codes.get(0))));
        } else {
            var list = ListSelector.of(codes.stream().map(StringLiteralExpression::of).toList());
            return Container.of(MembershipExpression.in(propertyExpr, list));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CodeModifier that = (CodeModifier) o;
        return path.equals(that.path) && codes.equals(that.codes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, codes);
    }
}
