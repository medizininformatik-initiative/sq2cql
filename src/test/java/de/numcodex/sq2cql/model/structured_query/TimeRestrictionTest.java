package de.numcodex.sq2cql.model.structured_query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.numcodex.sq2cql.SoftContainerAssertions;
import de.numcodex.sq2cql.model.Mapping;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.StandardIdentifierExpression;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static de.numcodex.sq2cql.Assertions.assertSoftly;
import de.numcodex.sq2cql.model.Mapping.TimeRestrictionMapping.Type;
import static java.time.format.DateTimeFormatter.ISO_DATE;
import static de.numcodex.sq2cql.model.structured_query.TimeRestriction.MIN_AFTER_DATE;
import static de.numcodex.sq2cql.model.structured_query.TimeRestriction.MAX_BEFORE_DATE;
import static org.assertj.core.api.Assertions.*;

public class TimeRestrictionTest {

    private static final LocalDate AFTER_DATE = LocalDate.of(1970, 1, 1);
    private static final LocalDate BEFORE_DATE = LocalDate.of(2000, 1, 1);

    @Nested
    class Construction {

        @Test
        @DisplayName("Construction should fail if the afterDate is after the beforeDate")
        void afterDateCannotBeAfterBeforeDate() {
            assertThatExceptionOfType(IllegalArgumentException.class)
                    .as("An IllegalArgumentException should be thrown if the afterDate is after the " +
                            "beforeDate")
                    .isThrownBy(() -> new TimeRestriction(BEFORE_DATE, AFTER_DATE));
        }

        @Test
        @DisplayName("Construction should fail if the represented interval is not within the bounds for the date " +
                "datatype defined in the CQL specification")
        void valuesHaveToBeWithinCQLBounds() {
            assertThatExceptionOfType(IllegalArgumentException.class)
                    .as("An IllegalArgumentException should be thrown if the afterDate parameter is " +
                            "before the minimum allowed date value allowed in CQL")
                    .isThrownBy(() -> new TimeRestriction(MIN_AFTER_DATE.minusDays(1), BEFORE_DATE));
            assertThatExceptionOfType(IllegalArgumentException.class)
                    .as("An IllegalArgumentException should be thrown if the beforeDate parameter is " +
                            "after the maximum allowed date value allowed in CQL")
                    .isThrownBy(() -> new TimeRestriction(AFTER_DATE, MAX_BEFORE_DATE.plusDays(1)));
        }

    }

    @Nested
    class Deserialization {

        private static final ObjectMapper MAPPER = new ObjectMapper();

        @Test
        @DisplayName("Deserialization of a TimeRestriction instance should handle neither attribute 'afterDate' nor " +
                "'beforeDate' being present")
        void neitherAfterDateNorBeforeDate() throws JsonProcessingException {
            assertThat(TimeRestriction.fromJsonNode(MAPPER.readTree("{ }"))).isNull();
        }

        @Test
        @DisplayName("Deserialization of a TimeRestriction instance should not require attribute 'beforeDate'")
        void noBeforeDate() throws JsonProcessingException {
            assertThat(TimeRestriction.fromJsonNode(MAPPER.readTree(
                    "{\"afterDate\": \"%s\"}".formatted(AFTER_DATE.format(ISO_DATE)
                    ))))
                    .extracting(TimeRestriction::afterDate, TimeRestriction::beforeDate)
                    .containsExactly(AFTER_DATE, MAX_BEFORE_DATE);
        }

        @Test
        @DisplayName("Deserialization of a TimeRestriction instance should not require attribute 'afterDate'")
        void noAfterDate() throws JsonProcessingException {
            assertThat(TimeRestriction.fromJsonNode(MAPPER.readTree(
                    "{\"beforeDate\": \"%s\"}".formatted(BEFORE_DATE.format(ISO_DATE))
                    )))
                    .extracting(TimeRestriction::afterDate, TimeRestriction::beforeDate)
                    .containsExactly(MIN_AFTER_DATE, BEFORE_DATE);
        }

        @Test
        @DisplayName("Deserialization of a TimeRestriction instance should be able to handle both attributes " +
                "'afterDate' and 'beforeDate' being present")
        void bothAfterDateAndBeforeDate() throws JsonProcessingException {
            assertThat(TimeRestriction.fromJsonNode(MAPPER.readTree(
                    "{\"afterDate\": \"%s\", \"beforeDate\": \"%s\"}"
                            .formatted(AFTER_DATE.format(ISO_DATE), BEFORE_DATE.format(ISO_DATE))
                    )))
                    .extracting(TimeRestriction::afterDate, TimeRestriction::beforeDate)
                    .containsExactly(AFTER_DATE, BEFORE_DATE);
        }

    }

    @Nested
    class Expression {

        private static final MappingContext EMPTY_MAPPING_CONTEXT = MappingContext.of();

