package de.numcodex.sq2cql.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.structured_query.ContextualTermCode;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Alexander Kiel
 */
class TermCodeNodeTest {


    static final TermCode CONTEXT = TermCode.of("context", "context", "context");
    static final ContextualTermCode ROOT = ContextualTermCode.of(CONTEXT, TermCode.of("foo", "root", "root"));
    static final ContextualTermCode C1 = ContextualTermCode.of(CONTEXT, TermCode.of("foo", "c1", "c1"));
    static final ContextualTermCode C2 = ContextualTermCode.of(CONTEXT, TermCode.of("foo", "c2", "c2"));
    static final ContextualTermCode C11 = ContextualTermCode.of(CONTEXT, TermCode.of("foo", "c11", "c11"));
    static final ContextualTermCode C12 = ContextualTermCode.of(CONTEXT, TermCode.of("foo", "c12", "c12"));
    static final ContextualTermCode C111 = ContextualTermCode.of(CONTEXT, TermCode.of("foo", "c111", "c111"));
    static final ContextualTermCode C112 = ContextualTermCode.of(CONTEXT, TermCode.of("foo", "c112", "c112"));

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
                {
                "context": {
                  "system": "context-152133",
                  "code": "context-152136",
                  "display": "context-152144"
                },
                "termCode": {
                   "system": "system-143705",
                   "code": "code-143708",
                   "display": "display-143716"
                 },
                 "children": []
                }
                """, TermCodeNode.class);
        assertEquals(ContextualTermCode.of(TermCode.of("context-152133", "context-152136", "context-152144"),
                TermCode.of("system-143705", "code-143708", "display-143716")), conceptNode.contextualTermCode());
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

        assertEquals("system-143705", conceptNode.contextualTermCode().termCode().system());
    }

    @Test
    void fromJson_WithChildren() throws Exception {
        var mapper = new ObjectMapper();

        var conceptNode = mapper.readValue("""
                {
                "context": {
                  "system": "context-152133",
                  "code": "context-152136",
                  "display": "context-152144"
                },
                "termCode": {
                   "system": "system-143705",
                   "code": "code-143708",
                   "display": "display-143716"
                 },
                 "children": [
                  {"context": {
                     "system": "child-1-context-155856",
                     "code": "child-1-context-155858",
                     "display": "child-1-context-155900"
                  },
                  "termCode": {
                     "system": "child-1-system-155856",
                     "code": "child-1-code-155858",
                     "display": "child-1-display-155900"
                  }},
                  {
                  "context": {
                     "system": "child-2-context-155956",
                     "code": "child-2-context-155958",
                     "display": "child-2-context-160000"
                  },
                  "termCode": {
                     "system": "child-2-system-155958",
                     "code": "child-2-code-160000",
                     "display": "child-2-display-160002"
                  }}
                 ]
                }
                """, TermCodeNode.class);

        assertEquals(ContextualTermCode.of(
                TermCode.of("context-152133", "context-152136", "context-152144"),
                TermCode.of("system-143705", "code-143708", "display-143716")), conceptNode.contextualTermCode());
        assertEquals(ContextualTermCode.of(
                        TermCode.of("child-1-context-155856", "child-1-context-155858", "child-1-context-155900"),
                        TermCode.of("child-1-system-155856", "child-1-code-155858", "child-1-display-155900")),
                conceptNode.children().get(0).contextualTermCode());
        assertEquals(ContextualTermCode.of(
                        TermCode.of("child-2-context-155956", "child-2-context-155958", "child-2-context-160000"),
                        TermCode.of("child-2-system-155958", "child-2-code-160000", "child-2-display-160002")),
                conceptNode.children().get(1).contextualTermCode());

    }
}
