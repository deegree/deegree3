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
package org.deegree.feature.gml;

import static org.junit.Assert.assertEquals;

import java.util.NoSuchElementException;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import junit.framework.Assert;

import org.deegree.commons.types.ows.CodeType;
import org.deegree.commons.types.ows.StringOrRef;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.feature.Property;
import org.deegree.geometry.Envelope;
import org.junit.Test;

public class GML311StandardPropertiesIteratorTest {

    @Test
    public void testIteration1()
                            throws XMLStreamException, FactoryConfigurationError {

        StringOrRef description = null;
        CodeType[] names = null;
        Envelope boundedBy = null;
        StandardFeatureProps props = new StandardFeatureProps( description, names, boundedBy );
        GML311StandardPropertiesIterator iter = new GML311StandardPropertiesIterator( props );
        Assert.assertFalse( iter.hasNext() );
    }
    
    @Test (expected=NoSuchElementException.class)
    public void testIteration2()
                            throws XMLStreamException, FactoryConfigurationError {

        StringOrRef description = new StringOrRef( "Description", null );
        CodeType[] names = new CodeType[] { new CodeType( "NAME1", null ), new CodeType( "NAME2", "deegree" ) };
        Envelope boundedBy = null;
        StandardFeatureProps props = new StandardFeatureProps( description, names, boundedBy );
        GML311StandardPropertiesIterator iter = new GML311StandardPropertiesIterator( props );

        Property<?> prop = iter.next();
        assertEquals (new QName(CommonNamespaces.GMLNS, "description"), prop.getName());
        assertEquals ("Description", prop.getValue());
        prop = iter.next();
        assertEquals (new QName(CommonNamespaces.GMLNS, "name"), prop.getName());
        assertEquals ("NAME1", ((CodeType) prop.getValue()).getCode());
        assertEquals (null, ((CodeType) prop.getValue()).getCodeSpace());
        prop = iter.next();
        assertEquals (new QName(CommonNamespaces.GMLNS, "name"), prop.getName());
        assertEquals ("NAME2", ((CodeType) prop.getValue()).getCode());
        assertEquals ("deegree", ((CodeType) prop.getValue()).getCodeSpace());        
        Assert.assertFalse( iter.hasNext() );
        iter.next();
    }    
}
