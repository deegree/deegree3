package org.deegree.layer.persistence.tile;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.filter.OperatorFilter;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.geometry.precision.PrecisionModel;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.standard.DefaultEnvelope;
import org.deegree.layer.LayerQuery;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.rendering.r2d.context.MapOptionsMaps;
import org.deegree.style.StyleRef;
import org.deegree.tile.DefaultTileDataSet;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileDataSet;
import org.deegree.tile.TileMatrix;
import org.deegree.tile.TileMatrixSet;
import org.deegree.workspace.ResourceMetadata;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TileLayerTest {

    private static List<TileDataSet> tileDataSets;

    @BeforeClass
    public static void setupBeforeClass()
                            throws Exception {
        tileDataSets = createTileDataSetsWithEpsg4326();
    }

    @Test
    public void testMapQueryWithEpsg4326ShouldNotCallRetrieveTilesInSourceCrs()
                            throws Exception {
        LayerQuery query = createLayerQuery( "EPSG:4326" );
        TileLayer tileLayer = spy( new TileLayer( mock( LayerMetadata.class ), tileDataSets ) );
        TileLayerData tileLayerData = tileLayer.mapQuery( query, mock( List.class ) );

        assertThat( tileLayerData, is( notNullValue() ) );
        verify( tileLayer, never() ).retrieveTilesInSourceCrs( any( LayerQuery.class ), any( Envelope.class ),
                                                               any( ICRS.class ) );
    }

    @Test
    public void testMapQueryWithEpsg25833ShouldCallRetrieveTilesInSourceCrs()
                            throws Exception {
        LayerQuery query = createLayerQuery( "EPSG:25833" );
        TileLayer tileLayer = spy( new TileLayer( mock( LayerMetadata.class ), tileDataSets ) );
        TileLayerData tileLayerData = tileLayer.mapQuery( query, mock( List.class ) );

        assertThat( tileLayerData, is( notNullValue() ) );
        verify( tileLayer, times( 1 ) ).retrieveTilesInSourceCrs( any( LayerQuery.class ), any( Envelope.class ),
                                                                  any( ICRS.class ) );
    }

    private static List<TileDataSet> createTileDataSetsWithEpsg4326()
                            throws Exception {
        List<ICRS> crsList = singletonList( CRSManager.lookup( "EPSG:4326" ) );
        SpatialMetadata spmd = new SpatialMetadata( mock( Envelope.class ), crsList );
        TileMatrixSet tms = new TileMatrixSet( "identifier", "wknScaleSet", mock( List.class ), spmd,
                                               mock( ResourceMetadata.class ) );
        TileDataLevel tdl = mock( TileDataLevel.class );
        doReturn( mock( TileMatrix.class ) ).when( tdl ).getMetadata();
        List<TileDataLevel> tdlList = singletonList( tdl );
        TileDataSet tds = spy( new DefaultTileDataSet( tdlList, tms, "format" ) );
        doReturn( mock( Iterator.class ) ).when( tds ).getTiles( any( Envelope.class ), anyDouble() );
        return singletonList( tds );
    }

    private LayerQuery createLayerQuery( String crs )
                            throws Exception {
        Envelope envelope = new DefaultEnvelope( "id", CRSManager.lookup( crs ), mock( PrecisionModel.class ),
                                                 mock( Point.class ), mock( Point.class ) );
        return new LayerQuery( envelope, 0, 0, mock( StyleRef.class ), mock( OperatorFilter.class ), mock( Map.class ),
                               mock( Map.class ), 0.0, mock( MapOptionsMaps.class ), envelope );
    }

}