//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.commons.utils;

import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;

import java.util.Collection;

import javax.xml.namespace.QName;

/**
 * Provides some utility methods for working with qualified names.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class QNameUtils {

    /**
     * Returns a string representation that includes the prefix (if present).
     * 
     * @param qName
     *            qualified name, must not be <code>null</code>
     * @return string representation, never <code>null</code>
     */
    public static String toString( QName qName ) {
        if ( qName.getPrefix().equals( DEFAULT_NS_PREFIX ) ) {
            return qName.toString();
        }
        return qName.getPrefix() + ":" + qName.toString();
    }

    /**
     * Finds the best possible match for a {@link QName} in a collection of {@link QName}s.
     * <p>
     * Performs the following checks in order:
     * <ol>
     * <li>exact match, same namespace, same local part</li>
     * <li>different namespace, same prefix, same local part</li>
     * <li>different namespace, different prefix, same local part</li>
     * </ol>
     * 
     * @param present
     *            name for which to find the best match, must not be <code>null</code>
     * @param candidates
     *            available candidates, must not be <code>null</code>
     * @return best match, or <code>null</code> if no match can be found at all
     */
    public static QName findBestMatch( QName present, Collection<QName> candidates ) {

        // first phase (exact match, same namespace, same local part)
        if ( candidates.contains( present ) ) {
            return present;
        }

        // second phase (different namespace, same prefix, same local part)
        for ( QName candidate : candidates ) {
            if ( present.getPrefix().equals( candidate.getPrefix() )
                 && present.getLocalPart().equals( candidate.getLocalPart() ) ) {
                return candidate;
            }
        }

        // third phase (different namespace, different prefix, same local part)
        for ( QName candidate : candidates ) {
            if ( present.getLocalPart().equals( candidate.getLocalPart() ) ) {
                return candidate;
            }
        }

        return null;
    }
}
