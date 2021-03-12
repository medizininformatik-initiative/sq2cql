package com.codex.sq2cql.model.cql;

import com.codex.sq2cql.PrintContext;

/**
 * @author Alexander Kiel
 */
public interface Statement {

    String print(PrintContext printContext);
}
