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
package org.deegree.services.wps.provider.jrxml.contentprovider;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.process.jaxb.java.AllowedValues;
import org.deegree.process.jaxb.java.LiteralInputDefinition;
import org.deegree.process.jaxb.java.LiteralInputDefinition.DataType;
import org.deegree.process.jaxb.java.ProcessletInputDefinition;
import org.deegree.process.jaxb.java.Range;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.input.LiteralInput;
import org.deegree.services.wps.input.ProcessletInput;
import org.deegree.services.wps.provider.jrxml.ParameterDescription;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link JrxmlContentProvider} for literal parameters
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * 
 */
public class OtherContentProvider extends AbstractJrxmlContentProvider {

    public OtherContentProvider( Workspace workspace ) {
        super( workspace );
    }

    private static final Logger LOG = LoggerFactory.getLogger( OtherContentProvider.class );

    @Override
    public void inspectInputParametersFromJrxml( Map<String, ParameterDescription> parameterDescriptions,
                                                 List<JAXBElement<? extends ProcessletInputDefinition>> inputs,
                                                 XMLAdapter jrxmlAdapter, Map<String, String> parameters,
                                                 List<String> handledParameters ) {

        for ( String parameterName : parameters.keySet() ) {
            if ( parameters.containsKey( parameterName ) && !handledParameters.contains( parameterName ) ) {
                LiteralInputDefinition lit = new LiteralInputDefinition();
                addInput( lit, parameterDescriptions, parameterName, 1, 0 );
                lit.setDefaultValue( parameterName );

                String parameterType = parameters.get( parameterName );
                String dtValue = null;
                String dtRef = null;
                if ( parameterType == null || "java.lang.String".equals( parameterType ) ) {
                    dtValue = "string";
                    dtRef = "http://www.w3.org/2001/XMLSchema.xsd#~string";
                } else if ( "java.lang.Boolean".equals( parameterType ) ) {
                    dtValue = "boolean";
                    dtRef = "http://www.w3.org/2001/XMLSchema.xsd#~boolean";
                } else if ( "java.lang.Double".equals( parameterType ) ) {
                    dtValue = "double";
                    dtRef = "http://www.w3.org/2001/XMLSchema.xsd#~double";
                } else if ( "java.lang.Integer".equals( parameterType ) ) {
                    dtValue = "integer";
                    dtRef = "http://www.w3.org/2001/XMLSchema.xsd#~integer";
                } else if ( "java.lang.Float".equals( parameterType ) ) {
                    dtValue = "float";
                    dtRef = "http://www.w3.org/2001/XMLSchema.xsd#~float";
                } else if ( "java.lang.Long".equals( parameterType ) ) {
                    dtValue = "long";
                    dtRef = "http://www.w3.org/2001/XMLSchema.xsd#~long";
                } else if ( "java.util.Date".equals( parameterType ) ) {
                    dtValue = "date";
                    dtRef = "http://www.w3.org/2001/XMLSchema.xsd#~date";
                } else if ( "java.sql.Timestamp".equals( parameterType ) ) {
                    dtValue = "dateTime";
                    dtRef = "http://www.w3.org/2001/XMLSchema.xsd#~dateTime";
                } else if ( "java.sql.Time".equals( parameterType ) ) {
                    dtValue = "time";
                    dtRef = "http://www.w3.org/2001/XMLSchema.xsd#~time";
                } else if ( "java.math.BigDecimal".equals( parameterType ) || "java.lang.Number".equals( parameterType ) ) {
                    dtValue = "decimal";
                    dtRef = "http://www.w3.org/2001/XMLSchema.xsd#~decimal";
                } else if ( "java.lang.Short".equals( parameterType ) ) {
                    dtValue = "integer";
                    dtRef = "http://www.w3.org/2001/XMLSchema.xsd#~integer";
                    AllowedValues allowedValues = new AllowedValues();
                    Range range = new Range();
                    range.setMinimumValue( Short.toString( Short.MIN_VALUE ) );
                    range.setMaximumValue( Short.toString( Short.MAX_VALUE ) );
                    allowedValues.getValueOrRange().add( range );
                    lit.setAllowedValues( allowedValues );
                } else if ( "java.lang.Byte".equals( parameterType ) ) {
                    dtValue = "integer";
                    dtRef = "http://www.w3.org/2001/XMLSchema.xsd#~integer";
                    AllowedValues allowedValues = new AllowedValues();
                    Range range = new Range();
                    range.setMinimumValue( Byte.toString( Byte.MIN_VALUE ) );
                    range.setMaximumValue( Byte.toString( Byte.MAX_VALUE ) );
                    allowedValues.getValueOrRange().add( range );
                    lit.setAllowedValues( allowedValues );
                } else {
                    LOG.info( "Unknown datatype of parameter '" + parameterName + "': " + parameterType );
                    return;
                }
                if ( dtValue != null ) {
                    DataType dataType = new DataType();
                    dataType.setValue( dtValue );
                    dataType.setReference( dtRef );
                    lit.setDataType( dataType );
                }

                inputs.add( new JAXBElement<LiteralInputDefinition>( new QName( "ProcessInput" ),
                                                                     LiteralInputDefinition.class, lit ) );
                handledParameters.add( parameterName );
            }
        }
    }

