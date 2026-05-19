package de.medizininformatikinitiative.cctb.model.structured_query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.JsonNode;
import de.medizininformatikinitiative.cctb.model.Mapping;
import de.medizininformatikinitiative.cctb.model.MappingContext;
import de.medizininformatikinitiative.cctb.model.common.TermCode;
import de.medizininformatikinitiative.cctb.model.cql.Container;
import de.medizininformatikinitiative.cctb.model.cql.QueryExpression;

import java.util.List;
import java.util.stream.Stream;

/**
 * @author Alexander Kiel
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public interface Modifier {

    @JsonCreator
    static Modifier create(@JsonProperty("types") List<String> types,
                           @JsonProperty("path") String path,
                           @JsonProperty("cardinality") Mapping.Cardinality cardinality,
                           @JsonProperty("value") JsonNode... values) {
        if (values == null) {
            throw new IllegalArgumentException("missing modifier values");
        }

        if (values.length == 0) {
            throw new IllegalArgumentException("empty modifier values");
        }

        if (List.of("code").equals(types)) {
            return new CodeModifier(path, Stream.of(values).map(TermCode::fromJsonNode).map(TermCode::code).toList());
        }

        if (List.of("Coding").equals(types) || List.of("CodeableConcept").equals(types)) {
            return new CodeEquivalentModifier(path, cardinality == null ? Mapping.Cardinality.SINGLE : cardinality, Stream.of(values).map(TermCode::fromJsonNode).toList());
        }

        throw new IllegalArgumentException("unknown types: " + String.join(", ", types));
    }

    Container<QueryExpression> updateQuery(MappingContext mappingContext, Container<QueryExpression> queryContainer);
}
