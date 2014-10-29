package org.deegree.geometry;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.standard.DefaultEnvelope;
import org.deegree.geometry.standard.primitive.DefaultPoint;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class GeometryTransformerTest {

    @Test
    public void transformEnvelopeKeepsMinAndMaxIn25833To4326 () throws UnknownCRSException, TransformationException {
        ICRS targetCrs = CRSManager.lookup("EPSG:4326");
        GeometryTransformer transformer = new GeometryTransformer(targetCrs);
        Envelope srcEnvelope = createEnvelopeInEpsg25833();
        Envelope targetEnvelope = transformer.transform(srcEnvelope);
        double minx = targetEnvelope.getMin().get0();
        double miny = targetEnvelope.getMin().get1();
        double maxx = targetEnvelope.getMax().get0();
        double maxy = targetEnvelope.getMax().get1();

        assertTrue(targetEnvelope.getCoordinateSystem().equals(targetCrs));
        assertTrue(minx < maxx);
        assertTrue(miny < maxy);
    }

    @Test
    public void transformEnvelopeKeepsMinAndMaxIn4326To25833 () throws UnknownCRSException, TransformationException {
        ICRS targetCrs = CRSManager.lookup("EPSG:25833");
        GeometryTransformer transformer = new GeometryTransformer(targetCrs);
        Envelope srcEnvelope = createEnvelopeInEpsg4326();
        Envelope targetEnvelope = transformer.transform(srcEnvelope);
        double minx = targetEnvelope.getMin().get0();
        double miny = targetEnvelope.getMin().get1();
        double maxx = targetEnvelope.getMax().get0();
        double maxy = targetEnvelope.getMax().get1();

        assertTrue(targetEnvelope.getCoordinateSystem().equals(targetCrs));
        assertTrue(minx < maxx);
        assertTrue(miny < maxy);
    }

    private Envelope createEnvelopeInEpsg25833( ) throws UnknownCRSException {
        ICRS crs = CRSManager.lookup("EPSG:25833");
        double minx = 372988.94024799997;
        double miny = 5723566.818151;
        double maxx = 382478.052521;
        double maxy = 5734058.460988999;
        DefaultPoint minPoint = new DefaultPoint( "minPoint", crs, null, new double[] { miny, minx } );
        DefaultPoint maxPoint = new DefaultPoint( "maxPoint", crs, null, new double[] { maxy, maxx } );
        return new DefaultEnvelope( "newEnvelope", crs, null, minPoint, maxPoint );
    }

    private Envelope createEnvelopeInEpsg4326( ) throws UnknownCRSException {
        ICRS crs = CRSManager.lookup("EPSG:4326");
        double minx = 51.64873620668787;
        double miny = 13.164151617571893;
        double maxx = 51.74509300270947;
        double maxy = 13.297706688558224;
        DefaultPoint minPoint = new DefaultPoint( "minPoint", crs, null, new double[] { miny, minx } );
        DefaultPoint maxPoint = new DefaultPoint( "maxPoint", crs, null, new double[] { maxy, maxx } );
        return new DefaultEnvelope( "newEnvelope", crs, null, minPoint, maxPoint );
    }

}