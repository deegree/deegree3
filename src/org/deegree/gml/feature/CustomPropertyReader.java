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

package org.deegree.gml.feature;

import javax.xml.stream.XMLStreamException;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;

/**
 * Interface for parsers that convert the GML representation of custom properties into an object representation.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 * 
 * @param <T>
 *            type of the object representation
 */
public interface CustomPropertyReader<T> {

    /**
     * Returns the object representation for the property element event that the cursor of the given
     * <code>XMLStreamReader</code> points at.
     * 
     * @param xmlStream
     *            cursor must point at the <code>START_ELEMENT</code> event of the property element, afterwards points
     *            at the next event after the <code>END_ELEMENT</code> event of the property element
     * @return object representation for the given property element
     * @throws XMLStreamException
     * @throws XMLParsingException
     */
    public T parse( XMLStreamReaderWrapper xmlStream )
                            throws XMLStreamException, XMLParsingException;

}
