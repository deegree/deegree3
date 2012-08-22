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
package org.deegree.ogcwebservices.wms.capabilities;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.deegree.graphics.sld.UserStyle;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.ogcwebservices.getcapabilities.MetadataURL;
import org.deegree.ogcwebservices.wms.configuration.AbstractDataSource;

/**
 * Each available map is advertised by a &lt;Layer&gt; element in the Capabilities XML. A single parent Layer encloses
 * any number of additional layers, which may be hierarchically nested as desired. Some properties defined in a parent
 * layer are inherited by the children it encloses. These inherited properties may be either redefined or added to by
 * the child.
 * <p>
 * A Map Server shall include at least one &lt;Layer&gt; element for each map layer offered. If desired, layers may be
 * repeated in different categories when relevant. No controlled vocabulary has been defined, so at present Layer and
 * Style Names, Titles and Keywords are arbitrary.
 * </p>
 * The &lt;Layer&gt; element can enclose child elements providing metadata about the Layer.
 * 
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp </a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * @version 2002-03-01
 */
public class Layer {

    private List<AuthorityURL> authorityURL;

    private List<Envelope> boundingBox;

    private List<AbstractDataSource> dataSource;

    private List<DataURL> dataURL;

    private List<Dimension> dimension;

    private List<Extent> extent;

    private List<FeatureListURL> featureListURL;

    private List<Identifier> identifier;

    private List<String> keywordList;

    private List<Layer> layer;

    private List<MetadataURL> metadataURL;

    private List<String> srs;

    private Attribution attribution;

    private Envelope latLonBoundingBox;

    private HashMap<String, Style> styles;

    private Style[] stylesArray;

    private Layer parent;

    private ScaleHint scaleHint;

    private String abstract_;

    private String name;

    private String title;

    private boolean noSubsets = false;

    private boolean opaque = false;

    private boolean queryable = false;

    private int cascaded = -1;

    private int fixedHeight = -1;

    private int fixedWidth = -1;

    /**
     * default constructor
     */
    private Layer() {
        keywordList = new ArrayList<String>( 20 );
        srs = new ArrayList<String>( 20 );
        boundingBox = new ArrayList<Envelope>();
        dimension = new ArrayList<Dimension>();
        extent = new ArrayList<Extent>();
        authorityURL = new ArrayList<AuthorityURL>();
        identifier = new ArrayList<Identifier>();
        metadataURL = new ArrayList<MetadataURL>();
        dataURL = new ArrayList<DataURL>();
        featureListURL = new ArrayList<FeatureListURL>();
        styles = new HashMap<String, Style>();
        layer = new ArrayList<Layer>( 50 );
        dataSource = new ArrayList<AbstractDataSource>();
    }

    /**
     * constructor initializing the class with the &lt;Layer&gt;
     * 
     * @param queryable
     * @param cascaded
     * @param opaque
     * @param noSubsets
     * @param fixedWidth
     * @param fixedHeight
     * @param name
     * @param title
     * @param abstract_
     * @param latLonBoundingBox
     * @param attribution
     * @param scaleHint
     * @param keywordList
     * @param srs
     * @param boundingBoxes
     * @param dimensions
     * @param extents
     * @param authorityURLs
     * @param identifiers
     * @param metadataURLs
     * @param dataURLs
     * @param featureListURLs
     * @param styles
     * @param layers
     * @param dataSource
     * @param parent
     */
    public Layer( boolean queryable, int cascaded, boolean opaque, boolean noSubsets, int fixedWidth, int fixedHeight,
                  String name, String title, String abstract_, Envelope latLonBoundingBox, Attribution attribution,
                  ScaleHint scaleHint, String[] keywordList, String[] srs, LayerBoundingBox[] boundingBoxes,
                  Dimension[] dimensions, Extent[] extents, AuthorityURL[] authorityURLs, Identifier[] identifiers,
                  MetadataURL[] metadataURLs, DataURL[] dataURLs, FeatureListURL[] featureListURLs, Style[] styles,
                  Layer[] layers, AbstractDataSource[] dataSource, Layer parent ) {
        this();
        this.queryable = queryable;
        this.cascaded = cascaded;
        this.opaque = opaque;
        this.noSubsets = noSubsets;
        this.fixedWidth = fixedWidth;
        this.fixedHeight = fixedHeight;
        setName( name );
        setTitle( title );
        setAbstract( abstract_ );
        setLatLonBoundingBox( latLonBoundingBox );
        setAttribution( attribution );
        setScaleHint( scaleHint );
        setKeywordList( keywordList );
        setSrs( srs );
        setBoundingBox( boundingBoxes );
        setDimension( dimensions );
        setExtent( extents );
        setAuthorityURL( authorityURLs );
        setIdentifier( identifiers );
        setMetadataURL( metadataURLs );
        setDataURL( dataURLs );
        setFeatureListURL( featureListURLs );
        setStyles( styles );
        setLayer( layers );
        setDataSource( dataSource );
        setParent( parent );
    }

