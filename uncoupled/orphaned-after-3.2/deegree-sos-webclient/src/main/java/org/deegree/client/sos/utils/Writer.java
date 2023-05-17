package org.deegree.client.sos.utils;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.deegree.client.sos.storage.StorageDescribeSensor;
import org.deegree.client.sos.storage.StorageGetObservation;
import org.deegree.client.sos.storage.components.DataArray;
import org.deegree.client.sos.storage.components.Field;
import org.deegree.client.sos.storage.components.OWSException;
import org.deegree.client.sos.storage.components.Observation;
import org.deegree.commons.utils.Pair;

/**
 * Helper class that writes HTML from the contents from either a StorageDescribeSensor or a StorageGetObservation object
 * into a PrintWriter. This will be called after an AJAX request.
 * 
 * @author <a href="mailto:neumeister@lat-lon.de">Ulrich Neumeister</a>
 * 
 */
public class Writer {

    private PrintWriter printWriter;

    public Writer( PrintWriter pw, StorageDescribeSensor sensorStorage ) {
        printWriter = pw;
        writeSensorData( sensorStorage );
    }

    public Writer( PrintWriter pw, StorageGetObservation observationStorage ) {
        printWriter = pw;
        writeObservationData( observationStorage );
    }

    public Writer( PrintWriter pw, OWSException exception ) {
        printWriter = pw;
        writeException( exception );
    }

