package de.numcodex.sq2cql;

import de.numcodex.sq2cql.model.cql.Container;
import org.assertj.core.api.SoftAssertions;

public class SoftContainerAssertions extends SoftAssertions {

    public ContainerAssert assertThat(Container<?> library) {
        return proxy(ContainerAssert.class, Container.class, library);
    }

}
