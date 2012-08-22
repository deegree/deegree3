<?xml version="1.0" encoding="UTF-8"?>
	<!--
	====================================================================================== 
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
 
    @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a> 
    @author last edited by: $Author: rbezema $ 
    @version  $Revision: 20048 $, $Date: 2009-10-07 16:31:03 +0200 (Mi, 07 Okt 2009) $ 
    ======================================================================================
	-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:jdbc="http://www.deegree.org/jdbc"
	xmlns:deegree="http://www.deegree.org/wms" xmlns:xlink="http://www.w3.org/1999/xlink">
	<xsl:variable name="DEFAULTONLINERESOURCE">
		<xsl:value-of select="/WMT_MS_Capabilities/deegree:DeegreeParam/deegree:DefaultOnlineResource/@xlink:href" />
	</xsl:variable>
	<xsl:variable name="VERSION">
		<xsl:choose>
			<xsl:when test="WMT_MS_Capabilities/@version">
				<xsl:value-of select="WMT_MS_Capabilities/@version" />
			</xsl:when>
			<xsl:otherwise>1.1.1</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:template match="WMT_MS_Capabilities">
		<WMT_MS_Capabilities xmlns:deegree="http://www.deegree.org/wms" xmlns:xlink="http://www.w3.org/1999/xlink"
			xmlns:gml="http://www.opengis.net/gml" xmlns:wfs="http://www.opengis.net/wfs" xmlns:ogc="http://www.opengis.net/ogc">
			<xsl:attribute name="version"><xsl:value-of select="$VERSION" /></xsl:attribute>
			<xsl:if test="./@updateSequence">
				<xsl:attribute name="updateSequence"><xsl:value-of select="./@updateSequence" /></xsl:attribute>
			</xsl:if>
			<xsl:copy-of select="deegree:DeegreeParam" />
			<xsl:choose>
				<xsl:when test="Service">
					<xsl:apply-templates select="Service" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="Service" />
				</xsl:otherwise>
			</xsl:choose>
			<xsl:apply-templates select="Capability" />
		</WMT_MS_Capabilities>
	</xsl:template>
	<xsl:template match="Service" name="Service">
		<Service>
			<xsl:choose>
				<xsl:when test="Name">
					<xsl:copy-of select="Name" />
				</xsl:when>
				<xsl:otherwise>
					<Name>deegree2 WMS</Name>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:choose>
				<xsl:when test="Title">
					<xsl:copy-of select="Title" />
				</xsl:when>
				<xsl:otherwise>
					<Title>deegree2 WMS</Title>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:choose>
				<xsl:when test="OnlineResource">
					<xsl:copy-of select="OnlineResource" />
				</xsl:when>
				<xsl:otherwise>
					<OnlineResource xlink:type="simple">
						<xsl:attribute name="xlink:href"><xsl:value-of select="$DEFAULTONLINERESOURCE" /></xsl:attribute>
					</OnlineResource>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:copy-of select="Abstract" />
			<xsl:copy-of select="KeywordList" />
			<xsl:copy-of select="ContactInformation" />
			<xsl:copy-of select="Fees" />
			<xsl:copy-of select="AccessConstraints" />
		</Service>
	</xsl:template>
	<xsl:template match="Capability">
		<Capability>
			<xsl:choose>
				<xsl:when test="./Request">
					<xsl:apply-templates select="Request" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="Request" />
				</xsl:otherwise>
			</xsl:choose>
			<xsl:choose>
				<xsl:when test="Exception">
					<xsl:copy-of select="Exception" />
				</xsl:when>
				<xsl:otherwise>
					<Exception>
						<Format>application/vnd.ogc.se_xml</Format>
						<Format>application/vnd.ogc.se_inimage</Format>
						<Format>application/vnd.ogc.se_blank</Format>
					</Exception>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:choose>
				<xsl:when test="UserDefinedSymbolization">
					<xsl:copy-of select="UserDefinedSymbolization" />
				</xsl:when>
				<xsl:otherwise>
					<UserDefinedSymbolization SupportSLD="0" UserLayer="0" UserStyle="0" RemoteWFS="0" />
				</xsl:otherwise>
			</xsl:choose>
			<xsl:apply-templates select="Layer" />
		</Capability>
	</xsl:template>

	<xsl:template match="Get | Post">
		<xsl:copy>
			<OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple">
				<xsl:attribute name="xlink:href">
        			<xsl:choose>
						<xsl:when test="OnlineResource/@xlink:href">
							<xsl:value-of select="OnlineResource/@xlink:href" />
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="$DEFAULTONLINERESOURCE" />
						</xsl:otherwise>
					</xsl:choose>
				</xsl:attribute>
			</OnlineResource>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="Request" name="Request">
		<Request>
			<GetCapabilities>
				<Format>application/vnd.ogc.wms_xml</Format>
				<DCPType>
					<HTTP>
						<xsl:apply-templates select="GetCapabilities/DCPType/HTTP/Get" />
					</HTTP>
				</DCPType>
			</GetCapabilities>
			<GetMap>
				<xsl:choose>
					<xsl:when test="./GetMap/Format">
						<xsl:copy-of select="./GetMap/Format" />
					</xsl:when>
					<xsl:otherwise>
						<Format>image/png</Format>
						<Format>image/jpeg</Format>
					</xsl:otherwise>
				</xsl:choose>
				<DCPType>
					<HTTP>
						<xsl:apply-templates select="GetMap/DCPType/HTTP/Get | GetMap/DCPType/HTTP/Post" />
					</HTTP>
				</DCPType>
			</GetMap>
			<xsl:if test="./GetFeatureInfo">
				<GetFeatureInfo>
					<xsl:choose>
						<xsl:when test="./GetFeatureInfo/Format">
							<xsl:copy-of select="./GetFeatureInfo/Format" />
						</xsl:when>
						<xsl:otherwise>
							<Format>application/vnd.ogc.gml</Format>
							<Format>text/html</Format>
						</xsl:otherwise>
					</xsl:choose>
					<DCPType>
						<HTTP>
							<xsl:apply-templates select="GetFeatureInfo/DCPType/HTTP/Get | GetFeatureInfo/DCPType/HTTP/Post" />
						</HTTP>
					</DCPType>
				</GetFeatureInfo>
			</xsl:if>
			<xsl:if test="./GetLegendGraphic">
				<GetLegendGraphic>
					<xsl:choose>
						<xsl:when test="./GetLegendGraphic/Format">
							<xsl:copy-of select="./GetLegendGraphic/Format" />
						</xsl:when>
						<xsl:otherwise>
							<Format>image/png</Format>
							<Format>image/jpeg</Format>
						</xsl:otherwise>
					</xsl:choose>
					<DCPType>
						<HTTP>
							<xsl:apply-templates select="GetLegendGraphic/DCPType/HTTP/Get | GetLegendGraphic/DCPType/HTTP/Post" />
						</HTTP>
					</DCPType>
				</GetLegendGraphic>
			</xsl:if>
			<xsl:if test="./GetStyles">
				<GetStyles>
					<Format>application/vnd.ogc.wms_xml</Format>
					<DCPType>
						<HTTP>
							<xsl:apply-templates select="GetStyles/DCPType/HTTP/Get | GetStyles/DCPType/HTTP/Post" />
						</HTTP>
					</DCPType>
				</GetStyles>
			</xsl:if>
			<xsl:if test="DescribeLayer">
				<DescribeLayer>
					<Format>application/vnd.ogc.wms_xml</Format>
					<DCPType>
						<HTTP>
							<xsl:apply-templates select="DescribeLayer/DCPType/HTTP/Get | DescribeLayer/DCPType/HTTP/Post" />
						</HTTP>
					</DCPType>
				</DescribeLayer>
			</xsl:if>
			<xsl:if test="./PutStyles">
				<PutStyles>
					<Format>application/vnd.ogc.wms_xml</Format>
					<DCPType>
						<HTTP>
							<xsl:apply-templates select="PutStyles/DCPType/HTTP/Get | PutStyles/DCPType/HTTP/Post" />
						</HTTP>
					</DCPType>
				</PutStyles>
			</xsl:if>
		</Request>
	</xsl:template>
	<xsl:template match="Layer">
		<Layer>
			<xsl:if test="./@queryable">
				<xsl:attribute name="queryable"><xsl:value-of select="./@queryable" /></xsl:attribute>
			</xsl:if>
			<xsl:if test="./@cascaded">
				<xsl:attribute name="cascaded"><xsl:value-of select="./@cascaded" /></xsl:attribute>
			</xsl:if>
			<xsl:if test="./@opaque">
				<xsl:attribute name="opaque"><xsl:value-of select="./@opaque" /></xsl:attribute>
			</xsl:if>
			<xsl:if test="./@noSubsets">
				<xsl:attribute name="noSubsets"><xsl:value-of select="./@noSubsets" /></xsl:attribute>
			</xsl:if>
			<xsl:if test="./@fixedWidth">
				<xsl:attribute name="fixedWidth"><xsl:value-of select="./@fixedWidth" /></xsl:attribute>
			</xsl:if>
			<xsl:if test="./@fixedHeight">
				<xsl:attribute name="fixedHeight"><xsl:value-of select="./@fixedHeight" /></xsl:attribute>
			</xsl:if>
			<xsl:copy-of select="Name" />
			<xsl:copy-of select="Title" />
			<xsl:copy-of select="Abstract" />
			<xsl:copy-of select="KeywordList" />
			<xsl:copy-of select="ScaleHint" />
			<xsl:choose>
				<xsl:when test="./SRS">
					<xsl:copy-of select="./SRS" />
				</xsl:when>
				<xsl:otherwise>
					<!-- use EPSG:4326 as default reference system -->
					<xsl:if test="local-name(..) = 'Capability'">
						<SRS>EPSG:4326</SRS>
					</xsl:if>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:choose>
				<xsl:when test="./LatLonBoundingBox">
					<xsl:copy-of select="./LatLonBoundingBox" />
				</xsl:when>
				<!--
				<xsl:otherwise>
                    <xsl:if test="../LatLonBoundingBox = '' ">
                        <LatLonBoundingBox minx="-180" miny="-90" maxx="180" maxy="90"/>
                    </xsl:if>
                </xsl:otherwise>
				-->
			</xsl:choose>
			<xsl:copy-of select="BoundingBox" />
			<xsl:copy-of select="Dimension" />
			<xsl:copy-of select="Extent" />
			<xsl:copy-of select="Attribution" />
			<xsl:copy-of select="AuthorityURL" />
			<xsl:copy-of select="Identifier" />
			<xsl:copy-of select="MetadataURL" />
			<xsl:copy-of select="DataURL" />
			<xsl:copy-of select="FeatureListURL" />
			<xsl:if test="Name">
				<xsl:if test="deegree:DataSource">
					<!-- just a layer having a requestable name can have a style -->
					<xsl:choose>
						<xsl:when test="Style">
							<xsl:for-each select="Style">
								<xsl:call-template name="Style">
									<xsl:with-param name="pStyle" select="." />
									<xsl:with-param name="bStyle" select="true()" />
								</xsl:call-template>
							</xsl:for-each>
						</xsl:when>
						<xsl:otherwise>
							<xsl:call-template name="Style">
								<xsl:with-param name="pStyle" select="/WMT_MS_Capabilities" />
							</xsl:call-template>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>
			</xsl:if>
			<xsl:apply-templates select="Layer" />
			<xsl:if test="Name">
				<!-- just a layer having a requestable name can have a datasource -->
				<xsl:choose>
					<xsl:when test="deegree:DataSource">
						<xsl:for-each select="deegree:DataSource">
							<xsl:call-template name="datasource">
								<xsl:with-param name="pDs" select="." />
							</xsl:call-template>
						</xsl:for-each>
					</xsl:when>
					<!--
						<xsl:otherwise>
                            <xsl:call-template name="datasource">
                                <xsl:with-param name="pDs" select="."/>
                            </xsl:call-template>
						</xsl:otherwise>
					-->
				</xsl:choose>
			</xsl:if>
		</Layer>
	</xsl:template>
	<xsl:template match="Style" name="Style">
		<xsl:param name="pStyle" />
		<xsl:param name="bStyle" />
		<xsl:variable name="STYLENAME">
			<xsl:choose>
				<xsl:when test="$pStyle/Name"><xsl:copy-of select="$pStyle/Name" /></xsl:when>
				<xsl:otherwise>default:<xsl:value-of select="./Name" /></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<Style>
			<Name><xsl:value-of select="$STYLENAME" /></Name>
			<xsl:choose>
				<xsl:when test="$pStyle/Title">
					<xsl:copy-of select="$pStyle/Title" />
				</xsl:when>
				<xsl:otherwise>
					<Title>
						<xsl:value-of select="$STYLENAME" />
					</Title>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:copy-of select="$pStyle/Abstract" />
			<xsl:choose>
				<xsl:when test="$pStyle/LegendURL">
					<xsl:copy-of select="$pStyle/LegendURL" />
				</xsl:when>
				<xsl:otherwise>
					<!-- use a GetLegendGraphic request as default if no LegendURL has been defined -->
					<LegendURL width="10" height="10">
						<Format>image/jpeg</Format>
						<OnlineResource xlink:type="simple">
							<xsl:choose>
								<xsl:when test="boolean($bStyle)">
									<xsl:attribute name="xlink:href"><xsl:value-of select="$DEFAULTONLINERESOURCE" />?request=GetLegendGraphic&amp;version=<xsl:value-of select="$VERSION"/>&amp;width=25&amp;height=25&amp;format=image/jpeg&amp;layer=<xsl:value-of select="../Name"/>&amp;style=<xsl:value-of select="$STYLENAME"/></xsl:attribute>
                           		</xsl:when>
                           		<xsl:otherwise>
                           			<xsl:attribute name="xlink:href"><xsl:value-of select="$DEFAULTONLINERESOURCE"/>?request=GetLegendGraphic&amp;version=<xsl:value-of select="$VERSION"/>&amp;width=25&amp;height=25&amp;format=image/jpeg&amp;layer=<xsl:value-of select="Name"/>&amp;style=<xsl:value-of select="$STYLENAME"/></xsl:attribute>
                            		</xsl:otherwise>
                             </xsl:choose>
                        </OnlineResource>
                    </LegendURL>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:copy-of select="$pStyle/StyleSheetURL"/>
            <xsl:copy-of select="$pStyle/StyleURL"/>
            <xsl:copy-of select="$pStyle/deegree:StyleResource"/>
        </Style>
    </xsl:template>
    <xsl:template match="deegree:DataSource" name="datasource">
        <xsl:param name="pDs"/>
        <deegree:DataSource>
            <xsl:if test="$pDs/@queryable">
                <xsl:attribute name="queryable"><xsl:value-of select="$pDs/@queryable"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$pDs/@failOnException">
                <xsl:attribute name="failOnException"><xsl:value-of select="$pDs/@failOnException"/></xsl:attribute>
            </xsl:if>
            <xsl:choose>
                <xsl:when test="$pDs/deegree:Name">
                    <xsl:copy-of select="$pDs/deegree:Name"/>
                </xsl:when>
                <xsl:otherwise>
                    <!-- use the layers name as default name for the datasource -->
                    <deegree:Name>
                        <xsl:value-of select="./Name"/>
                    </deegree:Name>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:choose>
                <xsl:when test="$pDs/deegree:Type">
                    <xsl:copy-of select="$pDs/deegree:Type"/>
                </xsl:when>
                <xsl:otherwise>
                    <!-- use LOCALWFS as default datasource type -->
                    <deegree:Type>LOCALWFS</deegree:Type>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:copy-of select="$pDs/deegree:GeometryProperty"/>            
            <xsl:copy-of select="$pDs/jdbc:JDBCConnection"/>
            <xsl:copy-of select="$pDs/deegree:GeometryField"/>
            <xsl:copy-of select="$pDs/deegree:SQLTemplate"/>
            <xsl:copy-of select="$pDs/deegree:NativeCRS"/>
            <xsl:copy-of select="$pDs/deegree:CustomSQLAllowed" />
            <xsl:copy-of select="$pDs/deegree:DimensionProperty" />
            <xsl:copy-of select="$pDs/deegree:ClassName" />
            <xsl:copy-of select="$pDs/deegree:ConfigurationFile" />
            <xsl:choose>
                <xsl:when test="$pDs/deegree:OWSCapabilities">
                    <xsl:copy-of select="$pDs/deegree:OWSCapabilities"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:choose>
                        <xsl:when test="$pDs/deegree:Type = 'LOCALWCS' ">
                            <!-- if datasource type is LOCALWCS use reference to LOCALWCS_capabilities.xml as default -->
                            <deegree:OWSCapabilities>
                                <deegree:OnlineResource xlink:type="simple" xlink:href="LOCALWCS_capabilities.xml"/>
                            </deegree:OWSCapabilities>
                        </xsl:when>
                        <xsl:otherwise>
                            <!-- use reference to LOCALWFS_capabilities.xml -->
                            <deegree:OWSCapabilities>
                                <deegree:OnlineResource xlink:type="simple" xlink:href="LOCALWFS_capabilities.xml"/>
                            </deegree:OWSCapabilities>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:if test="$pDs/deegree:FilterCondition">
                <xsl:copy-of select="$pDs/deegree:FilterCondition"/>
            </xsl:if>
            <xsl:if test="$pDs/deegree:Type = 'LOCALWCS' or $pDs/deegree:Type = 'REMOTEWCS' ">
                <xsl:copy-of select="$pDs/deegree:TransparentColors"/>
            </xsl:if>
            <xsl:if test="$pDs/deegree:Type = 'REMOTEWMS' ">
                <xsl:copy-of select="$pDs/deegree:TransparentColors"/>
            </xsl:if>
            <xsl:copy-of select="$pDs/deegree:ScaleHint"/>
            <xsl:copy-of select="$pDs/deegree:FeatureInfoTransformation"/>
            <xsl:copy-of select="$pDs/deegree:ValidArea"/>
            <xsl:copy-of select="$pDs/deegree:RequestTimeLimit"/>
        </deegree:DataSource>
    </xsl:template>
</xsl:stylesheet>
