//$HeadURL$

/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH
 and 
 grit GmbH
 http://www.grit.de   

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

package org.deegree.model.coverage.grid.oracle;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;

import org.deegree.datatypes.CodeList;
import org.deegree.datatypes.parameter.GeneralParameterValueIm;
import org.deegree.datatypes.parameter.InvalidParameterNameException;
import org.deegree.datatypes.parameter.InvalidParameterValueException;
import org.deegree.datatypes.parameter.OperationParameterIm;
import org.deegree.datatypes.parameter.ParameterNotFoundException;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.io.oraclegeoraster.GeoRasterDescription;
import org.deegree.io.oraclegeoraster.GeoRasterReader;
import org.deegree.model.coverage.grid.AbstractGridCoverageReader;
import org.deegree.model.coverage.grid.Format;
import org.deegree.model.coverage.grid.GridCoverage;
import org.deegree.model.coverage.grid.ImageGridCoverage;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.ogcwebservices.LonLatEnvelope;
import org.deegree.ogcwebservices.wcs.describecoverage.CoverageOffering;

/**
 * Reader for Coverages stored in Oracle 10g GeoRaster format
 * 
 * 
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 * @author <a href="mailto:lipski@grit.de">Eryk Lipski</a>
 * @author last edited by: $Author$
 * 
 * @version 1.0. $Revision$, $Date$
 * 
 * @since 2.0
 */
public class OracleGeoRasterGridCoverageReader extends AbstractGridCoverageReader {

    private static final ILogger LOG = LoggerFactory.getLogger( OracleGeoRasterGridCoverageReader.class );

    /**
     * 
     * @param grDesc
     * @param description
     * @param envelope
     * @param format
     */
    public OracleGeoRasterGridCoverageReader( GeoRasterDescription grDesc, CoverageOffering description,
                                              Envelope envelope, Format format ) {
        super( grDesc, description, envelope, format );
    }

    /**
     * 
     */
    public void dispose()
                            throws IOException {

    }

    /**
     * reads a GridCoverage from a Oracle 10g GeoRaster
     * 
     * @param parameters
     *            -
     */
    public GridCoverage read( GeneralParameterValueIm[] parameters )
                            throws InvalidParameterNameException, InvalidParameterValueException,
                            ParameterNotFoundException, IOException {

        float width = -1;
        float height = -1;
        for ( int i = 0; i < parameters.length; i++ ) {
            OperationParameterIm op = (OperationParameterIm) parameters[i].getDescriptor();
            String name = op.getName();
            if ( name.equalsIgnoreCase( "WIDTH" ) ) {
                Object o = op.getDefaultValue();
                width = ( (Integer) o ).intValue();
            } else if ( name.equalsIgnoreCase( "HEIGHT" ) ) {
                Object o = op.getDefaultValue();
                height = ( (Integer) o ).intValue();
            }
        }

        /*
         * creating an intersection is not needed, because the oracle raster exporter creates a new image in requested
         * size.
         */
        Object[] o = getEnvelopes();

        GeoRasterDescription grDesc = (GeoRasterDescription) getSource();

        RenderedImage img = null;
        try {
            img = GeoRasterReader.exportRaster( grDesc, (Envelope) o[0], width, height );
        } catch ( Exception e ) {
            LOG.logError( "could not read GeoRaster: ", e );
            throw new IOException( "could not read GeoRaster: " + e.getMessage() );
        }

        CoverageOffering co = (CoverageOffering) description.clone();
        co.setLonLatEnvelope( (LonLatEnvelope) o[1] );

        return new ImageGridCoverage( co, (Envelope) o[0], (BufferedImage) img );

    }

    /**
     * returns the region in native and LonLatEnvelope of the request.
     * 
     * @return returns the region in native and LonLatEnvelope of the request.
     */
    private Object[] getEnvelopes() {

        CodeList[] cl = description.getSupportedCRSs().getNativeSRSs();
        String code = cl[0].getCodes()[0];

        LonLatEnvelope lonLatEnvelope = calcLonLatEnvelope( envelope, code );

        return new Object[] { envelope, lonLatEnvelope };
    }
}