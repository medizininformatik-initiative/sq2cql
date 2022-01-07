package de.numcodex.sq2cql.model.structured_query;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.numcodex.sq2cql.Container;
import de.numcodex.sq2cql.PrintContext;
import de.numcodex.sq2cql.model.ConceptNode;
import de.numcodex.sq2cql.model.Mapping;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.BooleanExpression;
import de.numcodex.sq2cql.model.cql.CodeSystemDefinition;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static de.numcodex.sq2cql.model.common.Comparator.EQUAL;
import static de.numcodex.sq2cql.model.common.Comparator.GREATER_THAN;
import static de.numcodex.sq2cql.model.common.Comparator.LESS_THAN;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Alexander Kiel
 */
class NumericCriterionTest {

    public static final TermCode PLATELETS = TermCode.of("http://loinc.org", "26515-7", "Platelets");
    public static final TermCode SOFA_SCORE = TermCode.of("https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/ecrf-parameter-codes", "06", "SOFA-Score");
    public static final TermCode OTHER_VALUE_PATH = TermCode.of("foo", "other-value-path", "");

    public static final Map<String, String> CODE_SYSTEM_ALIASES = Map.of(
            "http://loinc.org", "loinc",
            "https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/ecrf-parameter-codes", "ecrf",
            "foo", "foo");

    public static final MappingContext MAPPING_CONTEXT = MappingContext.of(Map.of(
            PLATELETS, Mapping.of(PLATELETS, "Observation", "value"),
            SOFA_SCORE, Mapping.of(SOFA_SCORE, "Observation", "value"),
            OTHER_VALUE_PATH, Mapping.of(OTHER_VALUE_PATH, "Observation", "other")
    ), ConceptNode.of(), CODE_SYSTEM_ALIASES);

    public static final CodeSystemDefinition LOINC_CODE_SYSTEM_DEF = CodeSystemDefinition.of("loinc", "http://loinc.org");
    public static final CodeSystemDefinition ECRF_CODE_SYSTEM_DEF = CodeSystemDefinition.of("ecrf", "https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/ecrf-parameter-codes");
    public static final CodeSystemDefinition FOO_CODE_SYSTEM_DEF = CodeSystemDefinition.of("foo", "foo");

    @Test
    void fromJson() throws Exception {
        var mapper = new ObjectMapper();

        var criterion = (NumericCriterion) mapper.readValue("""
                {
                  "termCode": {
                    "system": "http://loinc.org",
                    "code": "26515-7",
                    "display": "Platelets"
                  },
                  "valueFilter": {
                    "type": "quantity-comparator",
                    "comparator": "gt",
                    "unit": {
                      "code": "g/dl"
                    },
                    "value": 50
                  }
                }
                """, Criterion.class);

        assertEquals(PLATELETS, criterion.getTermCode());
        assertEquals(GREATER_THAN, criterion.getComparator());
        assertEquals(BigDecimal.valueOf(50), criterion.getValue());
        assertEquals(Optional.of("g/dl"), criterion.getUnit());
    }

    @Test
    void fromJson_withoutUnit() throws Exception {
        var mapper = new ObjectMapper();

        var criterion = (NumericCriterion) mapper.readValue("""
                {
                  "termCode": {
                    "system": "https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/ecrf-parameter-codes",
                    "code": "06",
                    "display": "SOFA-Score"
                  },
                  "valueFilter": {
                    "type": "quantity-comparator",
                    "comparator": "eq",
                    "value": 6
                  }
                }
                """, Criterion.class);

        assertEquals(SOFA_SCORE, criterion.getTermCode());
        assertEquals(EQUAL, criterion.getComparator());
        assertEquals(BigDecimal.valueOf(6), criterion.getValue());
        assertEquals(Optional.empty(), criterion.getUnit());
    }

    @Test
    void toCql() {
        Criterion criterion = NumericCriterion.of(PLATELETS, LESS_THAN, BigDecimal.valueOf(50), "g/dl");

        Container<BooleanExpression> container = criterion.toCql(MAPPING_CONTEXT);

        assertEquals("""
                        exists from [Observation: Code '26515-7' from loinc] O
                          where O.value as Quantity < 50 'g/dl'""",
                container.getExpression().map(e -> e.print(PrintContext.ZERO)).orElse(""));
        assertEquals(Set.of(LOINC_CODE_SYSTEM_DEF), container.getCodeSystemDefinitions());
    }

    @Test
    void toCql_withoutUnit() {
        Criterion criterion = NumericCriterion.of(SOFA_SCORE, EQUAL, BigDecimal.valueOf(6));

        Container<BooleanExpression> container = criterion.toCql(MAPPING_CONTEXT);

        assertEquals("""
                        exists from [Observation: Code '06' from ecrf] O
                          where O.value as Quantity = 6""",
                container.getExpression().map(e -> e.print(PrintContext.ZERO)).orElse(""));
        assertEquals(Set.of(ECRF_CODE_SYSTEM_DEF), container.getCodeSystemDefinitions());
    }

    @Test
    void toCql_otherFhirValuePath() {
        Criterion criterion = NumericCriterion.of(OTHER_VALUE_PATH, EQUAL, BigDecimal.valueOf(1));

        Container<BooleanExpression> container = criterion.toCql(MAPPING_CONTEXT);

        assertEquals("""
                        exists from [Observation: Code 'other-value-path' from foo] O
                          where O.other as Quantity = 1""",
                container.getExpression().map(e -> e.print(PrintContext.ZERO)).orElse(""));
        assertEquals(Set.of(FOO_CODE_SYSTEM_DEF), container.getCodeSystemDefinitions());
    }
}
