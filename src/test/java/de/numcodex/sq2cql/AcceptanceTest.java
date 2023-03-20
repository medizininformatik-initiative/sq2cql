package de.numcodex.sq2cql;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.StringParam;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Functions;
import de.numcodex.sq2cql.model.Mapping;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.TermCodeNode;
import de.numcodex.sq2cql.model.structured_query.StructuredQuery;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.Parameters;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Map.entry;
import static org.hl7.fhir.r4.model.Bundle.BundleType.TRANSACTION;
import static org.hl7.fhir.r4.model.Bundle.HTTPVerb.POST;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@TestInstance(Lifecycle.PER_CLASS)
public class AcceptanceTest {

    private final Map<String, String> CODE_SYSTEM_ALIASES = Map.ofEntries(
            entry("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "icd10"),
            entry("http://loinc.org", "loinc"),
            entry("https://fhir.bbmri.de/CodeSystem/SampleMaterialType", "sample"),
            entry("http://fhir.de/CodeSystem/dimdi/atc", "atc"),
            entry("http://snomed.info/sct", "snomed"),
            entry("http://terminology.hl7.org/CodeSystem/condition-ver-status", "cvs"),
            entry("http://hl7.org/fhir/administrative-gender", "gender"),
            entry(
                    "https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/ecrf-parameter-codes",
                    "num-ecrf"),
            entry("urn:iso:std:iso:3166", "iso3166"),
            entry("https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/frailty-score",
                    "fraility-score"),
            entry("http://terminology.hl7.org/CodeSystem/consentcategorycodes", "consent"),
            entry("urn:oid:2.16.840.1.113883.3.1937.777.24.5.1", "mide-1"),
            entry("http://hl7.org/fhir/consent-provision-type", "provision-type"));

    private final GenericContainer<?> blaze = new GenericContainer<>(
        DockerImageName.parse("samply/blaze:0.20"))
            .withImagePullPolicy(PullPolicy.alwaysPull())
            .withExposedPorts(8080)
            .waitingFor(Wait.forHttp("/health").forStatusCode(200))
            .withStartupAttempts(3);

    private final FhirContext fhirContext = FhirContext.forR4();
    private IGenericClient fhirClient;
    private Translator translator;

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

    private Translator createTranslator() throws Exception {
        var mapper = new ObjectMapper();
        var mappings = Arrays.stream(mapper.readValue(slurp(
                        "codex-term-code-mapping.json"), Mapping[].class))
                .collect(Collectors.toMap(Mapping::key, Functions.identity()));
        var conceptTree = mapper.readValue(slurp("codex-code-tree.json"), TermCodeNode.class);
        var mappingContext = MappingContext.of(mappings, conceptTree, CODE_SYSTEM_ALIASES);
        return Translator.of(mappingContext);
    }

    @ParameterizedTest
    @MethodSource("de.numcodex.sq2cql.AcceptanceTest#getSqFileNames")
    public void runTestCase(String testCaseQueryName) throws Exception {
        var structuredQuery = readStructuredQuery(testCaseQueryName);
        var cql = translator.toCql(structuredQuery).print();
        var measureUri = createMeasureAndLibrary(cql);

        var report = evaluateMeasure(measureUri);

        assertEquals(1, report.getGroupFirstRep().getPopulationFirstRep().getCount());
    }

    private StructuredQuery readStructuredQuery(String testCaseQueryName) throws Exception {
        return new ObjectMapper().readValue(slurp("testCases/" + testCaseQueryName), StructuredQuery.class);
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

    public List<String> getSqFileNames() throws Exception {
        var disabledTests = readLines(resourcePath("disabled_test_cases"));
        try (Stream<Path> stream = Files.walk(resourcePath("testCases"))) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(filePath -> !disabledTests.contains(filePath))
                    .toList();
        }
    }

    private java.util.Set<String> readLines(Path path) throws IOException {
        try (var stream = Files.lines(path)) {
            return stream.collect(Collectors.toSet());
        }
    }

    private static Path resourcePath(String name) throws URISyntaxException {
        return Paths.get(Objects.requireNonNull(AcceptanceTest.class.getResource(name)).toURI());
    }

    private static String slurp(String name) throws Exception {
        return Files.readString(resourcePath(name));
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

    private static Bundle createBundle(Library library, Measure measure) {
        var bundle = new Bundle();
        bundle.setType(TRANSACTION);
        bundle.addEntry().setResource(library).getRequest().setMethod(POST).setUrl("Library");
        bundle.addEntry().setResource(measure).getRequest().setMethod(POST).setUrl("Measure");
        return bundle;
    }
}
