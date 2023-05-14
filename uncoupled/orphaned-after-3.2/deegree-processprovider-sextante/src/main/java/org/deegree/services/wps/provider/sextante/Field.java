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

import javax.xml.namespace.QName;

/**
 * Defines a column of a attribute table. <br>
 * 'Name' is the column name and 'Type' the data type.
 * 
 * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
 * 
 */
public class Field {
    private final String m_Name; // column name

    private final Class<?> m_Type; // data type

    private final String m_NamespaceURI; // namespace URI of this property

    private final String m_Prefix; // prefix of namespace

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
                f[i] = new Field( determineQName( names[i] ), types[i] );
            }
        } else {
            throw new IndexOutOfBoundsException( "The number of names and types aren't equal." );
        }

        return f;

    }

    /**
     * This method determine {@link QName} of a NameWithNamespaceAndPrefix.
     * 
     * @param name
     *            From the method getNameWithNamespaceAndPrefix().
     * @return {@link QName} of a NameWithNamespaceAndPrefix.
     */
    private static QName determineQName( String name ) {

        QName qName;

        if ( name != null ) {

            String[] nameAsArray = name.split( "~" );

            if ( nameAsArray.length == 3 ) {
                qName = new QName( nameAsArray[2], nameAsArray[0], nameAsArray[1] );
            } else {
                qName = new QName( VectorLayerAdapter.APP_NS, name, VectorLayerAdapter.APP_PREFIX );
            }

        } else {
            qName = new QName( VectorLayerAdapter.APP_NS, "PROPERTY_WITHOUT_NAME", VectorLayerAdapter.APP_PREFIX );
        }

        return qName;
    }

    /**
     * Creates a Field by name und type.
     * 
     * @param name
     *            Name
     * @param type
     *            Type
     */
    public Field( String name, Class<?> type ) {
        this( name, type, null, null );
    }

    /**
     * Creates a Field by {@link QName} und type.
     * 
     * @param name
     *            {@link QName}.
     * @param type
     *            Type.
     */
    public Field( QName name, Class<?> type ) {
        this( name.getLocalPart(), type, name.getNamespaceURI(), name.getPrefix() );
    }

    /**
     * Creates a Field by name, type, namespace URL and prefix.
     * 
     * @param name
     *            Name.
     * @param type
     *            Type.
     * @param ns
     *            Namespace URL.
     * @param prefix
     *            Prefix for namespace.
     */
    private Field( String name, Class<?> type, String ns, String prefix ) {

        if ( name == null )
            name = "PROPERTY_WITHOUT_NAME";

        if ( ns == null )
            ns = VectorLayerAdapter.APP_NS;

        if ( prefix == null )
            prefix = VectorLayerAdapter.APP_PREFIX;

        if ( type == null )
            type = String.class;

        m_Name = name.replace( " ", "" );
        m_Type = type;
        m_NamespaceURI = ns;
        m_Prefix = prefix;
    }

    /**
     * Returns the field name.
     * 
     * @return Field name.
     * 
     */
    public String getName() {
        return m_Name;
    }

    /**
     * Returns the field name with namespace and prefix.
     * 
     * @return Name with namespace and prefix like this "name~prefix~namespace". If namespace or prefix is null, it
     *         returns only the name.
     */
    public String getNameWithNamespaceAndPrefix() {
        String name = "";
        name += m_Name + "~" + m_Prefix + "~" + m_NamespaceURI;
        return name;
    }

    /**
     * Returns the field name as a {@link QName}.
     * 
     * @return Field name as a {@link QName} with namespace URL and prefix.
     */
    public QName getQName() {
        return new QName( m_NamespaceURI, m_Name, m_Prefix );
    }

    /**
     * Returns prefix of namespace.
     * 
     * @return Prefix of namespace.
     * 
     */
    public String getPrefix() {
        return m_Prefix;
    }

    /**
     * Returns namespace.
     * 
     * @return namespace.
     * 
     */
    public String getNamespaceURI() {
        return m_NamespaceURI;
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