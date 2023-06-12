/**
 *
 */
package org.deegree.feature.persistence.sql;

import org.deegree.commons.jdbc.TableName;
import org.deegree.cs.coordinatesystems.ICRS;

/**
 * @author markus
 *
 */
public class BBoxTableMapping {

	private final TableName ftTable;

	private final ICRS crs;

	public BBoxTableMapping(String ftTable, ICRS crs) {
		this.ftTable = new TableName(ftTable);
		this.crs = crs;
	}

	public TableName getTable() {
		return ftTable;
	}

	public ICRS getCRS() {
		return crs;
	}

	public String getFTNameColumn() {
		return "qname";
	}

	public String getBBoxColumn() {
		return "bbox";
	}

}
