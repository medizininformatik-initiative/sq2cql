# CODEX - Structured Query to CQL Translator

## Usage

```
var neoplasm = TermCode.of("http://fhir.de/CodeSystem/dimdi/icd-10-gm", "C71", "Malignant neoplasm of brain");
var codeSystemAliases = Map.of("http://fhir.de/CodeSystem/dimdi/icd-10-gm", "icd10");
var mappingContext = MappingContext.of(Map.of(neoplasm, Mapping.of(neoplasm, "Condition")), codeSystemAliases);

Library library = Translator.of(mappingContext).toCql(StructuredQuery.of(List.of(
        List.of(ConceptCriterion.of(neoplasm)))));

assertEquals("""
        library Retrieve
        using FHIR version '4.0.0'
        include FHIRHelpers version '4.0.0'
                                           
        codesystem icd10: 'http://fhir.de/CodeSystem/dimdi/icd-10-gm'                
                        
        define InInitialPopulation:
          exists([Condition: Code 'C71' from icd10])
        """, library.print(ZERO));
```

## License

Copyright [yyyy] [name of copyright owner]

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "
AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
language governing permissions and limitations under the License.
