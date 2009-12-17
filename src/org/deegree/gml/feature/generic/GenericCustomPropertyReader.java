//$HeadURL$
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

package org.deegree.gml.feature.generic;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.feature.types.GenericCustomPropertyValue;
import org.deegree.gml.feature.CustomPropertyReader;

/**
 * {@link CustomPropertyReader} that produces {@link GenericCustomPropertyValue} objects.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GenericCustomPropertyReader implements CustomPropertyReader<GenericCustomPropertyValue>{

    @Override
    public GenericCustomPropertyValue parse( XMLStreamReaderWrapper xmlStream )
                            throws XMLStreamException {

        GenericCustomPropertyValue value = new GenericCustomPropertyValue(xmlStream.getName());

        int attributeCount = xmlStream.getAttributeCount();
        for (int i = 0; i < attributeCount; i++) {
            value.setAttribute( xmlStream.getAttributeName( i ), xmlStream.getAttributeValue( i ));
        }

        while (xmlStream.next() != XMLStreamConstants.END_ELEMENT) {
            switch (xmlStream.getEventType()) {
            case XMLStreamConstants.CHARACTERS: {
                String text = xmlStream.getText().trim();
                if (text.length() > 0) {
                    value.addChild( xmlStream.getText().trim() );
                }
                break;
            }
            case XMLStreamConstants.START_ELEMENT: {
                value.addChild( parse(xmlStream) );
                break;
            }
            default: {
                break;
            }
            }
        }
        return value;
    }
}
