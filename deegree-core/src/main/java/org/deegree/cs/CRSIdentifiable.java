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

package org.deegree.cs;

import static org.deegree.commons.utils.ArrayUtils.contains;

import java.util.Arrays;
import java.util.List;

import org.deegree.commons.utils.ArrayUtils;
import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.cs.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>CRSIdentifiable</code> class can be used to identify Coordinate system components.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@LoggingNotes(debug = "Get information about metadata of Coordinate System components.")
public class CRSIdentifiable {

    private static final Logger LOG = LoggerFactory.getLogger( CRSIdentifiable.class );

    private CRSCodeType[] codes;

    private String[] versions;

    private String[] names;

    private String[] descriptions;

    private String[] areasOfUse;

    private double[] areaOfUseBBox;

    /**
     * Takes the references of the other object and stores them in this CRSIdentifiable Object.
     * 
     * @param other
     *            identifiable object.
     */
    public CRSIdentifiable( CRSIdentifiable other ) {
        this( other.getCodes(), other.getNames(), other.getVersions(), other.getDescriptions(), other.getAreasOfUse() );
    }

    /**
     * 
     * @param codes
     * @param names
     *            the human readable names of the object.
     * @param versions
     * @param descriptions
     * @param areasOfUse
     * @throws IllegalArgumentException
     *             if no identifier(s) was/were given.
     */
    public CRSIdentifiable( CRSCodeType[] codes, String[] names, String[] versions, String[] descriptions,
                            String[] areasOfUse ) {
        if ( codes == null || codes.length == 0 ) {
            throw new IllegalArgumentException( "An identifiable object must at least have one identifier." );
        }
        this.codes = codes;
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
    public CRSIdentifiable( CRSCodeType[] identifiers ) {
        this( identifiers, null, null, null, null );
    }

    /**
     * @param id
     *            of the Identifier
     */
    public CRSIdentifiable( CRSCodeType id ) {
        this( new CRSCodeType[] { id } );
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
    public final CRSCodeType getCode() {
        return codes[0];
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
        for ( int i = 0; i < codes.length; ++i ) {
            sb.append( codes[i] );
            if ( ( i + 1 ) < codes.length ) {
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
        sb.append( "]" );
        return sb.toString();
    }

    /**
     * @return the first id and the name (if given) as id: id, name: name.
     */
    public String getCodeAndName() {
        StringBuilder sb = new StringBuilder( "id: " ).append( getCode() );
        if ( getName() != null ) {
            sb.append( ", name: " ).append( getName() );
        }
        return sb.toString();
    }

    @Override
    public boolean equals( Object other ) {
        if ( other != null && other instanceof CRSIdentifiable ) {
            CRSIdentifiable that = ( (CRSIdentifiable) other );
            CRSCodeType[] mId = getCodes();
            CRSCodeType[] yId = that.getCodes();
            CRSCodeType[] small = mId.length >= yId.length ? yId : mId;
            CRSCodeType[] large = mId.length < yId.length ? yId : mId;

            List<CRSCodeType> list = Arrays.asList( large );
            boolean result = true;
            for ( int i = 0; result && ( i < small.length ); ++i ) {
                CRSCodeType id = small[i];
                if ( id != null ) {
                    result = list.contains( id );
                }
            }
            return result;
        }

        return false;
    }

    /**
     * Implementation as proposed by Joshua Block in Effective Java (Addison-Wesley 2001), which supplies an even
     * distribution and is relatively fast. It is created from field <b>f</b> as follows:
     * <ul>
     * <li>boolean -- code = (f ? 0 : 1)</li>
     * <li>byte, char, short, int -- code = (int)f</li>
     * <li>long -- code = (int)(f ^ (f &gt;&gt;&gt;32))</li>
     * <li>float -- code = Float.floatToIntBits(f);</li>
     * <li>double -- long l = Double.doubleToLongBits(f); code = (int)(l ^ (l &gt;&gt;&gt; 32))</li>
     * <li>all Objects, (where equals(&nbsp;) calls equals(&nbsp;) for this field) -- code = f.hashCode(&nbsp;)</li>
     * <li>Array -- Apply above rules to each element</li>
     * </ul>
     * <p>
     * Combining the hash code(s) computed above: result = 37 * result + code;
     * </p>
     * 
     * @return (int) ( result >>> 32 ) ^ (int) result;
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        // the 2nd millionth prime, :-)
        long code = 32452843;
        for ( CRSCodeType id : getCodes() ) {
            if ( id != null ) {
                code = code * 37 + id.hashCode();
            }
        }
        return (int) ( code >>> 32 ) ^ (int) code;
    }

    /**
     * @return the areasOfUse or <code>null</code> if no areasOfUse were given.
     */
    public final String[] getAreasOfUse() {
        return areasOfUse == null ? null : Arrays.copyOf( areasOfUse, areasOfUse.length );
    }

    /**
     * @return the descriptions or <code>null</code> if no descriptions were given.
     */
    public final String[] getDescriptions() {
        return descriptions == null ? null : Arrays.copyOf( descriptions, descriptions.length );
    }

    /**
     * @return the identifiers, each identifiable object has atleast one id.
     */
    public final CRSCodeType[] getCodes() {
        return Arrays.copyOf( codes, codes.length );
    }

    /**
     * @return the codetypes as the original strings, each identifiable object has atleast one id.
     */
    public final String[] getOrignalCodeStrings() {
        String[] result = new String[codes.length];
        for ( int i = 0; i < codes.length; ++i ) {
            result[i] = codes[i].getOriginal();
        }
        return result;
    }

    /**
     * @return the names or <code>null</code> if no names were given.
     */
    public final String[] getNames() {
        return names == null ? null : Arrays.copyOf( names, names.length );
    }

    /**
     * @return the versions or <code>null</code> if no versions were given.
     */
    public final String[] getVersions() {
        return versions == null ? null : Arrays.copyOf( versions, versions.length );
    }

    /**
     * @param id
     *            a string which could match this identifiable.
     * @return true if this identifiable can be identified with the given string, false otherwise.
     */
    public boolean hasCode( CRSCodeType id ) {
        for ( CRSCodeType code : codes ) {
            if ( code.equals( id ) )
                return true;
        }
        return false;
    }

    /**
     * Iterates over all Ids (code type originals) and Names and tests if either one matches the given string.
     * 
     * @param idOrName
     *            a String which might be an id or a name.
     * @param caseSensitive
     *            should the match me case sensitive
     * @param exact
     *            should the names and ids contain the given string or match exact.
     * @return true if any of the names or codes match without case the given string.
     */
    public boolean hasIdOrName( String idOrName, boolean caseSensitive, boolean exact ) {
        return ArrayUtils.contains( getOrignalCodeStrings(), idOrName, caseSensitive, exact )
               || ArrayUtils.contains( getNames(), idOrName, caseSensitive, exact );
    }

    /**
     * 
     * @param id
     * @param caseSensitive
     * @param exact
     * @return true if the given id is present in this objects id's.
     */
    public boolean hasId( String id, boolean caseSensitive, boolean exact ) {
        return contains( getOrignalCodeStrings(), id, caseSensitive, exact );
    }

    /**
     * Returns the area of use, i.e. the domain where this {@link CRSIdentifiable} is valid.
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
                for ( int i = 0; i < areasOfUse.length; ++i ) {
                    String bboxString = areasOfUse[i];
                    try {
                        double[] ords = parseAreaBBox( bboxString );
                        for ( int co = 0; co < 4; co++ ) {
                            if ( Double.isNaN( areaOfUseBBox[co] ) || areaOfUseBBox[i] > ords[co] ) {
                                areaOfUseBBox[co] = ords[co];
                            }
                        }
                    } catch ( Exception e ) {
                        LOG.debug( "Error parsing areaOfUse bbox (ignoring it): '" + e.getMessage() + "'" );
                    }
                }
            }
            if ( Double.isNaN( areaOfUseBBox[0] ) ) {
                LOG.debug( "No areaOfUse BBox available, assuming world." );
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

    /**
     * @param newCodeType
     * @param override
     */
    public void setDefaultId( CRSCodeType newCodeType, boolean override ) {
        if ( newCodeType != null ) {
            if ( override ) {
                this.codes[0] = newCodeType;
            } else {
                CRSCodeType[] newCodes = new CRSCodeType[codes.length + 1];
                newCodes[0] = newCodeType;
                System.arraycopy( codes, 0, newCodes, 1, codes.length );
                this.codes = newCodes;
            }
        }
    }

    /**
     * @param bbox
     *            an envelope of validity in epsg:4326 coordinates, min(lon,lat) max(lon,lat);
     */
    public void setDefaultAreaOfUse( double[] bbox ) {
        if ( bbox != null && bbox.length == 4 ) {
            if ( ( bbox[0] >= -180 && bbox[2] <= 180 && bbox[0] < bbox[2] )
                 && ( bbox[1] >= -90 && bbox[3] <= 90 && bbox[1] < bbox[3] ) ) {

                String[] newAreas = null;
                if ( areasOfUse == null || areasOfUse.length == 0 ) {
                    newAreas = new String[1];
                } else {
                    newAreas = new String[areasOfUse.length + 1];
                    System.arraycopy( areasOfUse, 0, newAreas, 1, areasOfUse.length );
                }
                StringBuilder sb = new StringBuilder( bbox[0] + "," + bbox[1] + "," + bbox[2] + "," + bbox[3] );
                newAreas[0] = sb.toString();
                areasOfUse = newAreas;
            }
        }
    }

    /**
     * @param areaOfUse
     */
    public void addAreaOfUse( String areaOfUse ) {
        if ( areaOfUse != null ) {
            String[] aou = new String[areasOfUse == null ? 1 : areasOfUse.length + 1];
            if ( areasOfUse == null || areasOfUse.length == 0 ) {
                aou[0] = areaOfUse;
            } else {
                System.arraycopy( areasOfUse, 0, aou, 0, areasOfUse.length );
                aou[aou.length - 1] = areaOfUse;
            }
            this.areasOfUse = aou;
        }
    }

    /**
     * @param name
     */
    public void addName( String name ) {
        if ( name != null ) {
            String[] nNames = new String[names == null ? 1 : names.length + 1];
            if ( names == null || names.length == 0 ) {
                nNames[0] = name;
            } else {
                System.arraycopy( names, 0, nNames, 0, names.length );
                nNames[nNames.length - 1] = name;
            }
            this.names = nNames;
        }

    }

    /**
     * @param defaultName
     *            the new default name
     * @param override
     *            true if the new name should override the name currently at position 0
     */
    public void setDefaultName( String defaultName, boolean override ) {
        if ( defaultName != null && !"".equals( defaultName ) ) {
            String[] newNames = new String[1];
            if ( override ) {
                if ( names != null && names.length >= 1 ) {
                    newNames = Arrays.copyOf( names, names.length );
                }
            } else {
                if ( names != null && names.length >= 1 ) {
                    newNames = new String[names.length + 1];
                    System.arraycopy( names, 0, newNames, 1, names.length );
                }
            }
            newNames[0] = defaultName;
            this.names = newNames;
        } else {
            if ( override ) {
                this.names = null;
            }
        }
    }

    /**
     * @param newDescription
     *            the new default description
     * @param override
     *            true if the new description should override the description currently at position 0
     */
    public void setDefaultDescription( String newDescription, boolean override ) {
        if ( newDescription != null && !"".equals( newDescription ) ) {
            String[] newDesc = new String[1];
            if ( override ) {
                if ( descriptions != null && descriptions.length >= 1 ) {
                    newDesc = Arrays.copyOf( descriptions, descriptions.length );
                }
            } else {
                if ( descriptions != null && descriptions.length >= 1 ) {
                    newDesc = new String[descriptions.length + 1];
                    System.arraycopy( descriptions, 0, newDesc, 1, descriptions.length );
                }
            }
            newDesc[0] = newDescription;
            this.descriptions = newDesc;
        } else {
            if ( override ) {
                this.descriptions = null;
            }
        }

    }

    /**
     * @param newVersion
     *            the new default version
     * @param override
     *            true if the new version should override the version currently at position 0
     */
    public void setDefaultVersion( String newVersion, boolean override ) {
        if ( newVersion != null && !"".equals( newVersion ) ) {
            String[] newVers = new String[1];
            if ( override ) {
                if ( versions != null && versions.length >= 1 ) {
                    newVers = Arrays.copyOf( versions, versions.length );
                }
            } else {
                if ( versions != null && versions.length >= 1 ) {
                    newVers = new String[versions.length + 1];
                    System.arraycopy( versions, 0, newVers, 1, versions.length );
                }
            }
            newVers[0] = newVersion;
            this.versions = newVers;
        } else {
            if ( override ) {
                this.versions = null;
            }
        }
    }
}
