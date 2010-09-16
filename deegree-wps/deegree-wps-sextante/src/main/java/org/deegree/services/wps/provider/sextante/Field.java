//$HeadURL: http://svn.wald.intevation.org/svn/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.services.wps.provider.sextante;


/**
 * Defines a column of a attribute table. <br>
 * 'Name' is the column name and 'Type' the data type.
 * 
 * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
 * @author last edited by: $Author: pabel $
 * 
 * @version $Revision: $, $Date: $
 */
public class Field {
    private final String m_Name; // column name

    private final Class<?> m_Type; // data type

    /**
     * Creates an array of {@link Field}s. If the length of names and types are different then this method returns null.
     * 
     * @param names
     *            Column name.
     * @param types
     *            Data type.
     * @return Array of {@link Field}s.
     */
    public static Field[] createFieldArray( String[] names, Class<?>[] types ) {

        Field[] f = null;

        if ( names.length == types.length ) {
            f = new Field[names.length];

            for ( int i = 0; i < f.length; i++ ) {
                f[i] = new Field( names[i], types[i] );
            }
        } else {
            // TODO throw Exception?
        }

        return f;

    }

    public Field( String name, Class<?> type ) {
        m_Name = name;
        m_Type = type;
    }

    /**
     * Returns the column name.
     * 
     * @return Column name.
     * 
     */
    public String getName() {
        return m_Name;
    }

    /**
     * Returns the data type.
     * 
     * @return Data type.
     */
    public Class<?> getType() {
        return m_Type;
    }

    public String toString() {
        String s = Field.class.getSimpleName() + "(";
        s += m_Name + ", " + m_Type.getName() + ")";
        return s;
    }
}