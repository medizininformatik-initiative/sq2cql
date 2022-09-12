package de.numcodex.sq2cql.model.structured_query;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.numcodex.sq2cql.PrintContext;
import de.numcodex.sq2cql.model.AttributeMapping;
import de.numcodex.sq2cql.model.Mapping;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.CodeSystemDefinition;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static de.numcodex.sq2cql.model.common.Comparator.EQUAL;
import static de.numcodex.sq2cql.model.common.Comparator.GREATER_THAN;
import static de.numcodex.sq2cql.model.common.Comparator.LESS_THAN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Alexander Kiel
 */
class NumericCriterionTest {

    static final TermCode PLATELETS = TermCode.of("http://loinc.org", "26515-7", "Platelets");
    static final TermCode SOFA_SCORE = TermCode.of("https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/ecrf-parameter-codes", "06", "SOFA-Score");
    static final TermCode OTHER_VALUE_PATH = TermCode.of("foo", "other-value-path", "");
    static final TermCode STATUS = TermCode.of("http://hl7.org/fhir", "observation-status", "observation-status");
    static final TermCode FINAL = TermCode.of("http://hl7.org/fhir/observation-status", "final", "final");
    static final TermCode AGE = TermCode.of("http://snomed.info/sct", "424144002", "Current chronological age (observable entity)");

    static final Map<String, String> CODE_SYSTEM_ALIASES = Map.of(
            "http://loinc.org", "loinc",
            "http://snomed.info/sct", "snomed",
            "https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/ecrf-parameter-codes", "ecrf",
            "foo", "foo");

    static final MappingContext MAPPING_CONTEXT = MappingContext.of(Map.of(
            PLATELETS, Mapping.of(PLATELETS, "Observation", "value"),
            SOFA_SCORE, Mapping.of(SOFA_SCORE, "Observation", "value", null, List.of(),
                    List.of(AttributeMapping.of("code", STATUS, "status"))),
            OTHER_VALUE_PATH, Mapping.of(OTHER_VALUE_PATH, "Observation", "other")
    ), null, CODE_SYSTEM_ALIASES);

    static final CodeSystemDefinition LOINC_CODE_SYSTEM_DEF = CodeSystemDefinition.of("loinc", "http://loinc.org");
    static final CodeSystemDefinition ECRF_CODE_SYSTEM_DEF = CodeSystemDefinition.of("ecrf", "https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/ecrf-parameter-codes");
    static final CodeSystemDefinition FOO_CODE_SYSTEM_DEF = CodeSystemDefinition.of("foo", "foo");

