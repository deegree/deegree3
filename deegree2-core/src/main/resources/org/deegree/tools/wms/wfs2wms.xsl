<?xml version="1.0" encoding="UTF-8"?>
<!--  ======================================================================================

This file is part of deegree.
Copyright (C) 2001-2008 by:
EXSE, Department of Geography, University of Bonn
http://www.giub.uni-bonn.de/deegree/
lat/lon GmbH
http://www.lat-lon.de

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

Contact:

Andreas Poth
lat/lon GmbH
Aennchenstr. 19
53177 Bonn
Germany
E-Mail: poth@lat-lon.de

Prof. Dr. Klaus Greve
Department of Geography
University of Bonn
Meckenheimer Allee 166
53115 Bonn
Germany
E-Mail: greve@giub.uni-bonn.de

@version $Revision: 9346 $
@author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
@author last edited by: $Author: apoth $

@version 1.0. $Revision: 9346 $, $Date: 2007-12-27 17:39:07 +0100 (Do, 27 Dez 2007) $
                 
====================================================================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:deegree="http://www.deegree.org/wms" xmlns:wfs="http://www.opengis.net/wfs" xmlns:ows="http://www.opengis.net/ows" xmlns:java="java" xmlns:wfs2wms="org.deegree.tools.wms.WFS2WMS">
	<xsl:param name="PARENTLAYER" select="Parent"/>
	<xsl:param name="SRS" select="EPSG_4326"/>
	<xsl:param name="MINX" select="-180"/>
	<xsl:param name="MINY" select="-90"/>
	<xsl:param name="MAXX" select="180"/>
	<xsl:param name="MAXY" select="90"/>
	<xsl:param name="WMSCAPS" select="0"/>
	<xsl:param name="WFSCAPS" select="0"/>
	<xsl:param name="STYLEDOC" select="0"/>
	<xsl:template match="wfs:WFS_Capabilities">
		<xsl:choose>
			<xsl:when test="$WMSCAPS = '1' ">
				<xsl:call-template name="WMSCAPS"/>
			</xsl:when>
			<xsl:otherwise>
				<Layer queryable="0" noSubsets="0" fixedWidth="0" fixedHeight="0">
					<Title>
						<xsl:value-of select="$PARENTLAYER"/>
					</Title>
					<SRS>
						<xsl:value-of select="$SRS"/>
					</SRS>
					<LatLonBoundingBox>
						<xsl:attribute name="minx"><xsl:value-of select="$MINX"/></xsl:attribute>
						<xsl:attribute name="miny"><xsl:value-of select="$MINY"/></xsl:attribute>
						<xsl:attribute name="maxx"><xsl:value-of select="$MAXX"/></xsl:attribute>
						<xsl:attribute name="maxy"><xsl:value-of select="$MAXY"/></xsl:attribute>
					</LatLonBoundingBox>
					<xsl:for-each select="wfs:FeatureTypeList">
						<xsl:apply-templates select="wfs:FeatureType"/>
					</xsl:for-each>
				</Layer>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="wfs:FeatureType">
		<Layer queryable="1" noSubsets="0" fixedWidth="0" fixedHeight="0">
			<Name>
				<xsl:value-of select="wfs:Name"/>
			</Name>
			<Title>
				<xsl:choose>
					<xsl:when test="wfs:Title != '' ">
						<xsl:value-of select="wfs:Title"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="wfs:Name"/>
					</xsl:otherwise>
				</xsl:choose>
			</Title>
			<SRS>
				<xsl:value-of select="wfs:DefaultSRS"/>
			</SRS>
			<LatLonBoundingBox>
				<xsl:attribute name="minx"><xsl:value-of select="wfs2wms:getMinX( ows:WGS84BoundingBox/ows:LowerCorner )"/></xsl:attribute>
				<xsl:attribute name="miny"><xsl:value-of select="wfs2wms:getMinY( ows:WGS84BoundingBox/ows:LowerCorner )"/></xsl:attribute>
				<xsl:attribute name="maxx"><xsl:value-of select="wfs2wms:getMinX( ows:WGS84BoundingBox/ows:UpperCorner )"/></xsl:attribute>
				<xsl:attribute name="maxy"><xsl:value-of select="wfs2wms:getMinY( ows:WGS84BoundingBox/ows:UpperCorner )"/></xsl:attribute>
			</LatLonBoundingBox>
			<deegree:DataSource failOnException="0" queryable="1">
				<deegree:Name>
					<xsl:value-of select="wfs:Name"/>
				</deegree:Name>
				<deegree:Type>LOCALWFS</deegree:Type>
				<deegree:GeometryProperty>app:geom</deegree:GeometryProperty>
				<deegree:OWSCapabilities>
					<deegree:OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" type="simple">
						<xsl:attribute name="xlink:href"><xsl:value-of select="$WFSCAPS"/></xsl:attribute>
					</deegree:OnlineResource>
				</deegree:OWSCapabilities>
			</deegree:DataSource>
			<Style>
				<Name><xsl:value-of select="concat( 'default:', wfs:Name)"/></Name>
				<Title>default</Title>
				<deegree:StyleResource>
					<xsl:value-of select="$STYLEDOC"/>
				</deegree:StyleResource>
			</Style>
		</Layer>
	</xsl:template>
	<xsl:template name="WMSCAPS">
		<WMT_MS_Capabilities xmlns:deegree="http://www.deegree.org/wms" xmlns:gml="http://www.opengis.net/gml" version="1.1.1" updateSequence="1.1.0">
			<deegree:DeegreeParam>
				<deegree:DefaultOnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple" xlink:href="http://127.0.0.1:8082/mrh-wms2/services"/>
				<deegree:CacheSize>100</deegree:CacheSize>
				<deegree:MaxLifeTime>3600</deegree:MaxLifeTime>
				<deegree:RequestTimeLimit>35</deegree:RequestTimeLimit>
				<deegree:MapQuality>1.00</deegree:MapQuality>
				<deegree:MaxMapWidth>5000</deegree:MaxMapWidth>
				<deegree:MaxMapHeight>5000</deegree:MaxMapHeight>
				<deegree:AntiAliased>true</deegree:AntiAliased>
				<deegree:DTDLocation>
					<deegree:OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple" xlink:href="http://schemas.opengis.net/wms/1.1.1/WMS_MS_Capabilities.dtd"/>
				</deegree:DTDLocation>
			</deegree:DeegreeParam>
			<Service>
				<Name>deegree wms</Name>
				<Title>deegree wms</Title>
				<Abstract>wms reference implementation</Abstract>
				<KeywordList>
					<Keyword>deegree</Keyword>
					<Keyword>wms</Keyword>
				</KeywordList>
				<OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple" xlink:href="http://127.0.0.1:8082/mrh-wms2"/>
				<ContactInformation>
					<ContactPersonPrimary>
						<ContactPerson>Andreas Poth</ContactPerson>
						<ContactOrganization>lat/lon</ContactOrganization>
					</ContactPersonPrimary>
					<ContactPosition>Technical Director</ContactPosition>
					<ContactAddress>
						<AddressType>XXXX</AddressType>
						<Address>Aennchenstr. 19</Address>
						<City>Bonn</City>
						<StateOrProvince>NRW</StateOrProvince>
						<PostCode>53177</PostCode>
						<Country>Germany</Country>
					</ContactAddress>
					<ContactVoiceTelephone>0049228184960</ContactVoiceTelephone>
					<ContactFacsimileTelephone>00492281849629</ContactFacsimileTelephone>
					<ContactElectronicMailAddress>info@lat-lon.de</ContactElectronicMailAddress>
				</ContactInformation>
				<Fees>none</Fees>
				<AccessConstraints>none</AccessConstraints>
			</Service>
			<Capability>
				<Request>
					<GetCapabilities>
						<Format>application/vnd.ogc.wms_xml</Format>
						<DCPType>
							<HTTP>
								<Get>
									<OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple" xlink:href="http://127.0.0.1:8082/mrh-wms2/services?"/>
								</Get>
							</HTTP>
						</DCPType>
					</GetCapabilities>
					<GetMap>
						<Format>image/gif</Format>
						<Format>image/png</Format>
						<Format>image/jpg</Format>
						<Format>image/tif</Format>
						<Format>image/bmp</Format>
						<DCPType>
							<HTTP>
								<Get>
									<OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple" xlink:href="http://127.0.0.1:8082/mrh-wms2/services?"/>
								</Get>
								<Post>
									<OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple" xlink:href="http://127.0.0.1:8082/mrh-wms2/services?"/>
								</Post>
							</HTTP>
						</DCPType>
					</GetMap>
					<GetFeatureInfo>
						<Format>application/vnd.ogc.gml</Format>
						<Format>text/plain</Format>
						<Format>text/html</Format>
						<DCPType>
							<HTTP>
								<Get>
									<OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple" xlink:href="http://127.0.0.1:8082/mrh-wms2/services?"/>
								</Get>
							</HTTP>
						</DCPType>
					</GetFeatureInfo>
					<GetLegendGraphic>
						<Format>image/gif</Format>
						<Format>image/png</Format>
						<Format>image/jpeg</Format>
						<Format>image/jpg</Format>
						<Format>image/tif</Format>
						<Format>image/bmp</Format>
						<DCPType>
							<HTTP>
								<Get>
									<OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple" xlink:href="http://127.0.0.1:8082/mrh-wms2/services?"/>
								</Get>
							</HTTP>
						</DCPType>
					</GetLegendGraphic>
				</Request>
				<Exception>
					<Format>application/vnd.ogc.se_xml</Format>
					<Format>application/vnd.ogc.se_inimage</Format>
					<Format>application/vnd.ogc.se_blank</Format>
				</Exception>
				<UserDefinedSymbolization SupportSLD="1" UserLayer="1" UserStyle="1" RemoteWFS="0"/>
				<Layer queryable="0" noSubsets="0" fixedWidth="0" fixedHeight="0">
					<Title>
						<xsl:value-of select="$PARENTLAYER"/>
					</Title>
					<SRS>
						<xsl:value-of select="$SRS"/>
					</SRS>
					<LatLonBoundingBox>
						<xsl:attribute name="minx"><xsl:value-of select="$MINX"/></xsl:attribute>
						<xsl:attribute name="miny"><xsl:value-of select="$MINY"/></xsl:attribute>
						<xsl:attribute name="maxx"><xsl:value-of select="$MAXX"/></xsl:attribute>
						<xsl:attribute name="maxy"><xsl:value-of select="$MAXY"/></xsl:attribute>
					</LatLonBoundingBox>
					<xsl:for-each select="wfs:FeatureTypeList">
						<xsl:apply-templates select="wfs:FeatureType"/>
					</xsl:for-each>
				</Layer>
			</Capability>
		</WMT_MS_Capabilities>
	</xsl:template>
</xsl:stylesheet>
