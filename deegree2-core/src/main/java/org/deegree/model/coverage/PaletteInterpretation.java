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
 * Describes the color entry in a color table.
 *
 * @UML codelist CV_PaletteInterpretation
 * @author <A HREF="http://www.opengis.org">OpenGIS&reg; consortium</A>
 * @version <A HREF="http://www.opengis.org/docs/01-004.pdf">Grid Coverage specification 1.0</A>
 *
 * @see ColorInterpretation
 * @see SampleDimension
 */
public final class PaletteInterpretation extends CodeList {
    /**
     * Serial number for compatibility with different versions.
     */
    private static final long serialVersionUID = -7387623392932592485L;

    /**
     * Gray Scale color palette.
     *
     * @UML conditional CV_Gray
     * @see java.awt.color.ColorSpace#TYPE_GRAY
     */
    public static final PaletteInterpretation GRAY = new PaletteInterpretation( "GRAY", 0 );

    /**
     * RGB (Red Green Blue) color palette.
     *
     * @UML conditional CV_RGB
     * @see java.awt.color.ColorSpace#TYPE_RGB
     */
    public static final PaletteInterpretation RGB = new PaletteInterpretation( "RGB", 1 );

    /**
     * CYMK (Cyan Yellow Magenta blacK) color palette.
     *
     * @UML conditional CV_CMYK
     * @see java.awt.color.ColorSpace#TYPE_CMYK
     */
    public static final PaletteInterpretation CMYK = new PaletteInterpretation( "CMYK", 2 );

    /**
     * HSL (Hue Saturation Lightness) color palette.
     *
     * @UML conditional CV_HLS
     * @see java.awt.color.ColorSpace#TYPE_HLS
     */
    public static final PaletteInterpretation HLS = new PaletteInterpretation( "HLS", 3 );

    /**
     * List of all enumerations of this type.
     */
    private static final PaletteInterpretation[] VALUES = new PaletteInterpretation[] { GRAY, RGB, CMYK, HLS };

    /**
     * Constructs an enum with the given name.
     */
    private PaletteInterpretation( final String name, final int ordinal ) {
        super( name, ordinal );
    }

    /**
     * Returns the list of <code>PaletteInterpretation</code>s.
     *
     * @return the list of <code>PaletteInterpretation</code>s.
     */
    public static PaletteInterpretation[] values() {
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
