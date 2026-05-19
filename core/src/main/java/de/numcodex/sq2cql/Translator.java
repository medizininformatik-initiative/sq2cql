package de.numcodex.sq2cql;

import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.cql.CodeSystemDefinition;
import de.numcodex.sq2cql.model.cql.Container;
import de.numcodex.sq2cql.model.cql.DefaultExpression;
import de.numcodex.sq2cql.model.structured_query.Criterion;
import de.numcodex.sq2cql.model.structured_query.StructuredQuery;
import de.numcodex.sq2cql.model.structured_query.TranslationException;

import java.util.List;

import static de.numcodex.sq2cql.model.cql.Container.AND;
import static de.numcodex.sq2cql.model.cql.Container.AND_NOT;
import static java.util.Objects.requireNonNull;

/**
 * The translator from Structured Query to CQL.
 * <p>
 * It needs {@code mappings} and will produce a CQL {@link Container} by calling {@link #toCql(StructuredQuery) toCql}.
 * <p>
 * Instances are immutable and thread-safe.
 *
 * @author Alexander Kiel
 */
public class Translator {

    private final MappingContext mappingContext;

    private Translator(MappingContext mappingContext) {
        this.mappingContext = requireNonNull(mappingContext);
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

    /**
     * Translates the given {@code structuredQuery} into a CQL {@link Container}.
     *
     * @param structuredQuery the Structured Query to translate
     * @return the translated CQL {@link Container}
     * @throws TranslationException if the given {@code structuredQuery} can't be translated into a
     *                              CQL {@link Container}
     */
    public Container<DefaultExpression> toCql(StructuredQuery structuredQuery) {
        var inclusionExpr = inclusionExpr(structuredQuery.inclusionCriteria());
        var exclusionExpr = exclusionExpr(structuredQuery.exclusionCriteria());

        return exclusionExpr.isEmpty()
                ? inclusionExpr.moveToPatientContext("InInitialPopulation")
                : AND_NOT.apply(inclusionExpr.moveToPatientContext("Inclusion"),
                        exclusionExpr.moveToPatientContext("Exclusion"))
                .moveToPatientContext("InInitialPopulation");
    }

    /**
     * Builds the inclusion expression as conjunctive normal form (CNF) of {@code criteria}.
     *
     * @param criteria a list of lists of {@link Criterion} representing a CNF
     * @return a {@link Container} of the boolean inclusion expression together with the used {@link
     * CodeSystemDefinition CodeSystemDefinitions}
     */
    private Container<DefaultExpression> inclusionExpr(List<List<Criterion>> criteria) {
        return criteria.stream().map(this::orExpr).reduce(Container.empty(), AND);
    }

    private Container<DefaultExpression> orExpr(List<Criterion> criteria) {
        return criteria.stream().map(c -> c.toCql(mappingContext)).reduce(Container.empty(), Container.OR);
    }

    /**
     * Builds the exclusion expression as disjunctive normal form (DNF) of {@code criteria}.
     *
     * @param criteria a list of lists of {@link Criterion} representing a DNF
     * @return a {@link Container} of the boolean exclusion expression together with the used {@link
     * CodeSystemDefinition CodeSystemDefinitions}
     */
    private Container<DefaultExpression> exclusionExpr(List<List<Criterion>> criteria) {
        return criteria.stream().map(this::andExpr).reduce(Container.empty(), Container.OR);
    }

    private Container<DefaultExpression> andExpr(List<Criterion> criteria) {
        return criteria.stream().map(c -> c.toCql(mappingContext))
                .reduce(Container.empty(), AND);
    }
}
