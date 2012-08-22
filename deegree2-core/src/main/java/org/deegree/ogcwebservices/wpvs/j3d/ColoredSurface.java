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
package org.deegree.ogcwebservices.wpvs.j3d;

import javax.media.j3d.Appearance;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Material;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Color3f;

import org.deegree.model.spatialschema.Geometry;
import org.deegree.ogcwebservices.wpvs.configuration.RenderingConfiguration;

/**
 * 
 * 
 * 
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version 1.0. $Revision$, $Date$
 * 
 * @since 2.0
 */
public class ColoredSurface extends DefaultSurface {

    private Appearance defaultAppearance;

    /**
     * 
     * @param objectID
     *            an Id for this Surface, for example a db primary key
     * @param parentId
     *            an Id for the parent of this Surface, for example if this is a wall the parent is the building.
     * @param geometry
     * @param red
     * @param green
     * @param blue
     * @param transparency
     */
    public ColoredSurface( String objectID, String parentId, Geometry geometry, float red, float green, float blue,
                           float transparency ) {
        super( objectID, parentId, geometry );
        Material material = createMaterial( red, green, blue );

        setAppearance( createAppearance( material, transparency ) );
    }

    /**
     * 
     * @param objectID
     *            an Id for this Surface, for example a db primary key
     * @param parentId
     *            an Id for the parent of this Surface, for example if this is a wall the parent is the building.
     * @param surface
     * @param material
     * @param transparency
     */
    public ColoredSurface( String objectID, String parentId, Geometry surface, Material material, float transparency ) {
        super( objectID, parentId, surface );
        setAppearance( createAppearance( material, transparency ) );
    }

    /**
     * 
     * @param objectID
     *            an Id for this Surface, for example a db primary key
     * @param parentId
     *            an Id for the parent of this Surface, for example if this is a wall the parent is the building.
     * @param surface
     * @param app
     */
    public ColoredSurface( String objectID, String parentId, Geometry surface, Appearance app ) {
        super( objectID, parentId, surface );
        setAppearance( app );
    }

    @Override
    public Appearance getAppearance() {
        return defaultAppearance;
    }

    @Override
    public void setAppearance( Appearance appearance ) {
        this.defaultAppearance = appearance;
        super.setAppearance( appearance );
    }

    /**
     * create a simple colored Material
     * 
     * @param red
     * @param green
     * @param blue
     * @return a new Material with specular and ambient and diffuse color.
     */
    private Material createMaterial( float red, float green, float blue ) {
        Color3f color = new Color3f( red, green, blue );
        Material targetMaterial = new Material();
        targetMaterial.setAmbientColor( color );
        targetMaterial.setDiffuseColor( color );
        targetMaterial.setSpecularColor( color );
        targetMaterial.setShininess( 75.0f );
        targetMaterial.setLightingEnable( true );
        targetMaterial.setCapability( Material.ALLOW_COMPONENT_WRITE );
        return targetMaterial;
    }

    /**
     * create Appearance from a material and a opacity value
     * 
     * @param material
     * @param transparency
     * @return a default appearance created with the material properties
     */
    private Appearance createAppearance( Material material, float transparency ) {
        // create the appearance and it's material properties
        Appearance appearance = new Appearance();

        RenderingConfiguration rc = RenderingConfiguration.getInstance();
        ColoringAttributes ca = rc.getColoringAttributes();
        if ( !rc.isObjectShadingEnabled() && material != null ) {
            material.setLightingEnable( false );
            Color3f amColor = new Color3f();
            material.getDiffuseColor( amColor );
            ca = new ColoringAttributes( amColor, ca.getShadeModel() );
        }
        appearance.setMaterial( material );
        // the coloring attributes
        appearance.setColoringAttributes( ca );

        // the polygon attributes
        appearance.setPolygonAttributes( rc.getSurfacePolygonAttributes() );

        RenderingAttributes ra = new RenderingAttributes();
        ra.setDepthBufferEnable( true );
        appearance.setRenderingAttributes( ra );

        if ( transparency != 0f ) {
            TransparencyAttributes transpAtt = new TransparencyAttributes( TransparencyAttributes.BLENDED, transparency );
            appearance.setTransparencyAttributes( transpAtt );
        }
        return appearance;
    }
}
