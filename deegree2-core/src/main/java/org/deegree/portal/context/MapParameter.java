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
package org.deegree.portal.context;

import java.util.ArrayList;

/**
 * encapsulates the part of the general web map context extension parameters that targets the map operation and feature
 * info format options. These are informations about the possible values and the current selected value for each of the
 * encapsulated parameters: <p/> feature info formats<p/> pan factors (% of the map size) <p/> zoom factors (% of the
 * map factors) <p/> minimum displayable scale (WMS scale definition) <p/> maximum displayable scale (WMS scale
 * definition) <p/>
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 */
public class MapParameter {
    private ArrayList<Format> offeredInfoFormats = new ArrayList<Format>();

    private ArrayList<MapOperationFactor> offeredPanFactors = new ArrayList<MapOperationFactor>();

    private ArrayList<MapOperationFactor> offeredZoomFactors = new ArrayList<MapOperationFactor>();

    private double maxScale = 0;

    private double minScale = 0;

    /**
     * Creates a new MapParameter object.
     *
     * @param offeredInfoFormats
     *            feature info formats
     * @param offeredPanFactors
     *            pan factors (% of the map size)
     * @param offeredZoomFactors
     *            pan factors (% of the map size)
     * @param minScale
     *            minimum displayable scale (WMS scale definition)
     * @param maxScale
     *            maximum displayable scale (WMS scale definition)
     */
    public MapParameter( Format[] offeredInfoFormats, MapOperationFactor[] offeredPanFactors,
                         MapOperationFactor[] offeredZoomFactors, double minScale, double maxScale ) {
        setOfferedInfoFormats( offeredInfoFormats );
        setOfferedPanFactors( offeredPanFactors );
        setOfferedZoomFactors( offeredZoomFactors );
        setMinScale( minScale );
        setMaxScale( maxScale );
    }

    /**
     * sets the offered pan factors (% of the map size) for a map context
     *
     * @param panFactors
     */
    public void setOfferedPanFactors( MapOperationFactor[] panFactors ) {
        offeredPanFactors.clear();

        if ( panFactors != null ) {
            for ( int i = 0; i < panFactors.length; i++ ) {
                addPanFactor( panFactors[i] );
            }
        }
    }

    /**
     * add a pan factor to a map context
     *
     * @param panFactor
     */
    public void addPanFactor( MapOperationFactor panFactor ) {
        offeredPanFactors.add( panFactor );
    }

    /**
     * returns the list of pan factors offered by this map context
     *
     * @return list of pan factors offered by this map context
     */
    public MapOperationFactor[] getOfferedPanFactors() {
        MapOperationFactor[] ms = new MapOperationFactor[0];

        if ( offeredPanFactors.size() == 0 ) {
            ms = null;
        } else {
            ms = new MapOperationFactor[offeredPanFactors.size()];
            ms = offeredPanFactors.toArray( ms );
        }

        return ms;
    }

    /**
     * returns the pan factor that is marked as selected. If no pan factor is marked, the first pan factor will be
     * returned.
     *
     * @return pan factor that is marked as selected
     */
    public MapOperationFactor getSelectedPanFactor() {
        MapOperationFactor ms = offeredPanFactors.get( 0 );

        for ( int i = 0; i < offeredPanFactors.size(); i++ ) {
            MapOperationFactor tmp = offeredPanFactors.get( i );
            if ( tmp.isSelected() ) {
                ms = tmp;
                break;
            }
        }

        return ms;
    }

    /**
     * removes a pan factor from a context
     *
     * @param panFactor
     * @throws ContextException
     *             if the map operation factior is selected.
     */
    public void removePanFactor( MapOperationFactor panFactor )
                            throws ContextException {
        for ( int i = 0; i < offeredPanFactors.size(); i++ ) {
            MapOperationFactor mof = offeredPanFactors.get( i );
            if ( mof.getFactor() == panFactor.getFactor() ) {
                if ( mof.isSelected() ) {
                    throw new ContextException( "The PanFactor can't be removed "
                                                + "from the context because it is  the " + "current one" );
                }
            }
        }
    }

    /**
     * sets the offered zoom factors (% of the map size) for a map context
     *
     * @param zoomFactors
     */
    public void setOfferedZoomFactors( MapOperationFactor[] zoomFactors ) {
        offeredZoomFactors.clear();

        if ( zoomFactors != null ) {
            for ( int i = 0; i < zoomFactors.length; i++ ) {
                addZoomFactor( zoomFactors[i] );
            }
        }
    }

    /**
     * adds a zoom factor to a map context
     *
     * @param zoomFactor
     */
    public void addZoomFactor( MapOperationFactor zoomFactor ) {
        offeredZoomFactors.add( zoomFactor );
    }

