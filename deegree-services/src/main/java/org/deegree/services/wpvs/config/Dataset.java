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

package org.deegree.services.wpvs.config;

import static org.slf4j.LoggerFactory.getLogger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.rendering.r3d.ViewParams;
import org.deegree.services.jaxb.wpvs.DatasetDefinitions;
import org.slf4j.Logger;

/**
 * The <code>DatasetWrapper</code> class defines methods for the retrieval of objects which match requested datasets
 * types and a given {@link ViewParams}.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * @param <CO>
 *            the constraint object.
 */
public abstract class Dataset<CO> {

    private final static Logger LOG = getLogger( Dataset.class );

    /** The geometry factory to be used */
    protected final static GeometryFactory geomFac = new GeometryFactory();

    List<Pair<String, List<Constraint<CO>>>> datasourceConstraints = new LinkedList<Pair<String, List<Constraint<CO>>>>();

    /**
     * Fill the wrapper with the values from the given dataset definition.
     * 
     * @param sceneEnvelope
     *            whic should be enlarged for all configured datasets, it is 3D!! in real world coordinates.
     * @param translationToLocalCRS
     *            which was configured
     * @param configAdapter
     *            to resolve urls
     * @param dsd
     *            containing the datasets.
     * @return the merged scene envelope
     */
    public abstract Envelope fillFromDatasetDefinitions( Envelope sceneEnvelope, double[] translationToLocalCRS,
                                                         XMLAdapter configAdapter, DatasetDefinitions dsd );

    /**
     * @param parentMaxPixelError
     * @param maxPixelError
     * @return the parent or the max pixel error if not <code>null<code>.
     */
    protected Double clarifyMaxPixelError( Double parentMaxPixelError, Double maxPixelError ) {
        return ( maxPixelError == null ) ? parentMaxPixelError : maxPixelError;
    }