    /**
     * If, and only if, a layer has a &lt;Name&gt;, then it is a map layer that can be requested by using that Name in
     * the LAYERS parameter of a GetMap request. If the layer has a Title but no Name, then that layer is only a
     * category title for all the layers nested within. A Map Server that advertises a Layer containing a Name element
     * shall be able to accept that Name as the value of LAYERS argument in a GetMap request and return the
     * corresponding map. A Client shall not attempt to request a layer that has a Title but no Name.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * sets the name of the layer
     * 
     * @param name
     */
    public void setName( String name ) {
        this.name = name;
    }

    /**
     * A &lt;Title&gt; is required for all layers; it is a human-readable string for presentation in a menu. The Title
     * is not inherited by child Layers.
     * 
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * sets the title for the layer
     * 
     * @param title
     */
    public void setTitle( String title ) {
        this.title = title;
    }

    /**
     * Abstract is a narrative description of the map layer. The Abstract elements are not inherited by child Layers.
     * 
     * @return the abstract
     */
    public String getAbstract() {
        return abstract_;
    }

    /**
     * sets the a narrative description of the map layer
     * 
     * @param abstract_
     */
    public void setAbstract( String abstract_ ) {
        this.abstract_ = abstract_;
    }

    /**
     * KeywordList contains zero or more Keywords to aid in catalog searches. The KeywordList elements are not inherited
     * by child Layers.
     * 
     * @return the keywords
     */
    public String[] getKeywordList() {
        return keywordList.toArray( new String[keywordList.size()] );
    }

    /**
     * adds the keywordList
     * 
     * @param keyword
     */
    public void addKeyword( String keyword ) {
        this.keywordList.add( keyword );
    }

    /**
     * sets the keywordList
     * 
     * @param keywordList
     */
    public void setKeywordList( String[] keywordList ) {
        if ( keywordList == null )
            this.keywordList.clear();
        else
            this.keywordList = Arrays.asList( keywordList );
    }

    /**
     * Every Layer is available in one or more spatial reference systems Every Layer shall have at least one &gt;SRS&gt;
     * element that is either stated explicitly or inherited from a parent Layer . The root &lt;Layer&gt; element shall
     * include a sequence of zero or more SRS elements listing all SRSes that are common to all subsidiary layers. Use a
     * single SRS element with empty content (like so: "&lt;SRS&gt;&lt;/SRS&gt; ") if there is no common SRS. Layers may
     * optionally add to the global SRS list, or to the list inherited from a parent layer. Any duplication shall be
     * ignored by clients. When a Layer is available in several Spatial Reference Systems, there are two ways to encode
     * the list of SRS values. The first of these is new in this version of the specification, the second is deprecated
     * but still included for backwards compatibility.
     * <p>
     * 1. Optional, recommended: Multiple single-valued &lt;SRS&gt; elements: a list of SRS values is represented as a
     * sequence of &lt;SRS&gt; elements, each of which contains only a single SRS name. Example:
     * &lt;SRS&gt;EPSG:1234&lt;/SRS&gt; &lt;SRS&gt;EPSG:5678&lt;/SRS&gt;.
     * </p>
     * 2. Deprecated: Single list-valued &lt;SRS&gt; element: a list of SRS values is represented asa
     * whitespace-separated list of SRS names contained within a single &lt;SRS&gt; element. Example:
     * &lt;SRS&gt;EPSG:1234 EPSG:5678&lt;/SRS&gt;.
     * 
     * @return the srs
     */
    public String[] getSrs() {
        String[] pSrs = null;

        if ( parent != null ) {
            pSrs = parent.getSrs();
        } else {
            pSrs = new String[0];
        }

        List<String> list = new ArrayList<String>( srs.size() + pSrs.length );
        list.addAll( srs );
        for ( int i = 0; i < pSrs.length; i++ ) {
            if ( !list.contains( pSrs[i] ) ) {
                list.add( pSrs[i] );
            }
        }

        return list.toArray( new String[list.size()] );
    }

