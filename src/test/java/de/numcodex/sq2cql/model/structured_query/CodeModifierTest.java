package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.cql.StandardIdentifierExpression;
import org.junit.jupiter.api.Test;

import static de.numcodex.sq2cql.Assertions.assertThat;

class CodeModifierTest {

    @Test
    void expression_OneCode() {
        var modifier = CodeModifier.of("status", "final");

        var expression = modifier.expression(MappingContext.of(), StandardIdentifierExpression.of("O"));

        assertThat(expression.moveToPatientContext("Criterion")).printsTo("""
                library Retrieve version '1.0.0'
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                                                    
                context Patient
                                
                define Criterion:
                  O.status = 'final'
                """);
    }

    @Test
    void expression_TwoCodes() {
        var modifier = CodeModifier.of("status", "completed", "in-progress");

        var expression = modifier.expression(MappingContext.of(), StandardIdentifierExpression.of("P"));

        assertThat(expression.moveToPatientContext("Criterion")).printsTo("""
                library Retrieve version '1.0.0'
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                                                    
                context Patient
                                
                define Criterion:
                  P.status in { 'completed', 'in-progress' }
                """);
    }
}
