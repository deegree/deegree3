package org.deegree.time.gml.writer;

import static org.deegree.time.position.IndeterminateValue.BEFORE;
import static org.junit.Assert.assertEquals;

import javax.xml.stream.XMLStreamException;

import org.deegree.time.position.IndeterminateValue;
import org.deegree.time.position.TimePosition;
import org.junit.Test;

public class GmlTimePositionTypeWriterTest {

	private final GmlTimePositionTypeWriter gmlTimePositionTypeWriter = new GmlTimePositionTypeWriter();

	@Test
	public void writeMandatory() throws XMLStreamException {
		final String frame = null;
		final String calendarEraName = null;
		final IndeterminateValue indeterminatePosition = null;
		final String value = null;
		final TimePosition timePosition = new TimePosition(frame, calendarEraName, indeterminatePosition, value);
		final MemoryXmlStreamWriter xmlWriter = new MemoryXmlStreamWriter();
		xmlWriter.writeEmptyElement("TimePosition");
		gmlTimePositionTypeWriter.write(timePosition, xmlWriter);
		xmlWriter.close();
		assertEquals("<TimePosition/>", xmlWriter.getOutput());
	}

	@Test
	public void writeOptional() throws XMLStreamException {
		final String frame = "http://my.big.org/TRS/calendars/japanese";
		final String calendarEraName = "Meiji";
		final IndeterminateValue indeterminatePosition = BEFORE;
		final String value = "0025-03";
		final TimePosition timePosition = new TimePosition(frame, calendarEraName, indeterminatePosition, value);
		final MemoryXmlStreamWriter xmlWriter = new MemoryXmlStreamWriter();
		xmlWriter.writeStartElement("TimePosition");
		gmlTimePositionTypeWriter.write(timePosition, xmlWriter);
		xmlWriter.writeEndElement();
		assertEquals(
				"<TimePosition frame=\"http://my.big.org/TRS/calendars/japanese\" calendarEraName=\"Meiji\" indeterminatePosition=\"before\">0025-03</TimePosition>",
				xmlWriter.getOutput());
	}

}