    /**
     * @param srs
     * @return s true if the submitted srs (name) is supported by the layer
     */
    public boolean isSrsSupported( String srs ) {
        String[] sr = getSrs();
        for ( int i = 0; i < sr.length; i++ ) {
            if ( sr[i].equals( srs ) ) {
                return true;
            }
        }
        return false;
    }

    /**
     * adds the spatial reference system (srs)
     * 
     * @param srs
     */
    public void addSrs( String srs ) {
        this.srs.add( srs );
    }

    /**
     * sets the srs
     * 
     * @param srs
     */
    public void setSrs( String[] srs ) {
        if ( srs == null )
            this.srs.clear();
        else
            this.srs = Arrays.asList( srs );
    }

    /**
     * Every Layer shall have exactly one &lt;LatLonBoundingBox&gt; element that is either stated explicitly or
     * inherited from a parent Layer. LatLonBoundingBox states the minimum bounding rectangle of the map data in the
     * EPSG:4326 geographic coordinate system. The LatLonBoundingBox attributes minx, miny, maxx, maxy indicate the
     * edges of an enclosing rectangle in decimal degrees. LatLonBoundingBox shall be supplied regardless of what SRS
     * the map server may support, but it may be approximate if EPSG:4326 is not supported. Its purpose is to facilitate
     * geographic searches without requiring coordinate transformations by the search engine.
     * 
     * @return the bbox
     */
    public Envelope getLatLonBoundingBox() {
        if ( ( latLonBoundingBox == null ) && ( parent != null ) ) {
            return parent.getLatLonBoundingBox();
        }
        return latLonBoundingBox;
    }

    /**
     * sets the LatLonBoundingBox element that is either stated explicitly or inherited from a parent Layer.
     * 
     * @param latLonBoundingBox
     */
    public void setLatLonBoundingBox( Envelope latLonBoundingBox ) {
        this.latLonBoundingBox = latLonBoundingBox;
    }

    /**
     * Layers may have zero or more &lt;BoundingBox&gt; elements that are either stated explicitly or inherited from a
     * parent Layer. Each BoundingBox states the bounding rectangle of the map data in a particular spatial reference
     * system; the attribute SRS indicates which SRS applies. If the data area is shaped irregularly then the
     * BoundingBox gives the minimum enclosing rectangle. The attributes minx, miny, maxx, maxy indicate the edges of
     * the bounding box in units of the specified SRS. Optional resx and resy attributes indicate the spatial resolution
     * of the data in those same units.
     * <p>
     * A Layer may have multiple BoundingBox element, but each one shall state a different SRS. A Layer inherits any
     * BoundingBox values defined by its parents. A BoundingBox inherited from the parent Layer for a particular SRS is
     * replaced by any declaration for the same SRS in the child Layer. A BoundingBox in the child for a new SRS not
     * already declared by the parent is added to the list of bounding boxes for the child Layer. A single Layer element
     * shall not contain more than one BoundingBox for the same SRS.
     * </p>
     * 
     * @return bounding boxes
     */
    public LayerBoundingBox[] getBoundingBoxes() {
        HashMap<String, LayerBoundingBox> list = new HashMap<String, LayerBoundingBox>( 100 );

        if ( parent != null ) {
            LayerBoundingBox[] plb = parent.getBoundingBoxes();

            for ( int i = 0; i < plb.length; i++ ) {
                list.put( plb[i].getSRS(), plb[i] );
            }
        }

        for ( int i = 0; i < boundingBox.size(); i++ ) {
            LayerBoundingBox lb = (LayerBoundingBox) boundingBox.get( i );
            list.put( lb.getSRS(), lb );
        }

        LayerBoundingBox[] lbs = new LayerBoundingBox[list.size()];
        return list.values().toArray( lbs );
    }

    /**
     * adds the &lt;BoundingBox&gt;
     * 
     * @param boundingBox
     */
    public void addBoundingBox( Envelope boundingBox ) {
        this.boundingBox.add( boundingBox );
    }

