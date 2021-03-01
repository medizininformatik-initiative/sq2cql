package com.codex.sq2cql.data_model.cql.translator;

import com.codex.sq2cql.data_model.common.TermCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConceptMapper {
    private final static HashMap<Integer, String> conceptMap = new HashMap<>() {{
        put(calculateHash(List.of("https://loinc.org", "LL2191-6")), "Patient.gender");
        put(calculateHash(List.of("http://fhir.de/CodeSystem/dimdi/icd-10-gm", "C71.0")), "Condition");
        put(calculateHash(List.of("http://fhir.de/CodeSystem/dimdi/icd-10-gm", "C71.1")), "Condition");
        put(calculateHash(List.of("http://fhir.de/CodeSystem/dimdi/icd-10-gm", "C71.2")), "Condition");
        put(calculateHash(List.of("http://fhir.de/CodeSystem/dimdi/icd-10-gm", "C71.3")), "Condition");
        put(calculateHash(List.of("http://fhir.de/CodeSystem/dimdi/icd-10-gm", "C71.4")), "Condition");
        put(calculateHash(List.of("http://fhir.de/CodeSystem/dimdi/icd-10-gm", "C71.5")), "Condition");
        put(calculateHash(List.of("https://loinc.org", "26515-7")), "Observation");
    }};

    private static int calculateHash(List<String> strings) {
        final int prime = 31;
        int result = 1;
        for (String s : strings) {
            result = result * prime + s.hashCode();
        }
        return result;
    }

    public static String getConcept(String system, String code) {
        var stringList = new ArrayList<String>();
        stringList.add(system);
        stringList.add(code);
        var key = calculateHash(stringList);
        if (conceptMap.containsKey(key)) {
            return conceptMap.get(calculateHash(stringList));
        }
        else {
            return code;
        }
    }

    public static String getConcept(TermCode termCode)
    {
        return getConcept(termCode.getSystem(), termCode.getCode());
    }
}
