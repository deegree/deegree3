/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.commons.jdbc;

/**
 * An SQL identifier (e.g. a table, column or sequence name) with optional qualifier and
 * optional escaping. <br/>
 * <h3>Qualification</h3> Each identifier consists of a name part and an optional
 * qualifier part and has the syntax <code>(qualifier.)* name</code>. Examples:
 * <ul>
 * <li><code>mytable</code></li>
 * <li><code>myschema.mytable</code></li>
 * <li><code>mycolumn</code></li>
 * <li><code>mytable.mycolumn</code></li>
 * <li><code>myschema.mytable.mycolumn</code></li>
 * </ul>
 * <h3>Escaping</h3> Escaping an identifier has two effects (TODO verify on all DBMS): it
 * allows to use normally reserved characters/words and tells the DBMS to treat the
 * identifier in a case-sensitive manner. However, different DBMSs use different ways to
 * mark an escaped identifier, e.g. for table names:
 * <ul>
 * <li>PostgreSQL uses quotes: "escaped table name"</li>
 * <li>Oracle uses quotes: "escaped table name" (needs verification)</li>
 * <li>SQL Server uses brackets: [escaped table name] (needs verification)</li>
 * <li>MySQL uses ticks: `escaped table name` (needs verification)</li>
 * </ul>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class SQLIdentifier implements Comparable<SQLIdentifier> {

	private boolean isEscaped;

	private final String qualifier;

	private final String name;

	private char escapeStartChar;

	private char escapeEndChar;

	private String normalizedString;

	/**
	 * Creates a new {@link SQLIdentifier} instance from a literal identifier, which may
	 * use a qualifier and/or be surrounded by escaping characters.
	 * <p>
	 * Checked escaping characters include:
	 * <ul>
	 * <li>Quotes: <code>"</code> (used by PostgreSQL and Oracle)</li>
	 * <li>Brackets: <code>[</code> and <code>]</code> (used by MS SQL Server)</li>
	 * <li>Backticks: <code>`</code> (used by MySQL)</li>
	 * </ul>
	 * </p>
	 * @param identifier literal identifier, must not be <code>null</code>
	 */
	public SQLIdentifier(String identifier) throws IllegalArgumentException {
		if (identifier == null || identifier.isEmpty()) {
			throw new IllegalArgumentException("An SQL identifier can not be empty.");
		}
		String s = identifier;
		if (identifier.length() > 1) {
			char firstChar = identifier.charAt(0);
			char lastChar = identifier.charAt(identifier.length() - 1);
			switch (firstChar) {
				case '"': {
					if (lastChar != '"') {
						throw new IllegalArgumentException("SQL identifier (=" + identifier
								+ ") starts with a quote, but doesn't end with a quote.");
					}
					isEscaped = true;
					escapeStartChar = '"';
					escapeEndChar = '"';
					s = identifier.substring(1, identifier.length() - 1);
					break;
				}
				case '[': {
					if (lastChar != ']') {
						throw new IllegalArgumentException("SQL identifier (=" + identifier
								+ ") starts with a bracket, but doesn't end with a bracket.");
					}
					isEscaped = true;
					escapeStartChar = '[';
					escapeEndChar = ']';
					s = identifier.substring(1, identifier.length() - 1);
					break;
				}
				case '`': {
					if (lastChar != '`') {
						throw new IllegalArgumentException("SQL identifier (=" + identifier
								+ ") starts with a backtick, but doesn't end with a backtick.");
					}
					isEscaped = true;
					escapeStartChar = '`';
					escapeEndChar = '`';
					s = identifier.substring(1, identifier.length() - 1);
					break;
				}
				default: {
					isEscaped = false;
					s = identifier;
				}
			}
		}

		int pos = s.lastIndexOf('.');
		if (pos >= 0) {
			qualifier = s.substring(0, pos);
			name = s.substring(pos + 1, s.length());
		}
		else {
			qualifier = null;
			name = s;
		}

		StringBuilder sb = new StringBuilder();
		if (isEscaped) {
			sb.append(escapeStartChar);
		}
		if (qualifier != null) {
			if (isEscaped) {
				sb.append(qualifier);
			}
			else {
				sb.append(qualifier.toUpperCase());
			}
			sb.append(".");
		}
		if (isEscaped) {
			sb.append(name);
		}
		else {
			sb.append(name.toUpperCase());
		}
		if (isEscaped) {
			sb.append(escapeEndChar);
		}
		normalizedString = sb.toString();
	}

	protected SQLIdentifier(String table, String schema) {
		this.name = table;
		this.qualifier = schema;
		this.isEscaped = false;
		if (schema == null) {
			normalizedString = table.toUpperCase();
		}
		else {
			normalizedString = schema.toUpperCase() + "." + table.toUpperCase();
		}
	}

	/**
	 * Returns the qualifier part of this identifier.
	 * @return qualifier, can be <code>null</code> (unqualified)
	 */
	public String getQualifier() {
		return qualifier;
	}

	/**
	 * Returns the name part of this identifier.
	 * @return name part, never <code>null</code>
	 */
	public String getName() {
		return isEscaped ? escapeStartChar + name + escapeEndChar : name;
	}

	/**
	 * Returns whether this identifier is escaped, i.e. if the identifier needs to be
	 * surrounded by special characters to be recognized by the DBMS.
	 * @return <code>true</code>, if the identifier is escaped, <code>false</code>
	 * otherwise
	 */
	public boolean isEscaped() {
		return isEscaped;
	}

	@Override
	public int hashCode() {
		return normalizedString.hashCode();
	}

	@Override
	public boolean equals(Object that) {
		if (!(that instanceof SQLIdentifier)) {
			return false;
		}
		return this.normalizedString.equals(((SQLIdentifier) that).normalizedString);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (isEscaped) {
			sb.append(escapeStartChar);
		}
		if (qualifier != null) {
			sb.append(qualifier);
			sb.append(".");
		}
		sb.append(name);
		if (isEscaped) {
			sb.append(escapeEndChar);
		}
		return sb.toString();
	}

	@Override
	public int compareTo(SQLIdentifier o) {
		return this.normalizedString.compareTo(o.normalizedString);
	}

}
