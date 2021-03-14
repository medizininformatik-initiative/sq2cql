package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

/**
 * @author Alexander Kiel
 */
public interface Statement {

    String print(PrintContext printContext);
}
