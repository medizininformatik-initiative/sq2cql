package de.numcodex.sq2cql.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.numcodex.sq2cql.model.common.TermCode;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Alexander Kiel
 */
class TermCodeNodeTest {

    public static final TermCode ROOT = TermCode.of("foo", "root", "root");
    public static final TermCode C1 = TermCode.of("foo", "c1", "c1");
    public static final TermCode C2 = TermCode.of("foo", "c2", "c2");
    public static final TermCode C11 = TermCode.of("foo", "c11", "c11");
    public static final TermCode C12 = TermCode.of("foo", "c12", "c12");
    public static final TermCode C111 = TermCode.of("foo", "c111", "c111");
    public static final TermCode C112 = TermCode.of("foo", "c112", "c112");

    @Test
    void noChildren() {
        var node = TermCodeNode.of(ROOT);

        assertTrue(node.children().isEmpty());
    }

    @Test
    void expandSelfLeaf() {
        var node = TermCodeNode.of(ROOT);

        assertEquals(Set.of(ROOT), node.expand(ROOT).collect(Collectors.toSet()));
    }

    @Test
    void expandSelf() {
        var node = TermCodeNode.of(ROOT, TermCodeNode.of(C1), TermCodeNode.of(C2));

        assertEquals(Set.of(ROOT, C1, C2), node.expand(ROOT).collect(Collectors.toSet()));
    }

    @Test
    void expandChildAndSelf() {
        var c1 = TermCodeNode.of(C1, TermCodeNode.of(C11), TermCodeNode.of(C12));
        var node = TermCodeNode.of(ROOT, c1, TermCodeNode.of(C2));

        assertEquals(Set.of(C1, C11, C12), node.expand(C1).collect(Collectors.toSet()));
    }

    @Test
    void expandChildDeep() {
        var c11 = TermCodeNode.of(C11, TermCodeNode.of(C111), TermCodeNode.of(C112));
        var c1 = TermCodeNode.of(C1, c11, TermCodeNode.of(C12));
        var node = TermCodeNode.of(ROOT, c1, TermCodeNode.of(C2));

        assertEquals(Set.of(C1, C11, C12, C111, C112), node.expand(C1).collect(Collectors.toSet()));
    }

    @Test
    void fromJson() throws Exception {
        var mapper = new ObjectMapper();

        var conceptNode = mapper.readValue("""
                {"termCode": {
                   "system": "system-143705",
                   "code": "code-143708",
                   "display": "display-143716"
                 },
                 "children": []
                }
                """, TermCodeNode.class);

        assertEquals("system-143705", conceptNode.termCode().system());
    }

    @Test
    void fromJson_AdditionalPropertyIsIgnored() throws Exception {
        var mapper = new ObjectMapper();

        var conceptNode = mapper.readValue("""
                {"foo-152133": "bar-152136",
                 "termCode": {
                   "system": "system-143705",
                   "code": "code-143708",
                   "display": "display-143716"
                 },
                 "children": []
                }
                """, TermCodeNode.class);

        assertEquals("system-143705", conceptNode.termCode().system());
    }

    @Test
    void fromJson_WithChildren() throws Exception {
        var mapper = new ObjectMapper();

        var conceptNode = mapper.readValue("""
                {"termCode": {
                   "system": "system-143705",
                   "code": "code-143708",
                   "display": "display-143716"
                 },
                 "children": [
                  {"termCode": {
                     "system": "child-1-system-155856",
                     "code": "child-1-code-155858",
                     "display": "child-1-display-155900"
                  }},
                  {"termCode": {
                     "system": "child-2-system-155958",
                     "code": "child-2-code-160000",
                     "display": "child-2-display-160002"
                  }}
                 ]
                }
                """, TermCodeNode.class);

        assertEquals("system-143705", conceptNode.termCode().system());
        assertEquals("child-1-system-155856", conceptNode.children().get(0).termCode().system());
        assertEquals("child-2-system-155958", conceptNode.children().get(1).termCode().system());
    }
}
