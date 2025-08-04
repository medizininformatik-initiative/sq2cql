package de.numcodex.sq2cql.model.structured_query;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.numcodex.sq2cql.model.mapping.AttributeMapping;
import de.numcodex.sq2cql.model.mapping.Mapping;
import de.numcodex.sq2cql.model.mapping.MappingContext;
import de.numcodex.sq2cql.model.common.TermCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.numcodex.sq2cql.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Alexander Kiel
 */
class RangeCriterionTest {

    static final TermCode CONTEXT = TermCode.of("context", "context", "context");
    static final ContextualTermCode PLATELETS = ContextualTermCode.of(CONTEXT, TermCode.of("http://loinc.org", "26515-7", "Platelets"));
    static final ContextualTermCode OTHER_VALUE_PATH = ContextualTermCode.of(CONTEXT, TermCode.of("foo", "other-value-path", ""));
    static final MappingContext MAPPING_CONTEXT;
    static final TermCode STATUS = TermCode.of("http://hl7.org/fhir", "observation-status", "observation-status");
    static final TermCode FINAL = TermCode.of("http://hl7.org/fhir/observation-status", "final", "final");
    static final Map<String, String> CODE_SYSTEM_ALIASES = Map.of(
            "http://loinc.org", "loinc",
            "foo", "foo");

    static {
        MAPPING_CONTEXT = MappingContext.of(Map.of(
                PLATELETS, Mapping.of(PLATELETS, "Observation", Mapping.PathMapping.of("value", Mapping.PathMapping.Type.QUANTITY), List.of(),
                        List.of(AttributeMapping.of(List.of("code"), STATUS, "status"))),
                OTHER_VALUE_PATH, Mapping.of(OTHER_VALUE_PATH, "Observation", Mapping.PathMapping.of("other", Mapping.PathMapping.Type.QUANTITY))
        ), null, CODE_SYSTEM_ALIASES);
    }

    @Test
    void fromJson() throws Exception {
        var mapper = new ObjectMapper();

        var criterion = (RangeCriterion) mapper.readValue("""
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
                    "type": "quantity-range",
                    "unit": {
                      "code": "g/dl"
                    },
                    "minValue": 20,
                    "maxValue": 30
                  }
                }
                """, Criterion.class);

        assertEquals(ContextualConcept.of(PLATELETS), criterion.getConcept());
        assertEquals(BigDecimal.valueOf(20), criterion.getLowerBound());
        assertEquals(BigDecimal.valueOf(30), criterion.getUpperBound());
        assertEquals(Optional.of("g/dl"), criterion.getUnit());
    }

    @Test
    void fromJson_withoutUnit() throws Exception {
        var mapper = new ObjectMapper();

        var criterion = (RangeCriterion) mapper.readValue("""
                {
                  "context": {
                    "system": "context",
                    "code": "context",
                    "display": "context"
                  },
                  "termCodes": [{
                    "system": "system-140946",
                    "code": "code-140950",
                    "display": ""
                  }],
                  "valueFilter": {
                    "type": "quantity-range",
                    "minValue": 0,
                    "maxValue": 1
                  }
                }
                """, Criterion.class);

        assertEquals(BigDecimal.valueOf(0), criterion.getLowerBound());
        assertEquals(BigDecimal.valueOf(1), criterion.getUpperBound());
        assertEquals(Optional.empty(), criterion.getUnit());
    }

    @Test
    void toCql() {
        var criterion = RangeCriterion.of(ContextualConcept.of(PLATELETS), BigDecimal.valueOf(20), BigDecimal.valueOf(30), "g/dl");

        var container = criterion.toCql(MAPPING_CONTEXT);

        assertThat(container).printsTo("""
                library Retrieve version '1.0.0'
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                
                codesystem loinc: 'http://loinc.org'
                
                context Patient
                
                define Criterion:
                  exists (from [Observation: Code '26515-7' from loinc] O
                    where O.value as Quantity between 20 'g/dl' and 30 'g/dl')
                """);
    }

    @Test
    void toCql_WithOtherFhirValuePath() {
        var criterion = RangeCriterion.of(ContextualConcept.of(OTHER_VALUE_PATH), BigDecimal.valueOf(1), BigDecimal.valueOf(2));

        var container = criterion.toCql(MAPPING_CONTEXT);

        assertThat(container).printsTo("""
                library Retrieve version '1.0.0'
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                
                codesystem foo: 'foo'
                
                context Patient
                
                define Criterion:
                  exists (from [Observation: Code 'other-value-path' from foo] O
                    where O.other as Quantity between 1 and 2)
                """);
    }

    @Test
    void toCql_WithAttributeFilter() {
        var criterion = RangeCriterion.of(ContextualConcept.of(PLATELETS), BigDecimal.valueOf(20), BigDecimal.valueOf(30),
                "g/dl").appendAttributeFilter(ValueSetAttributeFilter.of(STATUS, FINAL));

        var container = criterion.toCql(MAPPING_CONTEXT);

        assertThat(container).printsTo("""
                library Retrieve version '1.0.0'
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                
                codesystem loinc: 'http://loinc.org'
                
                context Patient
                
                define Criterion:
                  exists (from [Observation: Code '26515-7' from loinc] O
                    where O.value as Quantity between 20 'g/dl' and 30 'g/dl' and
                      O.status = 'final')
                """);
    }

    @Test
    void toCql_WithFixedCriteria() {
        var criterion = RangeCriterion.of(ContextualConcept.of(PLATELETS), BigDecimal.valueOf(20), BigDecimal.valueOf(30), "g/dl");
        var mappingContext = MappingContext.of(Map.of(
                PLATELETS, Mapping.of(PLATELETS, "Observation", Mapping.PathMapping.of("value", Mapping.PathMapping.Type.QUANTITY), List.of(CodeModifier.of("status", "final")),
                        List.of())
        ), null, CODE_SYSTEM_ALIASES);

        var container = criterion.toCql(mappingContext);

        assertThat(container).printsTo("""
                library Retrieve version '1.0.0'
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                
                codesystem loinc: 'http://loinc.org'
                                                    
                context Patient
                                
                define Criterion:
                  exists (from [Observation: Code '26515-7' from loinc] O
                    where O.value as Quantity between 20 'g/dl' and 30 'g/dl' and
                      O.status = 'final')
                """);
    }
}
