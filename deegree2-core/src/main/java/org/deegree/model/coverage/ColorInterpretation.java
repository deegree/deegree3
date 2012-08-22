//$HeadURL$
/*----------------------------------------------------------------------------
 This file originated as a part of GeoAPI.

 GeoAPI is free software. GeoAPI may be used, modified and
 redistributed by anyone for any purpose requring only maintaining the
 copyright and license terms on the source code and derivative files.
 See the OGC legal page for details.

 The copyright to the GeoAPI interfaces is held by the Open Geospatial
 Consortium, see http://www.opengeospatial.org/ogc/legal
----------------------------------------------------------------------------*/
package org.deegree.model.coverage;

/**
 * Specifies the mapping of a band to a color model component.
 *
 * @UML codelist CV_ColorInterpretation
 * @author <A HREF="http://www.opengis.org">OpenGIS&reg; consortium</A>
 * @version <A HREF="http://www.opengis.org/docs/01-004.pdf">Grid Coverage specification 1.0</A>
 *
 * @see PaletteInterpretation
 * @see SampleDimension
 */
final class ColorInterpretation extends CodeList {
    /**
     * Serial number for compatibility with different versions.
     */
    private static final long serialVersionUID = 6947933527594223350L;

    /**
     * Band is not associated with a color model component.
     *
     * @UML conditional CV_Undefined
     */
    public static final ColorInterpretation UNDEFINED = new ColorInterpretation( "UNDEFINED", 0 );

    /**
     * Band is an index into a lookup table.
     *
     * @UML conditional CV_GrayIndex
     * @see #PALETTE_INDEX
     * @see PaletteInterpretation#GRAY
     */
    public static final ColorInterpretation GRAY_INDEX = new ColorInterpretation( "GRAY_INDEX", 1 );

    /**
     * Band is a color index into a color table.
     *
     * @UML conditional CV_PaletteIndex
     * @see #GRAY_INDEX
     */
    public static final ColorInterpretation PALETTE_INDEX = new ColorInterpretation( "PALETTE_INDEX", 2 );

    /**
     * Red Band for the {@linkplain PaletteInterpretation#RGB RGB} color model components.
     *
     * @UML conditional CV_RedBand
     * @see #GREEN_BAND
     * @see #BLUE_BAND
     * @see #ALPHA_BAND
     * @see PaletteInterpretation#RGB
     */
    public static final ColorInterpretation RED_BAND = new ColorInterpretation( "RED_BAND", 3 );

    /**
     * Greend Band for the {@linkplain PaletteInterpretation#RGB RGB} color model components.
     *
     * @UML conditional CV_GreenBand
     * @see #RED_BAND
     * @see #BLUE_BAND
     * @see #ALPHA_BAND
     * @see PaletteInterpretation#RGB
     */
    public static final ColorInterpretation GREEN_BAND = new ColorInterpretation( "GREEN_BAND", 4 );

    /**
     * Blue Band for the {@linkplain PaletteInterpretation#RGB RGB} color model components.
     *
     * @UML conditional CV_BlueBand
     * @see #RED_BAND
     * @see #GREEN_BAND
     * @see #ALPHA_BAND
     * @see PaletteInterpretation#RGB
     */
    public static final ColorInterpretation BLUE_BAND = new ColorInterpretation( "BLUE_BAND", 5 );

    /**
     * Alpha Band for the {@linkplain PaletteInterpretation#RGB RGB} color model components. Alpha
     * band may or may not be present.
     *
     * @UML conditional CV_AlphaBand
     * @see #RED_BAND
     * @see #GREEN_BAND
     * @see #BLUE_BAND
     * @see PaletteInterpretation#RGB
     */
    public static final ColorInterpretation ALPHA_BAND = new ColorInterpretation( "ALPHA_BAND", 6 );

