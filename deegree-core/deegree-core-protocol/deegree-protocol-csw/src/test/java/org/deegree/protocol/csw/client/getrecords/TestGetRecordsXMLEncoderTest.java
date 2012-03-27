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
package org.deegree.protocol.csw.client.getrecords;

import static junit.framework.Assert.assertTrue;
import static org.deegree.commons.xml.CommonNamespaces.APISO;
import static org.deegree.commons.xml.CommonNamespaces.APISO_PREFIX;
import static org.deegree.filter.MatchAction.ANY;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.filter.Expression;
import org.deegree.filter.Filter;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.ValueReference;
import org.deegree.protocol.csw.CSWConstants.ResultType;
import org.deegree.protocol.csw.CSWConstants.ReturnableElement;
import org.junit.Assume;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * Validates the GetRecords requests.
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class TestGetRecordsXMLEncoderTest {

    @Test
    public void testExportMin()
                            throws XMLStreamException, FactoryConfigurationError, UnknownCRSException,
                            TransformationException, IOException, SAXException {
        GetRecords getRecords = new GetRecords( new Version( 2, 0, 2 ), ResultType.results, ReturnableElement.full,
                                                null );
        validateGetRecordsRequest( getRecords );
        assertTrue( true );
    }

    @Test
    public void testExportMax()
                            throws XMLStreamException, FactoryConfigurationError, UnknownCRSException,
                            TransformationException, IOException, SAXException {
        GetRecords getRecords = new GetRecords(
                                                new Version( 2, 0, 2 ),
                                                10,
                                                15,
                                                "application/xml",
                                                "http://www.isotc211.org/2005/gmd",
                                                Collections.singletonList( new QName(
                                                                                      CommonNamespaces.ISOAP10GMDNS,
                                                                                      "MD_Metadata",
                                                                                      CommonNamespaces.ISOAP10GMD_PREFIX ) ),
                                                ResultType.results, ReturnableElement.full, null );
        validateGetRecordsRequest( getRecords );
        assertTrue( true );
    }

    @Test
    public void testExportFilter()
                            throws XMLStreamException, FactoryConfigurationError, UnknownCRSException,
                            TransformationException, IOException, SAXException {
        Expression param1 = new ValueReference( new QName( APISO, "Identifier", APISO_PREFIX ) );
        Expression param2 = new Literal<PrimitiveValue>( "3528635identifer18745" );
        Operator rootOperator = new PropertyIsEqualTo( param1, param2, true, ANY );
        Filter filter = new OperatorFilter( rootOperator );
        
        GetRecords getRecords = new GetRecords(
                                                new Version( 2, 0, 2 ),
                                                10,
                                                15,
                                                "application/xml",
                                                "http://www.isotc211.org/2005/gmd",
                                                Collections.singletonList( new QName(
                                                                                      CommonNamespaces.ISOAP10GMDNS,
                                                                                      "MD_Metadata",
                                                                                      CommonNamespaces.ISOAP10GMD_PREFIX ) ),
                                                ResultType.results, ReturnableElement.full, filter );
        validateGetRecordsRequest( getRecords );
        assertTrue( true );
    }
    private void validateGetRecordsRequest( GetRecords getRecords )
                            throws XMLStreamException, FactoryConfigurationError, UnknownCRSException,
                            TransformationException, IOException, SAXException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter( os );

        GetRecordsXMLEncoder.export( getRecords, writer );

        writer.close();
        os.close();

        InputStream getRecordsRequest = new ByteArrayInputStream( os.toByteArray() );
        SchemaFactory factory = SchemaFactory.newInstance( "http://www.w3.org/2001/XMLSchema" );
        Validator validator = null;
        Source source = null;
        try {
            URL schemaLocation = new URL( "http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd" );
            Schema schema = factory.newSchema( schemaLocation );
            validator = schema.newValidator();
            source = new StreamSource( getRecordsRequest );
        } catch ( Exception e ) {
            Assume.assumeNoException( e );
            return;
        }
        validator.validate( source );
    }

}
