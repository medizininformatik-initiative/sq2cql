package de.medizininformatikinitiative.cctb;

import de.medizininformatikinitiative.cctb.model.cql.Container;
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
