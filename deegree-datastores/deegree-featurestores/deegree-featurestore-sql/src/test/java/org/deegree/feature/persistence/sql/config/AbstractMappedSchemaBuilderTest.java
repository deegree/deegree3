package org.deegree.feature.persistence.sql.config;

import org.deegree.feature.persistence.sql.expressions.Function;
import org.deegree.feature.persistence.sql.expressions.StringConst;
import org.deegree.sqldialect.filter.DBField;
import org.deegree.sqldialect.filter.MappingExpression;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.deegree.feature.persistence.sql.config.AbstractMappedSchemaBuilder.parseMappingExpression;
import static org.junit.Assert.*;

public class AbstractMappedSchemaBuilderTest {

	@Test
	public void testParseStringConst() {
		MappingExpression expression = parseMappingExpression("'foo'");
		assertNotNull(expression);
		assertTrue(expression instanceof StringConst);
		assertEquals("'foo'", expression.toString());
	}

	@Test
	public void testParseDBField() {
		MappingExpression expression = parseMappingExpression("column");
		assertNotNull(expression);
		assertTrue(expression instanceof DBField);
		DBField dbField = (DBField) expression;
		assertNull(dbField.getSchema());
		assertNull(dbField.getTable());
		assertEquals("column", dbField.getColumn());

		expression = parseMappingExpression("table.column");
		assertNotNull(expression);
		assertTrue(expression instanceof DBField);
		dbField = (DBField) expression;
		assertNull(dbField.getSchema());
		assertEquals("table", dbField.getTable());
		assertEquals("column", dbField.getColumn());

		expression = parseMappingExpression("schema.table.column");
		assertNotNull(expression);
		assertTrue(expression instanceof DBField);
		dbField = (DBField) expression;
		assertEquals("schema", dbField.getSchema());
		assertEquals("table", dbField.getTable());
		assertEquals("column", dbField.getColumn());
	}

	@Test
	public void testParseFunction() {
		MappingExpression expression = parseMappingExpression("foo('bar')");
		assertNotNull(expression);
		assertTrue(expression instanceof Function);
		Function function = (Function) expression;
		assertEquals("foo", function.toString());
		List<MappingExpression> functionArgs = function.getArgs();
		assertNotNull(functionArgs);
		assertEquals(1, functionArgs.size());
		MappingExpression arg = functionArgs.get(0);
		assertNotNull(arg);
		assertTrue(arg instanceof StringConst);
		assertEquals("'bar'", arg.toString());
	}

	@Test
	public void testParseRecognitionException() {
		String input = "CASE WHEN diameter IS NULL THEN TRUE ELSE FALSE END";
		MappingExpression expression = parseMappingExpression(input);
		assertNotNull(expression);
		assertTrue(expression instanceof Function);
		Function function = (Function) expression;
		assertEquals(input, function.toString());
		assertEquals(Collections.emptyList(), function.getArgs());

		input = "('https://example.org/' || value || '.txt)";
		expression = parseMappingExpression(input);
		assertNotNull(expression);
		assertTrue(expression instanceof Function);
		function = (Function) expression;
		assertEquals(input, function.toString());
		assertEquals(Collections.emptyList(), function.getArgs());
	}

}