    private void writeSensorData( StorageDescribeSensor storage ) {
        printWriter.write( "<table border='1' cellspacing='0' cellpadding='3'><tr><th colspan='2'>SensorML</th></tr>" );
        if ( storage.getCapabilities().size() > 0 ) {
            printWriter.write( "<tr><td colspan='2'><center><br />Capabilities</center></td></tr>" );
            for ( OMElement element : storage.getCapabilities() ) {
                if ( !element.getText().trim().equals( "" ) ) {
                    printWriter.write( "<tr><td>" + element.getLocalName() + "</td>" );
                    printWriter.write( "<td>" + element.getText() + "</td></tr>" );
                }
                Iterator<OMAttribute> attributes;
                for ( attributes = element.getAllAttributes(); attributes.hasNext(); ) {
                    OMAttribute attribute = attributes.next();
                    printWriter.write( "<tr><td>" + element.getLocalName() + ": " + attribute.getLocalName() + "</td>" );
                    printWriter.write( "<td>" + attribute.getAttributeValue() + "</td></tr>" );
                }
            }
        }
        if ( storage.getCharacteristics().size() > 0 ) {
            printWriter.write( "<tr><td colspan='2'><center><br />sml:characteristics</center></td></tr>" );
            for ( OMElement element : storage.getCharacteristics() ) {
                if ( !element.getText().trim().equals( "" ) ) {
                    printWriter.write( "<tr><td>" + element.getLocalName() + "</td>" );
                    printWriter.write( "<td>" + element.getText() + "</td></tr>" );
                }
                Iterator<OMAttribute> attributes;
                for ( attributes = element.getAllAttributes(); attributes.hasNext(); ) {
                    OMAttribute attribute = attributes.next();
                    printWriter.write( "<tr><td>" + element.getLocalName() + ": " + attribute.getLocalName() + "</td>" );
                    printWriter.write( "<td>" + attribute.getAttributeValue() + "</td></tr>" );
                }
            }
        }
        if ( storage.getClassification().size() > 0 ) {
            printWriter.write( "<tr><td colspan='2'><center><br />sml:classification</center></td></tr>" );
            for ( OMElement element : storage.getClassification() ) {
                if ( !element.getText().trim().equals( "" ) ) {
                    printWriter.write( "<tr><td>" + element.getLocalName() + "</td>" );
                    printWriter.write( "<td>" + element.getText() + "</td></tr>" );
                }
                Iterator<OMAttribute> attributes;
                for ( attributes = element.getAllAttributes(); attributes.hasNext(); ) {
                    OMAttribute attribute = attributes.next();
                    printWriter.write( "<tr><td>" + element.getLocalName() + ": " + attribute.getLocalName() + "</td>" );
                    printWriter.write( "<td>" + attribute.getAttributeValue() + "</td></tr>" );
                }
            }
        }
        if ( storage.getContact().size() > 0 ) {
            printWriter.write( "<tr><td colspan='2'><center><br />sml:contact</center></td></tr>" );
            for ( OMElement element : storage.getContact() ) {
                if ( !element.getText().trim().equals( "" ) ) {
                    printWriter.write( "<tr><td>" + element.getLocalName() + "</td>" );
                    printWriter.write( "<td>" + element.getText() + "</td></tr>" );
                }
                Iterator<OMAttribute> attributes;
                for ( attributes = element.getAllAttributes(); attributes.hasNext(); ) {
                    OMAttribute attribute = attributes.next();
                    printWriter.write( "<tr><td>" + element.getLocalName() + ": " + attribute.getLocalName() + "</td>" );
                    printWriter.write( "<td>" + attribute.getAttributeValue() + "</td></tr>" );
                }
            }
        }
        if ( storage.getDocumentation().size() > 0 ) {
            printWriter.write( "<tr><td colspan='2'><center><br />sml:documentation</center></td></tr>" );
            for ( OMElement element : storage.getDocumentation() ) {
                if ( !element.getText().trim().equals( "" ) ) {
                    printWriter.write( "<tr><td>" + element.getLocalName() + "</td>" );
                    printWriter.write( "<td>" + element.getText() + "</td></tr>" );
                }
                Iterator<OMAttribute> attributes;
                for ( attributes = element.getAllAttributes(); attributes.hasNext(); ) {
                    OMAttribute attribute = attributes.next();
                    printWriter.write( "<tr><td>" + element.getLocalName() + ": " + attribute.getLocalName() + "</td>" );
                    printWriter.write( "<td>" + attribute.getAttributeValue() + "</td></tr>" );
                }
            }
        }
        if ( storage.getHistory().size() > 0 ) {
            printWriter.write( "<tr><td colspan='2'><center><br />sml:history</center></td></tr>" );
            for ( OMElement element : storage.getHistory() ) {
                if ( !element.getText().trim().equals( "" ) ) {
                    printWriter.write( "<tr><td>" + element.getLocalName() + "</td>" );
                    printWriter.write( "<td>" + element.getText() + "</td></tr>" );
                }
                Iterator<OMAttribute> attributes;
                for ( attributes = element.getAllAttributes(); attributes.hasNext(); ) {
                    OMAttribute attribute = attributes.next();
                    printWriter.write( "<tr><td>" + element.getLocalName() + ": " + attribute.getLocalName() + "</td>" );
                    printWriter.write( "<td>" + attribute.getAttributeValue() + "</td></tr>" );
                }
            }
        }
        if ( storage.getIdentification().size() > 0 ) {
            printWriter.write( "<tr><td colspan='2'><center><br />sml:identification</center></td></tr>" );
            for ( OMElement element : storage.getIdentification() ) {
                if ( !element.getText().trim().equals( "" ) ) {
                    printWriter.write( "<tr><td>" + element.getLocalName() + "</td>" );
                    printWriter.write( "<td>" + element.getText() + "</td></tr>" );
                }
                Iterator<OMAttribute> attributes;
                for ( attributes = element.getAllAttributes(); attributes.hasNext(); ) {
                    OMAttribute attribute = attributes.next();
                    printWriter.write( "<tr><td>" + element.getLocalName() + ": " + attribute.getLocalName() + "</td>" );
                    printWriter.write( "<td>" + attribute.getAttributeValue() + "</td></tr>" );
                }
            }
        }
        if ( storage.getKeywords().size() > 0 ) {
            printWriter.write( "<tr><td colspan='2'><center><br />sml:keywords</center></td></tr>" );
            for ( OMElement element : storage.getKeywords() ) {
                if ( !element.getText().trim().equals( "" ) ) {
                    printWriter.write( "<tr><td>" + element.getLocalName() + "</td>" );
                    printWriter.write( "<td>" + element.getText() + "</td></tr>" );
                }
                Iterator<OMAttribute> attributes;
                for ( attributes = element.getAllAttributes(); attributes.hasNext(); ) {
                    OMAttribute attribute = attributes.next();
                    printWriter.write( "<tr><td>" + element.getLocalName() + ": " + attribute.getLocalName() + "</td>" );
                    printWriter.write( "<td>" + attribute.getAttributeValue() + "</td></tr>" );
                }
            }
        }
        if ( storage.getLegalConstraint().size() > 0 ) {
            printWriter.write( "<tr><td colspan='2'><center><br />sml:legalConstraint</center></td></tr>" );
            for ( OMElement element : storage.getLegalConstraint() ) {
                if ( !element.getText().trim().equals( "" ) ) {
                    printWriter.write( "<tr><td>" + element.getLocalName() + "</td>" );
                    printWriter.write( "<td>" + element.getText() + "</td></tr>" );
                }
                Iterator<OMAttribute> attributes;
                for ( attributes = element.getAllAttributes(); attributes.hasNext(); ) {
                    OMAttribute attribute = attributes.next();
                    printWriter.write( "<tr><td>" + element.getLocalName() + ": " + attribute.getLocalName() + "</td>" );
                    printWriter.write( "<td>" + attribute.getAttributeValue() + "</td></tr>" );
                }
            }
        }
        if ( storage.getMember().size() > 0 ) {
            printWriter.write( "<tr><td colspan='2'><center><br />sml:member</center></td></tr>" );
            for ( OMElement element : storage.getMember() ) {
                if ( !element.getText().trim().equals( "" ) ) {
                    printWriter.write( "<tr><td>" + element.getLocalName() + "</td>" );
                    printWriter.write( "<td>" + element.getText() + "</td></tr>" );
                }
                Iterator<OMAttribute> attributes;
                for ( attributes = element.getAllAttributes(); attributes.hasNext(); ) {
                    OMAttribute attribute = attributes.next();
                    printWriter.write( "<tr><td>" + element.getLocalName() + ": " + attribute.getLocalName() + "</td>" );
                    printWriter.write( "<td>" + attribute.getAttributeValue() + "</td></tr>" );
                }
            }
        }
        if ( storage.getSecurityConstraint().size() > 0 ) {
            printWriter.write( "<tr><td colspan='2'><center><br />sml:securityConstraint</center></td></tr>" );
            for ( OMElement element : storage.getSecurityConstraint() ) {
                if ( !element.getText().trim().equals( "" ) ) {
                    printWriter.write( "<tr><td>" + element.getLocalName() + "</td>" );
                    printWriter.write( "<td>" + element.getText() + "</td></tr>" );
                }
                Iterator<OMAttribute> attributes;
                for ( attributes = element.getAllAttributes(); attributes.hasNext(); ) {
                    OMAttribute attribute = attributes.next();
                    printWriter.write( "<tr><td>" + element.getLocalName() + ": " + attribute.getLocalName() + "</td>" );
                    printWriter.write( "<td>" + attribute.getAttributeValue() + "</td></tr>" );
                }
            }
        }
        if ( storage.getValidTime().size() > 0 ) {
            printWriter.write( "<tr><td colspan='2'><center><br />sml:validTime</center></td></tr>" );
            for ( OMElement element : storage.getValidTime() ) {
                if ( !element.getText().trim().equals( "" ) ) {
                    printWriter.write( "<tr><td>" + element.getLocalName() + "</td>" );
                    printWriter.write( "<td>" + element.getText() + "</td></tr>" );
                }
                Iterator<OMAttribute> attributes;
                for ( attributes = element.getAllAttributes(); attributes.hasNext(); ) {
                    OMAttribute attribute = attributes.next();
                    printWriter.write( "<tr><td>" + element.getLocalName() + ": " + attribute.getLocalName() + "</td>" );
                    printWriter.write( "<td>" + attribute.getAttributeValue() + "</td></tr>" );
                }
            }
        }
        printWriter.write( "</table>" );
    }