    @Override
    public Pair<InputStream, Boolean> prepareJrxmlAndReadInputParameters( InputStream jrxml,
                                                                          Map<String, Object> params,
                                                                          ProcessletInputs in,
                                                                          List<CodeType> processedIds,
                                                                          Map<String, String> parameters )
                            throws ProcessletException {
        for ( ProcessletInput parameter : in.getParameters() ) {
            if ( parameters.containsKey( parameter.getIdentifier().getCode() )
                 && !processedIds.contains( parameter.getIdentifier() ) ) {
                if ( parameter instanceof LiteralInput ) {
                    LiteralInput litIn = (LiteralInput) parameter;
                    String litValue = litIn.getValue();
                    Object value = litValue;
                    String parameterType = parameters.get( litIn.getIdentifier().getCode() );

                    LOG.debug( "Try to convert {} (parameter '{}') to {}", new Object[] { litIn.getIdentifier(),
                                                                                         litValue, parameterType } );

                    String datePattern = "yyyy-MM-dd";
                    if ( parameterType == null || "java.lang.String".equals( parameterType ) ) {
                    } else if ( "java.lang.Boolean".equals( parameterType ) ) {
                        value = Boolean.parseBoolean( litValue );
                    } else if ( "java.lang.Double".equals( parameterType ) ) {
                        try {
                            value = Double.parseDouble( litValue );
                        } catch ( NumberFormatException e ) {
                            throw new ProcessletException( "Invalid datatype for parameter '" + litIn.getIdentifier()
                                                           + "': " + litValue + " is not a double value!" );
                        }
                    } else if ( "java.lang.Integer".equals( parameterType ) ) {
                        try {
                            value = Integer.parseInt( litValue );
                        } catch ( NumberFormatException e ) {
                            throw new ProcessletException( "Invalid datatype for parameter '" + litIn.getIdentifier()
                                                           + "': " + litValue + " is not a integer value!" );
                        }
                    } else if ( "java.lang.Float".equals( parameterType ) ) {
                        try {
                            value = Float.parseFloat( litValue );
                        } catch ( NumberFormatException e ) {
                            throw new ProcessletException( "Invalid datatype for parameter '" + litIn.getIdentifier()
                                                           + "': " + litValue + " is not a float value!" );
                        }
                    } else if ( "java.lang.Long".equals( parameterType ) ) {
                        try {
                            value = Long.parseLong( litValue );
                        } catch ( NumberFormatException e ) {
                            throw new ProcessletException( "Invalid datatype for parameter '" + litIn.getIdentifier()
                                                           + "': " + litValue + " is not a long value!" );
                        }
                    } else if ( "java.util.Date".equals( parameterType ) ) {
                        try {
                            SimpleDateFormat df = new SimpleDateFormat( datePattern );
                            value = df.parse( litValue );
                        } catch ( ParseException e ) {
                            throw new ProcessletException( "Invalid datatype for parameter '" + litIn.getIdentifier()
                                                           + "': " + litValue + " is not a date value! Pattern: "
                                                           + datePattern );
                        }
                    } else if ( "java.sql.Timestamp".equals( parameterType ) ) {
                        try {
                            value = Timestamp.valueOf( litValue );
                        } catch ( NumberFormatException e ) {
                            throw new ProcessletException( "Invalid datatype for parameter '" + litIn.getIdentifier()
                                                           + "': " + litValue + " is not a dateTime value!" );
                        }
                    } else if ( "java.sql.Time".equals( parameterType ) ) {
                        try {
                            value = Time.valueOf( litValue );
                        } catch ( NumberFormatException e ) {
                            throw new ProcessletException( "Invalid datatype for parameter '" + litIn.getIdentifier()
                                                           + "': " + litValue + " is not a time value!" );
                        }
                    } else if ( "java.math.BigDecimal".equals( parameterType ) ) {
                        try {
                            value = new BigDecimal( litValue );
                        } catch ( NumberFormatException e ) {
                            throw new ProcessletException( "Invalid datatype for parameter '" + litIn.getIdentifier()
                                                           + "': " + litValue + " is not a decimal value!" );
                        }
                    } else if ( "java.lang.Number".equals( parameterType ) ) {
                        try {
                            Number n = null;
                            try {
                                n = Integer.parseInt( litValue );
                            } catch ( NumberFormatException e ) {
                                try {
                                    n = Double.parseDouble( litValue );
                                } catch ( NumberFormatException e1 ) {
                                }
                            }
                            value = n;
                        } catch ( NumberFormatException e ) {
                            throw new ProcessletException( "Invalid datatype for parameter '" + litIn.getIdentifier()
                                                           + "': " + litValue + " is not a number value!" );
                        }
                    } else if ( "java.lang.Short".equals( parameterType ) ) {
                        try {
                            value = Short.parseShort( litValue );
                        } catch ( NumberFormatException e ) {
                            throw new ProcessletException( "Invalid datatype for parameter '" + litIn.getIdentifier()
                                                           + "': " + litValue + " is not a short value!" );
                        }
                    } else if ( "java.lang.Byte".equals( parameterType ) ) {
                        try {
                            value = Byte.parseByte( litValue );
                        } catch ( NumberFormatException e ) {
                            throw new ProcessletException( "Invalid datatype for parameter '" + litIn.getIdentifier()
                                                           + "': " + litValue + " is not a byte value!" );
                        }
                    }

                    params.put( parameter.getIdentifier().getCode(), value );
                    processedIds.add( litIn.getIdentifier() );
                }
            }
        }
        // nothing to prepare here
        return new Pair<InputStream, Boolean>( jrxml, false );
    }

}