    /**
     * returns the list of zoom factors offered by the map context
     *
     * @return list of zoom factors offered by the map context
     */
    public MapOperationFactor[] getOfferedZoomFactors() {
        MapOperationFactor[] ms = new MapOperationFactor[0];

        if ( offeredZoomFactors.size() == 0 ) {
            ms = null;
        } else {
            ms = new MapOperationFactor[offeredZoomFactors.size()];
            ms = offeredZoomFactors.toArray( ms );
        }

        return ms;
    }

    /**
     * returns the zoom factor that is marked as selected. If no zoom factor is marked, the first zoom factor will be
     * returned.
     *
     * @return zoom factor that is marked as selected
     */
    public MapOperationFactor getSelectedZoomFactor() {
        MapOperationFactor ms = offeredZoomFactors.get( 0 );

        for ( int i = 0; i < offeredPanFactors.size(); i++ ) {
            MapOperationFactor tmp = offeredZoomFactors.get( i );

            if ( tmp.isSelected() ) {
                ms = tmp;
                break;
            }
        }

        return ms;
    }

    /**
     * removes a zomm factor from a map context
     *
     * @param zoomFactor
     * @throws ContextException
     *             if the map operation factor is selected.
     */
    public void removeZoomFactor( MapOperationFactor zoomFactor )
                            throws ContextException {
        for ( int i = 0; i < offeredZoomFactors.size(); i++ ) {
            MapOperationFactor mof = offeredZoomFactors.get( i );
            if ( mof.getFactor() == zoomFactor.getFactor() ) {
                if ( mof.isSelected() ) {
                    throw new ContextException( "The ZoomFactor can't be removed "
                                                + "from the context because it is  the current one" );
                }
            }
        }
    }

    /**
     * sets the info formats offered by a map context
     *
     * @param infoFormats
     */
    public void setOfferedInfoFormats( Format[] infoFormats ) {
        offeredInfoFormats.clear();

        if ( infoFormats != null ) {
            for ( int i = 0; i < infoFormats.length; i++ ) {
                addInfoFormat( infoFormats[i] );
            }
        }
    }

    /**
     * adds an info format to a map context
     *
     * @param infoFormat
     */
    public void addInfoFormat( Format infoFormat ) {
        offeredInfoFormats.add( infoFormat );
    }

    /**
     * returns the list of map formats offered by the map context
     *
     * @return list of map formats offered by the map context
     */
    public Format[] getOfferedInfoFormats() {
        Format[] ms = new Format[0];

        if ( offeredInfoFormats.size() == 0 ) {
            ms = null;
        } else {
            ms = new Format[offeredInfoFormats.size()];
            ms = offeredInfoFormats.toArray( ms );
        }

        return ms;
    }

    /**
     * returns the info format that is marked as selected. If no info format is marked, the first info format will be
     * returned.
     *
     * @return info format that is marked as selected
     */
    public Format getSelectedInfoFormat() {
        Format ms = offeredInfoFormats.get( 0 );
        for ( int i = 0; i < offeredInfoFormats.size(); i++ ) {
            Format tmp = offeredInfoFormats.get( i );

            if ( tmp.isCurrent() ) {
                ms = tmp;
                break;
            }
        }

        return ms;
    }

    /**
     * removes an info format from a map context
     *
     * @param format
     * @throws ContextException
     *             the format is the current format.
     */
    public void removeInfoFormat( Format format )
                            throws ContextException {
        for ( int i = 0; i < offeredInfoFormats.size(); i++ ) {
            Format frmt = offeredInfoFormats.get( i );
            if ( frmt.getName() == format.getName() ) {
                if ( format.isCurrent() ) {
                    throw new ContextException( "The Info Format can't be removed "
                                                + "from the context because it is  the " + "current one" );
                }
            }
        }
    }

    /**
     * returns the minimum map scale as defined at the OGC WMS specs that is offered by the map context
     *
     * @return minimum scale
     */
    public double getMinScale() {
        return minScale;
    }

    /**
     * sets the minimum map scale as defined at the OGC WMS specs that is offered by the map context
     *
     * @param minScale
     */
    public void setMinScale( double minScale ) {
        this.minScale = minScale;
    }

    /**
     * returns the maximum map scale as defined at the OGC WMS specs that is offered by the map context
     *
     * @return maximum scale
     */
    public double getMaxScale() {
        return maxScale;
    }

    /**
     * sets the maximum map scale as defined at the OGC WMS specs that is offered by the map context
     *
     * @param maxScale
     */
    public void setMaxScale( double maxScale ) {
        this.maxScale = maxScale;
    }

    /**
     *
     *
     * @return XML coded
     */
    public String exportAsXML() {
        return null;
    }
}
