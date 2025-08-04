package de.numcodex.sq2cql.model.mapping;

import ca.uhn.fhir.context.BaseRuntimeElementDefinition;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import org.hl7.fhir.instance.model.api.IBaseDatatype;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public record AttributeComponent(List<BaseRuntimeElementDefinition<?>> types, String path, Cardinality cardinality,
                                 List<IBaseDatatype> values) implements AttributeTreeNode {

    public AttributeComponent {
        requireNonNull(types);
        if (types.isEmpty()) throw new IllegalArgumentException("At least one type should be provided");
        requireNonNull(path);
        requireNonNull(cardinality);
        requireNonNull(values);
    }

    public static AttributeComponent of(
            List<BaseRuntimeElementDefinition<?>> types,
            String path,
            Cardinality cardinality,
            List<IBaseDatatype> values
    ) {
        return new AttributeComponent(types, path, cardinality, values);
    }

    @JsonCreator
    public static AttributeComponent of(
            @JsonProperty("types") List<String> types,
            @JsonProperty("path") String path,
            @JsonProperty("cardinality") Cardinality cardinality,
            @JsonProperty("components") @JsonDeserialize(using = StringDeserializer.class) List<String> values,
            @JacksonInject FhirContext ctx
    ) {
        List<BaseRuntimeElementDefinition<?>> elemDefs = types.stream().map(ctx::getElementDefinition).collect(Collectors.toList());
        return new AttributeComponent(
                elemDefs,
                path,
                cardinality,
                parseValues(values, elemDefs, ctx)
        );
    }

    private static List<IBaseDatatype> parseValues(List<String> values, List<BaseRuntimeElementDefinition<?>> types, FhirContext ctx) {
        IParser parser = ctx.newJsonParser();
        List<IBaseDatatype> parsedValues = new ArrayList<>(values.size());
        for (var v : values) {
            for (var t : types) {
                try {
                    IBaseDatatype instance = (IBaseDatatype) t.newInstance();
                    parser.parseInto(v, instance);
                    parsedValues.add(instance);
                    break;
                } catch (Exception ignored) {
                }
                throw new IllegalArgumentException("Value '" + v + "' cannot be parsed as any of data types " + types + " supported by the component ");
            }
        }
        return parsedValues;
    }

}
