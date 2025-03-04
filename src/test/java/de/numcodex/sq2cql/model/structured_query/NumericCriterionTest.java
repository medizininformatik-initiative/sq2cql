package de.numcodex.sq2cql.model.structured_query;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.numcodex.sq2cql.model.AttributeMapping;
import de.numcodex.sq2cql.model.Mapping;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.common.TermCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.numcodex.sq2cql.Assertions.assertThat;
import static de.numcodex.sq2cql.model.common.Comparator.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Alexander Kiel
 */
class NumericCriterionTest {

    static final TermCode CONTEXT = TermCode.of("context", "context", "context");
    static final ContextualTermCode PLATELETS = ContextualTermCode.of(CONTEXT, TermCode.of("http://loinc.org", "26515-7", "Platelets"));
    static final ContextualTermCode SOFA_SCORE = ContextualTermCode.of(CONTEXT, TermCode.of("https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/ecrf-parameter-codes", "06", "SOFA-Score"));
    static final ContextualTermCode OTHER_VALUE_PATH = ContextualTermCode.of(CONTEXT, TermCode.of("foo", "other-value-path", ""));
    static final MappingContext MAPPING_CONTEXT;
    static final Map<String, String> CODE_SYSTEM_ALIASES = Map.of(
            "http://loinc.org", "loinc",
            "http://snomed.info/sct", "snomed",
            "https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/ecrf-parameter-codes", "ecrf",
            "foo", "foo");
    static final TermCode STATUS = TermCode.of("http://hl7.org/fhir", "observation-status", "observation-status");
    static final TermCode FINAL = TermCode.of("http://hl7.org/fhir/observation-status", "final", "final");
    static final ContextualTermCode AGE = ContextualTermCode.of(CONTEXT, TermCode.of("http://snomed.info/sct", "424144002", "Current chronological age (observable entity)"));

    static {
        MAPPING_CONTEXT = MappingContext.of(Map.of(
                PLATELETS, Mapping.of(PLATELETS, "Observation", Mapping.PathMapping.of("value", Mapping.PathMapping.Type.QUANTITY)),
                SOFA_SCORE, Mapping.of(SOFA_SCORE, "Observation", Mapping.PathMapping.of("value", Mapping.PathMapping.Type.QUANTITY), List.of(),
                        List.of(AttributeMapping.of(List.of("code"), STATUS, "status"))),
                OTHER_VALUE_PATH, Mapping.of(OTHER_VALUE_PATH, "Observation", Mapping.PathMapping.of("other", Mapping.PathMapping.Type.QUANTITY))
        ), null, CODE_SYSTEM_ALIASES);
    }

