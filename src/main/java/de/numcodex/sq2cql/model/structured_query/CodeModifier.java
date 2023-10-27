package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.Container;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.cql.*;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * @author Alexander Kiel
 */
public record CodeModifier(String path, List<String> codes) implements SimpleModifier {

    public CodeModifier {
        requireNonNull(path);
        codes = List.copyOf(codes);
    }

    public static CodeModifier of(String path, String... codes) {
        return new CodeModifier(path, codes == null ? List.of() : List.of(codes));
    }

    @Override
    public Container<BooleanExpression> expression(MappingContext mappingContext, IdentifierExpression sourceAlias) {
        var propertyExpr = InvocationExpression.of(sourceAlias, path);
        if (codes.size() == 1) {
            return Container.of(ComparatorExpression.equal(propertyExpr, StringLiteralExpression.of(codes.get(0))));
        } else {
            var list = ListSelector.of(codes.stream().map(StringLiteralExpression::of).toList());
            return Container.of(MembershipExpression.in(propertyExpr, list));
        }
    }
}
