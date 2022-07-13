package org.deegree.commons.xml;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import javax.xml.transform.TransformerFactory;

import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class XsltUtilsTest {

    @Test
    public void verifyXslt_20_ImplementationIsAvailable() {
        TransformerFactory factory = TransformerFactory.newInstance();
        assertThat( factory, is( instanceOf( net.sf.saxon.TransformerFactoryImpl.class ) ) );
    }

    @Test
    public void testXslt10()
                            throws Exception {
        InputStream docToTransform = XsltUtilsTest.class.getResourceAsStream( "feature.gml" );
        URL xslt = XsltUtilsTest.class.getResource( "featureToHtml-Xslt10.xslt" );
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XsltUtils.transform( docToTransform, xslt, bos );
        bos.close();

        assertThat( bos.toString(), containsString( "Identifier: i1 Name: feature name 1 Props: 1a, 1b" ) );
        assertThat( bos.toString(), containsString( "Identifier: i2 Name: feature name 2 Props: 2a, 2b, 2c" ) );
    }

    @Test
    public void testXslt20()
                            throws Exception {
        InputStream docToTransform = XsltUtilsTest.class.getResourceAsStream( "feature.gml" );
        URL xslt = XsltUtilsTest.class.getResource( "featureToHtml-Xslt20.xslt" );
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XsltUtils.transform( docToTransform, xslt, bos );
        bos.close();

        assertThat( bos.toString(), containsString( "Identifier: i1 Name: feature name 1 Props: 1a, 1b" ) );
        assertThat( bos.toString(), containsString( "Identifier: i2 Name: feature name 2 Props: 2a, 2b, 2c" ) );
    }

}