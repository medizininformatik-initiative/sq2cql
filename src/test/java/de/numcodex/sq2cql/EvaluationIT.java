package de.numcodex.sq2cql;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.StringParam;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.numcodex.sq2cql.model.*;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.structured_query.ContextualConcept;
import de.numcodex.sq2cql.model.structured_query.ContextualTermCode;
import de.numcodex.sq2cql.model.structured_query.NumericCriterion;
import de.numcodex.sq2cql.model.structured_query.StructuredQuery;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static de.numcodex.sq2cql.Util.createTreeWithoutChildren;
import static de.numcodex.sq2cql.model.common.Comparator.LESS_THAN;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hl7.fhir.r4.model.Bundle.BundleType.TRANSACTION;
import static org.hl7.fhir.r4.model.Bundle.HTTPVerb.POST;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
public class EvaluationIT {

    static final TermCode CONTEXT = TermCode.of("context", "context", "context");
    static final String BLOOD_PRESSURE_CODE = "85354-9";
    static final String BLOOD_PRESSURE_SYSTEM = "http://loinc.org";
    static final ContextualTermCode BLOOD_PRESSURE = ContextualTermCode.of(CONTEXT, TermCode.of(BLOOD_PRESSURE_SYSTEM, BLOOD_PRESSURE_CODE,
            "Blood pressure panel with all children optional"));
    static final TermCode DIASTOLIC_BLOOD_PRESSURE = TermCode.of("http://loinc.org", "8462-4",
            "Diastolic blood pressure");
    static final Map<String, String> CODE_SYSTEM_ALIASES = Map.of("http://loinc.org", "loinc");

    @Container
    private final GenericContainer<?> blaze = new GenericContainer<>(DockerImageName.parse("samply/blaze:0.32"))
            .withImagePullPolicy(PullPolicy.alwaysPull())
            .withExposedPorts(8080)
            .waitingFor(Wait.forHttp("/health").forStatusCode(200))
            .withStartupAttempts(3);

    private final FhirContext fhirContext = FhirContext.forR4();
    private IGenericClient fhirClient;

    private static String slurp(String name) throws Exception {
        try (InputStream in = EvaluationIT.class.getResourceAsStream(name)) {
            if (in == null) {
                throw new Exception(format("Can't find `%s` in classpath.", name));
            } else {
                return new String(in.readAllBytes(), UTF_8);
            }
        } catch (IOException e) {
            throw new Exception(format("error while reading the file `%s` from classpath", name));
        }
    }

    private static Bundle createBundle(Library library, Measure measure) {
        var bundle = new Bundle();
        bundle.setType(TRANSACTION);
        bundle.addEntry().setResource(library).getRequest().setMethod(POST).setUrl("Library");
        bundle.addEntry().setResource(measure).getRequest().setMethod(POST).setUrl("Measure");
        return bundle;
    }

    private static Mapping readMapping(String s) throws Exception {
        return new ObjectMapper().readValue(s, Mapping.class);
    }

    private static StructuredQuery readStructuredQuery(String s) throws Exception {
        return new ObjectMapper().readValue(s, StructuredQuery.class);
    }

    @BeforeEach
    public void setUp() {
        fhirClient = fhirContext.newRestfulGenericClient(format("http://localhost:%d/fhir", blaze.getFirstMappedPort()));
    }

    @Test
    public void evaluateBloodPressure() throws Exception {
        fhirClient.transaction().withBundle(parseResource(Bundle.class, slurp("blood-pressure-bundle.json"))).execute();

        var valueFhirPath = format("component.where(code.coding.exists(system = '%s' and code = '%s')).value.first()",
                DIASTOLIC_BLOOD_PRESSURE.system(), DIASTOLIC_BLOOD_PRESSURE.code());
        var mappings = Map.of(BLOOD_PRESSURE, Mapping.of(BLOOD_PRESSURE, "Observation", valueFhirPath));
        var conceptTree = createTreeWithoutChildren(BLOOD_PRESSURE);
        var mappingContext = MappingContext.of(mappings, conceptTree, CODE_SYSTEM_ALIASES);
        var translator = Translator.of(mappingContext);
        var criterion = NumericCriterion.of(ContextualConcept.of(BLOOD_PRESSURE), LESS_THAN, BigDecimal.valueOf(80), "mm[Hg]");
        var structuredQuery = StructuredQuery.of(List.of(List.of(criterion)));
        var cql = translator.toCql(structuredQuery).print();
        var libraryUri = "urn:uuid" + UUID.randomUUID();
        var library = appendCql(parseResource(Library.class, slurp("Library.json")).setUrl(libraryUri), cql);
        var measureUri = "urn:uuid" + UUID.randomUUID();
        var measure = parseResource(Measure.class, slurp("Measure.json")).setUrl(measureUri).addLibrary(libraryUri);
        var bundle = createBundle(library, measure);

        fhirClient.transaction().withBundle(bundle).execute();

        var report = fhirClient.operation()
                .onType(Measure.class)
                .named("evaluate-measure")
                .withSearchParameter(Parameters.class, "measure", new StringParam(measureUri))
                .andSearchParameter("periodStart", new DateParam("1900"))
                .andSearchParameter("periodEnd", new DateParam("2100"))
                .useHttpGet()
                .returnResourceType(MeasureReport.class)
                .execute();

        assertEquals(1, report.getGroupFirstRep().getPopulationFirstRep().getCount());
    }

