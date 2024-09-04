package de.numcodex.sq2cql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Functions;
import de.numcodex.sq2cql.model.*;
import de.numcodex.sq2cql.model.structured_query.ContextualTermCode;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
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
        try (var in = zipFile.getInputStream(zipFile.getEntry("mapping/cql/mapping_cql.json"))) {
            var mapper = new ObjectMapper();
            return Arrays.stream(mapper.readValue(in, Mapping[].class))
                    .collect(Collectors.toMap(Mapping::key, Functions.identity()));
        }
    }

    private static MappingTreeBase readConceptTree(ZipFile zipFile) throws IOException {
        try (var in = zipFile.getInputStream(zipFile.getEntry("mapping/mapping_tree.json"))) {
            var mapper = new ObjectMapper();
            return new MappingTreeBase(
                    Arrays.stream(mapper.readValue(in, MappingTreeModuleRoot[].class)).toList());
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

    static MappingTreeBase createTreeWithoutChildren(ContextualTermCode c) {
        return new MappingTreeBase(List.of(new MappingTreeModuleRoot(c.context(), c.termCode().system(), Map.of(c.termCode().code(),
                new MappingTreeModuleEntry(c.termCode().code(), List.of())))));
    }

    static MappingTreeBase createTreeWithChildren(ContextualTermCode c, ContextualTermCode child1, ContextualTermCode child2) {
        return new MappingTreeBase(List.of(createTreeRootWithChildren(c, child1, child2)));
    }

    static MappingTreeModuleRoot createTreeRootWithChildren(ContextualTermCode c, ContextualTermCode child1, ContextualTermCode child2) {
        return new MappingTreeModuleRoot(c.context(), c.termCode().system(), Map.of(
                c.termCode().code(), new MappingTreeModuleEntry(c.termCode().code(), List.of(child1.termCode().code(), child2.termCode().code())),
                child1.termCode().code(), new MappingTreeModuleEntry(child1.termCode().code(), List.of()),
                child2.termCode().code(), new MappingTreeModuleEntry(child2.termCode().code(), List.of())));
    }

    static MappingTreeModuleRoot createTreeRootWithoutChildren(ContextualTermCode c) {
        return new MappingTreeModuleRoot(c.context(), c.termCode().system(), Map.of(
                c.termCode().code(), new MappingTreeModuleEntry(c.termCode().code(), List.of())));
    }
}
