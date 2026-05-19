package de.medizininformatikinitiative.cctb;

import de.medizininformatikinitiative.cctb.model.cql.Container;
import org.assertj.core.api.SoftAssertions;

public class SoftContainerAssertions extends SoftAssertions {

    public ContainerAssert assertThat(Container<?> library) {
        return proxy(ContainerAssert.class, Container.class, library);
    }

}
