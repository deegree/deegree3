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

// J2SE direct dependencies
import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collection;

/**
 * Base class for all code lists.
 *
 * @author <A HREF="http://www.opengis.org">OpenGIS&reg; consortium</A>
 * @version 2.0
 */
public abstract class CodeList implements Serializable {
    /**
     * Serial number for compatibility with different versions.
     */
    private static final long serialVersionUID = 5655809691319522885L;

    /**
     * The code value. For J2SE 1.3 profile only.
     */
    private transient final int ordinal;

    /**
     * The code name. For J2SE 1.3 profile only.
     */
    private final String name;

    /**
     * Create a new code list instance.
     *
     * @param name
     *            The code name.
     * @param ordinal
     *            The code value.
     */
    protected CodeList( final String name, final int ordinal ) {
        this.name = name;
        this.ordinal = ordinal;
    }

    /**
     * Create a new code list instance and add it to the given collection.
     *
     * @param name
     *            The code name.
     * @param values
     *            The collection to add the enum to.
     */
    CodeList( final String name, final Collection<CodeList> values ) {
        this.name = name;
        synchronized ( values ) {
            this.ordinal = values.size();
            if ( !values.add( this ) ) {
                throw new IllegalArgumentException( String.valueOf( values ) );
            }
        }
    }

    /**
     * Returns the ordinal of this enumeration constant (its position in its enum declaration, where
     * the initial constant is assigned an ordinal of zero).
     *
     * @return the ordinal of this enumeration constant.
     */
    public final int ordinal() {
        return ordinal;
    }

    /**
     * Returns the name of this enum constant.
     *
     * @return the name of this enum constant.
     */
    public final String name() {
        return name;
    }

    /**
     * Returns the list of enumerations of the same kind than this enum.
     *
     * @return the list of enumerations of the same kind than this enum.
     */
    public abstract CodeList[] family();

    /**
     * Returns a string representation of this code list.
     */
    @Override
    public String toString() {
        String classname = getClass().getName();
        final int i = classname.lastIndexOf( '.' );
        if ( i >= 0 ) {
            classname = classname.substring( i + 1 );
        }
        return classname + '[' + name + ']';
    }

    /**
     * Resolve the code list to an unique instance after deserialization. The instance is resolved
     * using its {@linkplain #name() name} only (not its {@linkplain #ordinal() ordinal}).
     *
     * @return This code list as a unique instance.
     * @throws ObjectStreamException
     *             if the deserialization failed.
     */
    protected Object readResolve()
                            throws ObjectStreamException {
        final CodeList[] codes = family();
        for ( int i = 0; i < codes.length; i++ ) {
            if ( name.equals( codes[i].name ) ) {
                return codes[i];
            }
        }
        throw new InvalidObjectException( toString() );
    }
}
