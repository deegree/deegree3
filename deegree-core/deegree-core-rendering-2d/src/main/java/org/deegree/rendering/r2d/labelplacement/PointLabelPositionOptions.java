//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.rendering.r2d.labelplacement;

import static org.slf4j.LoggerFactory.getLogger;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.deegree.rendering.r2d.Label;
import org.deegree.style.styling.TextStyling;
import org.deegree.style.utils.UomCalculator;
import org.slf4j.Logger;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;


/**
 * <code>Contains a {@link Label} and delivers its possible positions for automatic label placement</code>
 * 
 * @author Florian Bingel 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */


public class PointLabelPositionOptions {

    
    private static final Logger LOG = getLogger( PointLabelPositionOptions.class );
    protected final static org.locationtech.jts.geom.GeometryFactory jtsFactory = new org.locationtech.jts.geom.GeometryFactory();
    
    /**
     * Some constants, describing the 8 possible positions of the label and their qualities.
     */
    static final float[] qualities = new float[] { 1.0f, 1.5f, 1.33f, 1.27f, 1.15f, 2.0f, 1.1f, 1.7f };
    static final float[] anchorPointX = new float[] { 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.5f, 1.0f, 0.5f };
    static final float[] anchorPointY = new float[] { 0.0f, 1.0f, 1.0f, 0.0f, 0.5f, 1.0f, 0.5f, 0.0f };
    static final float[] displacmentMultiplicatorX = new float[] { 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 0.0f, -1.0f, 0.0f };
    static final float[] displacmentMultiplicatorY = new float[] { 1.0f, -1.0f, -1.0f, 1.0f, 0.0f, -1.0f, 0.0f, 1.0f };
    
    
    Label label;
    
    private int selection=0;
    
    double displacementX, displacementY;
    
    double totalMinX,totalMaxX,totalMinY,totalMaxY;
    double selMinX,selMaxX,selMinY,selMaxY;
    
    Polygon selectedPolygon;
    Polygon totalPolygon;
    
    /**
     * Creates a PointLabelPositionOptions - Object containing the original label,
     * 
     * @param label
     *          The label to autoposition
     *          
     * @param uomCalculator
     *          The {@link UomCalculator} from the RenderContext, used to calculate the correct displacement
     */
    public PointLabelPositionOptions( Label label, UomCalculator uomCalculator ){
        this.label = label;
        
        TextStyling styling = label.getStyling();
        displacementX = uomCalculator.considerUOM( styling.displacementX, styling.uom );
        displacementY = uomCalculator.considerUOM( styling.displacementY, styling.uom );
        
        select(0);
        calcTotalBoundingBox();
        totalPolygon = getPolygon(totalMinX, totalMinY, totalMaxX, totalMaxY);
    }
    
    double getMinX() {
        return totalMinX;
    }

    double getMaxX() {
        return totalMaxX;
    }

    double getMinY() {
        return totalMinY;
    }

    double getMaxY() {
        return totalMaxY;
    }
    
    double getSelectedMinX() {
        return selMinX;
    }

    double getSelectedMaxX() {
        return selMaxX;
    }

    double getSelectedMinY() {
        return selMinY;
    }

    double getSelectedMaxY() {
        return selMaxY;
    }
    
    /**
     * Returns the quality of the current position.
     */
    float getQuality(){
        return qualities[selection];
    }

    /**
     * Returns the quality for a position ( 0 to 7 ).
     * 
     * @param index
     *          position index for the requested quality
     *          
     */
    float getQuality(int index){
        return qualities[index];
    }
    
    /**
     * Selects one of the 8 possible positions.
     * 
     * @param selected
     *          A value between 0 and 7
     */
    public void select( int selected){
        selection = selected;
        
        Point2D.Double origin = label.getOrigin();
        Rectangle2D bounds = label.getLayout().getBounds();
        
        selMinX = origin.x + displacementX * displacmentMultiplicatorX[selection] + 0.5;
        selMaxY = origin.y - displacementY * displacmentMultiplicatorY[selection] + 0.5;
        selMinX -= anchorPointX[selection] * bounds.getWidth();
        selMaxY += anchorPointY[selection] * bounds.getHeight();
        selMaxX = selMinX + bounds.getWidth();
        selMinY = selMaxY - bounds.getHeight();
        
        selectedPolygon = getPolygon( selMinX, selMinY, selMaxX, selMaxY );
    }
    
    /**
     * Returns the current index of the selected option.
     */
    public int getSelectedIndex(){
        return selection;
    } 
    
    /**
     * Sets the contained {@link Label} to the selected position.
     */
    public void updateLabelPosition(){
        label.setDrawPosition( new Point2D.Double(selMinX,selMaxY) );
    }
    
