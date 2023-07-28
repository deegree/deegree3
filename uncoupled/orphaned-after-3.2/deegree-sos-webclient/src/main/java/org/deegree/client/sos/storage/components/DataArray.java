package org.deegree.client.sos.storage.components;

import java.util.List;

import org.deegree.commons.utils.Pair;

/**
 * Helper class for Observation class containing the contents of the optional
 * XML element "DataArray".
 * 
 * @author <a href="mailto:neumeister@lat-lon.de">Ulrich Neumeister</a>
 * 
 */
public class DataArray {

	private String count;

	private List<Field> elementTypes;

	private List<Pair<String, String>> separators;

	private String values;

	public DataArray() {

	}

	public String getCount() {
		return count;
	}

	public List<Field> getElementTypes() {
		return elementTypes;
	}

	public List<Pair<String, String>> getSeparators() {
		return separators;
	}

	public String getValues() {
		return values;
	}

	public void setCount(String that) {
		count = that;
	}

	public void setElementTypes(List<Field> that) {
		elementTypes = that;
	}

	public void setSeparators(List<Pair<String, String>> that) {
		separators = that;
	}

	public void setValues(String that) {
		values = that;
	}

}
