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
package org.deegree.model.coverage.grid;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.deegree.datatypes.parameter.GeneralParameterValueIm;
import org.deegree.datatypes.parameter.InvalidParameterNameException;
import org.deegree.datatypes.parameter.InvalidParameterValueException;
import org.deegree.datatypes.parameter.OperationParameterIm;
import org.deegree.datatypes.parameter.ParameterNotFoundException;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.CommonNamespaces;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Implementation of {@link "org.opengis.coverage.grid.GridCoverageWriter"} for writing a GridCoverage as GML document
 * to a defined destioation
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class GMLGridCoverageWriter extends AbstractGridCoverageWriter {

    private static ILogger LOG = LoggerFactory.getLogger( GMLGridCoverageWriter.class );

    private URL template = GMLGridCoverageWriter.class.getResource( "gml_rectifiedgrid_template.xml" );

    /**
     *
     * @param destination
     * @param metadata
     * @param subNames
     * @param currentSubname
     * @param format
     */
    public GMLGridCoverageWriter( Object destination, Map<String, Object> metadata, String[] subNames,
                                  String currentSubname, Format format ) {
        super( destination, metadata, subNames, currentSubname, format );

    }

    /**
     * disposes all resources assigned to a GMLGridCoverageWriter instance. For most cases this will be IO-resources
     *
     * @throws IOException
     */
    public void dispose()
                            throws IOException {
        // nothing here, why does it exist?
    }

    /**
     * @param coverage
     * @param parameters
     *            must contain the servlet URL within the first field; all other fields must contain the required
     *            parameters for a valid GetCoverage request
     * @throws InvalidParameterNameException
     * @throws InvalidParameterValueException
     * @throws ParameterNotFoundException
     * @throws IOException
     */
    public void write( GridCoverage coverage, GeneralParameterValueIm[] parameters )
                            throws InvalidParameterNameException, InvalidParameterValueException,
                            ParameterNotFoundException, IOException {
        XMLFragment xml = new XMLFragment();
        try {
            xml.load( template );
            Element root = xml.getRootElement();
            NamespaceContext nsc = CommonNamespaces.getNamespaceContext();

            String xpath = "gml:rectifiedGridDomain/gml:RectifiedGrid/gml:limits/gml:GridEnvelope/gml:low";
            Element element = (Element) XMLTools.getNode( root, xpath, nsc );
            double x = coverage.getEnvelope().getMin().getX();
            double y = coverage.getEnvelope().getMin().getY();
            Node node = root.getOwnerDocument().createTextNode( Double.toString( x ) + ' ' + Double.toString( y ) );
            element.appendChild( node );

            xpath = "gml:rectifiedGridDomain/gml:RectifiedGrid/gml:limits/gml:GridEnvelope/gml:high";
            element = (Element) XMLTools.getNode( root, xpath, nsc );
            x = coverage.getEnvelope().getMax().getX();
            y = coverage.getEnvelope().getMax().getY();
            node = root.getOwnerDocument().createTextNode( Double.toString( x ) + ' ' + Double.toString( y ) );
            element.appendChild( node );

            xpath = "gml:rectifiedGridDomain/gml:RectifiedGrid/gml:origin/gml:Point";
            element = (Element) XMLTools.getNode( root, xpath, nsc );
            element.setAttribute( "srsName", coverage.getCoordinateReferenceSystem().getIdentifier() );

            xpath = "gml:rectifiedGridDomain/gml:RectifiedGrid/gml:origin/gml:Point/gml:pos";
            element = (Element) XMLTools.getNode( root, xpath, nsc );
            x = coverage.getEnvelope().getMin().getX();
            y = coverage.getEnvelope().getMin().getY();
            node = root.getOwnerDocument().createTextNode( Double.toString( x ) + ' ' + Double.toString( y ) );
            element.appendChild( node );

            double[] res = calcGridResolution( coverage, parameters );

            xpath = "gml:rectifiedGridDomain/gml:RectifiedGrid/gml:offsetVector";
            List<Element> list = XMLTools.getElements( root, xpath, nsc );
            for ( int i = 0; i < list.size(); i++ ) {
                element = list.get( i );
                element.setAttribute( "srsName", coverage.getCoordinateReferenceSystem().getIdentifier() );
                if ( i == 0 ) {
                    node = root.getOwnerDocument().createTextNode( res[i] + " 0" );
                    element.appendChild( node );
                } else if ( i == 1 ) {
                    node = root.getOwnerDocument().createTextNode( "0 " + res[i] );
                    element.appendChild( node );
                } else if ( i == 2 ) {
                    node = root.getOwnerDocument().createTextNode( "0 0 " + res[i] );
                    element.appendChild( node );
                }
            }

            xpath = "gml:rangeSet/gml:File/gml:fileName";
            element = (Element) XMLTools.getNode( root, xpath, nsc );
            StringBuffer sb = new StringBuffer( 300 );
            OperationParameterIm op = (OperationParameterIm) parameters[0].getDescriptor();
            sb.append( op.getDefaultValue() ).append( '?' );
            for ( int i = 1; i < parameters.length; i++ ) {
                // OperationParameter
                op = (OperationParameterIm) parameters[i].getDescriptor();
                sb.append( op.getName() );
                sb.append( '=' ).append( op.getDefaultValue() );
                if ( i < parameters.length - 1 ) {
                    sb.append( '&' );
                }
            }
            node = root.getOwnerDocument().createCDATASection( sb.toString() );
            element.appendChild( node );
        } catch ( XMLParsingException e ) {
            LOG.logError( "could not parse GMLGridCoverage response template", e );
            throw new InvalidParameterValueException( "", e.getMessage(), "" );
        } catch ( Exception e ) {
            LOG.logError( "could not write GMLGridCoverage", e );
            throw new InvalidParameterValueException( "", e.getMessage(), "" );
        }
        xml.write( ( (OutputStream) destination ) );

    }

    /**
     * returns the resolution of the grid in x- and y- directory
     *
     * @param coverage
     * @param parameters
     * @return the resolution of the grid in x- and y- directory
     */
    private double[] calcGridResolution( GridCoverage coverage, GeneralParameterValueIm[] parameters ) {
        double wx = coverage.getEnvelope().getMax().getX() - coverage.getEnvelope().getMin().getX();
        double wy = coverage.getEnvelope().getMax().getY() - coverage.getEnvelope().getMin().getY();
        Integer width = (Integer) getNamedParameter( parameters, "width" ).getDefaultValue();
        Integer height = (Integer) getNamedParameter( parameters, "height" ).getDefaultValue();
        double dx = wx / width.doubleValue();
        double dy = wy / height.doubleValue();
        double[] res = new double[] { dx, dy };
        return res;
    }

    /**
     * selects the parameter matching the passed name from the passed array
     *
     * @param parameters
     * @param name
     * @return the parameter
     */
    private OperationParameterIm getNamedParameter( GeneralParameterValueIm[] parameters, String name ) {
        for ( int i = 0; i < parameters.length; i++ ) {
            // OperationParameter
            OperationParameterIm op = (OperationParameterIm) parameters[i].getDescriptor();
            if ( op.getName().equals( name ) ) {
                return op;
            }
        }
        return null;
    }

}
