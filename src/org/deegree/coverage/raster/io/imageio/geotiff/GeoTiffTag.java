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

package org.deegree.coverage.raster.io.imageio.geotiff;

/**
 * The <code>GeoTiffTag</code> interface defines GeoTIFF tags/numbers for the access of TiffDirectories in GeoTIFF
 * images http://www.gisdevelopment.net/technology/ip/mi03117pf.htm for further details.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 */
public interface GeoTiffTag {

    // general tiff tags
    /** width */
    public static final int ImageWidth = 256;

    /** ImageLength */
    public static final int ImageLength = 257;

    /** BitsPerSample */
    public static final int BitsPerSample = 258;

    /** Compression */
    public static final int Compression = 259;

    /** PhotometricInterpretation */
    public static final int PhotometricInterpretation = 262;

    /** FillOrder */
    public static final int FillOrder = 266;

    /** DocumentName */
    public static final int DocumentName = 269;

    /** ImageDescription */
    public static final int ImageDescription = 270;

    /** StripOffsets */
    public static final int StripOffsets = 273;

    /** Orientation */
    public static final int Orientation = 274;

    /** SamplesPerPixel */
    public static final int SamplesPerPixel = 277;

    /** RowsPerStrip */
    public static final int RowsPerStrip = 278;

    /** StripByteCounts */
    public static final int StripByteCounts = 279;

    /** XResolution */
    public static final int XResolution = 282;

    /** YResolution */
    public static final int YResolution = 283;

    /** PlanarConfiguration */
    public static final int PlanarConfiguration = 284;

    /** ResolutionUnit */
    public static final int ResolutionUnit = 296;

    /** Software */
    public static final int Software = 305;

    /** ColorMap */
    public static final int ColorMap = 320;

    /** TileWidth */
    public static final int TileWidth = 322;

    /** TileLength */
    public static final int TileLength = 323;

    /** TileOffsets */
    public static final int TileOffsets = 324;

    /** TileByteCounts */
    public static final int TileByteCounts = 325;

    /** SampleFormat */
    public static final int SampleFormat = 339;

    /** SMinSampleValue */
    public static final int SMinSampleValue = 340;

    /** SMaxSampleValue */
    public static final int SMaxSampleValue = 341;

    // tiff tags used for geotiff
    /** ModelPixelScaleTag */
    public static final int ModelPixelScaleTag = 33550;

    /** IntergraphMatrixTag */
    public static final int IntergraphMatrixTag = 33920;

    /** ModelTiepointTag */
    public static final int ModelTiepointTag = 33922;

    /** ModelTransformationTag, the transformation description */
    public static final int ModelTransformationTag = 34264;

    /** GeoKeyDirectoryTag, top level geo information */
    public static final int GeoKeyDirectoryTag = 34735;

    /** GeoDoubleParamsTag, directory references double values */
    public static final int GeoDoubleParamsTag = 34736;

    /** GeoAsciiParamsTag, directory references ascii strings */
    public static final int GeoAsciiParamsTag = 34737;

}
