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
package org.deegree.ogcwebservices.csw;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.filterencoding.capabilities.FilterCapabilities;
import org.deegree.ogcwebservices.csw.capabilities.CatalogueCapabilities;
import org.deegree.ogcwebservices.csw.capabilities.CatalogueCapabilitiesDocument;
import org.deegree.ogcwebservices.csw.capabilities.CatalogueCapabilitiesDocument_2_0_2;
import org.deegree.ogcwebservices.csw.configuration.CatalogueOutputSchemaParameter;
import org.deegree.ogcwebservices.csw.configuration.CatalogueOutputSchemaValue;
import org.deegree.ogcwebservices.csw.configuration.CatalogueTypeNameSchemaParameter;
import org.deegree.ogcwebservices.csw.configuration.CatalogueTypeNameSchemaValue;
import org.deegree.ogcwebservices.getcapabilities.Contents;
import org.deegree.ogcwebservices.getcapabilities.DCPType;
import org.deegree.ogcwebservices.getcapabilities.Operation;
import org.deegree.ogcwebservices.getcapabilities.OperationsMetadata;
import org.deegree.ogcwebservices.getcapabilities.ServiceIdentification;
import org.deegree.ogcwebservices.getcapabilities.ServiceProvider;
import org.deegree.owscommon.OWSCommonCapabilitiesDocument;
import org.deegree.owscommon.OWSDomainType;
import org.w3c.dom.Element;

