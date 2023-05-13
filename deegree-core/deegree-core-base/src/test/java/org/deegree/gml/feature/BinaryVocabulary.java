package org.deegree.gml.feature;

import com.sun.xml.fastinfoset.QualifiedName;
import com.sun.xml.fastinfoset.tools.PrintTable;
import com.sun.xml.fastinfoset.util.DuplicateAttributeVerifier;
import com.sun.xml.fastinfoset.util.LocalNameQualifiedNamesMap;
import com.sun.xml.fastinfoset.util.StringArray;
import com.sun.xml.fastinfoset.vocab.ParserVocabulary;
import com.sun.xml.fastinfoset.vocab.SerializerVocabulary;

public class BinaryVocabulary {

	private static final String RESERVED_ELEMENT_NAME = "RES_ELMNT_";

	private static final String RESERVED_ATTRIBUTE_NAME = "RES_ATT_";

	public static final SerializerVocabulary serializerVoc;

	public static final ParserVocabulary parserVoc;

	private static QualifiedName name;

	private static LocalNameQualifiedNamesMap.Entry entry;

	static {
		serializerVoc = new SerializerVocabulary();
		parserVoc = new ParserVocabulary();
		// serializerVoc.encodingAlgorithm.add( ByteEncodingAlgorithm.ALGORITHM_URI );
		// serializerVoc.encodingAlgorithm.add( DeltazlibIntArrayAlgorithm.ALGORITHM_URI
		// );
		// serializerVoc.encodingAlgorithm.add(
		// QuantizedzlibFloatArrayAlgorithm.ALGORITHM_URI );
		//
		// parserVoc.encodingAlgorithm.add( ByteEncodingAlgorithm.ALGORITHM_URI );
		// parserVoc.encodingAlgorithm.add( DeltazlibIntArrayAlgorithm.ALGORITHM_URI );
		// parserVoc.encodingAlgorithm.add( QuantizedzlibFloatArrayAlgorithm.ALGORITHM_URI
		// );

		addElement("http://www.opengis.net/gml", "gml", "FeatureCollection");
		addElement("http://www.opengis.net/gml", "gml", "featureMember");
		addElement("http://www.opengis.net/gml", "gml", "boundedBy");
		addElement("http://www.deegree.org/app", "app1", "id");
		addElement("http://www.deegree.org/app", "app1", "Philosopher");

		reserveElement(512);
		reserveAttribute(1024);
	}

	/**
	 * Add an element to the element table.
	 * @param eName The element name
	 */
	public static final void addElement(String eName) {
		int localNameIndex = serializerVoc.localName.obtainIndex(eName);

		if (localNameIndex > -1)
			System.out.println("Duplicate Element found: " + eName);
		else
			parserVoc.localName.add(eName);

		int idx = serializerVoc.elementName.getNextIndex();
		name = new QualifiedName("", "", eName, idx, -1, -1, idx);
		parserVoc.elementName.add(name);
		entry = serializerVoc.elementName.obtainEntry(eName);
		entry.addQualifiedName(name);
	}

	public static final void addElement(String ns, String prefix, String eName) {

		int localNameIndex = serializerVoc.localName.obtainIndex(eName);
		if (localNameIndex == -1) {
			parserVoc.localName.add(eName);
		}
		int idx = serializerVoc.elementName.getNextIndex();
		name = new QualifiedName(prefix, ns, eName, idx, -1, -1, idx);
		entry = serializerVoc.elementName.obtainEntry(eName);
		entry.addQualifiedName(name);

		// also add to parser vocabulary
		parserVoc.elementName.add(name);
	}

	/**
	 * Reserve element entries to the specficied value.
	 * @param val The value to reserve to
	 */
	public static final void reserveElement(int val) {
		int idx = serializerVoc.elementName.getNextIndex();

		if (idx >= val)
			return;

		int len = val - idx;

		for (int i = 0; i < len; i++) {
			String eName = RESERVED_ELEMENT_NAME + (idx + 1);

			int localNameIndex = serializerVoc.localName.obtainIndex(eName);

			if (localNameIndex > -1)
				System.out.println("Duplicate Element found: " + eName);
			else
				parserVoc.localName.add(eName);

			name = new QualifiedName("", "", eName, idx, -1, -1, idx);
			parserVoc.elementName.add(name);
			entry = serializerVoc.elementName.obtainEntry(eName);
			entry.addQualifiedName(name);
			idx = serializerVoc.elementName.getNextIndex();
		}
	}

	/**
	 * Add an attribute to the attribute table.
	 * @param aName The attribute name
	 */
	public static final void addAttribute(String aName) {
		int localNameIndex = serializerVoc.localName.obtainIndex(aName);
		if (localNameIndex > -1)
			System.out.println("Duplicate Attribute found: " + aName);
		else
			parserVoc.localName.add(aName);

		int idx = serializerVoc.attributeName.getNextIndex();
		name = new QualifiedName("", "", aName, idx, -1, -1, idx);
		name.createAttributeValues(DuplicateAttributeVerifier.MAP_SIZE);
		parserVoc.attributeName.add(name);
		entry = serializerVoc.attributeName.obtainEntry(aName);
		entry.addQualifiedName(name);
	}

	/**
	 * Reserve attribute entries to the specficied value.
	 * @param val The value to reserve to
	 */
	public static final void reserveAttribute(int val) {
		int idx = serializerVoc.attributeName.getNextIndex();

		if (idx >= val)
			return;

		int len = val - idx;

		for (int i = 0; i < len; i++) {
			String aName = RESERVED_ATTRIBUTE_NAME + (idx + 1);

			int localNameIndex = serializerVoc.localName.obtainIndex(aName);

			if (localNameIndex > -1)
				System.out.println("Duplicate Attribute found: " + aName);
			else
				parserVoc.localName.add(aName);

			name = new QualifiedName("", "", aName, idx, -1, -1, idx);
			name.createAttributeValues(DuplicateAttributeVerifier.MAP_SIZE);
			parserVoc.attributeName.add(name);
			entry = serializerVoc.attributeName.obtainEntry(aName);
			entry.addQualifiedName(name);
			idx = serializerVoc.attributeName.getNextIndex();
		}
	}

	// private static final void addAttributeValue( String s ) {
	// if ( serializerVoc.attributeValue.obtainIndex( s ) == KeyIntMap.NOT_PRESENT ) {
	// parserVoc.attributeValue.add( s );
	// }
	// }

	/*
	 * Print tables needed for ISO specification.
	 */
	public static void main(String[] args) {

		PrintTable.printVocabulary(BinaryVocabulary.parserVoc);

		StringArray a = BinaryVocabulary.parserVoc.encodingAlgorithm;
		for (int i = 0; i < a.getSize(); i++) {
			String name = a.get(i);
			System.out.println("<tr><td>" + (32 + i) + "</td><td>" + name + "</td></tr>");
		}
	}

}