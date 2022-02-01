package de.numcodex.sq2cql.model.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Alexander Kiel
 */
class TermCodeTest {

    @Test
    void fromJson() throws Exception {
        var mapper = new ObjectMapper();

        var termCode = mapper.readValue("""
                {
                  "system": "system-143705",
                  "code": "code-143708",
                  "display": "display-143716"
                }
                """, TermCode.class);

        assertEquals("system-143705", termCode.system());
        assertEquals("code-143708", termCode.code());
        assertEquals("display-143716", termCode.display());
    }

    @Test
    void fromJson_AdditionalPropertyIsIgnored() throws Exception {
        var mapper = new ObjectMapper();

        var termCode = mapper.readValue("""
                {
                  "system": "system-143705",
                  "code": "code-143708",
                  "display": "display-143716",
                  "foo-144401": "bar-144411"
                }
                """, TermCode.class);

        assertEquals("system-143705", termCode.system());
        assertEquals("code-143708", termCode.code());
        assertEquals("display-143716", termCode.display());
    }
}
