package org.deegree.commons.xml.schema;

import org.apache.xerces.util.XMLLocatorWrapper;
import org.apache.xerces.xni.parser.XMLParseException;

/**
 * A validation event (warning/error/fatal error) that occurred during schema validation.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @since 3.4
 */
public class SchemaValidationEvent {

	private final XMLParseException e;

	private final String domain;

	private final String key;

	SchemaValidationEvent(String domain, String key, XMLParseException e) {
		this.e = e;
		this.domain = domain;
		this.key = key;
	}

	public SchemaValidationEvent(Exception e) {
		this.e = new XMLParseException(new XMLLocatorWrapper(), e.getMessage(), e);
		this.domain = null;
		this.key = null;
	}

	/**
	 * The domain of the error. The domain can be any string but is suggested to be a
	 * valid URI. The domain can be used to conveniently specify a web site location of
	 * the relevant specification or document pertaining to this error.
	 * @return domain
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * The error key. This key can be any string and is implementation dependent.
	 * @return error key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * The actual exception that describes the event.
	 * @return actual exception, never <code>null</code>
	 */
	public XMLParseException getException() {
		return e;
	}

	public String toString() {
		String s = e.getLocalizedMessage();
		s += " (line: " + e.getLineNumber() + ", column: " + e.getColumnNumber();
		s += e.getExpandedSystemId() != null ? ", SystemID: '" + e.getExpandedSystemId() + "')" : ")";
		return s;
	}

}
