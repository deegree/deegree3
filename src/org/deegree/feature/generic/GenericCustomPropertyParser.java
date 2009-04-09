//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/tools/trunk/src/org/deegree/tools/rendering/manager/buildings/BuildingManager.java $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.feature.generic;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.feature.gml.CustomPropertyParser;

public class GenericCustomPropertyParser implements CustomPropertyParser<GenericCustomPropertyValue>{

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