    /**
     * Hue Band for the {@linkplain PaletteInterpretation#HLS HLS} color model.
     *
     * @UML conditional CV_HueBand
     * @see #SATURATION_BAND
     * @see #LIGHTNESS_BAND
     * @see PaletteInterpretation#HLS
     */
    public static final ColorInterpretation HUE_BAND = new ColorInterpretation( "HUE_BAND", 7 );

    /**
     * Saturation Band for the {@linkplain PaletteInterpretation#HLS HLS} color model.
     *
     * @UML conditional CV_SaturationBand
     * @see #HUE_BAND
     * @see #LIGHTNESS_BAND
     * @see PaletteInterpretation#HLS
     */
    public static final ColorInterpretation SATURATION_BAND = new ColorInterpretation( "SATURATION_BAND", 8 );

    /**
     * Lightness Band for the {@linkplain PaletteInterpretation#HLS HLS} color model.
     *
     * @UML conditional CV_LightnessBand
     * @see #HUE_BAND
     * @see #SATURATION_BAND
     * @see PaletteInterpretation#HLS
     */
    public static final ColorInterpretation LIGHTNESS_BAND = new ColorInterpretation( "LIGHTNESS_BAND", 9 );

    /**
     * Cyan Band for the {@linkplain PaletteInterpretation#CMYK CMYK} color model.
     *
     * @UML conditional CV_CyanBand
     * @see #MAGENTA_BAND
     * @see #YELLOW_BAND
     * @see #BLACK_BAND
     * @see PaletteInterpretation#CMYK
     */
    public static final ColorInterpretation CYAN_BAND = new ColorInterpretation( "CYAN_BAND", 10 );

    /**
     * Magenta Band for the {@linkplain PaletteInterpretation#CMYK CMYK} color model.
     *
     * @UML conditional CV_MagentaBand
     * @see #CYAN_BAND
     * @see #YELLOW_BAND
     * @see #BLACK_BAND
     * @see PaletteInterpretation#CMYK
     */
    public static final ColorInterpretation MAGENTA_BAND = new ColorInterpretation( "MAGENTA_BAND", 11 );

    /**
     * Yellow Band for the {@linkplain PaletteInterpretation#CMYK CMYK} color model.
     *
     * @UML conditional CV_YellowBand
     * @see #CYAN_BAND
     * @see #MAGENTA_BAND
     * @see #BLACK_BAND
     * @see PaletteInterpretation#CMYK
     */
    public static final ColorInterpretation YELLOW_BAND = new ColorInterpretation( "YELLOW_BAND", 12 );

    /**
     * Black Band for the {@linkplain PaletteInterpretation#CMYK CMYK} color model.
     *
     * @UML conditional CV_BlackBand
     * @see #CYAN_BAND
     * @see #MAGENTA_BAND
     * @see #YELLOW_BAND
     * @see PaletteInterpretation#CMYK
     */
    public static final ColorInterpretation BLACK_BAND = new ColorInterpretation( "BLACK_BAND", 13 );

    /**
     * List of all enumerations of this type.
     */
    private static final ColorInterpretation[] VALUES = new ColorInterpretation[] { UNDEFINED, GRAY_INDEX,
                                                                                   PALETTE_INDEX, RED_BAND, GREEN_BAND,
                                                                                   BLUE_BAND, ALPHA_BAND, HUE_BAND,
                                                                                   SATURATION_BAND, LIGHTNESS_BAND,
                                                                                   CYAN_BAND, MAGENTA_BAND,
                                                                                   YELLOW_BAND, BLACK_BAND };

    /**
     * Constructs an enum with the given name.
     */
    private ColorInterpretation( final String name, final int ordinal ) {
        super( name, ordinal );
    }

    /**
     * Returns the list of <code>ColorInterpretation</code>s.
     *
     * @return the list of <code>ColorInterpretation</code>s.
     */
    public static ColorInterpretation[] values() {
        return VALUES.clone();
    }

    /**
     * Returns the list of enumerations of the same kind than this enum.
     */
    @Override
    public CodeList[] family() {
        return values();
    }
}
