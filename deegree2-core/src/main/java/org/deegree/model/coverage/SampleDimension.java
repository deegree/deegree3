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
package org.deegree.model.coverage;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

import org.deegree.datatypes.parameter.ParameterValueIm;

/**
 * Contains information for an individual sample dimension of coverage. This interface is applicable
 * to any coverage type. For grid coverages, the sample dimension refers to an individual band.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class SampleDimension implements Serializable {

    private static final long serialVersionUID = 6130760380289685594L;

    private ColorInterpretation colorInterpretation = null;

    private String[] categoryNames = null;

    private String description = null;

    private double maximumValue = 0;

    private double minimumValue = 0;

    private HashMap<String, Object> metadata = null;

    private double[] noData = null;

    private int offset = -1;

    private int[][] colorPalette = null;

    private PaletteInterpretation paletteInterpretation = null;

    private SampleDimensionType sampleDimensionType = null;

    private double scale = -1;

    private String units = null;

    /**
     * @param categoryNames
     * @param description
     * @param minimumValue
     * @param maximumValue
     * @param metadata
     * @param noData
     * @param offset
     * @param colorPalette
     * @param scale
     * @param units
     * @param colorInterpretation
     * @param paletteInterpretation
     * @param sampleDimensionType
     */
    public SampleDimension( String[] categoryNames, String description, double minimumValue, double maximumValue,
                            ParameterValueIm[] metadata, double[] noData, int offset, int[][] colorPalette,
                            double scale, String units, ColorInterpretation colorInterpretation,
                            PaletteInterpretation paletteInterpretation, SampleDimensionType sampleDimensionType ) {
        this.categoryNames = categoryNames;
        this.maximumValue = maximumValue;
        this.minimumValue = minimumValue;
        this.scale = scale;
        this.noData = noData;
        this.offset = offset;
        this.colorPalette = colorPalette;
        this.description = description;
        this.metadata = new HashMap<String, Object>();
        if ( metadata != null ) {
            for ( int i = 0; i < metadata.length; i++ ) {
                this.metadata.put( metadata[i].getDescriptor().getName(), metadata[i].getValue() );
            }
        }
        this.units = units;
        if ( colorInterpretation == null ) {
            this.colorInterpretation = ColorInterpretation.UNDEFINED;
        } else {
            this.colorInterpretation = colorInterpretation;
        }
        if ( paletteInterpretation == null ) {
            this.paletteInterpretation = PaletteInterpretation.GRAY;
        } else {
            this.paletteInterpretation = paletteInterpretation;
        }
        this.sampleDimensionType = sampleDimensionType;

    }

    /**
     * Sequence of category names for the values contained in a sample dimension. This allows for
     * names to be assigned to numerical values. The first entry in the sequence relates to a cell
     * value of zero. For grid coverages, category names are only valid for a classified grid data.
     *
     * For example:<br>
     * <UL>
     * <li>0 Background</li>
     * <li>1 Water</li>
     * <li>2 Forest</li>
     * <li>3 Urban</li>
     * </UL>
     * Note: If no category names exist, an empty sequence is returned.
     *
     * @return the sequence of category names for the values contained in a sample dimension.
     *
     */
    public String[] getCategoryNames() {
        return categoryNames;
    }

    /**
     * Color interpretation of the sample dimension. A sample dimension can be an index into a color
     * palette or be a color model component. If the sample dimension is not assigned a color
     * interpretation the value is {@link ColorInterpretation#UNDEFINED Undefined}.
     *
     * @return the color interpretation of the sample dimension.
     *
     */
    public ColorInterpretation getColorInterpretation() {
        return colorInterpretation;
    }

    /**
     * Sample dimension title or description. This string may be null or empty if no description is
     * present.
     *
     * @return the sample dimension title or description.
     *
     */
    public String getDescription() {
        return description;
    }

    /**
     * The maximum value occurring in the sample dimension. If this value is not available, this
     * value can be determined from the {@link "org.opengis"} operation. This value can be empty if
     * this value is not provided by the implementation.
     *
     * @return the maximum value occurring in the sample dimension.
     *
     */
    public double getMaximumValue() {
        return maximumValue;
    }

    /**
     * The list of metadata keywords for a sample dimension. If no metadata is available, the
     * sequence will be empty.
     *
     * @return the list of metadata keywords for a sample dimension.
     *
     */
    public String[] getMetaDataNames() {
        Iterator<String> iterator = metadata.keySet().iterator();
        String[] names = new String[metadata.size()];
        int i = 0;
        while ( iterator.hasNext() ) {
            names[i++] = iterator.next();
        }
        return names;
    }

    /**
     * Retrieve the metadata value for a given metadata name.
     *
     * @param name
     *            Metadata keyword for which to retrieve metadata.
     * @return the metadata value for a given metadata name.
     *
     */
    public String getMetadataValue( String name ) {
        return (String) metadata.get( name );
    }

    /**
     * The minimum value occurring in the sample dimension. This value can be empty if this value is
     * not provided by the implementation.
     *
     * @return the minimum value occurring in the sample dimension.
     *
     */
    public double getMinimumValue() {
        return minimumValue;
    }

    /**
     * Values to indicate no data values for the sample dimension. For low precision sample
     * dimensions, this will often be no data values.
     *
     * @return the values to indicate no data values for the sample dimension.
     *
     */
    public double[] getNoDataValues() {
        return noData;
    }

    /**
     * Offset is the value to add to grid values for this sample dimension. This attribute is
     * typically used when the sample dimension represents elevation data. The default for this
     * value is 0.
     *
     * @return the offset is the value to add to grid values for this sample dimension.
     *
     */
    public double getOffset() {
        return offset;
    }

    /**
     * Color palette associated with the sample dimension. A color palette can have any number of
     * colors. See palette interpretation for meaning of the palette entries. If the grid coverage
     * has no color palette, an empty sequence will be returned.
     *
     * @return the color palette associated with the sample dimension.
     *
     */
    public int[][] getPalette() {
        return colorPalette;
    }

    /**
     * Indicates the type of color palette entry for sample dimensions which have a palette. If a
     * sample dimension has a palette, the color interpretation must be
     * {@link ColorInterpretation#GRAY_INDEX GrayIndex} or
     * {@link ColorInterpretation#PALETTE_INDEX PaletteIndex}. A palette entry type can be Gray,
     * RGB, CMYK or HLS.
     *
     * @return the type of color palette entry for sample dimensions which have a palette.
     *
     */
    public PaletteInterpretation getPaletteInterpretation() {
        return paletteInterpretation;
    }

    /**
     * A code value indicating grid value data type. This will also indicate the number of bits for
     * the data type.
     *
     * @return a code value indicating grid value data type.
     *
     */
    public SampleDimensionType getSampleDimensionType() {
        return sampleDimensionType;
    }

    /**
     * Scale is the value which is multiplied to grid values for this sample dimension. This
     * attribute is typically used when the sample dimension represents elevation data. The default
     * for this value is 1.
     *
     * @return the scale.
     *
     */
    public double getScale() {
        return scale;
    }

    /**
     * The unit information for this sample dimension. This interface typically is provided with
     * grid coverages which represent digital elevation data. This value will be <code>null</code>
     * if no unit information is available.
     *
     * @return the unit information for this sample dimension.
     *
     */
    public String getUnits() {
        return units;
    }

}
