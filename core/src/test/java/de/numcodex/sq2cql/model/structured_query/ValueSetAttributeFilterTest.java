package de.numcodex.sq2cql.model.structured_query;

import de.numcodex.sq2cql.model.common.TermCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValueSetAttributeFilterTest {

    private static final TermCode CODE = TermCode.of("foo", "bar", "baz");
    private static final TermCode CODE_1 = TermCode.of("foo1", "bar1", "baz1");

    @Test
    void of_withoutSelectedConcepts() {
        assertThatThrownBy(() -> ValueSetAttributeFilter.of(CODE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("empty selected concepts");
    }

    @Test
    void of_withOneSelectedConcept() {
        var filter = ValueSetAttributeFilter.of(CODE, CODE_1);

        assertThat(filter.attributeCode()).isEqualTo(CODE);
        assertThat(filter.selectedConcepts()).containsExactly(CODE_1);
    }
}
