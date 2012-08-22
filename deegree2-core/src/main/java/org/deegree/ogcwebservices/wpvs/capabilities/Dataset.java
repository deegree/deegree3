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

package org.deegree.ogcwebservices.wpvs.capabilities;

import java.util.ArrayList;
import java.util.List;

import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.metadata.iso19115.Keywords;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.ogcwebservices.wpvs.configuration.AbstractDataSource;

/**
 * This class represents a <code>Dataset</code> object.
 * Exactly one root dataset is mandatory. It may contain zero to any number of child datasets.
 *
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */
public class Dataset {

	// dataset attribs
	private boolean queryable;
	private boolean opaque;
	private boolean noSubset;
	private int fixedWidth;
	private int fixedHeight;

	// dataset elements
    private String name;
    private String title;
    private String abstract_;
    private Keywords[] keywords;
    private List<CoordinateSystem> crsList;
    private String[] mimeTypeFormat;
    private Dimension[] dimensions;
    private Identifier identifier;
    private DataProvider dataProvider;
    private Envelope wgs84BoundingBox;
    private Envelope[] boundingBoxes;
    private MetaData[] metadata;
    private DatasetReference[] datasetReferences;
    private double minimumScaleDenominator;
    private double maximumScaleDenominator;
    private FeatureListReference[] featureListReferences;
    private Style[] styles;
    private List<Dataset> datasets;
    private ElevationModel elevationModel;
    private AbstractDataSource[] dataSources;
	private Dataset parent;

    /**
     * Creates a new dataset object from the given parameters.
     *
     * @param queryable
     * @param opaque
     * @param noSubset
     * @param fixedWidth
     * @param fixedHeight
     * @param name
     * @param title
     * @param abstract_
     * @param keywords
     * @param crsList a list of available crs'
     * @param mimeTypeFormat
     * @param wgs84BoundingBox
     * @param boundingBoxes
     * @param dimensions
     * @param dataProvider
     * @param identifier
     * @param metadata
     * @param datasetReferences
     * @param featureListReferences
     * @param styles
     * @param minScaleDenominator
     * @param maxScaleDenominator
     * @param datasets
     * @param elevationModel
     * @param dataSources
     * @param parent
     */
    public Dataset( boolean queryable, boolean opaque, boolean noSubset, int fixedWidth,
    				int fixedHeight, String name, String title, String abstract_,
    				Keywords[] keywords, List<CoordinateSystem> crsList, String[] mimeTypeFormat,
    				Envelope wgs84BoundingBox, Envelope[] boundingBoxes, Dimension[] dimensions,
    				DataProvider dataProvider, Identifier identifier, MetaData[] metadata,
    				DatasetReference[] datasetReferences, FeatureListReference[] featureListReferences,
    				Style[] styles, double minScaleDenominator, double maxScaleDenominator,
    				Dataset[] datasets, ElevationModel elevationModel,
    				AbstractDataSource[] dataSources, Dataset parent ) {

        this.queryable = queryable;
		this.opaque = opaque;
		this.noSubset = noSubset;
		this.fixedWidth = fixedWidth;
		this.fixedHeight = fixedHeight;
        this.name = name;
        this.title = title;
        this.abstract_ = abstract_;
        this.keywords = keywords;
        this.crsList = crsList;
        this.mimeTypeFormat = mimeTypeFormat;
        this.wgs84BoundingBox = wgs84BoundingBox;
		this.boundingBoxes = boundingBoxes;
        this.dimensions = dimensions;
        this.dataProvider = dataProvider;
        this.identifier = identifier;
        this.metadata = metadata;
        this.datasetReferences = datasetReferences;
        this.featureListReferences = featureListReferences;
        this.styles = styles;
        this.minimumScaleDenominator = minScaleDenominator;
        this.maximumScaleDenominator = maxScaleDenominator;
        setDatasets(datasets);
		this.elevationModel = elevationModel;
        if( elevationModel != null ){
            this.elevationModel.setParentDataset( this );
        }

        this.dataSources = dataSources;
        this.parent = parent;

//        if ( dataSource instanceof LocalWCSDataSource ){
//        	 dataSource = (LocalWCSDataSource)dataSource;
//        } else {
//        	((LocalWFSDataSource)dataSource).getGeometryProperty();
//        }

    }

