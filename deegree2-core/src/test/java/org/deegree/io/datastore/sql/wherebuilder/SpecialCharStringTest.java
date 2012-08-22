//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/svn_classfile_header_template.xml $
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

package org.deegree.io.datastore.sql.wherebuilder;

import junit.framework.TestCase;

/**
 * Test class for SQL generation of some weird combinations of wildCard and escapeChar (in
 * PropertyIsLike expressions).
 * <p>
 * If you think, that the arguments in the assertions may be broken, consider the necessary escaping
 * for Java as well.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching </a>
 * @author last edited by: $Author:$
 *
 * @version $Revision:$, $Date:$
 */
public class SpecialCharStringTest extends TestCase {

    public void testFilter1()
                            throws Exception {
        String wildCard = "*";
        String singleChar = "#";
        String escape = "!";
        String inputString = "*Sartre*";
        SpecialCharString specialString = new SpecialCharString( inputString, wildCard, singleChar, escape );
        String output = specialString.toSQLStyle();
        assertEquals( "%Sartre%", output );
    }

    public void testFilter2()
                            throws Exception {
        String wildCard = "%";
        String singleChar = "_";
        String escape = "\\";
        String inputString = "%Sar\\%\\_tre%";
        SpecialCharString specialString = new SpecialCharString( inputString, wildCard, singleChar, escape );
        String output = specialString.toSQLStyle();
        assertEquals( "%Sar\\%\\_tre%", output );
    }

    public void testFilter3()
                            throws Exception {
        String wildCard = "?";
        String singleChar = "_";
        String escape = "\\";
        String inputString = "%Sar\\tre_";
        SpecialCharString specialString = new SpecialCharString( inputString, wildCard, singleChar, escape );
        String output = specialString.toSQLStyle();
        assertEquals( "\\%Sartre_", output );
    }

    public void testFilter4()
                            throws Exception {
        String wildCard = "%";
        String singleChar = "*";
        String escape = "_";
        String inputString = "*Sartre%";
        SpecialCharString specialString = new SpecialCharString( inputString, wildCard, singleChar, escape );
        String output = specialString.toSQLStyle();
        assertEquals( "_Sartre%", output );
    }

    public void testFilter5()
                            throws Exception {
        String wildCard = "?";
        String singleChar = "*";
        String escape = "\\";
        String inputString = "*Paul_Sartre*";
        SpecialCharString specialString = new SpecialCharString( inputString, wildCard, singleChar, escape );
        String output = specialString.toSQLStyle();
        assertEquals( "_Paul\\_Sartre_", output );
    }
}
