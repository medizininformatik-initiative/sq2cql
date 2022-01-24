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
    void fromJson_WithoutTermCodes() {
        var mapper = new ObjectMapper();

        try {
            mapper.readValue("{}", Criterion.class);
            fail();
        } catch (JsonProcessingException e) {
            assertEquals("missing JSON property: termCodes", e.getCause().getMessage());
        }
    }

    @Test
    void fromJson_UnknownValueFilterType() {
        var mapper = new ObjectMapper();

        try {
            mapper.readValue("""
                    {
                      "termCodes": [],
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
