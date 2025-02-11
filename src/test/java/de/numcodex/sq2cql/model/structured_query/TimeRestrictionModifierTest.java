package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.model.Mapping;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.cql.StandardIdentifierExpression;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;

import static de.numcodex.sq2cql.Assertions.assertThat;
import static de.numcodex.sq2cql.model.Mapping.TimeRestriction.Type.*;

class TimeRestrictionModifierTest {

    @Nested
    class Expression {

        @Test
        void effective() {
            var timeRestriction = TimeRestrictionModifier.of(Mapping.TimeRestriction.of("effective", DATE_TIME, PERIOD), LocalDate.of(2021, 1, 1), LocalDate.of(2021, 1, 2));
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

        @Test
        @DisplayName("recordedDate has no Period type")
        void recordedDate() {
            var timeRestriction = TimeRestrictionModifier.of(Mapping.TimeRestriction.of("recordedDate", DATE_TIME), LocalDate.of(2021, 1, 1), LocalDate.of(2021, 1, 2));
            var identifier = StandardIdentifierExpression.of("C");

            var expression = timeRestriction.expression(MappingContext.of(), identifier);

            assertThat(expression.moveToPatientContext("Criterion")).printsTo("""
                    library Retrieve version '1.0.0'
                    using FHIR version '4.0.0'
                    include FHIRHelpers version '4.0.0'
                    
                    context Patient
                    
                    define Criterion:
                      ToDate(C.recordedDate as dateTime) in Interval[@2021-01-01, @2021-01-02]
                    """);
        }

        @Test
        @DisplayName("potential date type test")
        void dateTest() {
            var timeRestriction = TimeRestrictionModifier.of(Mapping.TimeRestriction.of("date", DATE), LocalDate.of(2021, 1, 1), LocalDate.of(2021, 1, 2));
            var identifier = StandardIdentifierExpression.of("X");

            var expression = timeRestriction.expression(MappingContext.of(), identifier);

            assertThat(expression.moveToPatientContext("Criterion")).printsTo("""
                    library Retrieve version '1.0.0'
                    using FHIR version '4.0.0'
                    include FHIRHelpers version '4.0.0'
                    
                    context Patient
                    
                    define Criterion:
                      ToDate(X.date as date) in Interval[@2021-01-01, @2021-01-02]
                    """);
        }
    }
}
