//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth  
 lat/lon GmbH 
 Aennchenstr. 19
 53115 Bonn
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
package org.deegree.model.generic.implementation.schema;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.model.generic.schema.AttributeType;
import org.deegree.model.generic.schema.ObjectType;
import org.deegree.model.generic.schema.Occurrence;
import org.deegree.model.generic.schema.Sequence;
import org.deegree.model.generic.schema.TextType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * TODO add documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class DeegreeObjectTypeTest {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp()
                            throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown()
                            throws Exception {
    }

    @Test
    public void testSimpleObjectType() {
        ObjectType philosopherType = buildSimplePhilosopherObjectType();
        System.out.println( philosopherType );
    }

    public static ObjectType buildSimplePhilosopherObjectType() {

        ObjectType countryType = new GenericObjectType( new QName( "Country" ), new GenericTextType() );
        ObjectType diedInType = new GenericObjectType( new QName( "diedIn" ), countryType );
        ObjectType nameType = new GenericObjectType( new QName( "name" ), new GenericTextType() );

        List<Occurrence<?>> occurences = new ArrayList<Occurrence<?>>();
        occurences.add( new Occurrence<ObjectType>( diedInType ) );
        occurences.add( new Occurrence<ObjectType>( nameType ) );

        ObjectType philosopherType = new GenericObjectType( new QName( "Philosopher" ), new ArrayList<AttributeType>(),
                                                            new Sequence( occurences ), false, null );

        return philosopherType;
    }

}
