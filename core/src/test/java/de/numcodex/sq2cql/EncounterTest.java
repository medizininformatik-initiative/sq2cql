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

public class EncounterTest {

    private static Path resourcePath(String name) throws URISyntaxException {
        return Paths.get(Objects.requireNonNull(EncounterTest.class.getResource(name)).toURI());
    }

    private static String slurp(String name) throws Exception {
        return Files.readString(resourcePath(name));
    }

    private static StructuredQuery readStructuredQuery(String filename) throws Exception {
        return new ObjectMapper().readValue(slurp(filename), StructuredQuery.class);
    }

    @Test
    public void translateMedicationStatement() throws Exception {
        var translator = createTranslator();
        var structuredQuery = readStructuredQuery("EncounterInpatientEinrichtung.json");

        var library = translator.toCql(structuredQuery);

        assertThat(library).printsTo("""
                library Retrieve version '1.0.0'
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                
                codesystem Kontaktebene: 'http://fhir.de/CodeSystem/Kontaktebene'
                codesystem v3ActCode: 'http://terminology.hl7.org/CodeSystem/v3-ActCode'
                
                context Patient
                
                define Criterion:
                  exists (from [Encounter: class ~ Code 'IMP' from v3ActCode] E
                    where exists (from E.type C
                        where C ~ Code 'einrichtungskontakt' from Kontaktebene))
                
                define InInitialPopulation:
                  Criterion
                """);
    }
}
