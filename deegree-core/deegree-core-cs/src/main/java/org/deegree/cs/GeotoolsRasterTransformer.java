package org.deegree.cs;

import java.awt.image.BufferedImage;

/**
 * Can be used to transform raster images. The transform mechanism uses geotools.
 *
 * @author <a href="mailto:stenger@lat-lon.de">Dirk Stenger</a>
 * @author last edited by: $Author: stenger $
 * 
 * @version $Revision: $, $Date: $
 */
public class GeotoolsRasterTransformer {

    private final String targetCrs;

    public GeotoolsRasterTransformer( String targetCrs ) {
        this.targetCrs = targetCrs;
    }

    public BufferedImage transform( BufferedImage image ) {
        return image;
    }

}