package com.codex.sq2cql.data_model.cql;

import java.beans.Expression;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

public class LibraryGenerator {
    static public String generateLibrary(String library, Optional<String> version)
    {
        return version.isPresent() ? "library %s version %s\n".formatted(library, version) :
                "library %s\n".formatted(library);
    }

    static public String generateUsing(String model, Optional<String> version)
    {
        return version.isPresent() ? "using %s version %s\n".formatted(model, version) :
                "using %s\n".formatted(model);
    }

    static public String generateInclude(String identifier, Optional<String> version, Optional<String> localIdentifier)
    {
        return "include %s".formatted(identifier) + (version.isPresent() ? " version %s".formatted(version) : "")
                + (localIdentifier.isPresent() ? " called %s".formatted(localIdentifier) : "") + "\n";
    }

    static public String generateContext(String context)
    {
        return "context %s".formatted(context);
    }

    static public String generateDefinition(String valueName, Expression expression)
    {
        return "define %s:\n%s".formatted(valueName, expression.toString());
    }

    static public String generateCodesystems(HashMap<String, String> codesystemWithAlias)
    {
        StringBuilder result = new StringBuilder("");
        for (Map.Entry<String, String> entry : codesystemWithAlias.entrySet())
        {
            result.append("codesystem %s: %s\n".formatted(entry.getValue(), entry.getKey()));
        }
        return result.toString();
    }


}
