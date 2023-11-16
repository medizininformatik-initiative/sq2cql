package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.cql.StandardIdentifierExpression;
import org.junit.jupiter.api.Test;

import static de.numcodex.sq2cql.Assertions.assertThat;

class TimeRestrictionModifierTest {

    @Test
    void expression_before() {
        var timeRestriction = TimeRestrictionModifier.of("effective", null, "2021-01-01T");
        var identifier = StandardIdentifierExpression.of("O");

        var expression = timeRestriction.expression(MappingContext.of(), identifier);

        assertThat(expression.moveToPatientContext("Criterion")).printsTo("""
                library Retrieve version '1.0.0'
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                                                    
                context Patient
                                
                define Criterion:
                  ToDate(O.effective as dateTime) in Interval[@1900-01-01T, @2021-01-01T] or
                  O.effective overlaps Interval[@1900-01-01T, @2021-01-01T]
                """);
    }


    @Test
    void expression_after() {
        var timeRestriction = TimeRestrictionModifier.of("effective", "2020-01-01T", null);
        var identifier = StandardIdentifierExpression.of("O");

        var expression = timeRestriction.expression(MappingContext.of(), identifier);

        assertThat(expression.moveToPatientContext("Criterion")).printsTo("""
                library Retrieve version '1.0.0'
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                                                    
                context Patient
                                
                define Criterion:
                  ToDate(O.effective as dateTime) in Interval[@2020-01-01T, @2040-01-01T] or
                  O.effective overlaps Interval[@2020-01-01T, @2040-01-01T]
                """);
    }


    @Test
    void expression_beforeAfter() {
        var timeRestriction = TimeRestrictionModifier.of("effective", "2021-01-01T", "2021-01-01T");
        var identifier = StandardIdentifierExpression.of("O");

        var expression = timeRestriction.expression(MappingContext.of(), identifier);

        assertThat(expression.moveToPatientContext("Criterion")).printsTo("""
                library Retrieve version '1.0.0'
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                                                    
                context Patient
                                
                define Criterion:
                  ToDate(O.effective as dateTime) in Interval[@2021-01-01T, @2021-01-01T] or
                  O.effective overlaps Interval[@2021-01-01T, @2021-01-01T]
                """);
    }
}