/**
 * factory class for creating OGC CSW 2.0.2 compliant XML representation of a CatalogueCapabilities instance
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class XMLFactory_2_0_2 extends XMLFactory_2_0_0 {

    /**
     * Exports a <code>CatalogCapabilities</code> instance as an <code>XmlDocument</code>.
     * 
     * @param capabilities
     * @param sections
     *            names of sections to be exported, may contain 'All'
     * @return DOM representation of the <code>CatalogCapabilities</code>
     * @throws IOException
     *             if XML template could not be loaded
     */
    public static CatalogueCapabilitiesDocument export( CatalogueCapabilities capabilities, String[] sections )
                            throws IOException {

        // no sections specified? => return all sections
        if ( sections == null || sections.length == 0 ) {
            sections = new String[] { OWSCommonCapabilitiesDocument.ALL_NAME };
        }

        // build HashSet with the names of the sections to be exported
        Set<String> sectionSet = new HashSet<String>();
        for ( int i = 0; i < sections.length; i++ ) {
            sectionSet.add( sections[i] );
        }

        CatalogueCapabilitiesDocument_2_0_2 doc = new CatalogueCapabilitiesDocument_2_0_2();
        try {
            doc.createEmptyDocument();
            Element root = doc.getRootElement();

            // 'ServiceIdentification'-section
            if ( sectionSet.contains( OWSCommonCapabilitiesDocument.ALL_NAME )
                 || sectionSet.contains( OWSCommonCapabilitiesDocument.SERVICE_IDENTIFICATION_NAME ) ) {
                ServiceIdentification serviceIdentification = capabilities.getServiceIdentification();
                if ( serviceIdentification != null ) {
                    appendServiceIdentification( root, serviceIdentification );
                }
            }

            // 'ServiceProvider'-section
            if ( sectionSet.contains( OWSCommonCapabilitiesDocument.ALL_NAME )
                 || sectionSet.contains( OWSCommonCapabilitiesDocument.SERVICE_PROVIDER_NAME ) ) {
                ServiceProvider serviceProvider = capabilities.getServiceProvider();
                if ( serviceProvider != null ) {
                    appendServiceProvider( root, capabilities.getServiceProvider() );
                }
            }

            // 'OperationsMetadata'-section
            if ( sectionSet.contains( OWSCommonCapabilitiesDocument.ALL_NAME )
                 || sectionSet.contains( OWSCommonCapabilitiesDocument.OPERATIONS_METADATA_NAME ) ) {
                OperationsMetadata operationsMetadata = capabilities.getOperationsMetadata();
                if ( operationsMetadata != null ) {
                    appendOperationsMetadata( root, operationsMetadata, true );
                }
            }

            // 'Contents'-section
            if ( sectionSet.contains( OWSCommonCapabilitiesDocument.ALL_NAME )
                 || sectionSet.contains( OWSCommonCapabilitiesDocument.CONTENTS_NAME ) ) {
                Contents contents = capabilities.getContents();
                if ( contents != null ) {
                    // appendContents(root, contents);
                }
            }

            // 'Filter_Capabilities'-section
            if ( sectionSet.contains( OWSCommonCapabilitiesDocument.ALL_NAME )
                 || sectionSet.contains( CatalogueCapabilitiesDocument.FILTER_CAPABILITIES_NAME )
                 || sectionSet.contains( "Filter_Capabilities" ) ) {
                FilterCapabilities filterCapabilities = capabilities.getFilterCapabilities();
                if ( filterCapabilities != null ) {
                    org.deegree.model.filterencoding.XMLFactory.appendFilterCapabilities110( root, filterCapabilities );
                }
            }

        } catch ( Exception e ) {
            e.printStackTrace();
            LOG.logError( e.getMessage(), e );
        }
        return doc;
    }

    /**
     * Appends the DOM representation of the <code>OperationsMetadata</code>- section to the passed <code>Element</code>
     * .
     * 
     * @param root
     */
    protected static void appendOperationsMetadata( Element root, OperationsMetadata operationsMetadata,
                                                    boolean capabilities ) {

        // 'ows:OperationsMetadata'-element
        Element operationsMetadataNode = XMLTools.appendElement( root, OWSNS, "ows:OperationsMetadata" );

        // append all Operations
        Operation[] operations = operationsMetadata.getOperations();
        for ( int i = 0; i < operations.length; i++ ) {
            Operation operation = operations[i];

            // 'ows:Operation'-element
            Element operationElement = XMLTools.appendElement( operationsMetadataNode, OWSNS, "ows:Operation" );

            operationElement.setAttribute( "name", operation.getName() );

            // 'ows:DCP'-elements
            DCPType[] dcps = operation.getDCPs();
            for ( int j = 0; j < dcps.length; j++ ) {
                appendDCP( operationElement, dcps[j] );
            }

            // 'ows:Parameter'-elements
            OWSDomainType[] parameters = operation.getParameters();
            for ( int j = 0; j < parameters.length; j++ ) {
                if ( parameters[j] instanceof CatalogueOutputSchemaParameter ) {
                    appendParameter( operationElement, (CatalogueOutputSchemaParameter) parameters[j], "ows:Parameter",
                                     capabilities );
                } else if ( parameters[j] instanceof CatalogueTypeNameSchemaParameter ) {
                    appendParameter( operationElement, (CatalogueTypeNameSchemaParameter) parameters[j],
                                     "ows:Parameter", capabilities );
                } else {
                    appendParameter( operationElement, parameters[j], "ows:Parameter" );
                }
            }

            // 'ows:Metadata'-elements
            Object[] metadata = operation.getMetadata();
            if ( metadata != null ) {
                for ( int j = 0; j < metadata.length; j++ ) {
                    appendMetadata( operationElement, metadata[j] );
                }
            }
        }

        // append general parameters
        OWSDomainType[] parameters = operationsMetadata.getParameter();
        for ( int i = 0; i < parameters.length; i++ ) {
            appendParameter( operationsMetadataNode, parameters[i], "ows:Parameter" );
        }

        // append constraints
        OWSDomainType[] constraints = operationsMetadata.getConstraints();
        for ( int i = 0; i < constraints.length; i++ ) {
            appendParameter( operationsMetadataNode, constraints[i], "ows:Constraint" );
        }
    }

    /**
     * Appends the DOM representation of a <code>OWSDomainType</code> instance to the passed <code>Element</code>.
     * 
     * @param root
     * @param parameter
     */
    protected static void appendParameter( Element root, CatalogueOutputSchemaParameter parameter, String elementName,
                                           boolean capabilities ) {

        // 'ows:Parameter'-element
        Element parameterNode = XMLTools.appendElement( root, OWSNS, elementName );
        parameterNode.setAttribute( "name", parameter.getName() );

        // 'ows:Value'-elements
        CatalogueOutputSchemaValue[] values = parameter.getSpecializedValues();
        for ( int i = 0; i < values.length; i++ ) {
            Element elem = XMLTools.appendElement( parameterNode, OWSNS, "ows:Value", values[i].getValue() );
            if ( !capabilities ) {
                elem.setAttribute( "deegree:input", values[i].getInXsl() );
                elem.setAttribute( "deegree:ouput", values[i].getOutXsl() );
            }
        }
    }

    /**
     * Appends the DOM representation of a <code>OWSDomainType</code> instance to the passed <code>Element</code>.
     * 
     * @param root
     * @param parameter
     */
    protected static void appendParameter( Element root, CatalogueTypeNameSchemaParameter parameter,
                                           String elementName, boolean capabilities ) {

        // 'ows:Parameter'-element
        Element parameterNode = XMLTools.appendElement( root, OWSNS, elementName );
        parameterNode.setAttribute( "name", parameter.getName() );

        // 'ows:Value'-elements
        CatalogueTypeNameSchemaValue[] values = parameter.getSpecializedValues();
        for ( int i = 0; i < values.length; i++ ) {
            Element elem = XMLTools.appendElement( parameterNode, OWSNS, "ows:Value", values[i].getValue() );
            if ( !capabilities ) {
                elem.setAttribute( "deegree:schema", values[i].getSchema() );
            }
        }
    }

    /**
     * Appends the DOM representation of the <code>ServiceIdentification</code>- section to the passed
     * <code>Element</code>.
     * 
     * @param root
     * @param serviceIdentification
     * @throws XMLParsingException
     */
    protected static void appendServiceIdentification( Element root, ServiceIdentification serviceIdentification ) {

        // 'ServiceIdentification'-element
        Element serviceIdentificationNode = XMLTools.appendElement( root, OWSNS, "ows:ServiceIdentification" );

        // 'Title'-element
        XMLTools.appendElement( serviceIdentificationNode, OWSNS, "ows:Title", serviceIdentification.getTitle() );

        // 'Abstract'-element
        if ( serviceIdentification.getAbstract() != null ) {
            XMLTools.appendElement( serviceIdentificationNode, OWSNS, "ows:Abstract",
                                    serviceIdentification.getAbstract() );
        }

        // 'Keywords'-element
        appendOWSKeywords( serviceIdentificationNode, serviceIdentification.getKeywords() );

        // 'ServiceType'-element
        XMLTools.appendElement( serviceIdentificationNode, OWSNS, "ows:ServiceType",
                                serviceIdentification.getServiceType().getCode() );

        // 'ServiceTypeVersion'-elements
        String[] versions = serviceIdentification.getServiceTypeVersions();
        for ( int i = 0; i < versions.length; i++ ) {
            XMLTools.appendElement( serviceIdentificationNode, OWSNS, "ows:ServiceTypeVersion", versions[i] );
        }

        // 'Fees'-element
        XMLTools.appendElement( serviceIdentificationNode, OWSNS, "ows:Fees", serviceIdentification.getFees() );

        // 'AccessConstraints'-element
        String[] constraints = serviceIdentification.getAccessConstraints();
        if ( constraints != null ) {
            for ( int i = 0; i < constraints.length; i++ ) {
                XMLTools.appendElement( serviceIdentificationNode, OWSNS, "ows:AccessConstraints", constraints[i] );
            }
        }
    }

}