    /**
     * sets the boundingBox
     * 
     * @param boundingBox
     */
    public void setBoundingBox( LayerBoundingBox[] boundingBox ) {
        this.boundingBox.clear();

        if ( boundingBox != null ) {
            for ( int i = 0; i < boundingBox.length; i++ ) {
                this.boundingBox.add( boundingBox[i] );
            }
        }
    }

    /**
     * Dimension declarations are inherited from parent Layers. Any new Dimension declarations in the child are added to
     * the list inherited from the parent. A child shall not redefine a Dimension with the same name attribute as one
     * that was inherited.
     * 
     * @return the dimensions
     */
    public Dimension[] getDimension() {
        HashMap<String, Dimension> list = new HashMap<String, Dimension>();

        if ( parent != null ) {
            Dimension[] pDim = parent.getDimension();

            for ( int i = 0; i < pDim.length; i++ ) {
                list.put( pDim[i].getName(), pDim[i] );
            }
        }

        for ( int i = 0; i < dimension.size(); i++ ) {
            Dimension dim = dimension.get( i );

            if ( list.get( dim.getName() ) == null ) {
                list.put( dim.getName(), dim );
            }
        }

        return list.values().toArray( new Dimension[list.size()] );
    }

    /**
     * adds the dimension
     * 
     * @param dimension
     */
    public void addDimension( Dimension dimension ) {
        this.dimension.add( dimension );
    }

    /**
     * sets the dimension
     * 
     * @param dimension
     */
    public void setDimension( Dimension[] dimension ) {
        if ( dimension == null )
            this.dimension.clear();
        else
            this.dimension = Arrays.asList( dimension );
    }

    /**
     * Extent declarations are inherited from parent Layers. Any Extent declarations in the child with the same name
     * attribute as one inherited from the parent replaces the value declared by the parent. A Layer shall not declare
     * an Extent unless a Dimension with the same name has been declared or inherited earlier in the Capabilities XML.
     * 
     * @return the extents
     */
    public Extent[] getExtent() {
        HashMap<String, Extent> list = new HashMap<String, Extent>();

        if ( parent != null ) {
            Extent[] pEx = parent.getExtent();

            for ( int i = 0; i < pEx.length; i++ ) {
                list.put( pEx[i].getName(), pEx[i] );
            }
        }

        for ( int i = 0; i < extent.size(); i++ ) {
            Extent ex = extent.get( i );
            list.put( ex.getName(), ex );
        }

        return list.values().toArray( new Extent[list.size()] );
    }

    /**
     * adds the extent declarations
     * 
     * @param extent
     */
    public void addExtent( Extent extent ) {
        this.extent.add( extent );
    }

    /**
     * sets the extent
     * 
     * @param extent
     */
    public void setExtent( Extent[] extent ) {
        if ( extent == null )
            this.extent.clear();
        else
            this.extent = Arrays.asList( extent );
    }

    /**
     * The optional &lt;Attribution&gt; element provides a way to identify the source of the map data used in a Layer or
     * collection of Layers. Attribution encloses several optional elements: <OnlineResource>states the data provider's
     * URL; &lt;Title&gt; is a human-readable string naming the data provider; &lt;LogoURL&gt; is the URL of a logo
     * image. Client applications may choose to display one or more of these items. A &lt;Format&gt; element in LogoURL
     * indicates the MIME type of the logo image, and the attributes width and height state the size of the image in
     * pixels.
     * 
     * @return the attribution
     */
    public Attribution getAttribution() {
        if ( ( parent != null ) && ( attribution == null ) ) {
            return parent.getAttribution();
        }
        return attribution;
    }

    /**
     * sets the optional &lt;Attribution&gt; element
     * 
     * @param attribution
     */
    public void setAttribution( Attribution attribution ) {
        this.attribution = attribution;
    }

    /**
     * The authority attribute of the Identifier element corresponds to the name attribute of a separate
     * &lt;AuthorityURL&gt; element. AuthorityURL encloses an &lt;OnlineResource&gt; element which states the URL of a
     * document defining the meaning of the Identifier values.
     * 
     * @return the authority url object
     */
    public AuthorityURL[] getAuthorityURL() {
        HashMap<String, AuthorityURL> list = new HashMap<String, AuthorityURL>();

        if ( parent != null ) {
            AuthorityURL[] pAu = parent.getAuthorityURL();

            for ( int i = 0; i < pAu.length; i++ ) {
                list.put( pAu[i].getName(), pAu[i] );
            }
        }

        for ( int i = 0; i < authorityURL.size(); i++ ) {
            AuthorityURL au = authorityURL.get( i );

            if ( list.get( au.getName() ) == null ) {
                list.put( au.getName(), au );
            }
        }

        AuthorityURL[] aus = new AuthorityURL[list.size()];
        return list.values().toArray( aus );
    }

