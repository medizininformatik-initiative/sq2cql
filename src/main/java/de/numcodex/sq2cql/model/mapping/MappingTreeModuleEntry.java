package de.numcodex.sq2cql.model.mapping;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MappingTreeModuleEntry(String key, List<String> children) {
    @JsonCreator
    static MappingTreeModuleEntry fromJson(@JsonProperty("key") String key,
                                           @JsonProperty("children") List<String> children) {
        return new MappingTreeModuleEntry(key, children);
    }
}
