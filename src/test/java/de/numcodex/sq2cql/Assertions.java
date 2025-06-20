package de.numcodex.sq2cql;

import de.numcodex.sq2cql.model.cql.Container;
import org.assertj.core.api.SoftAssertionsProvider;

import java.util.function.Consumer;

public interface Assertions {

    static ContainerAssert assertThat(Container<?> library) {
        return new ContainerAssert(library);
    }

    static void assertSoftly(Consumer<SoftContainerAssertions> softly) {
        SoftAssertionsProvider.assertSoftly(SoftContainerAssertions.class, softly);
    }

}
