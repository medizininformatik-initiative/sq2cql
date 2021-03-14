package de.numcodex.sq2cql.model.structured_query;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.numcodex.sq2cql.Container;
import de.numcodex.sq2cql.model.ConceptNode;
import de.numcodex.sq2cql.model.Mapping;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.BooleanExpression;
import de.numcodex.sq2cql.model.cql.CodeSystemDefinition;
import de.numcodex.sq2cql.PrintContext;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import static de.numcodex.sq2cql.model.common.Comparator.GREATER_THAN;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Alexander Kiel
 */
class RangeCriterionTest {

    public static final TermCode PLATELETS = TermCode.of("http://loinc.org", "26515-7", "Platelets");
    public static final CodeSystemDefinition LOINC_CODE_SYSTEM_DEF = CodeSystemDefinition.of("loinc", "http://loinc.org");
    public static final Map<String, String> CODE_SYSTEM_ALIASES = Map.of(
            "http://loinc.org", "loinc");
    public static final MappingContext MAPPING_CONTEXT = MappingContext.of(Map.of(PLATELETS,
            Mapping.of(PLATELETS, "Observation")), ConceptNode.of(), CODE_SYSTEM_ALIASES);

    @Test
    void fromJson() throws Exception {
        var mapper = new ObjectMapper();

        var criterion = (RangeCriterion) mapper.readValue("""
                {
                  "termCode": {
                    "system": "http://loinc.org", 
                    "code": "26515-7",
                    "display": "Platelets"
                  },
                  "valueFilter": {
                    "type": "quantity-range",
                    "quantityUnit": {
                      "code": "g/dl"
                    },
                    "minValue": 20,
                    "maxValue": 30
                  }
                }
                """, Criterion.class);

        assertEquals(PLATELETS, criterion.getTermCode());
        assertEquals(BigDecimal.valueOf(20), criterion.getLowerBound());
        assertEquals(BigDecimal.valueOf(30), criterion.getUpperBound());
        assertEquals("g/dl", criterion.getUnit());
    }

    @Test
    void toCql() {
        Criterion criterion = RangeCriterion.of(PLATELETS, BigDecimal.valueOf(20), BigDecimal.valueOf(30),
                "g/dl");

        Container<BooleanExpression> container = criterion.toCql(MAPPING_CONTEXT);

        assertEquals("""
                        exists from [Observation: Code '26515-7' from loinc] O
                          where O.value as Quantity between 20 'g/dl' and 30 'g/dl'""",
                container.getExpression().map(e -> e.print(PrintContext.ZERO)).orElse(""));
        assertEquals(Set.of(LOINC_CODE_SYSTEM_DEF), container.getCodeSystemDefinitions());
    }
}
