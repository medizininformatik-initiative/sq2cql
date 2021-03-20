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
class ConceptNodeTest {

    public static final TermCode ROOT = TermCode.of("foo", "root", "root");
    public static final TermCode C1 = TermCode.of("foo", "c1", "c1");
    public static final TermCode C2 = TermCode.of("foo", "c2", "c2");
    public static final TermCode C11 = TermCode.of("foo", "c11", "c11");
    public static final TermCode C12 = TermCode.of("foo", "c12", "c12");
    public static final TermCode C111 = TermCode.of("foo", "c111", "c111");
    public static final TermCode C112 = TermCode.of("foo", "c112", "c112");

    @Test
    void noChildren() {
        var node = ConceptNode.of();

        assertTrue(node.getChildren().isEmpty());
    }

    @Test
    void expandSelfLeaf() {
        var node = ConceptNode.of(ROOT);

        assertEquals(Set.of(ROOT), node.expand(ROOT).collect(Collectors.toSet()));
    }

    @Test
    void expandSelf() {
        var node = ConceptNode.of(ROOT, ConceptNode.of(C1), ConceptNode.of(C2));

        assertEquals(Set.of(C1, C2), node.expand(ROOT).collect(Collectors.toSet()));
    }

    @Test
    void expandChild() {
        var c1 = ConceptNode.of(C1, ConceptNode.of(C11), ConceptNode.of(C12));
        var node = ConceptNode.of(ROOT, c1, ConceptNode.of(C2));

        assertEquals(Set.of(C11, C12), node.expand(C1).collect(Collectors.toSet()));
    }

    @Test
    void expandChildDeep() {
        var c11 = ConceptNode.of(C11, ConceptNode.of(C111), ConceptNode.of(C112));
        var c1 = ConceptNode.of(C1, c11, ConceptNode.of(C12));
        var node = ConceptNode.of(ROOT, c1, ConceptNode.of(C2));

        assertEquals(Set.of(C111, C112, C12), node.expand(C1).collect(Collectors.toSet()));
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
                """, ConceptNode.class);

        assertEquals("system-143705", conceptNode.getConcept().getSystem());
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
                """, ConceptNode.class);

        assertEquals("system-143705", conceptNode.getConcept().getSystem());
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
                """, ConceptNode.class);

        assertEquals("system-143705", conceptNode.getConcept().getSystem());
        assertEquals("child-1-system-155856", conceptNode.getChildren().get(0).getConcept().getSystem());
        assertEquals("child-2-system-155958", conceptNode.getChildren().get(1).getConcept().getSystem());
    }
}