    @Test
    void fromJson() throws Exception {
        var mapper = new ObjectMapper();

        var criterion = (NumericCriterion) mapper.readValue("""
                {
                  "termCodes": [{
                    "system": "http://loinc.org",
                    "code": "26515-7",
                    "display": "Platelets"
                  }],
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

        assertEquals(Concept.of(PLATELETS), criterion.getConcept());
        assertEquals(GREATER_THAN, criterion.getComparator());
        assertEquals(BigDecimal.valueOf(50), criterion.getValue());
        assertEquals(Optional.of("g/dl"), criterion.getUnit());
    }

    @Test
    void fromJson_withoutUnit() throws Exception {
        var mapper = new ObjectMapper();

        var criterion = (NumericCriterion) mapper.readValue("""
                {
                  "termCodes": [{
                    "system": "https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/ecrf-parameter-codes",
                    "code": "06",
                    "display": "SOFA-Score"
                  }],
                  "valueFilter": {
                    "type": "quantity-comparator",
                    "comparator": "eq",
                    "value": 6
                  }
                }
                """, Criterion.class);

        assertEquals(Concept.of(SOFA_SCORE), criterion.getConcept());
        assertEquals(EQUAL, criterion.getComparator());
        assertEquals(BigDecimal.valueOf(6), criterion.getValue());
        assertEquals(Optional.empty(), criterion.getUnit());
    }

    @Test
    void toCql() {
        var criterion = NumericCriterion.of(Concept.of(PLATELETS), LESS_THAN, BigDecimal.valueOf(50), "g/dl");

        var container = criterion.toCql(MAPPING_CONTEXT);

        assertEquals("""
                        exists (from [Observation: Code '26515-7' from loinc] O
                          where O.value as Quantity < 50 'g/dl')""",
                PrintContext.ZERO.print(container));
        assertEquals(Set.of(LOINC_CODE_SYSTEM_DEF), container.getCodeSystemDefinitions());
    }

    @Test
    void toCql_WithoutUnit() {
        var criterion = NumericCriterion.of(Concept.of(SOFA_SCORE), EQUAL, BigDecimal.valueOf(6));

        var container = criterion.toCql(MAPPING_CONTEXT);

        assertEquals("""
                        exists (from [Observation: Code '06' from ecrf] O
                          where O.value as Quantity = 6)""",
                PrintContext.ZERO.print(container));
        assertEquals(Set.of(ECRF_CODE_SYSTEM_DEF), container.getCodeSystemDefinitions());
    }

    @Test
    void toCql_WithOtherFhirValuePath() {
        var criterion = NumericCriterion.of(Concept.of(OTHER_VALUE_PATH), EQUAL, BigDecimal.valueOf(1));

        var container = criterion.toCql(MAPPING_CONTEXT);

        assertEquals("""
                        exists (from [Observation: Code 'other-value-path' from foo] O
                          where O.other as Quantity = 1)""",
                PrintContext.ZERO.print(container));
        assertEquals(Set.of(FOO_CODE_SYSTEM_DEF), container.getCodeSystemDefinitions());
    }

    @Test
    void toCql_WithStatusAttributeFilter() {
        var criterion = NumericCriterion.of(Concept.of(SOFA_SCORE), EQUAL, BigDecimal.valueOf(6),
                ValueSetAttributeFilter.of(STATUS, FINAL));

        var container = criterion.toCql(MAPPING_CONTEXT);

        assertEquals("""
                        exists (from [Observation: Code '06' from ecrf] O
                          where O.value as Quantity = 6 and
                            O.status = 'final')""",
                PrintContext.ZERO.print(container));
        assertEquals(Set.of(ECRF_CODE_SYSTEM_DEF), container.getCodeSystemDefinitions());
    }

    @Test
    void toCql_WithFixedCriteria() {
        var criterion = NumericCriterion.of(Concept.of(SOFA_SCORE), EQUAL, BigDecimal.valueOf(6));
        var mappingContext = MappingContext.of(Map.of(
                SOFA_SCORE, Mapping.of(SOFA_SCORE, "Observation", "value", null, List.of(CodeModifier.of("status", "final")),
                        List.of())
        ), null, CODE_SYSTEM_ALIASES);

        var container = criterion.toCql(mappingContext);

        assertEquals("""
                        exists (from [Observation: Code '06' from ecrf] O
                          where O.value as Quantity = 6 and
                            O.status = 'final')""",
                PrintContext.ZERO.print(container));
        assertEquals(Set.of(ECRF_CODE_SYSTEM_DEF), container.getCodeSystemDefinitions());
    }

    @Test
    void toCql_WithValueOnPatient() {
        var criterion = NumericCriterion.of(Concept.of(AGE), EQUAL, BigDecimal.valueOf(16), "a");
        var mappingContext = MappingContext.of(Map.of(
                AGE, Mapping.of(AGE, "Patient", "extension.where(url='https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/age').extension.where(url='age').value.first()")
        ), null, CODE_SYSTEM_ALIASES);

        var container = criterion.toCql(mappingContext);

        assertEquals("""
                        Patient.extension.where(url='https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/age').extension.where(url='age').value.first() as Quantity = 16 'a'""",
                PrintContext.ZERO.print(container));
        assertTrue(container.getCodeSystemDefinitions().isEmpty());
    }
}
