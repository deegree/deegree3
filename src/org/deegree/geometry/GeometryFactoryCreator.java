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

package org.deegree.geometry;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.deegree.commons.utils.FileUtils;
import org.deegree.geometry.configuration.GeometryFactoriesType;
import org.deegree.geometry.configuration.GeometryFactoryType;
import org.deegree.geometry.configuration.ModelConfiguration;
import org.deegree.geometry.configuration.SupportedCurveInterpolationType;
import org.deegree.geometry.configuration.SupportedSurfaceInterpolationType;
import org.deegree.geometry.primitive.curvesegments.CurveSegment;
import org.deegree.geometry.primitive.surfacepatches.SurfacePatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version. $Revision$, $Date$
 */
public class GeometryFactoryCreator {

    private Map<String, GeometryFactory> factories;

    private GeometryFactory geometryFactory;

    private static GeometryFactoryCreator creator;

    private static Logger LOG = LoggerFactory.getLogger( GeometryFactoryCreator.class );

    /**
     * sigelton access
     *
     * @return instance of GeometryFactoryCreator
     */
    public static GeometryFactoryCreator getInstance() {
        if ( creator == null ) {
            synchronized ( GeometryFactoryCreator.class ) {
                creator = new GeometryFactoryCreator();
            }
        }
        return creator;
    }

    /**
     *
     *
     */
    private GeometryFactoryCreator() {
        if ( factories == null ) {
            factories = new HashMap<String, GeometryFactory>();

            try {
                JAXBContext jc = JAXBContext.newInstance( "org.deegree.geometry.configuration" );
                Unmarshaller unmarshaller = jc.createUnmarshaller();

                URL configFile = FileUtils.loadDeegreeConfiguration( GeometryFactoryCreator.class,
                                                                     "configuration/geometry_configuration.xml" );
                if ( configFile == null ) {
                    LOG.error( "Could not load the geometry_configuration.xml file make sure it is located in / or in org/deegree/model/geometry/configuration/" );
                } else {
                    ModelConfiguration mc = (ModelConfiguration) unmarshaller.unmarshal( configFile );

                    // ModelConfiguration mc = ModelConfiguration.readConfiguration();
                    GeometryFactoriesType gfs = mc.getGeometryFactories();
                    List<GeometryFactoryType> marshalledFactories = gfs.getGeometryFactory();
                    for ( GeometryFactoryType factory : marshalledFactories ) {
                        createGeometryFactory( factory );
                    }
                    // if no factory has been declared as default -> take the first one
                    if ( geometryFactory == null ) {
                        geometryFactory = factories.get( marshalledFactories.get( 0 ).getName() );
                    }
                }
            } catch ( JAXBException e ) {
                LOG.error( e.getMessage(), e );
            } catch ( ClassNotFoundException e ) {
                LOG.error( e.getMessage(), e );
            } catch ( InstantiationException e ) {
                LOG.error( e.getMessage(), e );
            } catch ( IllegalAccessException e ) {
                LOG.error( e.getMessage(), e );
            }

        }
    }

    /**
     *
     * @param gf
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private void createGeometryFactory( GeometryFactoryType gf )
                            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        // create a factory instance from its classname
        Class<?> clzz = null;
        try {
            clzz = Class.forName( gf.getClassName() );
        } catch (ClassNotFoundException e) {
            throw e;
        }
        GeometryFactory factory = (GeometryFactory) clzz.newInstance();

        // set name and description
        factory.setName( gf.getName() );
        factory.setDescription( gf.getDescription() );

        // map CurveInterpolation to CurveSegment.INTERPOLATION and create a list as expected by the factory's
        // setSupportedCurveInterpolations method
        SupportedCurveInterpolationType cit = gf.getSupportedCurveInterpolation();
        List<String> configuredInterpolations = cit.getCurveInterpolation();
        List<CurveSegment.Interpolation> curveInterpolations = new ArrayList<CurveSegment.Interpolation>(
                                                                                                          configuredInterpolations.size() );
        for ( String interpolation : configuredInterpolations ) {
            curveInterpolations.add( CurveSegment.Interpolation.valueOf( interpolation ) );
        }
        factory.setSupportedCurveInterpolations( curveInterpolations );

        // map SurfaceInterpolation to SurfacePatch.INTERPOLATION and create a list as expected by the factory's
        // setSupportedSurfaceInterpolations method
        SupportedSurfaceInterpolationType sip = gf.getSupportedSurfaceInterpolation();
        configuredInterpolations = sip.getSurfaceInterpolation();
        List<SurfacePatch.Interpolation> surfaceInterpolations = new ArrayList<SurfacePatch.Interpolation>(
                                                                                                            configuredInterpolations.size() );
        for ( String interpolation : configuredInterpolations ) {
            surfaceInterpolations.add( SurfacePatch.Interpolation.valueOf( interpolation ) );
        }
        factory.setSupportedCurveInterpolations( curveInterpolations );

        factories.put( gf.getName(), factory );
        if ( gf.isIsDefault() ) {
            // if more than one factory is declared as default the least one will be used as default GeometryFactory
            geometryFactory = factory;
        }
    }

    /**
     *
     * @return default geometry factory
     */
    public GeometryFactory getGeometryFactory() {
        return geometryFactory;
    }

    /**
     *
     * @param name
     * @return geometry factory identified by its name
     */
    public GeometryFactory getGeometryFactory( String name ) {
        return factories.get( name );
    }

    /**
     *
     * @param supportedGeometries
     * @param supportedCurveInterpolation
     * @param supportedSurfaceInterpolation
     * @return list of {@link GeometryFactory} supporting the passed requirements
     */
    public List<GeometryFactory> findGeometryFactory( List<Class<?>> supportedGeometries,
                                                      List<CurveSegment.Interpolation> supportedCurveInterpolation,
                                                      List<SurfacePatch.Interpolation> supportedSurfaceInterpolation ) {
        throw new UnsupportedOperationException( "Not supported yet" );
    }
}
