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

public class MedicationAdministrationTest {

    private static Path resourcePath(String name) throws URISyntaxException {
        return Paths.get(Objects.requireNonNull(MedicationAdministrationTest.class.getResource(name)).toURI());
    }

    private static String slurp(String name) throws Exception {
        return Files.readString(resourcePath(name));
    }

    private static StructuredQuery readStructuredQuery(String filename) throws Exception {
        return new ObjectMapper().readValue(slurp(filename), StructuredQuery.class);
    }

    @Test
    public void translateMedicationAdministration() throws Exception {
        var translator = createTranslator();
        var structuredQuery = readStructuredQuery("MedicationAdministrationSQ.json");

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
                  exists (from [MedicationAdministration] M
                    where M.medication.reference in B01AB01Ref)
                
                define InInitialPopulation:
                  Criterion
                """);
    }

    @Test
    public void translateMedicationAdministrationTimeRestriction() throws Exception {
        var translator = createTranslator();
        var structuredQuery = readStructuredQuery("MedicationAdministrationSQTimeRestriction.json");

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
                  exists (from [MedicationAdministration] M
                    where M.medication.reference in B01AB01Ref and
                      (ToDate(M.effective as dateTime) in Interval[@2024-01-01, @2024-02-01] or
                      M.effective overlaps Interval[@2024-01-01, @2024-02-01]))
                
                define InInitialPopulation:
                  Criterion
                """);
    }

    @Test
    public void translateMedicationAdministrationDoubleCriteria() throws Exception {
        var translator = createTranslator();
        var structuredQuery = readStructuredQuery("MedicationAdministrationSQDoubleCriteria.json");

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
                
                define "Criterion 1":
                  exists (from [MedicationAdministration] M
                    where M.medication.reference in B01AB01Ref and
                      (ToDate(M.effective as dateTime) in Interval[@2024-01-01, @2024-02-01] or
                      M.effective overlaps Interval[@2024-01-01, @2024-02-01]))
                
                define "Criterion 2":
                  exists (from [MedicationAdministration] M
                    where M.medication.reference in B01AB01Ref and
                      (ToDate(M.effective as dateTime) in Interval[@2023-01-01, @2023-02-01] or
                      M.effective overlaps Interval[@2023-01-01, @2023-02-01]))
                
                define InInitialPopulation:
                  "Criterion 1" and
                  "Criterion 2"
                """);
    }

    @Test
    public void translateMedicationAdministrationTwoCriteria() throws Exception {
        var translator = createTranslator();
        var structuredQuery = readStructuredQuery("MedicationAdministrationSQTwoCriteria.json");

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
                
                define B01AC06Ref:
                  from [Medication: Code 'B01AC06' from atc] M
                    return 'Medication/' + M.id
                
                context Patient
                
                define "Criterion 1":
                  exists (from [MedicationAdministration] M
                    where M.medication.reference in B01AB01Ref)
                
                define "Criterion 2":
                  exists (from [MedicationAdministration] M
                    where M.medication.reference in B01AC06Ref)
                
                define InInitialPopulation:
                  "Criterion 1" and
                  "Criterion 2"
                """);
    }
}