        @Test
        @DisplayName("Valid CQL expressions should be generated for a right-open time interval")
        public void after() {
            var tr = TimeRestriction.of(AFTER_DATE, MAX_BEFORE_DATE);

            assertSoftly(softly -> {
                assertForDate(softly, tr, AFTER_DATE, MAX_BEFORE_DATE);
                assertForDateTime(softly, tr, AFTER_DATE, MAX_BEFORE_DATE);
                assertForPeriod(softly, tr, AFTER_DATE, MAX_BEFORE_DATE);
            });
        }

        @Test
        @DisplayName("Valid CQL expressions should be generated for a left-open time interval")
        public void before() {
            var tr = TimeRestriction.of(MIN_AFTER_DATE, BEFORE_DATE);

            assertSoftly(softly -> {
                assertForDate(softly, tr, MIN_AFTER_DATE, BEFORE_DATE);
                assertForDateTime(softly, tr, MIN_AFTER_DATE, BEFORE_DATE);
                assertForPeriod(softly, tr, MIN_AFTER_DATE, BEFORE_DATE);
            });
        }

        @Test
        @DisplayName("Valid CQL expressions should be generated for a closed time interval")
        public void between() {
            var tr = TimeRestriction.of(AFTER_DATE, BEFORE_DATE);

            assertSoftly(softly -> {
                assertForDate(softly, tr, AFTER_DATE, BEFORE_DATE);
                assertForDateTime(softly, tr, AFTER_DATE, BEFORE_DATE);
                assertForPeriod(softly, tr, AFTER_DATE, BEFORE_DATE);
            });
        }

        private void assertForDate(SoftContainerAssertions softly, TimeRestriction timeRestriction, LocalDate startDate,
                                   LocalDate endDate) {
            var mapping = liftToMapping(Mapping.TimeRestrictionMapping.of("elementDate", Type.DATE));
            var timeRestrictionModifier = (TimeRestrictionModifier) timeRestriction.toModifier(mapping);
            var identifier = StandardIdentifierExpression.of("X");

            var expression = timeRestrictionModifier.expression(EMPTY_MAPPING_CONTEXT, identifier);

            softly.assertThat(expression.moveToPatientContext("Criterion"))
                    .as("Failed for datatype 'date'")
                    .printsTo("""
                    library Retrieve version '1.0.0'
                    using FHIR version '4.0.0'
                    include FHIRHelpers version '4.0.0'
                    
                    context Patient
                    
                    define Criterion:
                      ToDate(X.elementDate as date) in Interval[@%s, @%s]
                    """.formatted(startDate, endDate));
        }

        private void assertForDateTime(SoftContainerAssertions softly, TimeRestriction timeRestriction,
                                       LocalDate startDate, LocalDate endDate) {
            var mapping = liftToMapping(Mapping.TimeRestrictionMapping.of("elementDateTime", Type.DATE_TIME));
            var timeRestrictionModifier = (TimeRestrictionModifier) timeRestriction.toModifier(mapping);
            var identifier = StandardIdentifierExpression.of("X");

            var expression = timeRestrictionModifier.expression(EMPTY_MAPPING_CONTEXT, identifier);

            softly.assertThat(expression.moveToPatientContext("Criterion"))
                    .as("Failed for datatype 'dateTime'")
                    .printsTo("""
                    library Retrieve version '1.0.0'
                    using FHIR version '4.0.0'
                    include FHIRHelpers version '4.0.0'
                    
                    context Patient
                    
                    define Criterion:
                      ToDate(X.elementDateTime as dateTime) in Interval[@%sT, @%sT]
                    """.formatted(startDate, endDate));
        }

        private void assertForPeriod(SoftContainerAssertions softly, TimeRestriction timeRestriction,
                                     LocalDate startDate, LocalDate endDate) {
            var mapping = liftToMapping(Mapping.TimeRestrictionMapping.of("elementPeriod", Type.PERIOD));
            var timeRestrictionModifier = (TimeRestrictionModifier) timeRestriction.toModifier(mapping);
            var identifier = StandardIdentifierExpression.of("X");

            var expression = timeRestrictionModifier.expression(EMPTY_MAPPING_CONTEXT, identifier);

            softly.assertThat(expression.moveToPatientContext("Criterion"))
                    .as("Failed for datatype 'period'")
                    .printsTo("""
                    library Retrieve version '1.0.0'
                    using FHIR version '4.0.0'
                    include FHIRHelpers version '4.0.0'
                    
                    context Patient
                    
                    define Criterion:
                      X.elementPeriod overlaps Interval[@%sT, @%sT]
                    """.formatted(startDate, endDate));
        }

        private Mapping liftToMapping(Mapping.TimeRestrictionMapping trm) {
            return Mapping.of(
                    ContextualTermCode.of(
                            TermCode.of("context", "context", "context"),
                            TermCode.of("system", "code", "display")
                    ),
                    "ResourceType",
                    null,
                    null,
                    null,
                    trm
            );
        }

    }

}