    /**
     * adds the authority attribute of the Identifier element
     * 
     * @param authorityURL
     */
    public void addAuthorityURL( AuthorityURL authorityURL ) {
        this.authorityURL.add( authorityURL );
    }

    /**
     * sets the authority attribute of the Identifier element
     * 
     * @param authorityURL
     */
    public void setAuthorityURL( AuthorityURL[] authorityURL ) {
        if ( authorityURL == null )
            this.authorityURL.clear();
        else
            this.authorityURL = Arrays.asList( authorityURL );
    }

    /**
     * A Map Server may use zero or more &lt;Identifier&gt; elements to list ID numbers or labels defined by a
     * particular Authority. The text content of the Identifier element is the ID value.
     * 
     * @return the identifiers
     */
    public Identifier[] getIdentifier() {
        HashMap<String, Identifier> list = new HashMap<String, Identifier>();

        if ( parent != null ) {
            Identifier[] pIden = parent.getIdentifier();

            for ( int i = 0; i < pIden.length; i++ ) {
                list.put( pIden[i].getAuthority(), pIden[i] );
            }
        }

        for ( int i = 0; i < identifier.size(); i++ ) {
            Identifier iden = identifier.get( i );

            if ( list.get( iden.getAuthority() ) == null ) {
                list.put( iden.getAuthority(), iden );
            }
        }

        Identifier[] ids = new Identifier[list.size()];
        return list.values().toArray( ids );
    }

    /**
     * adds the &lt;Identifier&gt;
     * 
     * @param identifier
     */
    public void addIdentifier( Identifier identifier ) {
        this.identifier.add( identifier );
    }

    /**
     * sets the &lt;Identifier&gt;
     * 
     * @param identifier
     */
    public void setIdentifier( Identifier[] identifier ) {
        if ( identifier == null )
            this.identifier.clear();
        else
            this.identifier = Arrays.asList( identifier );
    }

    /**
     * A Map Server should use one or more &lt;MetadataURL&gt; elements to offer detailed, standardized metadata about
     * the data underneath a particular layer. The type attribute indicates the standard to which the metadata complies.
     * Two types are defined at present: the value 'TC211' refers to [ISO 19115]; the value 'FGDC' refers to
     * [FGDC-STD-001-1988]. The MetadataURL element shall not be used to reference metadata in a non-standardized
     * metadata format; see DataURL instead. The enclosed &lt;Format&gt; element indicates the file format MIME type of
     * the metadata record.
     * 
     * @return the metadata urls
     */
    public MetadataURL[] getMetadataURL() {
        return metadataURL.toArray( new MetadataURL[metadataURL.size()] );
    }

    /**
     * adds the metadataURL
     * 
     * @param metadataURL
     */
    public void addMetadataURL( MetadataURL metadataURL ) {
        this.metadataURL.add( metadataURL );
    }

    /**
     * sets the metadataURL
     * 
     * @param metadataURL
     */
    public void setMetadataURL( MetadataURL[] metadataURL ) {
        if ( metadataURL == null )
            this.metadataURL.clear();
        else
            this.metadataURL = Arrays.asList( metadataURL );
    }

    /**
     * A Map Server may use DataURL to offer more information about the data represented by a particular layer. While
     * the semantics are not well-defined, as long as the results of an HTTP GET request against the DataURL are
     * properly MIME-typed, Viewer Clients and Cascading Map Servers can make use of this. Use 6lt;MetadataURL&gt;
     * instead for a precisely defined reference to standardized metadata records.
     * 
     * @return the data URLs
     */
    public DataURL[] getDataURL() {
        return dataURL.toArray( new DataURL[dataURL.size()] );
    }

    /**
     * adds the dataURL
     * 
     * @param dataURL
     */
    public void addDataURL( DataURL dataURL ) {
        this.dataURL.add( dataURL );
    }

    /**
     * sets the dataURL
     * 
     * @param dataURL
     */
    public void setDataURL( DataURL[] dataURL ) {
        if ( dataURL == null )
            this.dataURL.clear();
        else
            this.dataURL = Arrays.asList( dataURL );
    }