    @Override
    public String toString ( ){
        StringBuilder sb = new StringBuilder( 512 );
        sb.append( "name=").append( name );
        sb.append( "; title=").append( title );
        sb.append( "; queryable=" ).append( queryable );
        sb.append( "; opaque=").append( opaque );
        sb.append( "; noSubset=").append( noSubset );
        sb.append( "; fixedWidth=").append( fixedWidth );
        sb.append( "; fixedHeight=").append( fixedHeight );
        sb.append( "; crsList=").append( crsList);
        sb.append( "; wgs84BoundingBox=").append( wgs84BoundingBox );
        sb.append( "; identifier=").append( identifier );
        sb.append( "; minimumScaleDenominator=").append( minimumScaleDenominator );
        sb.append( "; maximumScaleDenominator=").append( maximumScaleDenominator );
        sb.append( "; elevationModel=").append( elevationModel);
        return sb.toString();
    }


    /**
     * Each dataset may contain zero to any number of child datasets.
     *
     * @param datasets  the datasets within this dataset.
     */
    public void setDatasets(Dataset[] datasets) {
        if( datasets == null ){
            return;
        }

    	if ( this.datasets == null ){
    		this.datasets = new ArrayList<Dataset>();
    	} else {
    	    this.datasets.clear();
        }

        for (int i = 0; i < datasets.length; i++) {
        	this.datasets.add( datasets[i] );
        }

    }

	/**
	 * @return Returns the abstract.
	 */
	public String getAbstract() {
		return abstract_;
	}

	/**
	 * @return Returns the boundingBoxes.
	 */
	public Envelope[] getBoundingBoxes() {
		return boundingBoxes;
	}

	/**
	 * @return Returns the crs.
	 */
	public CoordinateSystem[] getCrs() {
		return crsList.toArray( new CoordinateSystem[crsList.size()]);
	}

	/**
	 * @return Returns the dataProvider.
	 */
	public DataProvider getDataProvider() {
		return dataProvider;
	}

	/**
	 * @return all child datasets.
	 */
	public Dataset[] getDatasets() {
		return datasets.toArray(new Dataset[datasets.size()]);
	}

	/**
	 * @return Returns the datasetReferences.
	 */
	public DatasetReference[] getDatasetReferences() {
		return datasetReferences;
	}

	/**
	 * @return Returns the dataSources.
	 */
	public AbstractDataSource[] getDataSources() {
		return dataSources;
	}

	/**
	 * @return Returns the dimensions.
	 */
	public Dimension[] getDimensions() {
		return dimensions;
	}

	/**
	 * @return Returns the featureListReferences.
	 */
	public FeatureListReference[] getFeatureListReferences() {
		return featureListReferences;
	}

	/**
	 * @return Returns the identifier.
	 */
	public Identifier getIdentifier() {
		return identifier;
	}

	/**
	 * @return Returns the keywords.
	 */
	public Keywords[] getKeywords() {
		return keywords;
	}

	/**
	 * @return Returns the maximumScaleDenominator.
	 */
	public double getMaximumScaleDenominator() {
		return maximumScaleDenominator;
	}

	/**
	 * @return Returns the metadata.
	 */
	public MetaData[] getMetadata() {
		return metadata;
	}

	/**
	 * @return Returns the mimeTypeFormat.
	 */
	public String[] getMimeTypeFormat() {
		return mimeTypeFormat;
	}

	/**
	 * @return Returns the minimumScaleDenominator.
	 */
	public double getMinimumScaleDenominator() {
		return minimumScaleDenominator;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Returns the styles.
	 */
	public Style[] getStyles() {
		return styles;
	}

	/**
	 * @return Returns the title.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return Returns the wgs84BoundingBox.
	 */
	public Envelope getWgs84BoundingBox() {
		return wgs84BoundingBox;
	}

	/**
	 * @return Returns the fixedHeight.
	 */
	public int getFixedHeight() {
		return fixedHeight;
	}

	/**
	 * @return Returns the fixedWidth.
	 */
	public int getFixedWidth() {
		return fixedWidth;
	}

	/**
	 * @return Returns the noSubset.
	 */
	public boolean getNoSubset() {
		return noSubset;
	}

	/**
	 * @return Returns the opaque.
	 */
	public boolean getOpaque() {
		return opaque;
	}

	/**
	 * @return Returns the queryable.
	 */
	public boolean getQueryable() {
		return queryable;
	}

	/**
	 * @return Returns the elevationModel.
	 */
	public ElevationModel getElevationModel() {
		return elevationModel;
	}

	/**
	 * Returns the parent dataset of this dataset. If the method returns
     * <code>null</code> the current dataset is the root dataset.
	 *
	 * @return Returns the parent.
	 */
	public Dataset getParent() {
		return parent;
	}


    /**
     * @param elevationModel An other elevationModel.
     */
    public void setElevationModel( ElevationModel elevationModel ) {
        this.elevationModel = elevationModel;
    }



}