    private void writeObservationData( StorageGetObservation storage ) {
        List<Observation> observationCollection = storage.getObservationCollection();
        for ( Observation observation : observationCollection ) {
            String procedure = observation.getProcedure();
            DataArray dataArray = observation.getDataArray();
            if ( dataArray != null ) {
                String values = dataArray.getValues();
                String tokenSeparator = "";
                String blockSeparator = "";
                for ( Pair<String, String> separator : dataArray.getSeparators() ) {
                    if ( separator.first.equals( "tokenSeparator" ) ) {
                        tokenSeparator = separator.second;
                    } else if ( separator.first.equals( "blockSeparator" ) ) {
                        blockSeparator = separator.second;
                    }
                }
                String[] blocks = values.split( blockSeparator );
                printWriter.write( "<br />" + procedure
                                   + ":<br /><br /><table border='1' cellspacing='0' cellpadding='3'>" );
                for ( int i = 0; i < blocks.length; i++ ) {
                    String[] value = blocks[i].split( tokenSeparator );
                    printWriter.write( "<tr>" );
                    if ( i == 0 ) {
                        for ( Field field : dataArray.getElementTypes() ) {
                            printWriter.write( "<th>" + field.getName() + "</th>" );
                        }
                    } else {
                        for ( int j = 0; j < value.length; j++ ) {
                            printWriter.write( "<td>" + value[j] + "</td>" );
                        }
                    }
                    printWriter.write( "</tr>" );
                }
            }
            printWriter.write( "</table>" );
            printWriter.write( "<br /><br /><button onclick='generateChart(\"" + procedure
                               + "\")'>generate Chart</button>" );
            printWriter.write( "<br /><br /><br /><br />" );
        }
    }

    public void writeException( OWSException exception ) {
        printWriter.write( "Sorry, but an exception report has been sent from the server:<br>" );
        printWriter.write( "<br><table border='1' cellspacing='0' cellpadding='3'" );
        printWriter.write( "<tr><th>" + exception.getExceptionCode() + ": " + exception.getLocator() + "</th></tr>" );
        printWriter.write( "<tr><td>" + exception.getExceptionText() + "</td></tr>" );
        printWriter.write( "</table>" );
    }

}
