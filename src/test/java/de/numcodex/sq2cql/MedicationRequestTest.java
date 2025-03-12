package de.numcodex.sq2cql;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.numcodex.sq2cql.model.structured_query.StructuredQuery;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static de.numcodex.sq2cql.Assertions.assertThat;
import static de.numcodex.sq2cql.Util.createTranslator;

public class MedicationRequestTest {

    private static Path resourcePath(String name) throws URISyntaxException {
        return Paths.get(Objects.requireNonNull(MedicationRequestTest.class.getResource(name)).toURI());
    }

    private static String slurp(String name) throws Exception {
        return Files.readString(resourcePath(name));
    }

    private static StructuredQuery readStructuredQuery(String filename) throws Exception {
        return new ObjectMapper().readValue(slurp(filename), StructuredQuery.class);
    }

    @Test
    public void translateMedicationRequest() throws Exception {
        var translator = createTranslator();
        var structuredQuery = readStructuredQuery("MedicationRequestSQ.json");

        var library = translator.toCql(structuredQuery);

        assertThat(library).printsTo("""
                library Retrieve version '1.0.0'
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                
                codesystem atc: 'http://fhir.de/CodeSystem/bfarm/atc'
                
                context Unfiltered
                
                define B01AB01Ref:
                  from [Medication: Code 'B01AB01' from atc] M
                    return 'Medication/' + M.id
                
                context Patient
                
                define Criterion:
                  exists (from [MedicationRequest] M
                    where M.medication.reference in B01AB01Ref)
                
                define InInitialPopulation:
                  Criterion
                """);
    }

    @Test
    public void translateMedicationRequestTimeRestriction() throws Exception {
        var translator = createTranslator();
        var structuredQuery = readStructuredQuery("MedicationRequestSQTimeRestriction.json");

        var library = translator.toCql(structuredQuery);

        assertThat(library).printsTo("""
                library Retrieve version '1.0.0'
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                
                codesystem atc: 'http://fhir.de/CodeSystem/bfarm/atc'
                
                context Unfiltered
                
                define B01AB01Ref:
                  from [Medication: Code 'B01AB01' from atc] M
                    return 'Medication/' + M.id
                
                context Patient
                
                define Criterion:
                  exists (from [MedicationRequest] M
                    where M.medication.reference in B01AB01Ref and
                      ToDate(M.authoredOn as dateTime) in Interval[@2024-01-01T, @2024-02-01T])
                
                define InInitialPopulation:
                  Criterion
                """);
    }
}
