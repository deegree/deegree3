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

package org.deegree.crs;

import static org.deegree.framework.log.LoggerFactory.getLogger;

import java.io.Serializable;

import org.deegree.framework.log.ILogger;
import org.deegree.i18n.Messages;

/**
 * The <code>Identifiable</code> class can be used to identify a crs, ellipsoid, Datum and primemeridian
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */

public class Identifiable implements Serializable {

    private static final long serialVersionUID = 4687689233685635124L;

    private static final ILogger LOG = getLogger( Identifiable.class );

    private String[] identifiers;

    private String[] versions;

    private String[] names;

    private String[] descriptions;

    private String[] areasOfUse;

    private double[] areaOfUseBBox;

    /**
     * Takes the references of the other object and stores them in this Identifiable Object.
     * 
     * @param other
     *            identifiable object.
     */
    public Identifiable( Identifiable other ) {
        this( other.getIdentifiers(), other.getNames(), other.getVersions(), other.getDescriptions(),
              other.getAreasOfUse() );
    }

    /**
     * 
     * @param identifiers
     * @param names
     *            the human readable names of the object.
     * @param versions
     * @param descriptions
     * @param areasOfUse
     * @throws IllegalArgumentException
     *             if no identifier(s) was/were given.
     */
    public Identifiable( String[] identifiers, String[] names, String[] versions, String[] descriptions,
                         String[] areasOfUse ) {
        if ( identifiers == null || identifiers.length == 0 ) {
            throw new IllegalArgumentException( "An identifiable object must at least have one identifier." );
        }
        this.identifiers = identifiers;

        this.names = names;
        this.versions = versions;
        this.descriptions = descriptions;
        this.areasOfUse = areasOfUse;
    }

    /**
     * Creates arrays fromt the given identifier and name without setting the versions, descriptions and areasOfUse.
     * 
     * @param identifiers
     *            of the object.
     */
    public Identifiable( String[] identifiers ) {
        this( identifiers, null, null, null, null );
    }

    // /**
    // * Creates arrays fromt the given identifier and name without setting the versions,
    // descriptions and areasOfUse.
    // *
    // * @param identifier of the object.
    // * @param name the human readable name of the object.
    // */
    // protected Identifiable( String identifier, String name ) {
    // this( new String[]{identifier}, new String[]{name}, null, null, null );
    // }

    /**
     * @param id
     *            of the Identifier
     */
    public Identifiable( String id ) {
        this( new String[] { id } );
    }

    /**
     * @return the first of all areasOfUse or <code>null</code> if no areasOfUse were given.
     */
    public final String getAreaOfUse() {
        return ( areasOfUse != null && areasOfUse.length > 0 ) ? areasOfUse[0] : null;
    }

    /**
     * @return the first of all descriptions or <code>null</code> if no descriptions were given.
     */
    public final String getDescription() {
        return ( descriptions != null && descriptions.length > 0 ) ? descriptions[0] : null;
    }

    /**
     * @return the first of all identifiers.
     */
    public final String getIdentifier() {
        return identifiers[0];
    }

    /**
     * @return the first of all names or <code>null</code> if no names were given.
     */
    public final String getName() {
        return ( names != null && names.length > 0 ) ? names[0] : null;
    }

    /**
     * @return the first of all versions or <code>null</code> if no versions were given.
     */
    public final String getVersion() {
        return ( versions != null && versions.length > 0 ) ? versions[0] : null;
    }

    /**
     * throws an InvalidParameterException if the given object is null
     * 
     * @param toBeChecked
     *            for <code>null</code>
     * @param message
     *            to put into the exception. If absent, the default message (CRS_INVALID_NULL_PARAMETER) will be
     *            inserted.
     * @throws IllegalArgumentException
     *             if the given object is <code>null</code>.
     */
    protected void checkForNullObject( Object toBeChecked, String message )
                            throws IllegalArgumentException {
        if ( toBeChecked == null ) {
            if ( message == null || "".equals( message.trim() ) ) {
                message = Messages.getMessage( "CRS_INVALID_NULL_PARAMETER" );
            }
            throw new IllegalArgumentException( message );
        }

    }

