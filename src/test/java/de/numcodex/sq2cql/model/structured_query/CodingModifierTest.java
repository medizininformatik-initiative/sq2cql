package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.PrintContext;
import de.numcodex.sq2cql.model.ConceptNode;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.AliasExpression;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Alexander Kiel
 */
class CodingModifierTest {

    public static final TermCode CONFIRMED = TermCode.of("http://terminology.hl7.org/CodeSystem/condition-ver-status",
            "confirmed", "Conformed");

    public static final Map<String, String> CODE_SYSTEM_ALIASES = Map.of(
            "http://terminology.hl7.org/CodeSystem/condition-ver-status", "ver_status");

    public static final MappingContext MAPPING_CONTEXT = MappingContext.of(Map.of(), ConceptNode.of(),
            CODE_SYSTEM_ALIASES);

    @Test
    void expression() {
        var modifier = CodingModifier.of("verificationStatus", CONFIRMED);

        var expression = modifier.expression(MAPPING_CONTEXT, AliasExpression.of("C"));

        assertEquals("C.verificationStatus.coding contains Code 'confirmed' from ver_status",
                expression.getExpression().map(e -> e.print(PrintContext.ZERO)).orElse(""));
    }
}