    /**
     * A Map Server may use a &lt;FeatureListURL&gt; element to point to a list of the features represented in a Layer.
     * 
     * @return the feature list urls
     */
    public FeatureListURL[] getFeatureListURL() {
        return featureListURL.toArray( new FeatureListURL[featureListURL.size()] );
    }

    /**
     * adds the &lt;FeatureListURL&gt;
     * 
     * @param featureListURL
     */
    public void addFeatureListURL( FeatureListURL featureListURL ) {
        this.featureListURL.add( featureListURL );
    }

    /**
     * sets the &lt;FeatureListURL&gt;
     * 
     * @param featureListURL
     */
    public void setFeatureListURL( FeatureListURL[] featureListURL ) {
        if ( featureListURL == null )
            this.featureListURL.clear();
        else
            this.featureListURL = Arrays.asList( featureListURL );
    }

    /**
     * @return a list of style that can be used for rendering the layer.
     */
    public Style[] getStyles() {
        LinkedList<Style> list = new LinkedList<Style>();

        // styles are inherited here
        // probably that's not what SLD/SE want, but let's keep it for backwards compatibility
        if ( parent != null ) {
            list.addAll( asList( parent.getStyles() ) );
        }

        // overwrite the inherited styles with the ones defined here
        for ( Style style : stylesArray ) {
            ListIterator<Style> iter = list.listIterator();
            while ( iter.hasNext() ) {
                if ( iter.next().getName().equals( style.getName() ) ) {
                    iter.remove();
                }
            }
        }
        list.addAll( asList( stylesArray ) );

        return list.toArray( new Style[list.size()] );
    }

    /**
     * adds a list of style that can be used form rendering the layer.
     * 
     * @param style
     */
    public void addStyles( Style style ) {
        this.styles.put( style.getName(), style );
    }

    /**
     * sets a list of style that can be used form rendering the layer.
     * 
     * @param styles
     */
    public void setStyles( Style[] styles ) {
        stylesArray = styles;
        if ( styles == null ) {
            this.styles.clear();
        } else {
            for ( Style style : styles ) {
                this.styles.put( style.getName(), style );
            }
        }
    }

    /**
     * returns the <tt>UserStyle</tt> (SLD) representation of the style identified by the submitted name.
     * 
     * @param name
     *            of the requested style
     * @return SLD - UserStyle
     * 
     */
    public UserStyle getStyle( String name ) {

        Style style = styles.get( name );
        UserStyle us = null;

        if ( style == null ) {
            if ( parent != null ) {
                us = parent.getStyle( name );
            }
        } else {
            us = style.getStyleContent();
        }

        return us;
    }

    /**
     * returns the <tt>Style</tt> identified by the submitted name.
     * 
     * @param name
     *            of the requested style
     * @return Style
     * 
     */
    public Style getStyleResource( String name ) {

        Style style = styles.get( name );

        if ( style == null && name.length() == 0 ) {
            String tmpName = "default";
            style = styles.get( tmpName );
            if ( style == null && name.length() == 0 ) {
                tmpName = "default:" + this.name;
                style = styles.get( tmpName );
            }
        } else if ( style == null && "default".equals( name ) ) {
            String tmpName = "default:" + this.name;
            style = styles.get( tmpName );
        }

        if ( style == null ) {
            if ( parent != null ) {
                style = parent.getStyleResource( name );
            }
        }

        return style;
    }

    /**
     * Layers may include a &lt;ScaleHint&gt; element that suggests minimum and maximum scales for which it is
     * appropriate to display this layer. Because WMS output is destined for output devices of arbitrary size and
     * resolution, the usual definition of scale as the ratio of map size to real-world size is not appropriate here.
     * The following definition of Scale Hint is recommended. Consider a hypothetical map with a given Bounding Box,
     * width and height. The central pixel of that map (or the pixel just to the northwest of center) will have some
     * size, which can be expressed as the ground distance in meters of the southwest to northeast diagonal of that
     * pixel. The two values in ScaleHint are the minimum and maximum recommended values of that diagonal. It is
     * recognized that this definition is not geodetically precise, but at the same time the hope is that by including
     * it conventions will develop that can be later specified more clearly.
     * 
     * @return the scale hint
     */
    public ScaleHint getScaleHint() {
        if ( ( parent != null ) && ( scaleHint == null ) ) {
            return parent.getScaleHint();
        }
        return scaleHint;
    }

