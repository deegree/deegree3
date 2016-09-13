package org.deegree.uom;

import org.deegree.commons.uom.Measure;

import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Quantity;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

/**
 * Contains useful methods to convert one value to another using jscience.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class UomConverter {

    /**
     * @param measureToConvert containing the value and uom, never <code>null</code>
     * @return the value in the unit of the measure
     * @throws IllegalArgumentException if the value to convert to is not supported (currently only symbols are supported)
     */
    public static double toMeter( Measure measureToConvert ) {
        double valueAsDouble = measureToConvert.getValueAsDouble();
        String uomUri = measureToConvert.getUomUri();
        if ( uomUri != null && !uomUri.isEmpty() ) {
            return convertToMeter( valueAsDouble, uomUri );
        }
        return valueAsDouble;
    }

    private static double convertToMeter( double valueAsDouble, String uomUri ) {
        Unit<? extends Quantity> unitToConvertFrom = detectUnit( uomUri );
        UnitConverter converterTo = unitToConvertFrom.getConverterTo( SI.METER );
        return converterTo.convert( valueAsDouble );
    }

    private static Unit<? extends Quantity> detectUnit( String uomUri ) {
        try {
            return Unit.valueOf( uomUri );
        } catch ( IllegalArgumentException e ) {
            // TODO: try with mapping to symbol
            throw new IllegalArgumentException(
                                    "Could not transform from " + uomUri + " to meter. Unit " + uomUri +
                                    " is not supported" );
        }
    }

}
