package de.numcodex.sq2cql.model.mapping;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.structured_query.CodeModifier;
import de.numcodex.sq2cql.model.structured_query.CodeEquivalentModifier;
import de.numcodex.sq2cql.model.structured_query.ContextualTermCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static de.numcodex.sq2cql.model.mapping.Mapping.TimeRestrictionMapping.Type.DATE_TIME;
import static de.numcodex.sq2cql.model.mapping.Mapping.TimeRestrictionMapping.Type.PERIOD;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alexander Kiel
 */
class MappingTest {

    private static final TermCode CONTEXT = TermCode.of("context", "context", "context");
    private static final ContextualTermCode TC_1 = ContextualTermCode.of(CONTEXT, TermCode.of("http://loinc.org", "72166-2", "tabacco smoking status"));
    private static final ContextualTermCode TC_2 = ContextualTermCode.of(CONTEXT, TermCode.of("http://loinc.org", "21908-9", "Stage group.clinical Cancer"));
    private static final TermCode CONFIRMED = TermCode.of("http://terminology.hl7.org/CodeSystem/condition-ver-status",
            "confirmed", "Confirmed");
    private static final TermCode TNM_T = TermCode.of("http://loinc.org", "21899-0", "Primary tumor.pathology Cancer");
    private static final TermCode DIAGNOSE = TermCode.of("http://hl7.org/fhir/StructureDefinition", "festgestellteDiagnose", "Festgestellte Diagnose");

    private static final FhirContext FHIR_CONTEXT = FhirContext.forR4Cached();

    private static Mapping parse(String s) throws JsonProcessingException {
        return new ObjectMapper().readValue(s, Mapping.class);
    }

    @Nested
    class FromJson {

        @Test
        void simple() throws Exception {
            var mapping = parse("""
                    {
                      "context": {
                        "system": "context",
                        "code": "context",
                        "display": "context"
                      },
                      "key": {
                        "system": "http://loinc.org",
                        "code": "72166-2",
                        "display": "tobacco smoking status"
                      },
                      "resourceType": "Observation",
                      "value": {
                        "path": "value",
                        "types": [ "Quantity" ]
                      }
                    }
                    """);

            assertThat(mapping.key()).isEqualTo(TC_1);
            assertThat(mapping.resourceType()).isEqualTo("Observation");
            assertThat(mapping.valueMapping()).contains(Mapping.PathMapping.of("value", Mapping.PathMapping.Type.QUANTITY));
        }

        @Test
        void additionalPropertiesAreIgnored() throws Exception {
            var mapping = parse("""
                    {
                      "foo-153729": "bar-153733",
                      "context": {
                        "system": "context",
                        "code": "context",
                        "display": "context"
                      },
                      "key": {
                        "system": "http://loinc.org",
                        "code": "72166-2",
                        "display": "tobacco smoking status"
                      },
                      "resourceType": "Observation"
                    }
                    """);

            assertThat(mapping.key()).isEqualTo(TC_1);
            assertThat(mapping.resourceType()).isEqualTo("Observation");
        }

        @ParameterizedTest
        @ValueSource(strings = {"value", "other"})
        void withValueMapping(String valueFhirPath) throws Exception {
            var mapping = parse(String.format("""
                    {
                      "context": {
                        "system": "context",
                        "code": "context",
                        "display": "context"
                      },
                      "key": {
                        "system": "http://loinc.org",
                        "code": "72166-2",
                        "display": "tobacco smoking status"
                      },
                      "resourceType": "Observation",
                      "value": {
                        "path": "%s",
                        "types": [ "Quantity" ]
                      }
                    }
                    """, valueFhirPath));

            assertThat(mapping.key()).isEqualTo(TC_1);
            assertThat(mapping.resourceType()).isEqualTo("Observation");
            assertThat(mapping.valueMapping()).contains(Mapping.PathMapping.of(valueFhirPath, Mapping.PathMapping.Type.QUANTITY));
        }

        @Test
        void withTimeRestrictionMapping() throws Exception {
            var mapping = parse("""
                    {
                      "context": {
                        "system": "context",
                        "code": "context",
                        "display": "context"
                      },
                      "key": {
                        "system": "http://loinc.org",
                        "code": "21908-9",
                        "display": "Stage group.clinical Cancer"
                      },
                      "resourceType": "Observation",
                      "timeRestriction": {
                        "path": "effective",
                        "types": ["dateTime", "Period"]
                      }
                    }
                    """);

            assertThat(mapping.key()).isEqualTo(TC_2);
            assertThat(mapping.resourceType()).isEqualTo("Observation");
            assertThat(mapping.timeRestrictionMapping()).contains(Mapping.TimeRestrictionMapping.of("effective", DATE_TIME, PERIOD));
        }

        @Nested
        class WithAttributeMapping {

