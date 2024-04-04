package de.numcodex.sq2cql;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.StringParam;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.numcodex.sq2cql.model.structured_query.StructuredQuery;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipFile;

import static de.numcodex.sq2cql.Util.createTranslator;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hl7.fhir.r4.model.Bundle.BundleType.TRANSACTION;
import static org.hl7.fhir.r4.model.Bundle.HTTPVerb.POST;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@TestInstance(Lifecycle.PER_CLASS)
public class AcceptanceTest {

    private static final Logger logger = LoggerFactory.getLogger(AcceptanceTest.class);

    private final GenericContainer<?> blaze = new GenericContainer<>(
            DockerImageName.parse("samply/blaze:0.25"))
            .withImagePullPolicy(PullPolicy.alwaysPull())
            .withEnv("LOG_LEVEL", "debug")
            .withExposedPorts(8080)
            .waitingFor(Wait.forHttp("/health").forStatusCode(200))
            .withLogConsumer(new Slf4jLogConsumer(logger));

    private final FhirContext fhirContext = FhirContext.forR4();
    private IGenericClient fhirClient;
    private Translator translator;

    private static Path resourcePath(String name) throws URISyntaxException {
        return Paths.get(Objects.requireNonNull(AcceptanceTest.class.getResource(name), "resource `%s` is missing".formatted(name)).toURI());
    }

    private static String slurp(String name) throws Exception {
        return Files.readString(resourcePath(name));
    }

    private static Bundle createBundle(Library library, Measure measure) {
        var bundle = new Bundle();
        bundle.setType(TRANSACTION);
        bundle.addEntry().setResource(library).getRequest().setMethod(POST).setUrl("Library");
        bundle.addEntry().setResource(measure).getRequest().setMethod(POST).setUrl("Measure");
        return bundle;
    }

