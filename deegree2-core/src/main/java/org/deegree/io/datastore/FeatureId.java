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

package org.deegree.io.datastore;

import org.deegree.datatypes.Types;
import org.deegree.datatypes.UnknownTypeException;
import org.deegree.i18n.Messages;
import org.deegree.io.datastore.idgenerator.IdGenerationException;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.io.datastore.schema.MappedGMLId;
import org.deegree.io.datastore.schema.content.MappingField;

/**
 * Used to identify persistent (stored) feature instances.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class FeatureId implements Comparable<FeatureId> {

    private MappedFeatureType ft;

    private MappedGMLId fidDefinition;

    private Object[] values;

    /**
     * Creates a new instance of <code>FeatureId</code> from the given parameters.
     *
     * @param ft
     *            cannot be null, must not be an abstract feature type
     * @param values
     *            cannot be null or empty
     */
    public FeatureId( MappedFeatureType ft, Object[] values ) {
        this.ft = ft;
        this.fidDefinition = ft.getGMLId();
        this.values = values;
    }

    /**
     * Creates a new instance of <code>FeatureId</code> from the given parameters.
     *
     * @param ft
     *            cannot be null, must not be an abstract feature type
     * @param fid
     *            cannot be null
     * @throws IdGenerationException
     */
    public FeatureId( MappedFeatureType ft, String fid ) throws IdGenerationException {
        this.ft = ft;
        this.fidDefinition = ft.getGMLId();
        this.values = new Object[1];
        this.values[0] = removeFIDPrefix( fid, fidDefinition );
    }

  

    /**
     * Return the {@link MappedFeatureType} of the identified feature.
     * <p>
     * The feature type is concrete, never abstract.
     *
     * @return type of the identified feature, never abstract
     */
    public MappedFeatureType getFeatureType() {
        return this.ft;
    }

    /**
     * Return the underlying {@link MappedGMLId}.
     *
     * @return MappedGMLId
     */
    public MappedGMLId getFidDefinition() {
        return this.fidDefinition;
    }

    /**
     * Returns the number of components that the key consists of.
     *
     * @return the number of components that the key consists of
     */
    public int getLength() {
        return this.values.length;
    }

    /**
     * Returns all column values of the key.
     *
     * @return all column values of the key
     */
    public Object[] getValues() {
        return this.values;
    }

    /**
     * Returns a certain column value of the key.
     *
     * @param i
     *            requested column
     * @return the requested column value of the key
     */
    public Object getValue( int i ) {
        return this.values[i];
    }

    /**
     * Returns the canonical textual representation, i.e. the key components, separated by the separator defined in the
     * associated {@link MappedGMLId}.
     *
     * @return the canonical textual representation
     */
    public String getAsString() {
        StringBuffer sb = new StringBuffer( fidDefinition.getPrefix() );
        for ( int i = 0; i < this.values.length; i++ ) {
            sb.append( values[i] );
            if ( i != this.values.length - 1 ) {
                sb.append( fidDefinition.getSeparator() );
            }
        }
        return sb.toString();
    }

    public int compareTo( FeatureId that ) {
        return this.getAsString().compareTo( that.getAsString() );
    }

    @Override
    public int hashCode() {
        int hashCode = fidDefinition.hashCode();
        for ( int i = 0; i < this.values.length; i++ ) {
            hashCode += this.values[i].toString().hashCode();
        }
        return hashCode;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( obj == null || !( obj instanceof FeatureId ) ) {
            return false;
        }
        FeatureId that = (FeatureId) obj;
        if ( this.fidDefinition != that.fidDefinition ) {
            return false;
        }
        if ( this.values == null && that.values == null ) {
            return true;
        }
        if ( ( this.values != null && that.values == null ) || ( this.values == null && that.values != null )
             || ( this.values.length != that.values.length ) ) {
            return false;
        }
        for ( int i = 0; i < this.values.length; i++ ) {
            if ( ( this.values[i] != null && that.values[i] == null )
                 || ( this.values[i] == null && that.values[i] != null ) ) {
                return false;
            }
            if ( this.values[i] != null && that.values[i] != null ) {
                if ( !this.values[i].equals( that.values[i] ) ) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append( "fid=" );
        sb.append( getAsString() );
        sb.append( ", " );
        MappingField[] fidFields = fidDefinition.getIdFields();
        for ( int i = 0; i < fidFields.length; i++ ) {
            sb.append( fidFields[i].getField() );
            sb.append( "=" );
            sb.append( values[i] );
            if ( i != fidFields.length - 1 ) {
                sb.append( ", " );
            }
        }
        return sb.toString();
    }

    /**
     * Removes the prefix from the given feature id.
     * <p>
     * The prefix is taken from the given gml:id mapping.
     *
     * @param id
     *            feature id (including prefix).
     * @param idMapping
     *            target gml:id mapping (where the fid will be stored)
     * @return feature id (without prefix) as an object of the right type (matching the table column)
     * @throws IdGenerationException
     *             if the given fid does not begin with the expected prefix from the gml:id mapping
     */
    public static Object removeFIDPrefix( String id, MappedGMLId idMapping )
                            throws IdGenerationException {
        String plainIdValue = id;
        String prefix = idMapping.getPrefix();
        if ( prefix != null && prefix.length() > 0 ) {
            if ( !id.startsWith( prefix ) ) {
                String msg = Messages.getMessage( "DATASTORE_FEATURE_ID_NO_PREFIX", id, prefix );
                throw new IdGenerationException( msg );
            }
            plainIdValue = id.substring( prefix.length() );
        }

        String[] plainIdValues = null;
        String separator = idMapping.getSeparator();
        if ( separator != null && separator.length() > 0 ) {
            plainIdValues = plainIdValue.split( separator );
        } else {
            plainIdValues = new String[1];
            plainIdValues[0] = plainIdValue;
        }

        MappingField[] idFields = idMapping.getIdFields();
        if ( idFields.length != plainIdValues.length ) {
            String msg = "The given feature id \"" + id + "\" consists of " + plainIdValues.length
                         + " value parts. Should be " + idFields.length + ".";
            throw new IdGenerationException( msg );
        }

        int sqlTypeCode;
        Object fidObject = null;
        Object[] fidObjects = new Object[idFields.length];
        for ( int i = 0; i < idFields.length; i++ ) {
            plainIdValue = plainIdValues[i];
            sqlTypeCode = idFields[i].getType();
            switch ( sqlTypeCode ) {
            case Types.NUMERIC:
            case Types.DECIMAL:
            case Types.DOUBLE: {
                try {
                    fidObject = Double.parseDouble( plainIdValue );
                } catch ( NumberFormatException e ) {
                    String msg = Messages.getMessage( "DATASTORE_FEATURE_ID_CONVERT", plainIdValue, "Double" );
                    throw new IdGenerationException( msg );
                }
                break;
            }
            case Types.FLOAT: {
                try {
                    fidObject = Float.parseFloat( plainIdValue );
                } catch ( NumberFormatException e ) {
                    String msg = Messages.getMessage( "DATASTORE_FEATURE_ID_CONVERT", plainIdValue, "Float" );
                    throw new IdGenerationException( msg );
                }
                break;
            }
            case Types.INTEGER: {
                try {
                    fidObject = Integer.parseInt( plainIdValue );
                } catch ( NumberFormatException e ) {
                    String msg = Messages.getMessage( "DATASTORE_FEATURE_ID_CONVERT", plainIdValue, "Integer" );
                    throw new IdGenerationException( msg );
                }
                break;
            }
            case Types.VARCHAR:
            case Types.CHAR: {
                fidObject = plainIdValue;
                break;
            }
            default: {
                String msg = null;
                try {
                    msg = Messages.getMessage( "DATASTORE_FEATURE_ID_CONVERT", plainIdValue,
                                               Types.getTypeNameForSQLTypeCode( sqlTypeCode ) );
                } catch ( UnknownTypeException e ) {
                    throw new IdGenerationException( e.getMessage() );
                }
                throw new IdGenerationException( msg );
            }
            }
            fidObjects[i] = fidObject;
        }

        if ( fidObjects.length > 1 ) {
            return fidObjects;
        }
        return fidObject;
    }

}
