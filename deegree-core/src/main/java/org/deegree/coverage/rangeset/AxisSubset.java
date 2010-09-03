//$HeadURL: svn+ssh://rbezema@svn.wald.intevation.org/deegree/deegree3/services/trunk/src/org/deegree/services/wcs/model/AxisSubset.java $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.coverage.rangeset;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The <code>AxisSubset</code> class represents the subset defined on one of the axis of the coverage.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author: rbezema $
 * @version $Revision: 19041 $, $Date: 2009-08-11 17:04:57 +0200 (Di, 11 Aug 2009) $
 * 
 */
public class AxisSubset {

    private final List<Interval<?, ?>> intervals;

    private final List<SingleValue<?>> singleValues;

    private final String name;

    private final String label;

    private ValueType type;

    /**
     * @param name
     * @param label
     *            may be <code>null</code>, in this case the name will be returned as label.
     * @param intervals
     * @param singleValues
     */
    public AxisSubset( String name, String label, List<Interval<?, ?>> intervals, List<SingleValue<?>> singleValues ) {
        this.name = name;
        this.label = label;
        this.intervals = intervals;
        this.singleValues = singleValues;
        ValueType tmpType = determineType( intervals );
        if ( tmpType == null ) {
            tmpType = determineTypeFromSingles( singleValues );
        }
        this.type = tmpType == null ? ValueType.Void : tmpType;

    }

    /**
     * @return the intervals
     */
    public final List<Interval<?, ?>> getIntervals() {
        return intervals;
    }

    /**
     * @return the singleValues
     */
    public final List<SingleValue<?>> getSingleValues() {
        return singleValues;
    }

    /**
     * @return the name
     */
    public final String getName() {
        return name;
    }

    /**
     * @return true if the given axis has interval or singlevalues defined.
     */
    public boolean hasAxisConstraints() {
        return ( intervals != null && !intervals.isEmpty() ) || ( singleValues != null && !singleValues.isEmpty() );
    }

    /**
     * @param other
     * @param convert
     *            if true the intervals and singlevalues of this instance will be converted to the type of the given
     *            axis subset if their names match and if this type is unknown (void).
     * @return true if this {@link AxisSubset} matches the given AxisSubset, e.g. if the names are equal and the axis
     *         values have matching parameters in the given one.
     */
    public boolean match( AxisSubset other, boolean convert ) {
        boolean result = other.getName().equalsIgnoreCase( name );
        if ( result ) {
            if ( this.type == ValueType.Void && convert ) {
                convertTypes( other.getType() );
            }
            boolean ic = checkIntervals( other.getIntervals() );
            boolean sc = checkSingles( other.getSingleValues(), other.getIntervals() );
            result = ic && sc;
        }
        return result;
    }

    /**
     * @param otherValues
     * @param convert
     *            if true the given intervals will be converted to the type they match if they were of type void.
     * @return true if the given singleValues match these of the given single values, the types are considered.
     */
    private boolean checkSingles( List<SingleValue<?>> otherValues, List<Interval<?, ?>> otherIntervals ) {
        boolean result = false;
        if ( singleValues == null || singleValues.isEmpty() ) {
            // if this axissubset has no singlevalues, than they match
            return true;
        }

        if ( otherValues != null && !otherValues.isEmpty() ) {
            for ( SingleValue<?> sv : singleValues ) {
                // rb: iterate over all values, if one of them mismatches this method will return false.
                if ( sv != null ) {
                    // if the value == null, the default value must be taken into account, therefore no validity check
                    // can be done.
                    if ( sv.value != null ) {
                        Iterator<SingleValue<?>> iterator = otherValues.iterator();
                        while ( iterator.hasNext() && !result ) {
                            SingleValue<?> ov = iterator.next();
                            result = sv.equals( ov );
                        }

                        if ( !result ) {
                            // could not find a single value matching.
                            break;
                        }

                    }
                } else {
                    result = true;
                }

            }
        } else if ( otherIntervals != null && !otherIntervals.isEmpty() ) {
            // if the other has intervals, does these single values fit in the intervals?
            for ( SingleValue<?> sv : singleValues ) {
                // rb: iterate over all values, if one of them mismatches this method will return false.
                if ( sv != null ) {
                    // if the value == null, the default value must be taken into account, therefore no validity check
                    // can
                    // be done.
                    if ( sv.value != null ) {
                        Iterator<Interval<?, ?>> iterator = otherIntervals.iterator();
                        while ( iterator.hasNext() && !result ) {
                            // rb: type checking is done, but the compiler can not resolve it.
                            Interval tmpInter = iterator.next();
                            if ( tmpInter != null ) {
                                SingleValue<?> min = tmpInter.getMin();
                                if ( min.type != sv.type ) {
                                    if ( min.type == ValueType.Void ) {
                                        tmpInter = Interval.createFromStrings( sv.type.toString(),
                                                                               tmpInter.getMin().value.toString(),
                                                                               tmpInter.getMax().value.toString(),
                                                                               tmpInter.getClosure(),
                                                                               tmpInter.getSemantic(),
                                                                               tmpInter.isAtomic(),
                                                                               tmpInter.getSpacing() );
                                    } else if ( sv.type == ValueType.Void ) {
                                        sv = SingleValue.createFromString( min.type.toString(), sv.value.toString() );
                                    } else {
                                        continue;
                                    }

                                }
                                result = tmpInter.liesWithin( sv.value );
                            }

                        }

                        if ( !result ) {
                            // could not find a single value matching.
                            break;
                        }

                    }
                } else {
                    result = true;
                }

            }
        }

        return result;
    }