    /**
     * Selects one of the possible label positions randomly.
     */
    public void selectLabelPositionRandomly() {
        select( (int) ( Math.random() * ( 8 - 1 ) + 0.5 ) );
    }
    
    /**
     * Tests the currently selected label position for intersection with the current selection of another label position.
     * If both labels are not rotated, an efficient AABB test is used, otherwise a polygon intersection is done
     * 
     * @param labelPosOption
     *          Another {@link PointLabelPositionOptions} to intersect with
     * 
     */
    boolean intersectsSelection( PointLabelPositionOptions labelPosOption ){
        
        if( label.getStyling().rotation != 0 || labelPosOption.label.getStyling().rotation != 0 ){
            
            return selectedPolygon.intersects( labelPosOption.selectedPolygon);
            
        }else
            return ((selMinX < labelPosOption.getSelectedMaxX()) && (selMaxX > labelPosOption.getSelectedMinX()) && (selMinY < labelPosOption.getSelectedMaxY()) && (selMaxY > labelPosOption.getSelectedMinY()));
    }
    
    /**
     * Tests if the bounding rectangle of all possible positions intersects with the bounding rectangle of another PointLabelPositionOptions-object
     * If both labels are not rotated, an efficient AABB test is used, otherwise a polygon intersection is done
     * 
     * @param labelPosOption
     *          Another {@link PointLabelPositionOptions} to intersect with
     * 
     */
    boolean intersectsAny( PointLabelPositionOptions labelPosOption ){
        
        if( label.getStyling().rotation != 0 || labelPosOption.label.getStyling().rotation != 0 ){
            
            return totalPolygon.intersects( labelPosOption.totalPolygon);

        }else
            return ((totalMinX < labelPosOption.getMaxX()) && (totalMaxX > labelPosOption.getMinX()) && (totalMinY < labelPosOption.getMaxY()) && (totalMaxY > labelPosOption.getMinY()));
    }
    

    /**
     * Creates a JTS-Polygon from min/max values and returns it.
     */
    private Polygon getPolygon(double minX, double minY, double maxX, double maxY){
        
        Point2D.Double origin = label.getOrigin();
           
        double x[] = new double[] {minX, minX, maxX, maxX};
        double y[] = new double[] {minY, maxY, maxY, minY};
        
        //rotating the points if necessary. This is much faster than using JTS-methods (AffineTransformation)
        double rot = label.getStyling().rotation;
        if( rot != 0 )
            rotatePoints(x,y,4,origin.x,origin.y,(Math.toRadians(rot)));

        Coordinate c1 = new Coordinate(x[0],y[0]);
        Coordinate c2 = new Coordinate(x[1],y[1]);
        Coordinate c3 = new Coordinate(x[2],y[2]);
        Coordinate c4 = new Coordinate(x[3],y[3]);
        
        return jtsFactory.createPolygon( new Coordinate[]{c1,c2,c3,c4,c1} );
    }
    
    
    private void rotatePoints( double x[], double y[], int num, double tx, double ty, double rotation ) {

        double cos = Math.cos( rotation );
        double sin = Math.sin( rotation );

        for(int i=0;i<num;i++){

            double m02 = tx - tx * cos + ty * sin;
            double m12 = ty - tx * sin - ty * cos;

            double xTmp = ( cos * x[i] + (-sin) * y[i] + m02 + 0.5 );
            y[i] = sin * x[i] + cos * y[i] + m12 + 0.5;
            x[i] = xTmp;
        }
    }

    /**
     * Calculates the bounding box around all possible label positions
     */
    private void calcTotalBoundingBox(){
        totalMinX = Float.MAX_VALUE;
        totalMaxX = -Float.MAX_VALUE;
        totalMinY = Float.MAX_VALUE;
        totalMaxY = -Float.MAX_VALUE;
        
        Point2D.Double origin = label.getOrigin();
        Rectangle2D bounds = label.getLayout().getBounds();
        
        for ( int i = 0; i < 4; i++){
        
            double px = origin.x + displacementX * displacmentMultiplicatorX[i] + 0.5;
            double py = origin.y - displacementY * displacmentMultiplicatorY[i] + 0.5;
            px -= anchorPointX[i] * bounds.getWidth();
            py += anchorPointY[i] * bounds.getHeight();
            if( px < totalMinX ) totalMinX = (float) px;
            if( px > totalMaxX ) totalMaxX = (float) px;
            if( py < totalMinY ) totalMinY = (float) py;
            if( py > totalMaxY ) totalMaxY = (float) py;
        }
        totalMaxX += bounds.getWidth();
        totalMinY -= bounds.getHeight();
        
    }
    
}