    /**
     * @param name
     *            of dataset
     * @return true if a dataset was previously defined with the given name.
     */
    protected boolean isUnAmbiguous( String name ) {
        for ( Pair<String, List<Constraint<CO>>> kv : datasourceConstraints ) {
            if ( kv != null && kv.first != null && kv.first.equals( name ) ) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the number of configured datasets.
     */
    public int size() {
        return datasourceConstraints.size();
    }

    /**
     * Add a constrained to the given name of a dataset.
     * 
     * @param name
     * @param datasourceObject
     * @param bbt
     *            in the WPVS scene coordinate system.
     * @return true if no previous mapping for the given name was found.
     */
    protected boolean addConstraint( String name, CO datasourceObject, Envelope bbt ) {
        if ( name == null ) {
            LOG.warn( "Name may not be null" );
            return false;
        }
        List<Constraint<CO>> dsConstraints = null;
        for ( int i = 0; i < datasourceConstraints.size() && dsConstraints == null; ++i ) {
            Pair<String, List<Constraint<CO>>> kv = datasourceConstraints.get( i );
            if ( kv != null && kv.first != null && kv.first.equals( name ) ) {
                dsConstraints = kv.second;
            }
        }
        Constraint<CO> newC = new Constraint<CO>( datasourceObject, bbt );
        if ( newC.getValidEnvelope().getMin().getCoordinateDimension() != 3 ) {
            LOG.warn( "Given envelope of datasource: " + name
                      + " is not 3 dimensional, please configure this datasource to be 3d." );
        }
        if ( dsConstraints == null ) {
            dsConstraints = new LinkedList<Constraint<CO>>();
            dsConstraints.add( newC );
            datasourceConstraints.add( new Pair<String, List<Constraint<CO>>>( name, dsConstraints ) );
            return true;
        }
        for ( Constraint<CO> c : dsConstraints ) {
            if ( c != null && c.equals( newC ) ) {
                LOG.info( "Ignoring datasource it is already defined. " );
                return false;
            }
            // if ( c.minScale < newC.minScale && c.maxScale > newC.maxScale ) {
            // if ( c.validEnvelope.intersects( newC.validEnvelope ) ) {
            // LOG.warn( "Found overlapping scales and envelopes for datasource, this may not be." );
            // return false;
            // }
            // }

        }
        dsConstraints.add( newC );
        return true;
    }

    /**
     * Iterates over all configured datasets and add them to the list
     * 
     * @return the list of matching objects, never <code>null</code>
     */
    public List<CO> getAllDatasourceObjects() {
        List<CO> result = new LinkedList<CO>();
        if ( datasourceConstraints != null && !datasourceConstraints.isEmpty() ) {
            for ( Pair<String, List<Constraint<CO>>> kv : datasourceConstraints ) {
                if ( kv != null && kv.second != null && !kv.second.isEmpty() ) {
                    for ( Constraint<CO> constraint : kv.second ) {
                        result.add( constraint.getDatasourceObject() );
                    }
                }
            }
        }
        return result;
    }

    private List<Constraint<CO>> getDatasetsForName( String name ) {
        for ( Pair<String, List<Constraint<CO>>> kv : datasourceConstraints ) {
            if ( kv != null && kv.first != null && name.equals( kv.first ) ) {
                return kv.second;
            }
        }
        return null;
    }

    /**
     * Matches the given names to the configured datasets and tests retrieves the datasources/datasets which match the
     * names and the viewparams.
     * 
     * @param requestedDatasets
     * @param viewParams
     * @return the list of matching objects, never <code>null</code>
     */
    public List<CO> getMatchingDatasourceObjects( Collection<String> requestedDatasets, ViewParams viewParams ) {
        List<CO> result = new LinkedList<CO>();
        if ( requestedDatasets != null && !requestedDatasets.isEmpty() ) {
            for ( String ds : requestedDatasets ) {
                if ( ds != null ) {
                    List<Constraint<CO>> dsConst = getDatasetsForName( ds );
                    if ( dsConst != null && !dsConst.isEmpty() ) {
                        for ( Constraint<CO> constraint : dsConst ) {
                            if ( constraint.matches( viewParams ) ) {
                                result.add( constraint.getDatasourceObject() );
                            }
                        }
                    }
                }

            }
        }
        return result;
    }

    /**
     * @return all configured and requestable titles known to this dataset type.
     */
    public Set<String> datasetTitles() {
        Set<String> names = new HashSet<String>();
        for ( Pair<String, List<Constraint<CO>>> kv : datasourceConstraints ) {
            if ( kv != null && kv.first != null ) {
                names.add( kv.first );
            }
        }
        return names;
    }

    /**
     * Resolve the given url to the config xml file.
     * 
     * @param configAdapter
     * @param url
     * @return the URL resolved to the configuration xml file.
     */
    protected URL resolve( XMLAdapter configAdapter, String url ) {
        URL result = null;
        try {
            result = configAdapter.resolve( url );
        } catch ( MalformedURLException e ) {
            LOG.error( "Could not resolve url: " + url + " because: " + e.getLocalizedMessage(), e );
        }
        return result;
    }

    private class Constraint<DO> {

        private final DO datasourceObject;

        Envelope validEnvelope;

        /**
         * @param datasource
         * @param createEnvelope
         * @param min
         * @param max
         */
        Constraint( DO datasource, Envelope createEnvelope ) {
            this.datasourceObject = datasource;
            validEnvelope = createEnvelope;

        }

        /**
         * @return the object which is defined by this constraint.
         */
        public DO getDatasourceObject() {
            return datasourceObject;
        }

        /**
         * @param viewParams
         * @param boundingBox
         * @return true if the given this constraints match the given values.
         */
        boolean matches( ViewParams viewParams ) {
            if ( viewParams == null ) {
                return true;
            }
            boolean result = true;
            if ( validEnvelope.getCoordinateDimension() == 3 ) {
                double[][] bbox = new double[][] { validEnvelope.getMin().getAsArray(),
                                                  validEnvelope.getMax().getAsArray() };
                result = viewParams.getViewFrustum().intersects( bbox );
            }
            return result;
        }

        /**
         * @return the validEnvelope
         */
        public final Envelope getValidEnvelope() {
            return validEnvelope;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean equals( Object other ) {
            if ( other != null && other instanceof Constraint ) {
                final Constraint<DO> that = (Constraint<DO>) other;
                return this.datasourceObject.equals( that.datasourceObject )
                       && this.validEnvelope.equals( that.validEnvelope );
            }
            return false;
        }
    }
}
