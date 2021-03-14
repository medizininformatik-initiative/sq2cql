package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.Container;
import de.numcodex.sq2cql.model.Mapping;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.BooleanExpression;
import de.numcodex.sq2cql.model.cql.CodeSystemDefinition;
import de.numcodex.sq2cql.PrintContext;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Alexander Kiel
 */
class ConceptCriterionTest {

    public static final TermCode NEOPLASM = TermCode.of("http://fhir.de/CodeSystem/dimdi/icd-10-gm", "C71",
            "Malignant neoplasm of brain");

    private final static Map<String, String> CODE_SYSTEM_ALIASES = Map.of(
            "http://fhir.de/CodeSystem/dimdi/icd-10-gm", "icd10");

    public static final MappingContext MAPPING_CONTEXT = MappingContext.of(Map.of(NEOPLASM,
            Mapping.of(NEOPLASM, "Observation")), CODE_SYSTEM_ALIASES);

    public static final CodeSystemDefinition ICD10_CODE_SYSTEM_DEF = CodeSystemDefinition.of("icd10", "http://fhir.de/CodeSystem/dimdi/icd-10-gm");

    @Test
    void toCql() {
        Criterion criterion = ConceptCriterion.of(NEOPLASM);

        Container<BooleanExpression> container = criterion.toCql(MAPPING_CONTEXT);

        assertEquals("exists([Observation: Code 'C71' from icd10])", container.getExpression().map(e -> e.print(PrintContext.ZERO))
                .orElse(""));
        assertEquals(Set.of(ICD10_CODE_SYSTEM_DEF), container.getCodeSystemDefinitions());
    }
}
