package de.numcodex.sq2cql;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.StringParam;
import de.numcodex.sq2cql.model.Mapping;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.TermCodeNode;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.structured_query.Concept;
import de.numcodex.sq2cql.model.structured_query.NumericCriterion;
import de.numcodex.sq2cql.model.structured_query.StructuredQuery;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.Measure;
import org.hl7.fhir.r4.model.MeasureReport;
import org.hl7.fhir.r4.model.Parameters;
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

import static de.numcodex.sq2cql.model.common.Comparator.LESS_THAN;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hl7.fhir.r4.model.Bundle.BundleType.TRANSACTION;
import static org.hl7.fhir.r4.model.Bundle.HTTPVerb.POST;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
public class EvaluationIT {

    static final TermCode ROOT = TermCode.of("", "", "");
    static final TermCode BLOOD_PRESSURE = TermCode.of("http://loinc.org", "85354-9",
            "Blood pressure panel with all children optional");
    static final TermCode DIASTOLIC_BLOOD_PRESSURE = TermCode.of("http://loinc.org", "8462-4",
            "Diastolic blood pressure");
    static final Map<String, String> CODE_SYSTEM_ALIASES = Map.of("http://loinc.org", "loinc");

    @Container
    private final GenericContainer<?> blaze = new GenericContainer<>(DockerImageName.parse("samply/blaze:0.18"))
            .withImagePullPolicy(PullPolicy.alwaysPull())
            .withExposedPorts(8080)
            .waitingFor(Wait.forHttp("/health").forStatusCodeMatching(c -> c >= 200 && c <= 500))
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
        var conceptTree = TermCodeNode.of(ROOT, TermCodeNode.of(BLOOD_PRESSURE));
        var mappingContext = MappingContext.of(mappings, conceptTree, CODE_SYSTEM_ALIASES);
        var translator = Translator.of(mappingContext);
        var criterion = NumericCriterion.of(Concept.of(BLOOD_PRESSURE), LESS_THAN, BigDecimal.valueOf(80), "mm[Hg]");
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
