package de.numcodex.sq2cql.model.structured_query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Alexander Kiel
 */
class CriterionTest {

    @Test
    void fromJson_MissingValues() {
        var mapper = new ObjectMapper();

        try {
            mapper.readValue("""
                    {
                      "type": "foo",
                      "fhirPath": "bar",
                      "valueFilter": {
                        "type": "foo"
                      }
                    }
                    """, Criterion.class);
            fail();
        } catch (JsonProcessingException e) {
            assertEquals("unknown valueFilter type: foo", e.getCause().getMessage());
        }
    }
}
