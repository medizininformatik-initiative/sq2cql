package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.cql.StandardIdentifierExpression;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static de.numcodex.sq2cql.Assertions.assertThat;

class TimeRestrictionModifierTest {

    @Test
    void expression_beforeAfter() {
        var timeRestriction = TimeRestrictionModifier.of("effective", LocalDate.of(2021, 1, 1), LocalDate.of(2021, 1, 2));
        var identifier = StandardIdentifierExpression.of("O");

        var expression = timeRestriction.expression(MappingContext.of(), identifier);

        assertThat(expression.moveToPatientContext("Criterion")).printsTo("""
                library Retrieve version '1.0.0'
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                
                context Patient
                
                define Criterion:
                  ToDate(O.effective as dateTime) in Interval[@2021-01-01, @2021-01-02] or
                  O.effective overlaps Interval[@2021-01-01, @2021-01-02]
                """);
    }
}
