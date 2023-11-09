package de.numcodex.sq2cql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Functions;
import de.numcodex.sq2cql.model.Mapping;
import de.numcodex.sq2cql.model.MappingContext;
import de.numcodex.sq2cql.model.TermCodeNode;
import de.numcodex.sq2cql.model.structured_query.ContextualTermCode;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

import static java.util.Map.entry;

public interface Util {

    Map<String, String> CODE_SYSTEM_ALIASES = Map.ofEntries(
            entry("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "icd10"),
            entry("http://loinc.org", "loinc"),
            entry("https://fhir.bbmri.de/CodeSystem/SampleMaterialType", "sample"),
            entry("http://fhir.de/CodeSystem/bfarm/atc", "atc"),
            entry("http://snomed.info/sct", "snomed"),
            entry("http://terminology.hl7.org/CodeSystem/condition-ver-status", "cvs"),
            entry("http://hl7.org/fhir/administrative-gender", "gender"),
            entry("https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/ecrf-parameter-codes",
                    "num_ecrf"), entry("urn:iso:std:iso:3166", "iso3166"),
            entry("https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/frailty-score",
                    "fraility_score"),
            entry("http://terminology.hl7.org/CodeSystem/consentcategorycodes", "consent"),
            entry("urn:oid:2.16.840.1.113883.3.1937.777.24.5.1", "mide_1"),
            entry("urn:oid:2.16.840.1.113883.3.1937.777.24.5.3", "consent_policy"),
            entry("urn:oid:2.16.840.1.113883.6.43.1", "icd_o_3"),
            entry("http://hl7.org/fhir/sid/icd-o-3", "icd_o_3_fhir"),
            entry("http://hl7.org/fhir/consent-provision-type", "provision_type"),
            entry("http://fhir.de/CodeSystem/bfarm/ops", "oops"));

    private static Map<ContextualTermCode, Mapping> readMappings(ZipFile zipFile) throws IOException {
        try (var in = zipFile.getInputStream(zipFile.getEntry("ontology/mapping/mapping_cql.json"))) {
            var mapper = new ObjectMapper();
            return Arrays.stream(mapper.readValue(in, Mapping[].class))
                    .collect(Collectors.toMap(Mapping::key, Functions.identity()));
        }
    }

    private static TermCodeNode readConceptTree(ZipFile zipFile) throws IOException {
        try (var in = zipFile.getInputStream(zipFile.getEntry("ontology/mapping/mapping_tree.json"))) {
            var mapper = new ObjectMapper();
            return mapper.readValue(in, TermCodeNode.class);
        }
    }

    static Translator createTranslator() throws Exception {
        try (ZipFile zipFile = new ZipFile("target/mapping.zip")) {
            var mappings = readMappings(zipFile);
            var conceptTree = readConceptTree(zipFile);
            var mappingContext = MappingContext.of(mappings, conceptTree, CODE_SYSTEM_ALIASES);
            return Translator.of(mappingContext);
        }
    }
}
