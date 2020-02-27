package org.deegree.services.wfs.format.csv;

import static org.deegree.protocol.wfs.getfeature.ResultType.RESULTS;

import org.deegree.protocol.wfs.describefeaturetype.DescribeFeatureType;
import org.deegree.protocol.wfs.getfeature.GetFeature;
import org.deegree.protocol.wfs.getfeature.ResultType;
import org.deegree.protocol.wfs.getgmlobject.GetGmlObject;
import org.deegree.protocol.wfs.getpropertyvalue.GetPropertyValue;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.wfs.WebFeatureService;
import org.deegree.services.wfs.format.Format;
import org.deegree.services.wfs.format.csv.request.CsvGetFeatureHandler;

/**
 * {@link Format} implementation for CSV.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class CsvFormat implements Format {

    private final CsvGetFeatureHandler csvGetFeatureHandler;

    /**
     * Instantiate {@link CsvFormat}
     *
     * @param webFeatureService
     *            the {@link WebFeatureService} using this format, never <code>null</code>
     */
    public CsvFormat( WebFeatureService webFeatureService ) {
        this.csvGetFeatureHandler = new CsvGetFeatureHandler( webFeatureService );
    }

    @Override
    public void destroy() {
        // nothing to do
    }

    @Override
    public void doGetFeature( GetFeature request, HttpResponseBuffer response )
                            throws Exception {
        ResultType type = request.getPresentationParams().getResultType();
        if ( type == RESULTS || type == null ) {
            this.csvGetFeatureHandler.doGetFeatureResults( request, response );
        } else {
            throw new UnsupportedOperationException( "GetFeature with RESULTTYPE=HITS for CSV is not supported" );
        }
    }

    @Override
    public void doDescribeFeatureType( DescribeFeatureType request, HttpResponseBuffer response, boolean isSoap )
                            throws Exception {
        throw new UnsupportedOperationException( "DescribeFeatureType for GeoJSON is not supported" );
    }

    @Override
    public void doGetGmlObject( GetGmlObject request, HttpResponseBuffer response )
                            throws Exception {
        throw new UnsupportedOperationException( "GetGmlObject for GeoJSON is not supported" );
    }

    @Override
    public void doGetPropertyValue( GetPropertyValue getPropertyValue, HttpResponseBuffer response )
                            throws Exception {
        throw new UnsupportedOperationException( "GetPropertyValue for GeoJSON is not supported" );
    }
}
