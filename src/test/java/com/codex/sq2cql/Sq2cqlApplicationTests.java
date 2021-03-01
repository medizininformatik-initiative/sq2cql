package com.codex.sq2cql;

import com.codex.sq2cql.data_model.common.TermCode;
import com.codex.sq2cql.data_model.cql.translator.CNFTranslator;
import com.codex.sq2cql.data_model.cql.translator.DNFTranslator;
import com.codex.sq2cql.data_model.structured_query.Criterion;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

//TODO: Create algorithm to remove redundant parenthesis from logic expression -> Change expected result to display name (02.02.2021 lorenz)

class Sq2cqlApplicationTests {

	@Nested
	@DisplayName("CNF")
	class CNF {
		@Test
		@DisplayName("A and B")
		public void cnfAandB ()
		{
			var criterionA = new Criterion(new TermCode("A", "test.org"));
			var criterionB = new Criterion(new TermCode("B", "test.org"));
			var cnf = (List.of(List.of(criterionA), List.of(criterionB)));
			var expression = CNFTranslator.parseCNF(cnf);
			assertEquals("((A)) and\n((B))", expression.toString());
		}

		@Test
		@DisplayName("A or B")
		public void cnfAorB ()
		{
			var criterionA = new Criterion(new TermCode("A", "test.org"));
			var criterionB = new Criterion(new TermCode("B", "test.org"));
			var cnf = (List.of(List.of(criterionA, criterionB)));
			var expression = CNFTranslator.parseCNF(cnf);
			assertEquals("((A) or\n(B))", expression.toString());
		}

		@Test
		@DisplayName("A and (B or C)")
		public void cnfAandBorC ()
		{
			var criterionA = new Criterion(new TermCode("A", "test.org"));
			var criterionB = new Criterion(new TermCode("B", "test.org"));
			var criterionC = new Criterion(new TermCode("C", "test.org"));
			var cnf = (List.of(List.of(criterionA), List.of(criterionB, criterionC)));
			var expression = CNFTranslator.parseCNF(cnf);
			assertEquals("((A)) and\n((B) or\n(C))", expression.toString());
		}
	}

	@Nested
	@DisplayName("DNF")
	class DNF {

		@Test
		@DisplayName("A and B")
		public void dnfAandB ()
		{
			var criterionA = new Criterion(new TermCode("A", "test.org"));
			var criterionB = new Criterion(new TermCode("B", "test.org"));
			var dnf = (List.of(List.of(criterionA, criterionB)));
			var expression = DNFTranslator.parseDNF(dnf);
			assertEquals("((A) and\n(B))", expression.toString());

		}

		@Test
		@DisplayName("A or B")
		public void dnfAorB ()
		{
			var criterionA = new Criterion(new TermCode("A", "test.org"));
			var criterionB = new Criterion(new TermCode("B", "test.org"));
			var dnf = (List.of(List.of(criterionA), List.of(criterionB)));
			var expression = DNFTranslator.parseDNF(dnf);
			assertEquals("((A)) or\n((B))", expression.toString());
		}


		@Test
		@DisplayName("A or (B and C)")
		public void dnfAorBandC() {
			var criterionA = new Criterion(new TermCode("A", "test.org"));
			var criterionB = new Criterion(new TermCode("B", "test.org"));
			var criterionC = new Criterion(new TermCode("C", "test.org"));
			var dnf = (List.of(List.of(criterionA), List.of(criterionB, criterionC)));
			var expression = DNFTranslator.parseDNF(dnf);
			assertEquals("((A)) or\n((B) and\n(C))", expression.toString());
		}
	}

}
