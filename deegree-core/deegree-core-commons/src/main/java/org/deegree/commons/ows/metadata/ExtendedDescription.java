package org.deegree.commons.ows.metadata;

import java.util.List;

import javax.xml.namespace.QName;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class ExtendedDescription {

	private String name;

	private QName type;

	private String metadata;

	private List<String> values;

	public ExtendedDescription(String name, QName type, String metadata, List<String> values) {
		this.name = name;
		this.type = type;
		this.metadata = metadata;
		this.values = values;
	}

	public String getName() {
		return name;
	}

	public QName getType() {
		return type;
	}

	public String getMetadata() {
		return metadata;
	}

	public List<String> getValues() {
		return values;
	}

}
