//$HeadURL$
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

package org.deegree.coverage;

import java.util.HashMap;

import org.deegree.coverage.rangeset.RangeSet;

/**
 * The <code>SupplementProperties</code> class can be used to add supplement data (like name, label or any kind of
 * object) to a coverage.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class SupplementProperties extends HashMap<String, Object> {

    /**
     * the name key
     */
    public final static String NAME = "name";

    /**
     * the label key
     */
    public final static String LABEL = "label";

    /**
     * the rangeset key
     */
    public final static String RANGESET = "rangeset";

    /**
     * 
     */
    private static final long serialVersionUID = -5778613664306283101L;

    /**
     * @param name
     *            to set.
     * @return the old name value of null if no previous name was set.
     */
    public String setName( String name ) {
        return (String) super.put( NAME, name );
    }

    /**
     * @param label
     *            describing the coverage
     * @return the old name value of null if no previous name was set.
     */
    public String setLabel( String label ) {
        return (String) super.put( LABEL, label );
    }

    /**
     * @return the name of the coverage
     */
    public String getName() {
        return (String) super.get( NAME );
    }

    /**
     * @return the label of the coverage
     */
    public String getLabel() {
        return (String) super.get( LABEL );
    }

    /**
     * @return the {@link RangeSet} associated with the given raster.
     */
    public RangeSet getRangeset() {
        return (RangeSet) super.get( RANGESET );
    }

    /**
     * Overridden, because of the keys which are assumed to have special objects assigned to them.
     */
    @Override
    public Object put( String key, Object value ) {
        if ( LABEL.equals( key ) || NAME.equals( key ) ) {
            if ( !( value instanceof String ) ) {
                return super.put( key, value.toString() );
            }
        } else if ( RANGESET.equals( key ) ) {
            if ( !( value instanceof RangeSet ) ) {
                return null;
            }
        }
        return super.put( key, value );
    }

}