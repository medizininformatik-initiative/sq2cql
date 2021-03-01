package com.codex.sq2cql.data_model.cql;

import java.util.ArrayList;
import java.util.List;

public class NamedTypeSpecifier implements TypeSpecifier {
    private List<String> fullQualifier = new ArrayList<>();
    private String type;

    public NamedTypeSpecifier(String type) {
        this.type = type;
    }

    public void setFullQualifier(List<String> fullQualifier) {
        this.fullQualifier = fullQualifier;
    }

    public String toString()
    {
        return fullQualifier.isEmpty() ? type : String.join(".", fullQualifier) +  "." + type;
    }

    public String getType() {
        return type;
    }
}
