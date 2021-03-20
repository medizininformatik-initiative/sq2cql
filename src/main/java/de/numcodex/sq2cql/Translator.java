package de.numcodex.sq2cql;

import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.cql.AndExpression;
import de.numcodex.sq2cql.model.cql.BooleanExpression;
import de.numcodex.sq2cql.model.cql.CodeSystemDefinition;
import de.numcodex.sq2cql.model.cql.ExpressionDefinition;
import de.numcodex.sq2cql.model.cql.IdentifierExpression;
import de.numcodex.sq2cql.model.cql.Library;
import de.numcodex.sq2cql.model.cql.NotExpression;
import de.numcodex.sq2cql.model.structured_query.Criterion;
import de.numcodex.sq2cql.model.structured_query.StructuredQuery;

import java.util.List;

/**
 * The translator from Structured Query to CQL.
 * <p>
 * It needs {@code mappings} and will produce a CQL {@link Library} by calling {@link #toCql(StructuredQuery) toCql}.
 * <p>
 * Instances are immutable and thread-safe.
 *
 * @author Alexander Kiel
 */
public final class Translator {

    public static final ExpressionDefinition IN_INITIAL_POPULATION = ExpressionDefinition
            .of("InInitialPopulation", AndExpression.of(IdentifierExpression.of("Inclusion"), NotExpression
                    .of(IdentifierExpression.of("Exclusion"))));

    private final MappingContext mappingContext;

    private Translator(MappingContext mappingContext) {
        this.mappingContext = mappingContext;
    }

    /**
     * Returns a translator without any mappings.
     *
     * @return a translator without any mappings
     */
    public static Translator of() {
        return new Translator(MappingContext.of());
    }

    /**
     * Returns a translator with mappings defined in {@code mappingContext}.
     *
     * @return a translator with mappings defined in {@code mappingContext}
     */
    public static Translator of(MappingContext mappingContext) {
        return new Translator(mappingContext);
    }

    private static Library inclusionOnlyLibrary(Container<BooleanExpression> inclusionExpr) {
        assert inclusionExpr.getExpression().isPresent();

        return Library.of(inclusionExpr.getCodeSystemDefinitions(),
                List.of(ExpressionDefinition.of("InInitialPopulation", inclusionExpr.getExpression().get())));
    }

    private static Library library(Container<BooleanExpression> inclusionExpr,
                                   Container<BooleanExpression> exclusionExpr) {
        assert inclusionExpr.getExpression().isPresent();
        assert exclusionExpr.getExpression().isPresent();

        return Library.of(Sets.union(inclusionExpr.getCodeSystemDefinitions(), exclusionExpr.getCodeSystemDefinitions()),
                List.of(ExpressionDefinition.of("Inclusion", inclusionExpr.getExpression().get()),
                        ExpressionDefinition.of("Exclusion", exclusionExpr.getExpression().get()),
                        IN_INITIAL_POPULATION));
    }

    /**
     * Translates {@code structuredQuery} into a CQL {@link Library}.
     *
     * @param structuredQuery the Structured Query to translate
     * @return the translated CQL {@link Library}
     */
    public Library toCql(StructuredQuery structuredQuery) {
        Container<BooleanExpression> inclusionExpr = inclusionExpr(structuredQuery.getInclusionCriteria());
        Container<BooleanExpression> exclusionExpr = exclusionExpr(structuredQuery.getExclusionCriteria());

        return exclusionExpr.isEmpty()
                ? inclusionOnlyLibrary(inclusionExpr)
                : library(inclusionExpr, exclusionExpr);
    }

    /**
     * Builds the inclusion expression as conjunctive normal form (CNF) of {@code criteria}.
     *
     * @param criteria a list of lists of {@link Criterion} representing a CNF
     * @return a {@link Container} of the boolean inclusion expression together with the used
     * {@link CodeSystemDefinition CodeSystemDefinitions}
     */
    private Container<BooleanExpression> inclusionExpr(List<List<Criterion>> criteria) {
        return criteria.stream().map(this::orExpr).reduce(Container.empty(), Container.AND);
    }

    private Container<BooleanExpression> orExpr(List<Criterion> criteria) {
        return criteria.stream().map(c -> c.toCql(mappingContext)).reduce(Container.empty(), Container.OR);
    }

    /**
     * Builds the exclusion expression as disjunctive normal form (DNF) of {@code criteria}.
     *
     * @param criteria a list of lists of {@link Criterion} representing a DNF
     * @return a {@link Container} of the boolean exclusion expression together with the used
     * {@link CodeSystemDefinition CodeSystemDefinitions}
     */
    private Container<BooleanExpression> exclusionExpr(List<List<Criterion>> criteria) {
        return criteria.stream().map(this::andExpr).reduce(Container.empty(), Container.OR);
    }

    private Container<BooleanExpression> andExpr(List<Criterion> criteria) {
        return criteria.stream().map(c -> c.toCql(mappingContext)).reduce(Container.empty(), Container.AND);
    }
}
