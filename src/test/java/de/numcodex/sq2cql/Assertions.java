package de.numcodex.sq2cql;

import de.numcodex.sq2cql.model.cql.Container;

public interface Assertions {

    static ContainerAssert assertThat(Container<?> library) {
        return new ContainerAssert(library);
    }
}