    @Test
    void fromJson() throws Exception {
        var mapper = new ObjectMapper();

        var criterion = (NumericCriterion) mapper.readValue("""
                {
                  "context": {
                    "system": "context",
                    "code": "context",
                    "display": "context"
                  },
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

        assertEquals(ContextualConcept.of(PLATELETS), criterion.getConcept());
        assertEquals(GREATER_THAN, criterion.getComparator());
        assertEquals(BigDecimal.valueOf(50), criterion.getValue());
        assertEquals(Optional.of("g/dl"), criterion.getUnit());
    }

    @Test
    void fromJson_withoutUnit() throws Exception {
        var mapper = new ObjectMapper();

        var criterion = (NumericCriterion) mapper.readValue("""
                {
                  "context": {
                    "system": "context",
                    "code": "context",
                    "display": "context"
                  },
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

        assertEquals(ContextualConcept.of(SOFA_SCORE), criterion.getConcept());
        assertEquals(EQUAL, criterion.getComparator());
        assertEquals(BigDecimal.valueOf(6), criterion.getValue());
        assertEquals(Optional.empty(), criterion.getUnit());
    }

    @Test
    void toCql() {
        var criterion = NumericCriterion.of(ContextualConcept.of(PLATELETS), LESS_THAN, BigDecimal.valueOf(50), "g/dl");

        var container = criterion.toCql(MAPPING_CONTEXT);

        assertThat(container).printsTo("""
                library Retrieve version '1.0.0'
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                
                codesystem loinc: 'http://loinc.org'
                
                context Patient
                
                define Criterion:
                  exists (from [Observation: Code '26515-7' from loinc] O
                    where O.value as Quantity < 50 'g/dl')
                """);
    }

    @Test
    void toCql_WithoutUnit() {
        var criterion = NumericCriterion.of(ContextualConcept.of(SOFA_SCORE), EQUAL, BigDecimal.valueOf(6));

        var container = criterion.toCql(MAPPING_CONTEXT);

        assertThat(container).printsTo("""
                library Retrieve version '1.0.0'
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                
                codesystem ecrf: 'https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/ecrf-parameter-codes'
                
                context Patient
                
                define Criterion:
                  exists (from [Observation: Code '06' from ecrf] O
                    where O.value as Quantity = 6)
                """);
    }

    @Test
    void toCql_WithOtherFhirValuePath() {
        var criterion = NumericCriterion.of(ContextualConcept.of(OTHER_VALUE_PATH), EQUAL, BigDecimal.valueOf(1));

        var container = criterion.toCql(MAPPING_CONTEXT);

        assertThat(container).printsTo("""
                library Retrieve version '1.0.0'
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                
                codesystem foo: 'foo'
                
                context Patient
                
                define Criterion:
                  exists (from [Observation: Code 'other-value-path' from foo] O
                    where O.other as Quantity = 1)
                """);
    }

    @Test
    void toCql_WithStatusAttributeFilter() {
        var criterion = NumericCriterion.of(ContextualConcept.of(SOFA_SCORE), EQUAL, BigDecimal.valueOf(6))
                .appendAttributeFilter(ValueSetAttributeFilter.of(STATUS, FINAL));

        var container = criterion.toCql(MAPPING_CONTEXT);

        assertThat(container).printsTo("""
                library Retrieve version '1.0.0'
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                
                codesystem ecrf: 'https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/ecrf-parameter-codes'
                
                context Patient
                
                define Criterion:
                  exists (from [Observation: Code '06' from ecrf] O
                    where O.value as Quantity = 6 and
                      O.status = 'final')
                """);
    }

    @Test
    void toCql_WithFixedCriteria() {
        var criterion = NumericCriterion.of(ContextualConcept.of(SOFA_SCORE), EQUAL, BigDecimal.valueOf(6));
        var mappingContext = MappingContext.of(Map.of(
                SOFA_SCORE, Mapping.of(SOFA_SCORE, "Observation", Mapping.PathMapping.of("value", Mapping.PathMapping.Type.QUANTITY), List.of(CodeModifier.of("status", "final")),
                        List.of())
        ), null, CODE_SYSTEM_ALIASES);

        var container = criterion.toCql(mappingContext);

        assertThat(container).printsTo("""
                library Retrieve version '1.0.0'
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                
                codesystem ecrf: 'https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/ecrf-parameter-codes'
                
                context Patient
                
                define Criterion:
                  exists (from [Observation: Code '06' from ecrf] O
                    where O.value as Quantity = 6 and
                      O.status = 'final')
                """);
    }

    @Test
    void toCql_WithValueOnPatient() {
        var criterion = NumericCriterion.of(ContextualConcept.of(AGE), EQUAL, BigDecimal.valueOf(16), "a");
        var mappingContext = MappingContext.of(Map.of(
                AGE, Mapping.of(AGE, "Patient", Mapping.PathMapping.of("birthDate", Mapping.PathMapping.Type.DATE))
        ), null, CODE_SYSTEM_ALIASES);

        var container = criterion.toCql(mappingContext);

        assertThat(container).printsTo("""
                library Retrieve version '1.0.0'
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                
                context Patient
                
                define Criterion:
                  AgeInYears() = 16
                """);
    }
}
