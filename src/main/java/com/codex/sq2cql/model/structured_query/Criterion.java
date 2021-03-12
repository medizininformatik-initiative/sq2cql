package com.codex.sq2cql.model.structured_query;

import com.codex.sq2cql.Container;
import com.codex.sq2cql.model.MappingContext;
import com.codex.sq2cql.model.cql.BooleanExpression;
import com.codex.sq2cql.model.cql.CodeSystemDefinition;

/**
 * A single, atomic criterion in Structured Query.
 *
 * @author Alexander Kiel
 */
public interface Criterion {

    /**
     * A criterion that always evaluates to {@code true}.
     */
    Criterion TRUE = mappingContext -> Container.of(BooleanExpression.TRUE);

    /**
     * A criterion that always evaluates to {@code false}.
     */
    Criterion FALSE = mappingContext -> Container.of(BooleanExpression.FALSE);

    /**
     * Translates this criterion into a CQL expression.
     *
     * @param mappingContext contains the mappings needed to create the CQL expression
     * @return a {@link Container} of the CQL expression together with its used {@link CodeSystemDefinition
     * CodeSystemDefinitions}
     */
    Container<BooleanExpression> toCql(MappingContext mappingContext);
}
