package de.numcodex.sq2cql.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.numcodex.sq2cql.model.common.TermCode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    void expandSelfLeaf() {
        var node = ConceptNode.of(ROOT);

        assertEquals(Set.of(ROOT), node.expand(ROOT).collect(Collectors.toSet()));
    }

    @Test
    void expandSelf() {
        var node = ConceptNode.of(ROOT, List.of(ConceptNode.of(C1), ConceptNode.of(C2)));

        assertEquals(Set.of(C1, C2), node.expand(ROOT).collect(Collectors.toSet()));
    }

    @Test
    void expandChild() {
        var c1 = ConceptNode.of(C1, List.of(ConceptNode.of(C11), ConceptNode.of(C12)));
        var node = ConceptNode.of(ROOT, List.of(c1, ConceptNode.of(C2)));

        assertEquals(Set.of(C11, C12), node.expand(C1).collect(Collectors.toSet()));
    }

    @Test
    void expandChildDeep() {
        var c11 = ConceptNode.of(C11, List.of(ConceptNode.of(C111), ConceptNode.of(C112)));
        var c1 = ConceptNode.of(C1, List.of(c11, ConceptNode.of(C12)));
        var node = ConceptNode.of(ROOT, List.of(c1, ConceptNode.of(C2)));

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
}
