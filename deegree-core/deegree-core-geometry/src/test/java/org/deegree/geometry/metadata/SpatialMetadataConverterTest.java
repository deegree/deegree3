package org.deegree.geometry.metadata;

import org.deegree.geometry.Envelope;
import org.deegree.geometry.metadata.jaxb.EnvelopeType;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class SpatialMetadataConverterTest {

    @Test
    public void testConvertEnvelopeFromJaxb() throws JAXBException {
        EnvelopeType env = createEnvelopeType();
        Envelope envelope = SpatialMetadataConverter.fromJaxb(env);
        assertThat(envelope.getMin().get(0), is(8.3));
        assertThat(envelope.getMin().get(1), is(53.2));
        assertThat(envelope.getMax().get(0), is(10.4));
        assertThat(envelope.getMax().get(1), is(54.0));

    }

    private EnvelopeType createEnvelopeType() throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(EnvelopeType.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        InputStream resourceAsStream = getClass().getResourceAsStream("envelope.xml");
        return unmarshaller.unmarshal(new StreamSource(resourceAsStream), EnvelopeType.class).getValue();
    }

}
