package de.numcodex.sq2cql.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FhirModelInfo {

    public static final String RESOURCE_PATH = "/de/numcodex/sq2cql/elm/elm-modelinfo.xml";

    private static final FhirModelInfo INSTANCE = new FhirModelInfo();

    private final Set<String> retrievableTypes;
    private final Map<String, Set<String>> searchPaths;

    private FhirModelInfo() {
        JsonNode tree;
        try (InputStream is = FhirModelInfo.class.getResourceAsStream(RESOURCE_PATH)) {
            var mapper = new XmlMapper();
            tree = mapper.readTree(is);
        } catch (Exception exc) {
            throw new RuntimeException("Failed to initialize ELM FHIR model info lookup", exc);
        }

        var typeInfo = tree.get("typeInfo");
        retrievableTypes = typeInfo.valueStream()
                .filter(v -> v.get("retrievable").asBoolean())
                .map(v -> v.get("name").asText())
                .collect(Collectors.toSet());
        searchPaths = typeInfo.valueStream()
                .collect(Collectors.toMap(
                        v -> v.get("name").asText(),
                        v ->  {
                            var search = v.get("search");
                            return search == null ?
                                    Collections.emptySet()
                                    :
                                    search.valueStream().filter(s -> s.has("path"))
                                            .map(s -> s.get("path").asText()).collect(Collectors.toSet());
                        }
                ));
    }

    public static boolean isRetrievableType(String typeIdentifier) {
        return INSTANCE.retrievableTypes.contains(typeIdentifier);
    }

    public static Set<String> searchPaths(String typeIdentifier) {
        return INSTANCE.searchPaths.getOrDefault(typeIdentifier, Collections.emptySet());
    }

    public static boolean isRetrievable(String typeIdentifier, String path) {
        return isRetrievableType(typeIdentifier) && searchPaths(typeIdentifier).contains(path);
    }

}
