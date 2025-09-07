package de.numcodex.sq2cql.model.mapping;

import ca.uhn.fhir.context.BaseRuntimeElementDefinition;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import de.numcodex.sq2cql.model.common.Comparator;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.cql.*;
import de.numcodex.sq2cql.model.structured_query.*;
import org.hl7.fhir.instance.model.api.IBaseDatatype;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Quantity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CodingAttributeComponent.class, names = {"Coding", "CodeableConcept"}),
        @JsonSubTypes.Type(value = QuantityAttributeComponent.class, names = {"Quantity"}),
        @JsonSubTypes.Type(value = PeriodAttributeComponent.class, names = {"Period"})
})
public abstract non-sealed class AttributeComponent<T extends IBaseDatatype, E extends Enum<E>> implements AttributeTreeNode<T> {

    private final String path;
    private final Mapping.Cardinality cardinality;
    private final E operator;
    private final String valueType;
    private final List<T> values;

    public AttributeComponent(String path, Mapping.Cardinality cardinality, E operator, String valueType, List<T> values) {
        requireNonNull(path);
        requireNonNull(cardinality);
        requireNonNull(operator);
        if (valueType != null && !supportedValueTypes().contains(valueType)) throw new IllegalArgumentException("Unsupported value data type %s. Expected one of ".formatted(valueType, String.join(", ", supportedValueTypes())));
        this.path = path;
        this.cardinality = cardinality;
        this.operator = operator;
        this.valueType = valueType;
        this.values = values != null ? Collections.unmodifiableList(values) : Collections.emptyList();
    }

    @JsonCreator
    public static AttributeComponent of(
            @JsonProperty("type") String type,
            @JsonProperty("path") String path,
            @JsonProperty("cardinality") Mapping.Cardinality cardinality,
            @JsonProperty("operator") @JsonDeserialize(using = StringDeserializer.class) String operator,
            @JsonProperty("valueType") String valueType,
            @JsonProperty("values") @JsonDeserialize(using = StringDeserializer.class) List<String> values,
            @JacksonInject FhirContext ctx
    ) {
        var typeDef = ctx.getElementDefinition(valueType);
        if (typeDef == null)
            throw new IllegalArgumentException("No such data type '%s' in HL7 FHIR version %s".formatted(valueType, ctx.getVersion().toString()));
        var parsedValues = parseValues(values, typeDef, ctx.newJsonParser());
        switch (type) {
            case "Coding", "CodeableConcept" ->
                    new CodingAttributeComponent(
                            path,
                            cardinality,
                            CodingAttributeComponent.Operator.valueOf(operator.toLowerCase()),
                            valueType,
                            parsedValues
                    );
            case "Quantity" ->
                    new QuantityAttributeComponent(
                            path,
                            cardinality,
                            QuantityAttributeComponent.Operator.valueOf(operator.toLowerCase()),
                            valueType,
                            parsedValues
                    );
            case "Period" -> new
        }
    }

    private static List<IBaseDatatype> parseValues(
            List<String> values,
            BaseRuntimeElementDefinition<?> typeDef,
            IParser parser
    ) {
        return values.stream().map(v -> {
            var instance = typeDef.newInstance();
            parser.parseInto(v, instance);
            return (IBaseDatatype) instance;
        }).collect(Collectors.toList());
    }

    public String path() {
        return this.path;
    }

    public Mapping.Cardinality cardinality() {
        return this.cardinality;
    }

    public E operator() {
        return this.operator;
    }

    public String valueType() {
        return this.valueType;
    }

    public List<T> values() {
        return this.values;
    }

    public abstract Set<String> supportedValueTypes();
}