    /**
     * throws an InvalidParameterException if the given object is null
     * 
     * @param toBeChecked
     *            for <code>null</code>
     * @param functionName
     *            of the caller
     * @param paramName
     *            of the parameter to be checked.
     * @throws IllegalArgumentException
     *             if the given object is <code>null</code>.
     */
    public static void checkForNullObject( Object toBeChecked, String functionName, String paramName )
                            throws IllegalArgumentException {
        if ( toBeChecked == null ) {
            throw new IllegalArgumentException( Messages.getMessage( "CRS_PARAMETER_NOT_NULL", functionName, paramName ) );
        }
    }

    /**
     * throws an IllegalArgumentException if the given object array is null or empty
     * 
     * @param toBeChecked
     *            for <code>null</code> or empty
     * @param message
     *            to put into the exception. If absent, the default message (CRS_INVALID_NULL_PARAMETER) will be
     *            inserted.
     * @throws IllegalArgumentException
     *             if the given object array is <code>null</code> or empty.
     */
    public static void checkForNullObject( Object[] toBeChecked, String message )
                            throws IllegalArgumentException {
        if ( toBeChecked != null && toBeChecked.length != 0 ) {
            return;
        }
        if ( message == null || "".equals( message.trim() ) ) {
            message = Messages.getMessage( "CRS_INVALID_NULL_ARRAY" );
        }
        throw new IllegalArgumentException( message );
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder( "id: [" );
        for ( int i = 0; i < identifiers.length; ++i ) {
            sb.append( identifiers[i] );
            if ( ( i + 1 ) < identifiers.length ) {
                sb.append( ", " );
            }
        }
        if ( getName() != null ) {
            sb.append( "], name: [" );
            for ( int i = 0; i < names.length; ++i ) {
                sb.append( names[i] );
                if ( ( i + 1 ) < names.length ) {
                    sb.append( ", " );
                }
            }
        }
        if ( getVersion() != null ) {
            sb.append( "], version: [" );
            for ( int i = 0; i < versions.length; ++i ) {
                sb.append( versions[i] );
                if ( ( i + 1 ) < versions.length ) {
                    sb.append( ", " );
                }
            }
        }
        if ( getDescription() != null ) {
            sb.append( "], description: [" );
            for ( int i = 0; i < descriptions.length; ++i ) {
                sb.append( descriptions[i] );
                if ( ( i + 1 ) < descriptions.length ) {
                    sb.append( ", " );
                }
            }
        }
        if ( getAreaOfUse() != null ) {
            sb.append( "], areasOfUse: [" );
            for ( int i = 0; i < areasOfUse.length; ++i ) {
                sb.append( areasOfUse[i] );
                if ( ( i + 1 ) < areasOfUse.length ) {
                    sb.append( ", " );
                }
            }
        }
        return sb.toString();
    }

    /**
     * @return the first id and the name (if given) as id: id, name: name.
     */
    public String getIdAndName() {
        StringBuilder sb = new StringBuilder( "id: " ).append( getIdentifier() );
        if ( getName() != null ) {
            sb.append( ", name: " ).append( getName() );
        }
        return sb.toString();
    }

    @Override
    public boolean equals( Object other ) {
        if ( other != null && other instanceof Identifiable ) {
            final Identifiable that = (Identifiable) other;
            boolean isThisEPSG = false;
            boolean isThatEPSG = false;
            for ( String id : getIdentifiers() ) {
                if ( id.toLowerCase().contains( "epsg" ) ) {
                    isThisEPSG = true;
                    break;
                }
            }
            for ( String id : that.getIdentifiers() ) {
                if ( id.toLowerCase().contains( "epsg" ) ) {
                    isThatEPSG = true;
                    break;
                }
            }
            if ( isThatEPSG && isThisEPSG ) {
                return idsMatch( that.identifiers );
            }
            return true;// idsMatch( that.identifiers );
        }
        return false;
    }