    public static List<StructuredQuery> getTestQueriesReturningOnePatient() throws URISyntaxException, IOException {
        var exclusions = Set.of("new_testdata/1-age.json",
                // Blaze can't parse the unit [arb'U]/mL https://github.com/samply/blaze/issues/1234
                "new_testdata/ObservationLab-38dfe76b-ae35-8290-6d80-ab08c963d148",
                "new_testdata/ObservationLab-16408169-a38d-8afc-fdd2-ed7af97ccc57",
                "new_testdata/ObservationLab-0fa07a3f-2e29-5065-6fa2-31e959acdd98",
                "new_testdata/ObservationLab-43eb280e-7901-7990-64e3-22cfa51de78b",
                "new_testdata/ObservationLab-09c67417-306a-a871-feef-71cbc915d113",
                "new_testdata/ObservationLab-26184c80-edf6-b1e0-ee8f-0e0999755cb9",
                "new_testdata/ObservationLab-9d44c93e-7799-a8e2-b368-c5539c30ceaa",
                "new_testdata/ObservationLab-755a3ac1-32ae-2a20-1ac9-02ee25777cf0",
                "new_testdata/ObservationLab-8ec9ea98-6581-f934-9bcf-b1c4f87e3560",
                "new_testdata/ObservationLab-315e8080-7425-f4e9-3891-aef5ebe0572c",
                "new_testdata/ObservationLab-44c8fd00-1a0f-f218-9eb8-83257add8fed",
                "new_testdata/ObservationLab-7a2be049-40d2-d16f-3db6-12f46df2fc82",
                "new_testdata/ObservationLab-78c5a976-1786-72e8-006b-8fd6af157ed9",
                "new_testdata/ObservationLab-254bf7ae-1d0a-b994-f20b-575d4e28e674",
                "new_testdata/ObservationLab-4bf41e10-1c62-2f82-d081-3d923aca43f2",
                // Blaze can't parse the unit /[HPF]
                "new_testdata/ObservationLab-bf7b68ae-1f89-41b6-e6a1-a40bf031f4b9",
                "new_testdata/ObservationLab-b080e003-5e7f-503c-4b13-47f601d6d903",
                "new_testdata/ObservationLab-3dd0c866-0649-def5-0fb2-de1ea0b976c2",
                "new_testdata/ObservationLab-98b33c6e-0a14-b90a-7795-e98680ee526e",
                // Blaze can't parse the unit /100{WBCs}
                "new_testdata/ObservationLab-d2d07223-0b20-ee0f-8505-0a17d2e1ed4d"
        );
        try (var zipFile = new ZipFile(resourcePath("returningOnePatient.zip").toString())) {
            return zipFile.stream()
                    .filter(entry -> !exclusions.contains(entry.toString()))
                    .map(entry -> {
                        try {
                            return new ObjectMapper().readValue(zipFile.getInputStream(entry), StructuredQuery.class);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
        }
    }

    @BeforeAll
    public void setUp() throws Exception {
        blaze.start();
        fhirContext.getRestfulClientFactory().setSocketTimeout(200 * 1000);
        fhirClient = fhirContext.newRestfulGenericClient(
                format("http://localhost:%d/fhir", blaze.getFirstMappedPort()));
        fhirClient.transaction().withBundle(parseResource(Bundle.class, slurp("testData.json")))
                .execute();

        translator = createTranslator();
    }

    @ParameterizedTest
    @MethodSource("de.numcodex.sq2cql.AcceptanceTest#getTestQueriesReturningOnePatient")
    public void runTestCase(StructuredQuery structuredQuery) throws Exception {
        var cql = translator.toCql(structuredQuery).print();
        var measureUri = createMeasureAndLibrary(cql);
        var report = evaluateMeasure(measureUri);

        assertEquals(1, report.getGroupFirstRep().getPopulationFirstRep().getCount());
    }

    @Test
    public void allCriteriaTime() throws Exception {
        var structuredQuery = new ObjectMapper().readValue(slurp("example-all-crits-time.json"), StructuredQuery.class);
        var cql = translator.toCql(structuredQuery).print();
        var measureUri = createMeasureAndLibrary(cql);
        var report = evaluateMeasure(measureUri);

        assertEquals(0, report.getGroupFirstRep().getPopulationFirstRep().getCount());
    }

    @Test
    public void consent() throws Exception {
        var structuredQuery = new ObjectMapper().readValue(slurp("consent.json"), StructuredQuery.class);
        var cql = translator.toCql(structuredQuery).print();
        var measureUri = createMeasureAndLibrary(cql);
        var report = evaluateMeasure(measureUri);

        assertEquals(1, report.getGroupFirstRep().getPopulationFirstRep().getCount());
    }

    @ParameterizedTest
    @ValueSource(strings = {"large-query-worst-case-with-time-constraints.json", "test-large-query-more-crit-time-rest-1.json"})
    public void largeQuery(String filename) throws Exception {
        var structuredQuery = new ObjectMapper().readValue(slurp(filename), StructuredQuery.class);
        var cql = translator.toCql(structuredQuery).print();
        var measureUri = createMeasureAndLibrary(cql);
        var report = evaluateMeasure(measureUri);

        assertEquals(0, report.getGroupFirstRep().getPopulationFirstRep().getCount());
    }

    private String createMeasureAndLibrary(String cql) throws Exception {
        var libraryUri = "urn:uuid" + UUID.randomUUID();
        var library = appendCql(parseResource(Library.class, slurp("Library.json")).setUrl(libraryUri), cql);
        var measureUri = "urn:uuid" + UUID.randomUUID();
        var measure = parseResource(Measure.class, slurp("Measure.json"))
                .setUrl(measureUri)
                .addLibrary(libraryUri);
        var bundle = createBundle(library, measure);

        fhirClient.transaction().withBundle(bundle).execute();

        return measureUri;
    }

    private MeasureReport evaluateMeasure(String measureUri) {
        return fhirClient.operation()
                .onType(Measure.class)
                .named("evaluate-measure")
                .withSearchParameter(Parameters.class, "measure", new StringParam(measureUri))
                .andSearchParameter("periodStart", new DateParam("1900"))
                .andSearchParameter("periodEnd", new DateParam("2100"))
                .useHttpGet()
                .returnResourceType(MeasureReport.class)
                .execute();
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