    /**
     * @param otherIntervals
     * @param convert
     *            if true the given intervals will be converted to the type they match if they were of type void.
     * @return true if the given intervals match this intervals.
     */
    private boolean checkIntervals( List<Interval<?, ?>> otherIntervals ) {
        boolean result = false;
        if ( intervals == null || intervals.isEmpty() ) {
            // if this axissubset has no intervals, than they match
            return true;
        }
        for ( Interval<?, ?> inter : intervals ) {
            if ( inter != null ) {
                Iterator<Interval<?, ?>> iterator = otherIntervals.iterator();
                while ( iterator.hasNext() && !result ) {
                    Interval<?, ?> oi = iterator.next();
                    result = inter.isInBounds( oi );
                }
                if ( !result ) {
                    // could not find a single value matching.
                    break;
                }
            }
        }

        return result;
    }

    /**
     * @return the label or if not present the name
     */
    public String getLabel() {
        return label == null ? name : label;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append( "[" ).append( name );
        if ( label != null && !"".equals( label ) ) {
            sb.append( " {'" ).append( label ).append( "'}" );
        }
        sb.append( ": " );
        if ( intervals != null && !intervals.isEmpty() ) {
            Iterator<Interval<?, ?>> it = intervals.iterator();
            while ( it.hasNext() ) {
                Interval<?, ?> in = it.next();
                if ( in != null ) {
                    sb.append( "[" ).append( in.toString() ).append( "]" );
                }
                if ( it.hasNext() ) {
                    sb.append( "," );
                }
            }
        }
        if ( singleValues != null && !singleValues.isEmpty() ) {
            Iterator<SingleValue<?>> it = singleValues.iterator();
            while ( it.hasNext() ) {
                SingleValue<?> sv = it.next();
                if ( sv != null ) {
                    sb.append( "[" ).append( sv.toString() ).append( "]" );
                }
                if ( it.hasNext() ) {
                    sb.append( "," );
                }
            }
        }
        sb.append( "]" );
        return sb.toString();
    }

    /**
     * Convert the types of the intervals and single values of this axis subset to the given type, if and only if the
     * type of this axis is {@link ValueType#Void}, if the conversion fails, the old types will not be changed.
     * 
     * @param newType
     *            to convert the types of this axis to.
     */
    public void convertTypes( ValueType newType ) {
        if ( newType != null && newType != ValueType.Void && type == ValueType.Void ) {
            // my type is void, fix it.
            String type = newType.name();
            if ( intervals != null && !intervals.isEmpty() ) {
                this.type = newType;

                List<Interval<?, ?>> convertedIntervals = new ArrayList<Interval<?, ?>>( intervals.size() );
                Iterator<Interval<?, ?>> it = intervals.iterator();
                while ( it.hasNext() ) {
                    Interval<?, ?> origInter = it.next();
                    if ( origInter != null ) {
                        Interval<?, ?> converted = Interval.createFromStrings( type,
                                                                               origInter.getMin().value.toString(),
                                                                               origInter.getMax().value.toString(),
                                                                               origInter.getClosure(),
                                                                               origInter.getSemantic(),
                                                                               origInter.isAtomic(),
                                                                               origInter.getSpacing() );
                        if ( converted != null ) {
                            convertedIntervals.add( converted );
                        }
                    }
                    if ( this.intervals.size() == convertedIntervals.size() ) {
                        this.intervals.clear();
                        this.intervals.addAll( convertedIntervals );
                    }

                }

            }
            if ( singleValues != null && !singleValues.isEmpty() ) {
                List<SingleValue<?>> convertedSingles = new ArrayList<SingleValue<?>>( singleValues.size() );
                Iterator<SingleValue<?>> it = singleValues.iterator();
                while ( it.hasNext() ) {
                    SingleValue<?> origSingle = it.next();
                    if ( origSingle != null ) {
                        SingleValue<?> converted = SingleValue.createFromString( type, origSingle.value.toString() );

                        if ( converted != null ) {
                            convertedSingles.add( converted );
                        }
                    }
                }
                if ( this.singleValues.size() == convertedSingles.size() ) {
                    this.singleValues.clear();
                    this.singleValues.addAll( convertedSingles );
                }
            }
        }

    }

    /**
     * @param singles
     * @return
     */
    private ValueType determineTypeFromSingles( List<SingleValue<?>> singles ) {
        ValueType result = null;
        if ( singles != null && !singles.isEmpty() ) {
            Iterator<SingleValue<?>> it = singleValues.iterator();
            while ( it.hasNext() && result == null ) {
                SingleValue<?> sv = it.next();
                if ( sv != null ) {
                    result = sv.type;
                }
            }
        }
        return result;
    }

    /**
     * @param inters
     * @return
     */
    private ValueType determineType( List<Interval<?, ?>> inters ) {
        ValueType result = null;
        if ( inters != null && !inters.isEmpty() ) {
            Iterator<Interval<?, ?>> it = intervals.iterator();
            while ( it.hasNext() && result == null ) {
                Interval<?, ?> interval = it.next();
                if ( interval != null ) {
                    result = interval.getMin().type;
                }
            }
        }
        return result;
    }

    /**
     * Returns the type of this axis ranges.
     * 
     * @return the determined type of this axis ranges, if the type could not be determined it will be
     *         {@link ValueType#Void}
     */
    public ValueType getType() {
        return type;
    }

}