    /**
     * Checks for the equality of id's between to different identifiable objects.
     * 
     * @param otherIDs
     *            of the other identifiable object.
     * @return true if the given strings match this.identifiers false otherwise.
     */
    private boolean idsMatch( String[] otherIDs ) {
        if ( otherIDs == null || identifiers.length != otherIDs.length ) {
            return false;
        }
        for ( int i = 0; i < identifiers.length; ++i ) {
            String tmp = identifiers[i];
            String other = otherIDs[i];
            if ( tmp != null ) {
                if ( !tmp.equals( other ) ) {
                    return false;
                }
            } else if ( other != null ) {
                return false;
            }
        }
        return true;

    }

    /**
     * @return the areasOfUse or <code>null</code> if no areasOfUse were given.
     */
    public final String[] getAreasOfUse() {
        return areasOfUse;
    }

    /**
     * @return the descriptions or <code>null</code> if no descriptions were given.
     */
    public final String[] getDescriptions() {
        return descriptions;
    }

    /**
     * @return the identifiers, each identifiable object has atleast one id.
     */
    public final String[] getIdentifiers() {
        return identifiers;
    }

    /**
     * @return the names or <code>null</code> if no names were given.
     */
    public final String[] getNames() {
        return names;
    }

    /**
     * @return the versions or <code>null</code> if no versions were given.
     */
    public final String[] getVersions() {
        return versions;
    }

    /**
     * @param id
     *            a string which could match this identifiable.
     * @return true if this identifiable can be identified with the given string, false otherwise.
     */
    public boolean hasID( String id ) {
        if ( id == null || "".equals( id.trim() ) ) {
            return false;
        }
        for ( String s : getIdentifiers() ) {
            if ( id.equalsIgnoreCase( s ) ) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the area of use, i.e. the domain where this {@link Identifiable} is valid.
     * 
     * @return the domain of validity (EPSG:4326 coordinates), order: minX, minY, maxX, maxY, never <code>null</code>
     *         (-180,-90,180,90) if no such information is available
     */
    public double[] getAreaOfUseBBox() {

        if ( areaOfUseBBox == null ) {
            areaOfUseBBox = new double[4];
            areaOfUseBBox[0] = Double.NaN;
            areaOfUseBBox[1] = Double.NaN;
            areaOfUseBBox[2] = Double.NaN;
            areaOfUseBBox[3] = Double.NaN;
            if ( areasOfUse != null ) {
                for ( String bboxString : areasOfUse ) {
                    try {
                        double[] ords = parseAreaBBox( bboxString );
                        for ( int i = 0; i < 4; i++ ) {
                            if ( Double.isNaN( areaOfUseBBox[i] ) || areaOfUseBBox[i] > ords[i] ) {
                                areaOfUseBBox[i] = ords[i];
                            }
                        }
                    } catch ( Exception e ) {
                        LOG.logDebug( "Error parsing areaOfUse bbox (ignoring it): '" + e.getMessage() + "'" );
                    }
                }
            }
            if ( Double.isNaN( areaOfUseBBox[0] ) ) {
                LOG.logDebug( "No areaOfUse BBox available, assuming world." );
                areaOfUseBBox[0] = -180;
                areaOfUseBBox[1] = -90;
                areaOfUseBBox[2] = 180;
                areaOfUseBBox[3] = 90;
            }
        }
        return areaOfUseBBox;
    }

    private double[] parseAreaBBox( String s )
                            throws IllegalArgumentException, NumberFormatException {
        String[] tokens = s.split( "," );
        if ( tokens.length != 4 ) {
            throw new IllegalArgumentException( "Invalid areaOfUse: expected CSV-list of length 4." );
        }
        double[] ords = new double[4];
        for ( int i = 0; i < 4; i++ ) {
            ords[i] = Double.parseDouble( tokens[i] );
        }
        return ords;
    }
}
