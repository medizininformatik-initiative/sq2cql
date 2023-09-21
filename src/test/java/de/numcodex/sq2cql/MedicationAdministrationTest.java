package de.numcodex.sq2cql;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Functions;
import de.numcodex.sq2cql.model.Mapping;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.TermCodeNode;
import de.numcodex.sq2cql.model.structured_query.StructuredQuery;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class MedicationAdministrationTest {

  private final Map<String, String> CODE_SYSTEM_ALIASES = Map.ofEntries(
      entry("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "icd10"),
      entry("http://loinc.org", "loinc"),
      entry("https://fhir.bbmri.de/CodeSystem/SampleMaterialType", "sample"),
      entry("http://fhir.de/CodeSystem/bfarm/atc", "atc"),
      entry("http://snomed.info/sct", "snomed"),
      entry("http://terminology.hl7.org/CodeSystem/condition-ver-status", "cvs"),
      entry("http://hl7.org/fhir/administrative-gender", "gender"),
      entry("https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/ecrf-parameter-codes",
          "num-ecrf"), entry("urn:iso:std:iso:3166", "iso3166"),
      entry("https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/frailty-score",
          "fraility-score"),
      entry("http://terminology.hl7.org/CodeSystem/consentcategorycodes", "consent"),
      entry("urn:oid:2.16.840.1.113883.3.1937.777.24.5.1", "mide-1"),
      entry("http://hl7.org/fhir/consent-provision-type", "provision-type"),
      entry("http://fhir.de/CodeSystem/bfarm/ops", "oops"));

  private static Path resourcePath(String name) throws URISyntaxException {
    return Paths.get(Objects.requireNonNull(MedicationAdministrationTest.class.getResource(name)).toURI());
  }

  private static String slurp(String name) throws Exception {
    return Files.readString(resourcePath(name));
  }

  private Translator createTranslator() throws Exception {
    var mapper = new ObjectMapper();
    var mappings = Arrays.stream(mapper.readValue(slurp("mapping.json"), Mapping[].class))
        .collect(Collectors.toMap(Mapping::key, Functions.identity()));
    var conceptTree = mapper.readValue(slurp("codex-code-tree.json"), TermCodeNode.class);
    var mappingContext = MappingContext.of(mappings, conceptTree, CODE_SYSTEM_ALIASES);
    return Translator.of(mappingContext);
  }

  @Disabled
  @Test
  public void translateMedicationAdministration() throws Exception {
    var translator = createTranslator();
    var structuredQuery = readStructuredQuery();
    var cql = translator.toCql(structuredQuery).print();
    assertEquals("""
        library Retrieve version '1.0.0'
        using FHIR version '4.0.0'
        include FHIRHelpers version '4.0.0'
                
        codesystem atc: 'http://fhir.de/CodeSystem/bfarm/atc'
                
        context Unfiltered
                
        define HeparinB01AB01Ref:
          from [Medication: Code 'B01AB01' from atc] M
            return 'Medication/' + M.id
                
        context Patient
                
        define InInitialPopulation:
          exists (from [MedicationAdministration] M
            where M.medication.reference in HeparinB01AB01Ref)
            """, cql);

  }

  private StructuredQuery readStructuredQuery() throws Exception {
    return new com.fasterxml.jackson.databind.ObjectMapper().readValue(slurp(
            "MedicationAdministration.json"),
        StructuredQuery.class);
  }

}
