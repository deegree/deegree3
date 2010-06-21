//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.client.mdeditor.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class InputFormField extends FormField {

    // TODO
    private String timePattern = "yyyy-MM-dd";

    private INPUT_TYPE inputType;

    private Validation validation;

    private int occurence;

    public InputFormField( FormFieldPath path, String id, String label, boolean visible, boolean required, String help,
                           INPUT_TYPE inputType, int occurence, String defaultValue, Validation validation ) {
        super( path, id, label, visible, required, help, defaultValue );
        this.inputType = inputType;
        this.validation = validation;
        this.occurence = occurence;

        if ( validation != null ) {
            switch ( inputType ) {
            case TIMESTAMP:
                setTitle( validation.getTimestampPattern() != null ? validation.getTimestampPattern() : timePattern );
                break;
            case DOUBLE:
                setTitle( "double value between " + validation.getMinValue() + " and " + validation.getMaxValue() );
                break;
            case INT:
                setTitle( "int value between " + validation.getMinValue() + " and " + validation.getMaxValue() );
                break;
            case TEXT:
                if ( validation.getLength() > 0 ) {
                    setTitle( "string with max length " + validation.getLength() );
                }
                break;
            }
        }
    }

    public void setInputType( INPUT_TYPE inputType ) {
        this.inputType = inputType;
    }

    public INPUT_TYPE getInputType() {
        return inputType;
    }

    public void setOccurence( int occurence ) {
        this.occurence = occurence;
    }

    public int getOccurence() {
        return occurence;
    }

    public Object getValue() {
        if ( !( value instanceof List<?> ) && occurence != 1 ) {
            ArrayList<Object> valueList = new ArrayList<Object>();
            valueList.add( value );
            return valueList;
        }
        return value;
    }

    public void setValidation( Validation validation ) {
        this.validation = validation;
    }

    public Validation getValidation() {
        return validation;
    }

    @Override
    public Map<VALIDATION_TYPE, String[]> validate() {
        Map<VALIDATION_TYPE, String[]> validationMap = super.validate();
        if ( value != null ) {
            if ( value instanceof List<?> ) {
                // TODO: validate list
            } else {
                switch ( inputType ) {
                case TIMESTAMP:
                    try {
                        if ( validation != null && validation.getTimestampPattern() != null ) {
                            timePattern = validation.getTimestampPattern();
                        }
                        SimpleDateFormat format = new SimpleDateFormat( timePattern );
                        format.parse( (String) value );
                    } catch ( Exception e ) {
                        addValidation( validationMap, VALIDATION_TYPE.DATE, timePattern );
                    }
                    break;
                case DOUBLE:
                    try {
                        double d = Double.parseDouble( (String) value );
                        validateNumber( validationMap, d );
                    } catch ( Exception e ) {
                        addValidation( validationMap, VALIDATION_TYPE.DOUBLE );
                    }
                    break;
                case INT:
                    try {
                        int i = Integer.parseInt( (String) value );
                        validateNumber( validationMap, i );
                    } catch ( Exception e ) {
                        addValidation( validationMap, VALIDATION_TYPE.INT );
                    }
                    break;
                case TEXT:
                    String s = (String) value;
                    if ( ( validation != null && validation.getLength() > 0 && s.length() >= validation.getLength() ) ) {
                        addValidation( validationMap, VALIDATION_TYPE.LENGTH, Integer.toString( validation.getLength() ) );
                    }
                    break;
                }
            }
        }
        return validationMap;
    }

    private void validateNumber( Map<VALIDATION_TYPE, String[]> validationMap, double i ) {
        boolean minInvalid = !( validation != null && i >= validation.getMinValue() );
        boolean maxInvalid = !( validation != null && i <= validation.getMaxValue() );
        if ( minInvalid && maxInvalid ) {
            addValidation( validationMap, VALIDATION_TYPE.RANGE, Double.toString( validation.getMinValue() ),
                           Double.toString( validation.getMaxValue() ) );

        } else if ( minInvalid ) {
            addValidation( validationMap, VALIDATION_TYPE.MIN, Double.toString( validation.getMinValue() ) );
        } else if ( maxInvalid ) {
            addValidation( validationMap, VALIDATION_TYPE.MAX, Double.toString( validation.getMaxValue() ) );
        }
    }
}