    @Test
    public void evaluateBloodPressureAttribute() throws Exception {
        fhirClient.transaction().withBundle(parseResource(Bundle.class, slurp("blood-pressure-bundle.json"))).execute();

        var mapping = readMapping("""
                {
                    "context": {
                          "system": "context",
                          "code": "context",
                          "display": "context"
                    },
                    "key": {
                        "system": "http://loinc.org",
                        "code": "85354-9",
                        "display": "Blood pressure panel with all children optional"
                    },
                    "resourceType": "Observation",
                    "attributeFhirPaths": [
                      {
                        "attributeType": "Coding",
                        "attributeKey": {
                          "system": "http://loinc.org",
                          "code": "8462-4",
                          "display": "Diastolic blood pressure"
                        },
                        "attributePath": "component.where(code.coding.exists(system = 'http://loinc.org' and code = '8462-4')).value.first()"
                      }
                    ]
                }
                """);
        var mappings = Map.of(BLOOD_PRESSURE, mapping);
        var conceptTree = createTreeWithoutChildren(BLOOD_PRESSURE);
        var mappingContext = MappingContext.of(mappings, conceptTree, CODE_SYSTEM_ALIASES);
        var translator = Translator.of(mappingContext);
        var structuredQuery = readStructuredQuery("""
                {
                  "version": "https://medizininformatik-initiative.de/fdpg/StructuredQuery/v3/schema",
                  "display": "",
                  "inclusionCriteria": [
                    [
                      {
                        "context": {
                          "system": "context",
                          "code": "context",
                          "display": "context"
                        },
                        "termCodes": [
                          {
                            "system": "http://loinc.org",
                            "code": "85354-9",
                            "display": "Blood pressure panel with all children optional"
                          }
                        ],
                        "attributeFilters": [
                          {
                            "attributeCode": {
                              "system": "http://loinc.org",
                              "code": "8462-4",
                              "display": "Diastolic blood pressure"
                            },
                            "type": "quantity-comparator",
                            "comparator": "lt",
                            "value": 80,
                            "unit": {
                              "code": "mm[Hg]"
                            }
                          }
                        ]
                      }
                    ]
                  ]
                }
                """);
        var cql = translator.toCql(structuredQuery).print();
        var libraryUri = "urn:uuid" + UUID.randomUUID();
        var library = appendCql(parseResource(Library.class, slurp("Library.json")).setUrl(libraryUri), cql);
        var measureUri = "urn:uuid" + UUID.randomUUID();
        var measure = parseResource(Measure.class, slurp("Measure.json")).setUrl(measureUri).addLibrary(libraryUri);
        var bundle = createBundle(library, measure);

        fhirClient.transaction().withBundle(bundle).execute();

        var report = fhirClient.operation()
                .onType(Measure.class)
                .named("evaluate-measure")
                .withSearchParameter(Parameters.class, "measure", new StringParam(measureUri))
                .andSearchParameter("periodStart", new DateParam("1900"))
                .andSearchParameter("periodEnd", new DateParam("2100"))
                .useHttpGet()
                .returnResourceType(MeasureReport.class)
                .execute();

        assertEquals(1, report.getGroupFirstRep().getPopulationFirstRep().getCount());
    }

    private <T extends IBaseResource> T parseResource(Class<T> type, String s) {
        var parser = fhirContext.newJsonParser();
        return type.cast(parser.parseResource(s));
    }

    private Library appendCql(Library library, String cql) {
        library.getContentFirstRep().setContentType("text/cql");
        library.getContentFirstRep().setData(cql.getBytes(UTF_8));
        return library;
    }
}