    /**
     * sets the <ScaleHint>
     * 
     * @param scaleHint
     */
    public void setScaleHint( ScaleHint scaleHint ) {
        this.scaleHint = scaleHint;
    }

    /**
     * returns a list of layers the are enclosed by this layer.
     * 
     * @return the layers
     */
    public Layer[] getLayer() {
        return layer.toArray( new Layer[layer.size()] );
    }

    /**
     * removes a Layer identified by its name from the parent Layer. A reference to the removed layer will be returned.
     * If no Layer matching the passed name can be found nothing happens and <tt>null</tt> will be returned.
     * 
     * @param name
     * 
     * @return removerd Layer
     */
    public Layer removeLayer( String name ) {
        for ( int i = 0; i < layer.size(); i++ ) {
            Layer ly = layer.get( i );
            if ( ly.getName() != null ) {
                if ( ly.getName().equals( name ) ) {
                    layer.remove( i );
                    return ly;
                }
            }
        }
        return null;
    }

    /**
     * removes a Layer identified by its title from the parent Layer. A reference to the removed layer will be returned.
     * If no Layer matching the passed title can be found nothing happens and <tt>null</tt> will be returned.
     * 
     * @param title
     * 
     * @return removerd Layer
     */
    public Layer removeLayerByTitle( String title ) {
        for ( int i = 0; i < layer.size(); i++ ) {
            Layer ly = layer.get( i );
            if ( ly.getTitle().equals( title ) ) {
                layer.remove( i );
                return ly;
            }
        }
        return null;
    }

    /**
     * adds a list of layers the are enclosed by this layer.
     * 
     * @param layer
     */
    public void addLayer( Layer layer ) {
        this.layer.add( layer );
    }

    /**
     * sets a list of layers the are enclosed by this layer.
     * 
     * @param layer
     */
    public void setLayer( Layer[] layer ) {
        if ( layer == null ) {
            this.layer.clear();
        } else {
            this.layer = new ArrayList<Layer>( Arrays.asList( layer ) );
        }
    }

    /**
     * source where the WMS can find the data of a layer.
     * 
     * @return the data sources
     */
    public AbstractDataSource[] getDataSource() {
        return dataSource.toArray( new AbstractDataSource[dataSource.size()] );
    }

    /**
     * source where the WMS can find the data of a layer.
     * 
     * @param dataSource
     */
    public void setDataSource( AbstractDataSource[] dataSource ) {
        if ( dataSource == null )
            this.dataSource.clear();
        else
            this.dataSource = Arrays.asList( dataSource );
    }

    /**
     * source where the WMS can find the data of a layer.
     * 
     * @param dataSource
     */
    public void addDataSource( AbstractDataSource dataSource ) {
        this.dataSource.add( dataSource );
    }

    /**
     * @return the parent layer of this layer. If the method returns <tt>null</tt> the current layer is the root layer.
     *         In addition with the <tt>getLayer</tt> method this enables a program to traverse the layer tree in both
     *         directions.
     */
    public Layer getParent() {
        return parent;
    }

    /**
     * sets the parent layer of this layer.
     * 
     * @param parent
     */
    public void setParent( Layer parent ) {
        this.parent = parent;
    }

    /**
     * @return '0' if the layer is provided directly form the deegree WMS. other it returns the number of cascaded WMS
     *         servers the is passed through
     * 
     */
    public int getCascaded() {
        return cascaded;
    }

    /**
     * @return '0' if the WMS can resize map to arbitrary height. nonzero: map has a fixed height that cannot be changed
     *         by the WMS.
     * 
     */
    public int getFixedHeight() {
        return fixedHeight;
    }

    /**
     * @return '0' if the WMS can resize map to arbitrary width. nonzero: map has a fixed width that cannot be changed
     *         by the WMS.
     * 
     */
    public int getFixedWidth() {
        return fixedWidth;
    }

    /**
     * @return false if the WMS can map a subset of the full bounding box.
     * 
     */
    public boolean hasNoSubsets() {
        return noSubsets;
    }

    /**
     * @return false if map data represents vector features that probably do not completely fill space.
     * 
     */
    public boolean isOpaque() {
        return opaque;
    }

    /**
     * @return true if the layer is queryable. That means it can be targeted by a GetFeatureInfo request.
     * 
     */
    public boolean isQueryable() {
        return queryable;
    }

}
