package de.numcodex.sq2cql;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.numcodex.sq2cql.model.structured_query.StructuredQuery;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static de.numcodex.sq2cql.Util.createTranslator;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SpecimenTest {

    private static Path resourcePath(String name) throws URISyntaxException {
        return Paths.get(Objects.requireNonNull(SpecimenTest.class.getResource(name)).toURI());
    }

    private static String slurp(String name) throws Exception {
        return Files.readString(resourcePath(name));
    }

    @Test
    public void translate() throws Exception {
        var translator = createTranslator();
        var structuredQuery = readStructuredQuery("SpecimenSQ.json");

        var cql = translator.toCql(structuredQuery).print();

        assertEquals("""
                library Retrieve version '1.0.0'
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                        
                codesystem icd10: 'http://fhir.de/CodeSystem/bfarm/icd-10-gm'
                codesystem snomed: 'http://snomed.info/sct'
                        
                context Patient
                        
                define "Diagnose E13.9":
                  [Condition: Code 'E13.9' from icd10] union
                  [Condition: Code 'E13.91' from icd10] union
                  [Condition: Code 'E13.90' from icd10]
                        
                define InInitialPopulation:
                  exists (from [Specimen: Code '119364003' from snomed] S
                    with "Diagnose E13.9" C
                      such that S.extension.where(url='https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/Diagnose').first().value.as(Reference).reference = 'Condition/' + C.id)
                    """, cql);
    }

    @Test
    public void translateExclusion() throws Exception {
        var translator = createTranslator();
        var structuredQuery = readStructuredQuery("SpecimenSQExclusion.json");

        var cql = translator.toCql(structuredQuery).print();

        assertEquals("""
                library Retrieve version '1.0.0'
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                        
                codesystem icd10: 'http://fhir.de/CodeSystem/bfarm/icd-10-gm'
                codesystem snomed: 'http://snomed.info/sct'
                        
                context Patient
                        
                define "Diagnose E13.9":
                  [Condition: Code 'E13.9' from icd10] union
                  [Condition: Code 'E13.91' from icd10] union
                  [Condition: Code 'E13.90' from icd10]
                                                       
                define Inclusion:
                  Patient.gender = 'female'
                        
                define Exclusion:
                  exists (from [Specimen: Code '119364003' from snomed] S
                    with "Diagnose E13.9" C
                      such that S.extension.where(url='https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/Diagnose').first().value.as(Reference).reference = 'Condition/' + C.id)
                      
                define InInitialPopulation:
                  Inclusion and
                  not Exclusion
                    """, cql);
    }

    @Test
    public void translateTwoInclusion() throws Exception {
        var translator = createTranslator();
        var structuredQuery = readStructuredQuery("SpecimenSQTwoInclusion.json");

        var cql = translator.toCql(structuredQuery).print();

        assertEquals("""
                library Retrieve version '1.0.0'
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                        
                codesystem icd10: 'http://fhir.de/CodeSystem/bfarm/icd-10-gm'
                codesystem snomed: 'http://snomed.info/sct'
                        
                context Patient
                        
                define "Diagnose E13.9":
                  [Condition: Code 'E13.9' from icd10] union
                  [Condition: Code 'E13.91' from icd10] union
                  [Condition: Code 'E13.90' from icd10]
                        
                define InInitialPopulation:
                  Patient.gender = 'female' and
                  exists (from [Specimen: Code '119364003' from snomed] S
                    with "Diagnose E13.9" C
                      such that S.extension.where(url='https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/Diagnose').first().value.as(Reference).reference = 'Condition/' + C.id)
                    """, cql);
    }

    @Test
    public void translateTwoReferenceCriteria() throws Exception {
        var translator = createTranslator();
        var structuredQuery = readStructuredQuery("SpecimenSQTwoReferenceCriteria.json");

        var cql = translator.toCql(structuredQuery).print();

        assertEquals("""
                library Retrieve version '1.0.0'
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                        
                codesystem icd10: 'http://fhir.de/CodeSystem/bfarm/icd-10-gm'
                codesystem snomed: 'http://snomed.info/sct'
                        
                context Patient
                                                       
                define "Diagnose E13.9 and Diagnose E13.1":
                  [Condition: Code 'E13.9' from icd10] union
                  [Condition: Code 'E13.91' from icd10] union
                  [Condition: Code 'E13.90' from icd10] union
                  [Condition: Code 'E13.1' from icd10] union
                  [Condition: Code 'E13.11' from icd10]
                        
                define InInitialPopulation:
                  exists (from [Specimen: Code '119364003' from snomed] S
                    with "Diagnose E13.9 and Diagnose E13.1" C
                      such that S.extension.where(url='https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/Diagnose').first().value.as(Reference).reference = 'Condition/' + C.id)
                    """, cql);
    }

    @Test
    public void translateAndBodySite() throws Exception {
        var translator = createTranslator();
        var structuredQuery = readStructuredQuery("SpecimenSQAndBodySite.json");

        var cql = translator.toCql(structuredQuery).print();

        assertEquals("""
                library Retrieve version '1.0.0'
                using FHIR version '4.0.0'
                include FHIRHelpers version '4.0.0'
                        
                codesystem icd-o-3: 'urn:oid:2.16.840.1.113883.6.43.1'
                codesystem icd10: 'http://fhir.de/CodeSystem/bfarm/icd-10-gm'
                codesystem snomed: 'http://snomed.info/sct'
                        
                context Patient
                        
                define "Diagnose E13.9":
                  [Condition: Code 'E13.9' from icd10] union
                  [Condition: Code 'E13.91' from icd10] union
                  [Condition: Code 'E13.90' from icd10]
                        
                define InInitialPopulation:
                  exists (from [Specimen: Code '119364003' from snomed] S
                    with "Diagnose E13.9" C
                      such that S.extension.where(url='https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/Diagnose').first().value.as(Reference).reference = 'Condition/' + C.id
                    where S.collection.bodySite.coding contains Code 'C44.6' from icd-o-3)
                    """, cql);
    }

    private StructuredQuery readStructuredQuery(String name) throws Exception {
        return new ObjectMapper().readValue(slurp(name), StructuredQuery.class);
    }
}
