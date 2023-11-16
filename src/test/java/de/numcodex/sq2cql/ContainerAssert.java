package de.numcodex.sq2cql;

import de.numcodex.sq2cql.model.cql.Container;
import org.assertj.core.api.AbstractObjectAssert;

public class ContainerAssert extends AbstractObjectAssert<ContainerAssert, Container<?>> {

    protected ContainerAssert(Container<?> library) {
        super(library, ContainerAssert.class);
    }

    public void printsTo(String expected) {
        returns(expected, Container::print);
    }

    public void patientContextPrintsTo(String expected) {
        returns(expected, Container::printPatientContext);
    }
}
