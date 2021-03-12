package com.codex.sq2cql.model.structured_query;

import com.codex.sq2cql.Container;
import com.codex.sq2cql.model.Mapping;
import com.codex.sq2cql.model.MappingContext;
import com.codex.sq2cql.model.common.TermCode;
import com.codex.sq2cql.model.cql.BooleanExpression;
import com.codex.sq2cql.model.cql.CodeSystemDefinition;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import static com.codex.sq2cql.PrintContext.ZERO;
import static com.codex.sq2cql.model.common.Comparator.LESS_THAN;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Alexander Kiel
 */
class NumericCriterionTest {

    public static final TermCode PLATELETS = TermCode.of("http://loinc.org", "26515-7", "Platelets");

    private final static Map<String, String> CODE_SYSTEM_ALIASES = Map.of(
            "http://loinc.org", "loinc");

    public static final MappingContext MAPPING_CONTEXT = MappingContext.of(Map.of(PLATELETS,
            Mapping.of(PLATELETS, "Observation")), CODE_SYSTEM_ALIASES);

    public static final CodeSystemDefinition LOINC_CODE_SYSTEM_DEF = CodeSystemDefinition.of("loinc", "http://loinc.org");

    @Test
    void toCql() {
        Criterion criterion = NumericCriterion.of(PLATELETS, LESS_THAN, BigDecimal.valueOf(50), "g/dl");

        Container<BooleanExpression> container = criterion.toCql(MAPPING_CONTEXT);

        assertEquals("""
                        exists(from [Observation: Code '26515-7' from loinc] O
                          where (O.value as Quantity) < (50 'g/dl'))""",
                container.getExpression().map(e -> e.print(ZERO)).orElse(""));
        assertEquals(Set.of(LOINC_CODE_SYSTEM_DEF), container.getCodeSystemDefinitions());
    }
}
