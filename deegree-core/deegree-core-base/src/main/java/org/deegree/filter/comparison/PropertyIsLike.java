/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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
package org.deegree.filter.comparison;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.utils.Pair;
import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.MatchAction;
import org.deegree.filter.XPathEvaluator;

/**
 * TODO add documentation here
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 */
public class PropertyIsLike extends ComparisonOperator {

	private final String wildCard;

	private final String singleChar;

	private final String escapeChar;

	private final Expression propName;

	private final Expression literal;

	private final boolean matchCase;

	/**
	 * @param testValue
	 * @param pattern
	 * @param wildCard
	 * @param singleChar
	 * @param escapeChar
	 * @param matchCase
	 */
	public PropertyIsLike(Expression testValue, Expression pattern, String wildCard, String singleChar,
			String escapeChar, Boolean matchCase, MatchAction matchAction) {
		super(matchCase, matchAction);
		this.propName = testValue;
		this.literal = pattern;
		this.wildCard = wildCard;
		this.singleChar = singleChar;
		this.escapeChar = escapeChar;
		this.matchCase = matchCase;
	}

	public Expression getExpression() {
		return propName;
	}

	public Expression getPattern() {
		return literal;
	}

	public Boolean isMatchCase() {
		return matchCase;
	}

	public String getWildCard() {
		return "" + wildCard;
	}

	public String getSingleChar() {
		return "" + singleChar;
	}

	public String getEscapeChar() {
		return "" + escapeChar;
	}

	@Override
	public SubType getSubType() {
		return SubType.PROPERTY_IS_LIKE;
	}

	@Override
	public <T> boolean evaluate(T obj, XPathEvaluator<T> xpathEvaluator) throws FilterEvaluationException {

		TypedObjectNode[] param1Values = propName.evaluate(obj, xpathEvaluator);
		TypedObjectNode[] param2Values = literal.evaluate(obj, xpathEvaluator);

		// evaluate to true if at least one pair of values matches the condition
		for (TypedObjectNode value1 : param1Values) {
			for (TypedObjectNode value2 : param2Values) {
				if (value1 == null && value2 == null) {
					return true;
				}
				if (value1 != null && value2 != null) {
					Pair<PrimitiveValue, PrimitiveValue> primitivePair = getPrimitiveValues(value1, value2);
					String s1 = primitivePair.first.toString();
					String s2 = primitivePair.second.toString();
					if (!matchCase) {
						s1 = s1.toLowerCase();
						s2 = s2.toLowerCase();
					}
					if (matches(s2, s1)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Checks if a given <code>String</code> matches a pattern that is a sequence of:
	 * <ul>
	 * <li>standard characters</li>
	 * <li>wildcard characters (like * in most shells)</li>
	 * <li>singlechar characters (like ? in most shells)</li>
	 * </ul>
	 * @param pattern the pattern to compare to
	 * @param buffer the <code>String</code> to test
	 * @return true, if the <code>String</code> matches the pattern
	 * @throws FilterEvaluationException
	 */
	private boolean matches(String pattern, String buffer) throws FilterEvaluationException {

		// match must be successful if both the pattern and the buffer are empty
		if (pattern.length() == 0 && buffer.length() == 0) {
			return true;
		}

		// build the prefix that has to match the beginning of the buffer
		// prefix ends at the first (unescaped!) wildcard / singlechar character
		StringBuffer sb = new StringBuffer();
		boolean escapeMode = false;
		int length = pattern.length();
		char specialChar = '\0';

		if (wildCard.length() != 1 || singleChar.length() != 1 || escapeChar.length() != 1) {
			String msg = "At the moment, wildCard, singleChar and escapeChar must each be exactly one character.";
			throw new FilterEvaluationException(msg);
		}
		char escapeChar = this.escapeChar.charAt(0);
		char singleChar = this.singleChar.charAt(0);
		char wildCard = this.wildCard.charAt(0);

		for (int i = 0; i < length; i++) {
			char c = pattern.charAt(i);

			if (escapeMode) {
				// just append every character (except the escape character)
				if (c != escapeChar) {
					sb.append(c);
				}
				escapeMode = false;
			}
			else {
				// escapeChar means: switch to escapeMode
				if (c == escapeChar) {
					escapeMode = true;
				}
				// wildCard / singleChar means: prefix ends here
				else if (c == wildCard || c == singleChar) {
					specialChar = c;
					break;
				}
				else {
					sb.append(c);
				}
			}
		}
		String prefix = sb.toString();
		int skip = prefix.length();

		// the buffer must begin with the prefix or else there is no match
		if (!buffer.startsWith(prefix))
			return false;

		if (specialChar == wildCard) {
			// the prefix is terminated by a wildcard-character
			pattern = pattern.substring(skip + 1, pattern.length());
			// try to find a match for the rest of the pattern
			for (int i = skip; i <= buffer.length(); i++) {
				String rest = buffer.substring(i, buffer.length());
				if (matches(pattern, rest)) {
					return true;
				}
			}
		}
		else if (specialChar == singleChar) {
			// the prefix is terminated by a singlechar-character
			pattern = pattern.substring(skip + 1, pattern.length());
			if (skip + 1 > buffer.length()) {
				return false;
			}
			String rest = buffer.substring(skip + 1, buffer.length());
			if (matches(pattern, rest)) {
				return true;
			}
		}
		else if (specialChar == '\0') {
			// the prefix is terminated by the end of the pattern
			if (buffer.length() == prefix.length()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString(String indent) {
		String s = indent + "-PropertyIsLike\n";
		s += propName.toString(indent + "  ");
		s += literal.toString(indent + "  ");
		return s;
	}

	@Override
	public Expression[] getParams() {
		return new Expression[] { propName, literal };
	}

}
