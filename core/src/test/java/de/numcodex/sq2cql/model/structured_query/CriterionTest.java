package de.numcodex.sq2cql.model.structured_query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Alexander Kiel
 */
class CriterionTest {

    @Nested
    class FromJson {

        ObjectMapper mapper = new ObjectMapper();

        @Test
        void emptyObject() {
            assertThatThrownBy(() -> mapper.readValue("{}", Criterion.class))
                    .isInstanceOf(JsonProcessingException.class)
                    .hasRootCauseMessage("missing JSON property: context");
        }

        @Test
        void unknownValueFilterType() {
            assertThatThrownBy(() -> mapper.readValue("""
                    {
                      "context": {
                        "system": "context",
                        "code": "context",
                        "display": "context"
                      },
                      "termCodes": [],
                      "valueFilter": {
                        "type": "foo"
                      }
                    }
                    """, Criterion.class))
                    .isInstanceOf(JsonProcessingException.class)
                    .hasRootCauseMessage("unknown valueFilter type: foo");
        }

        @Test
        void emptyTimeRestrictionIsIgnored() throws JsonProcessingException {
            assertThat(mapper.readValue("""
                    {
                      "context": {
                        "system": "context",
                        "code": "context",
                        "display": "context"
                      },
                      "termCodes": [],
                      "timeRestriction": {}
                    }
                    """, Criterion.class).timeRestriction()).isNull();
        }

        @Test
        void invalidTimeRestrictionAfterDate() {
            assertThatThrownBy(() -> mapper.readValue("""
                    {
                      "context": {
                        "system": "context",
                        "code": "context",
                        "display": "context"
                      },
                      "termCodes": [],
                      "timeRestriction": {
                        "afterDate": "2023-02-29"
                      }
                    }
                    """, Criterion.class))
                    .isInstanceOf(JsonProcessingException.class)
                    .hasRootCauseMessage("Invalid date 'February 29' as '2023' is not a leap year");
        }

        @Test
        void invalidTimeRestrictionBeforeDate() {
            assertThatThrownBy(() -> mapper.readValue("""
                    {
                      "context": {
                        "system": "context",
                        "code": "context",
                        "display": "context"
                      },
                      "termCodes": [],
                      "timeRestriction": {
                        "afterDate": "2023-02-28",
                        "beforeDate": "2023-02-29"
                      }
                    }
                    """, Criterion.class))
                    .isInstanceOf(JsonProcessingException.class)
                    .hasRootCauseMessage("Invalid date 'February 29' as '2023' is not a leap year");
        }

        @Test
        void invalidTimeRestriction() {
            assertThatThrownBy(() -> mapper.readValue("""
                    {
                      "context": {
                        "system": "context",
                        "code": "context",
                        "display": "context"
                      },
                      "termCodes": [],
                      "timeRestriction": {
                        "afterDate": "2024-11-20",
                        "beforeDate": "2024-11-19"
                      }
                    }
                    """, Criterion.class))
                    .isInstanceOf(JsonProcessingException.class)
                    .hasRootCauseMessage("Invalid time restriction: beforeDate `2024-11-19` is before afterDate `2024-11-20` but should not be.");
        }

        @Test
        void timeRestrictionSameDay() throws JsonProcessingException {
            assertThat(mapper.readValue("""
                    {
                      "context": {
                        "system": "context",
                        "code": "context",
                        "display": "context"
                      },
                      "termCodes": [],
                      "timeRestriction": {
                        "afterDate": "2024-11-20",
                        "beforeDate": "2024-11-20"
                      }
                    }
                    """, Criterion.class).timeRestriction())
                    .isEqualTo(TimeRestriction.of(LocalDate.of(2024, 11, 20), LocalDate.of(2024, 11, 20)));
        }

        @Nested
        class WithReferenceAttributeFilter {

            @Test
            void invalidTimeRestriction() {
                assertThatThrownBy(() -> mapper.readValue("""
                        {
                           "context": {
                             "code": "Specimen",
                             "system": "fdpg.mii.cds",
                             "version": "1.0.0",
                             "display": "Bioprobe"
                           },
                           "termCodes": [
                             {
                               "code": "119364003",
                               "system": "http://snomed.info/sct",
                               "version": "http://snomed.info/sct/900000000000207008/version/20220930",
                               "display": "Serum specimen"
                             }
                           ],
                           "attributeFilters": [
                             {
                               "type": "reference",
                               "attributeCode": {
                                 "code": "festgestellteDiagnose",
                                 "display": "Festgestellte Diagnose",
                                 "system": "http://hl7.org/fhir/StructureDefinition"
                               },
                               "criteria": [
                                 {
                                   "termCodes": [
                                     {
                                       "code": "E13.9",
                                       "system": "http://fhir.de/CodeSystem/bfarm/icd-10-gm",
                                       "version": "2023",
                                       "display": "Sonstiger näher bezeichneter Diabetes mellitus : Ohne Komplikationen"
                                     }
                                   ],
                                   "context": {
                                     "code": "Diagnose",
                                     "system": "fdpg.mii.cds",
                                     "version": "1.0.0",
                                     "display": "Diagnose"
                                   },
                                   "timeRestriction": {
                                     "afterDate": "2024-11-20",
                                     "beforeDate": "2024-11-19"
                                   }
                                 }
                               ]
                             }
                           ]
                        }
                        """, Criterion.class))
                        .isInstanceOf(JsonProcessingException.class)
                        .hasRootCauseMessage("Invalid time restriction: beforeDate `2024-11-19` is before afterDate `2024-11-20` but should not be.");
            }

            @Test
            void timeRestrictionSameDay() throws JsonProcessingException {
                var criterion = mapper.readValue("""
                        {
                           "context": {
                             "code": "Specimen",
                             "system": "fdpg.mii.cds",
                             "version": "1.0.0",
                             "display": "Bioprobe"
                           },
                           "termCodes": [
                             {
                               "code": "119364003",
                               "system": "http://snomed.info/sct",
                               "version": "http://snomed.info/sct/900000000000207008/version/20220930",
                               "display": "Serum specimen"
                             }
                           ],
                           "attributeFilters": [
                             {
                               "type": "reference",
                               "attributeCode": {
                                 "code": "festgestellteDiagnose",
                                 "display": "Festgestellte Diagnose",
                                 "system": "http://hl7.org/fhir/StructureDefinition"
                               },
                               "criteria": [
                                 {
                                   "termCodes": [
                                     {
                                       "code": "E13.9",
                                       "system": "http://fhir.de/CodeSystem/bfarm/icd-10-gm",
                                       "version": "2023",
                                       "display": "Sonstiger näher bezeichneter Diabetes mellitus : Ohne Komplikationen"
                                     }
                                   ],
                                   "context": {
                                     "code": "Diagnose",
                                     "system": "fdpg.mii.cds",
                                     "version": "1.0.0",
                                     "display": "Diagnose"
                                   },
                                   "timeRestriction": {
                                     "afterDate": "2024-11-20",
                                     "beforeDate": "2024-11-20"
                                   }
                                 }
                               ]
                             }
                           ]
                        }
                        """, Criterion.class);

                var attributeFilter = (ReferenceAttributeFilter) criterion.attributeFilters().get(0);
                var referencedCriterion = attributeFilter.criteria().get(0);

                assertThat(referencedCriterion.timeRestriction())
                        .isEqualTo(TimeRestriction.of(LocalDate.of(2024, 11, 20), LocalDate.of(2024, 11, 20)));
            }
        }
    }
}
