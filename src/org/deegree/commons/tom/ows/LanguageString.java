//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.commons.tom.ows;

/**
 * Text string with the language of the string identified as recommended in the XML 1.0 W3C Recommendation, section
 * 2.12 (RFC 4646 language code).
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 *
 * @version $Revision: $, $Date: $
 */
public class LanguageString {

    private String string;

    private String lang;

    /**
     * Creates a new {@link LanguageString} instance with the given string value and optional language information.
     *
     * @param value
     *            string value
     * @param lang
     *            RFC 4646 language code of the human-readable text, may be null
     */
    public LanguageString( String value, String lang ) {
        this.string = value;
        this.lang = lang;
    }

    /**
     * Returns the string value.
     *
     * @return the string value
     */
    public String getString() {
        return string;
    }

    /**
     * Returns the RFC 4646 language code.
     *
     * @return the RFC 4646 language code, or null if not specified
     */
    public String getLanguage() {
        return lang;
    }

    @Override
    public String toString() {
        return string + (lang != null ? " (lang=" + lang + ")" : "");
    }
}