            @Test
            void withTypeCoding() throws Exception {
                var mapping = parse("""
                        {
                          "context": {
                            "system": "context",
                            "code": "context",
                            "display": "context"
                          },
                          "key": {
                            "system": "http://loinc.org",
                            "code": "21908-9",
                            "display": "Stage group.clinical Cancer"
                          },
                          "resourceType": "Observation",
                          "attributes": [
                            {
                              "_type": "Attribute"
                              "key": {
                                "system": "http://loinc.org",
                                "code": "21899-0",
                                "display": "Primary tumor.pathology Cancer"
                              },
                              "component": [
                                {
                                  "_type": "AttributeComponent",
                                  "types": ["Coding"],
                                  "path": "component.where(code.coding.exists(system = 'http://loinc.org' and code = '21899-0')).value.first()",
                                  "cardinality": "many",
                                  "values": []
                                }
                              ]
                            }
                          ]
                        }
                        """);

                assertThat(mapping.key()).isEqualTo(TC_2);
                assertThat(mapping.resourceType()).isEqualTo("Observation");
                assertThat(mapping.attributeMappings()).containsEntry(TNM_T, AttributeMapping.of(
                        TNM_T,
                        List.of(AttributeComponent.of(
                                List.of(FHIR_CONTEXT.getElementDefinition("Coding")),
                                "component.where(code.coding.exists(system = 'http://loinc.org' and code = '21899-0')).value.first()",
                                Mapping.Cardinality.MANY,
                                List.of()
                        ))));
            }

            @Test
            void withTypeReference() throws Exception {
                var mapping = parse("""
                        {
                          "context": {
                            "system": "context",
                            "code": "context",
                            "display": "context"
                          },
                          "key": {
                            "system": "http://loinc.org",
                            "code": "21908-9",
                            "display": "Stage group.clinical Cancer"
                          },
                          "resourceType": "Observation",
                          "attributes": [
                            {
                              "types": ["Reference"],
                              "key": {
                                "code": "festgestellteDiagnose",
                                "display": "Festgestellte Diagnose",
                                "system": "http://hl7.org/fhir/StructureDefinition"
                              },
                              "path": "extension.where(url='https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/Diagnose').value.code.coding"
                            }
                          ]
                        }
                        """);

                assertThat(mapping.key()).isEqualTo(TC_2);
                assertThat(mapping.resourceType()).isEqualTo("Observation");
                assertThat(mapping.attributeMappings()).containsEntry(DIAGNOSE, AttributeMapping.of(
                        DIAGNOSE,
                        List.of(AttributeComponent.of(
                                List.of(FHIR_CONTEXT.getElementDefinition("Reference")),
                                "extension.where(url='https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/Diagnose').value.code.coding",
                                Mapping.Cardinality.MANY,
                                List.of()
                        ))
                ));
            }
        }

        @Nested
        class WithFixedCriteria {

            @Test
            void withTypeCode() throws Exception {
                var mapping = parse("""
                        {
                          "context": {
                            "system": "context",
                            "code": "context",
                            "display": "context"
                          },
                          "key": {
                            "system": "http://loinc.org",
                            "code": "72166-2",
                            "display": "tobacco smoking status"
                          },
                          "resourceType": "Observation",
                          "fixedCriteria": [
                            {
                              "types": ["code"],
                              "path": "status",
                              "value": [ {
                                  "code": "completed",
                                  "system": "http://hl7.org/fhir/report-status-codes",
                                  "display": "completed"
                                },
                                {
                                  "code": "in-progress",
                                  "system": "http://hl7.org/fhir/report-status-codes",
                                  "display": "in-progress"
                                }
                              ]
                            }
                          ]
                        }
                        """);

                assertThat(mapping.key()).isEqualTo(TC_1);
                assertThat(mapping.resourceType()).isEqualTo("Observation");
                assertThat(mapping.fixedCriteria()).containsExactly(CodeModifier.of("status", "completed", "in-progress"));
            }

            @Test
            void withTypeCoding() throws Exception {
                var mapping = parse("""
                        {
                          "context": {
                            "system": "context",
                            "code": "context",
                            "display": "context"
                          },
                          "key": {
                            "system": "http://loinc.org",
                            "code": "72166-2",
                            "display": "tobacco smoking status"
                          },
                          "resourceType": "Observation",
                          "fixedCriteria": [
                            {
                              "types": ["Coding"],
                              "path": "verificationStatus",
                              "value": [
                                {
                                  "system": "http://terminology.hl7.org/CodeSystem/condition-ver-status",
                                  "code": "confirmed",
                                  "display": "Confirmed"
                                }
                              ]
                            }
                          ]
                        }
                        """);

                assertThat(mapping.key()).isEqualTo(TC_1);
                assertThat(mapping.resourceType()).isEqualTo("Observation");
                assertThat(mapping.fixedCriteria()).containsExactly(CodeEquivalentModifier.of("verificationStatus", CONFIRMED));
            }
        }
    }
}
