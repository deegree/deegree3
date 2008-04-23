//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2007 by:
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

package org.deegree.model.geometry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.model.configuration.GeometryFactories;
import org.deegree.model.configuration.ModelConfiguration;
import org.deegree.model.configuration.types.CurveInterpolationType;
import org.deegree.model.configuration.types.SurfaceInterpolationType;
import org.deegree.model.geometry.primitive.CurveSegment;
import org.deegree.model.geometry.primitive.SurfacePatch;

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
    
    /**
     * sigelton access
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
                ModelConfiguration mc = ModelConfiguration.readConfiguration();
                GeometryFactories gfs = mc.getGeometryFactories();
                org.deegree.model.configuration.GeometryFactory[] gfArray = gfs.getGeometryFactory();
                for ( int i = 0; i < gfArray.length; i++ ) {
                    createGeometryFactory( gfArray[i] );
                }
                // if no factory has been declared as default -> take the first one
                if ( geometryFactory == null ) {
                    geometryFactory = factories.get( gfArray[0].getName() );
                }
            } catch ( Exception e ) {
                e.printStackTrace();
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
    private void createGeometryFactory( org.deegree.model.configuration.GeometryFactory gf )
                            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        // create a factory instance from its classname
        Class clzz = Class.forName( gf.getClassName() );
        GeometryFactory factory = (GeometryFactory) clzz.newInstance();
        // set name and description
        factory.setName( gf.getName() );
        factory.setDescription( gf.getDescription() );
        // map CurveInterpolation to CurveSegment.INTERPOLATION and create a list from it
        // as expected by the factory's setSupportedCurveInterpolations method
        CurveInterpolationType[] cit = gf.getSupportedCurveInterpolation().getCurveInterpolation();
        List<CurveSegment.INTERPOLATION> cInter = new ArrayList<CurveSegment.INTERPOLATION>( cit.length );
        for ( int j = 0; j < cit.length; j++ ) {
            cInter.add( CurveSegment.INTERPOLATION.valueOf( cit[j].toString() ) );
        }
        factory.setSupportedCurveInterpolations( cInter );
        // map SurfaceInterpolation to SurfacePatch.INTERPOLATION and create a list from it
        // as expected by the factory's setSupportedSurfaceInterpolations method
        SurfaceInterpolationType[] sit = gf.getSupportedSurfaceInterpolation().getSurfaceInterpolation();
        List<SurfacePatch.INTERPOLATION> sInter = new ArrayList<SurfacePatch.INTERPOLATION>( sit.length );
        for ( int j = 0; j < sit.length; j++ ) {
            sInter.add( SurfacePatch.INTERPOLATION.valueOf( sit[j].toString() ) );
        }
        factory.setSupportedCurveInterpolations( cInter );

        factories.put( gf.getName(), factory );
        if ( gf.isIsDefault() ) {
            // if more than one factory is declared as default the least one will be used as
            // default GeometryFactory
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

    public List<GeometryFactory> findGeometryFactory( List<Class> supportedGeometries,
                                                      List<CurveSegment.INTERPOLATION> supportedCurveInterpolation,
                                                      List<SurfacePatch.INTERPOLATION> supportedSurfaceInterpolation ) {
        return null;
    }

}
