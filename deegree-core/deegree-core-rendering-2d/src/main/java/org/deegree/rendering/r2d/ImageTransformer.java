package org.deegree.rendering.r2d;

import org.deegree.geometry.Envelope;

import java.awt.image.BufferedImage;

/**
 * Interface for image transformers.
 *
 * @author <a href="mailto:stenger@lat-lon.de">Dirk Stenger</a>
 * @author last edited by: $Author: stenger $
 * 
 * @version $Revision: $, $Date: $
 */
public interface ImageTransformer {

    /**
     * Transforms an image.
     * 
     * @param image
     *            image to transform, never <code>null</code>
     * @param sourceEnvelope
     *            source envelope of image, never <code>null</code>
     * @return transformed image
     */
    BufferedImage transform( BufferedImage image, Envelope sourceEnvelope );

}