package org.deegree.client.sos.storage.components;

/**
 * Helper class for DataArray class containing the contents of the XML element
 * "Field".
 * 
 * @author <a href="mailto:neumeister@lat-lon.de">Ulrich Neumeister</a>
 * 
 */
public class Field {

	private String definition;

	private int index;

	private String name;

	private String type;

	public Field() {

	}

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public String getDefinition() {
		return definition;
	}

	public void setIndex(int that) {
		index = that;
	}

	public void setName(String that) {
		name = that;
	}

	public void setType(String that) {
		type = that;
	}

	public void setDefinition(String that) {
		definition = that;
	}

}
