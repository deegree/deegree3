//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2014 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -
 and others

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

 e-mail: info@deegree.org
 website: http://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.commons.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Responsible for the access to messages that are visible to the user.
 * <p>
 * Messages are read from the properties file <code>messages_LANG.properties</code> (LANG
 * is always a lowercased ISO 639 code), so internationalization is supported. If a
 * certain property (or the property file) for the specific default language of the system
 * is not found, the message is taken from <code>messages_en.properties</code>.
 *
 * @see Locale#getLanguage()
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 */
public class Messages {

	private static final ResourceBundle bundle = ResourceBundle.getBundle("org.deegree.commons.i18n.messages");

	/**
	 * Returns the message assigned to the passed key. If no message is assigned, an error
	 * message will be returned that indicates the missing key.
	 *
	 * @see MessageFormat for conventions on string formatting and escape characters.
	 * @param key
	 * @param arguments
	 * @return the message assigned to the passed key
	 */
	public static String getMessage(String key, Object... arguments) {
		return getMessage(key, arguments);
	}

	/**
	 * Short version for lazy people.
	 * @param key
	 * @param arguments
	 * @return the same as #getMessage
	 */
	public static String get(String key, Object... arguments) {
		try {
			if (key != null)
				return MessageFormat.format(bundle.getString(key), arguments);
		}
		catch (MissingResourceException e) {
		}
		return "$Message with key: " + key + " not found$";
	}

}