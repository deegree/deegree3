// $HeadURL$
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
package org.deegree.ogcwebservices.wcs.configuration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/**
 * Default implementation of WCS CoverageDescription for handling informations about coverage data
 * backend.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class DefaultExtension implements Extension {

    /**
     * all resolutions
     */
    protected TreeSet<Resolution> resolutions = null;

    /**
     *
     */
    protected double minScale = 0;

    /**
     *
     */
    protected double maxScale = 9E99;

    private String type = null;

    private double offset = 0;

    private double scaleFactor = 1;

    /**
     * constructor initializing an empty <tt>Extension</tt>
     *
     * @param type
     * @throws UnknownCVExtensionException
     */
    public DefaultExtension( String type ) throws UnknownCVExtensionException {
        resolutions = new TreeSet<Resolution>();
        setType( type );
    }

    /**
     * initializing the <tt>Extension</tt> with the passed <tt>Resolution</tt>s
     *
     * @param type
     * @param resolutions
     * @param offset
     * @param scaleFactor
     * @throws UnknownCVExtensionException
     */
    public DefaultExtension( String type, Resolution[] resolutions, double offset, double scaleFactor )
                            throws UnknownCVExtensionException {
        this( type );
        minScale = 9E99;
        maxScale = 0;
        for ( int i = 0; i < resolutions.length; i++ ) {
            this.resolutions.add( resolutions[i] );
            if ( resolutions[i].getMinScale() < minScale ) {
                minScale = resolutions[i].getMinScale();
            }
            if ( resolutions[i].getMaxScale() > maxScale ) {
                maxScale = resolutions[i].getMaxScale();
            }
        }
        this.offset = offset;
        this.scaleFactor = scaleFactor;
    }

    /**
     * returns the type of the coverage source that is described be an extension
     *
     * @return the type of the coverage source that is described be an extension
     */
    public String getType() {
        return type;
    }

    /**
     * returns the type of the coverage source that is described be an extension. Valid types are:
     * <ul>
     * <li>shapeIndexed
     * <li>nameIndexed
     * <li>file
     * </ul>
     * This list may be extended in future versions of deegree
     *
     * @param type
     * @throws UnknownCVExtensionException
     */
    public void setType( String type )
                            throws UnknownCVExtensionException {
        if ( type == null
             || ( !Extension.SHAPEINDEXED.equals( type ) && !Extension.NAMEINDEXED.equals( type )
                  && !Extension.FILEBASED.equals( type ) && !Extension.ORACLEGEORASTER.equals( type )
                  && !Extension.DATABASEINDEXED.equals( type ) && !Extension.SCRIPTBASED.equals( type ) ) ) {
            throw new UnknownCVExtensionException( "unknown extension type: " + type );
        }
        this.type = type;
    }

    /**
     * returns the minimum scale of objects that are described by an <tt>Extension</tt> object
     *
     * @return the minimum scale of objects that are described by an <tt>Extension</tt> object
     */
    public double getMinScale() {
        return minScale;
    }

    /**
     * returns the offset of the data. 0 will be returned if no offset is defined. Data first must
     * be divided by the scale factor (@see #getScaleFactor()) before sustracting the offset
     *
     * @return the offset
     */
    public double getOffset() {
        return offset;
    }

    /**
     * returns the scale factor of the data. If no scale factor is defined 1 will be returned. Data
     * first must be divided by the scale factor (@see #getScaleFactor()) before sustracting the
     * offset
     *
     * @return the scale factor
     */
    public double getScaleFactor() {
        return scaleFactor;
    }

    /**
     * returns the maximum scale of objects that are described by an <tt>Extension</tt> object
     *
     * @return the maximum scale of objects that are described by an <tt>Extension</tt> object
     */
    public double getMaxScale() {
        return maxScale;
    }

    /**
     * returns all <tt>Resolution</tt>s . If no <tt>Resolution</tt> can be found for the passed
     * scale an empty array will be returned.
     *
     * @return <tt>Resolution</tt>s matching the passed scale
     */
    public Resolution[] getResolutions() {
        return resolutions.toArray( new Resolution[resolutions.size()] );
    }

    /**
     * returns the <tt>Resolution</tt>s matching the passed scale. If no <tt>Resolution</tt>
     * can be found for the passed scale an empty array will be returned.
     *
     * @param scale
     *            scale the returned resolutions must fit
     *
     * @return <tt>Resolution</tt>s matching the passed scale
     */
    public Resolution[] getResolutions( double scale ) {
        if ( scale < minScale || scale > maxScale ) {
            return new Resolution[0];
        }
        List<Resolution> list = new ArrayList<Resolution>();
        Iterator iterator = resolutions.iterator();
        while ( iterator.hasNext() ) {
            Resolution res = (Resolution) iterator.next();
            if ( scale >= res.getMinScale() && scale <= res.getMaxScale() ) {
                list.add( res );
            }
        }
        return list.toArray( new Resolution[list.size()] );
    }

    /**
     * @param resolution
     */
    public void addResolution( Resolution resolution ) {
        resolutions.add( resolution );
    }

}
