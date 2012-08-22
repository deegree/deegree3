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
  
  @version $Revision: 20048 $
  @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
  @author last edited by: $Author: rbezema $
  
  @version 1.0. $Revision: 20048 $, $Date: 2009-10-07 16:31:03 +0200 (Mi, 07 Okt 2009) $
  
  ====================================================================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:deegree="http://www.deegree.org/wms" xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:wms="http://www.opengis.net/wms" xmlns:sld="http://www.opengis.net/sld" xmlns:jdbc="http://www.deegree.org/jdbc">
  <xsl:variable name="DEFAULTONLINERESOURCE">
    <xsl:value-of
      select="/wms:WMS_Capabilities/deegree:DeegreeParam/deegree:DefaultOnlineResource/@xlink:href" />
  </xsl:variable>
  <xsl:variable name="VERSION">
    <xsl:choose>
      <xsl:when test="wms:WMS_Capabilities/@version">
        <xsl:value-of select="wms:WMS_Capabilities/@version" />
      </xsl:when>
      <xsl:otherwise>1.3.0</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:template match="wms:WMS_Capabilities">
    <wms:WMS_Capabilities xmlns:deegree="http://www.deegree.org/wms"
      xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:gml="http://www.opengis.net/gml"
      xmlns:wfs="http://www.opengis.net/wfs" xmlns:ogc="http://www.opengis.net/ogc">
      <xsl:attribute name="version">
        <xsl:value-of select="$VERSION" />
      </xsl:attribute>
      <xsl:if test="./@updateSequence">
        <xsl:attribute name="updateSequence">
          <xsl:value-of select="./@updateSequence" />
        </xsl:attribute>
      </xsl:if>
      <xsl:copy-of select="deegree:DeegreeParam" />
      <xsl:choose>
        <xsl:when test="wms:Service">
          <xsl:apply-templates select="wms:Service" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="wms:Service" />
        </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="wms:Capability" />
    </wms:WMS_Capabilities>
  </xsl:template>
  <xsl:template match="wms:Service" name="wms:Service">
    <wms:Service>
      <xsl:choose>
        <xsl:when test="wms:Name">
          <xsl:copy-of select="wms:Name" />
        </xsl:when>
        <xsl:otherwise>
          <wms:Name>deegree2 WMS</wms:Name>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:choose>
        <xsl:when test="wms:Title">
          <xsl:copy-of select="wms:Title" />
        </xsl:when>
        <xsl:otherwise>
          <wms:Title>deegree2 WMS</wms:Title>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:choose>
        <xsl:when test="wms:LayerLimit">
          <xsl:copy-of select="wms:LayerLimit" />
        </xsl:when>
      </xsl:choose>
      <xsl:choose>
        <xsl:when test="wms:MaxWidth">
          <xsl:copy-of select="wms:MaxWidth" />
        </xsl:when>
      </xsl:choose>
      <xsl:choose>
        <xsl:when test="wms:MaxHeight">
          <xsl:copy-of select="wms:MaxHeight" />
        </xsl:when>
      </xsl:choose>
      <xsl:choose>
        <xsl:when test="wms:OnlineResource">
          <xsl:copy-of select="wms:OnlineResource" />
        </xsl:when>
        <xsl:otherwise>
          <wms:OnlineResource xlink:type="simple">
            <xsl:attribute name="xlink:href">
              <xsl:value-of select="$DEFAULTONLINERESOURCE" />
            </xsl:attribute>
          </wms:OnlineResource>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:copy-of select="wms:Abstract" />
      <xsl:copy-of select="wms:KeywordList" />
      <xsl:copy-of select="wms:ContactInformation" />
      <xsl:copy-of select="wms:Fees" />
      <xsl:copy-of select="wms:AccessConstraints" />
    </wms:Service>
  </xsl:template>
  <xsl:template match="wms:Capability">
    <wms:Capability>
      <xsl:choose>
        <xsl:when test="./wms:Request">
          <xsl:apply-templates select="wms:Request" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="wms:Request" />
        </xsl:otherwise>
      </xsl:choose>
      <xsl:choose>
        <xsl:when test="wms:Exception">
          <xsl:copy-of select="wms:Exception" />
        </xsl:when>
        <xsl:otherwise>
          <wms:Exception>
            <wms:Format>XML</wms:Format>
            <wms:Format>INIMAGE</wms:Format>
            <wms:Format>BLANK</wms:Format>
          </wms:Exception>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="wms:Layer" />
    </wms:Capability>
  </xsl:template>

  <xsl:template match="wms:Request" name="wms:Request">
    <wms:Request>
      <wms:GetCapabilities>
        <wms:Format>text/xml</wms:Format>
        <wms:DCPType>
          <wms:HTTP>
            <wms:Get>
              <xsl:choose>
                <xsl:when
                  test="wms:GetCapabilities/wms:DCPType/wms:HTTP/wms:Get/wms:OnlineResource/@xlink:href">
                  <wms:OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink"
                    xlink:type="simple">
                    <xsl:attribute name="xlink:href">
                      <xsl:value-of
                        select="wms:GetCapabilities/wms:DCPType/wms:HTTP/wms:Get/wms:OnlineResource/@xlink:href" />
                    </xsl:attribute>
                  </wms:OnlineResource>
                </xsl:when>
                <xsl:otherwise>
                  <wms:OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink"
                    xlink:type="simple">
                    <xsl:attribute name="xlink:href">
                      <xsl:value-of select="$DEFAULTONLINERESOURCE" />
                    </xsl:attribute>
                  </wms:OnlineResource>
                </xsl:otherwise>
              </xsl:choose>
            </wms:Get>
            <xsl:copy-of select="./wms:GetCapabilities/wms:DCPType/wms:HTTP/wms:Post" />
          </wms:HTTP>
        </wms:DCPType>
      </wms:GetCapabilities>
      <wms:GetMap>
        <xsl:choose>
          <xsl:when test="./wms:GetMap/wms:Format">
            <xsl:copy-of select="./wms:GetMap/wms:Format" />
          </xsl:when>
          <xsl:otherwise>
            <wms:Format>image/png</wms:Format>
            <wms:Format>image/jpeg</wms:Format>
          </xsl:otherwise>
        </xsl:choose>
        <wms:DCPType>
          <wms:HTTP>
            <wms:Get>
              <xsl:choose>
                <xsl:when
                  test="wms:GetMap/wms:DCPType/wms:HTTP/wms:Get/wms:OnlineResource/@xlink:href">
                  <wms:OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink"
                    xlink:type="simple">
                    <xsl:attribute name="xlink:href">
                      <xsl:value-of
                        select="./wms:GetMap/wms:DCPType/wms:HTTP/wms:Get/wms:OnlineResource/@xlink:href" />
                    </xsl:attribute>
                  </wms:OnlineResource>
                </xsl:when>
                <xsl:otherwise>
                  <wms:OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink"
                    xlink:type="simple">
                    <xsl:attribute name="xlink:href">
                      <xsl:value-of select="$DEFAULTONLINERESOURCE" />
                    </xsl:attribute>
                  </wms:OnlineResource>
                </xsl:otherwise>
              </xsl:choose>
            </wms:Get>
            <xsl:if test="wms:GetMap/wms:DCPType/wms:HTTP/wms:Post">
              <wms:Post>
                <xsl:choose>
                  <xsl:when
                    test="wms:GetMap/wms:DCPType/wms:HTTP/wms:Post/wms:OnlineResource/@xlink:href">
                    <wms:OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink"
                      xlink:type="simple">
                      <xsl:attribute name="xlink:href">
                        <xsl:value-of
                          select="./wms:GetMap/wms:DCPType/wms:HTTP/wms:Post/wms:OnlineResource/@xlink:href" />
                      </xsl:attribute>
                    </wms:OnlineResource>
                  </xsl:when>
                  <xsl:otherwise>
                    <wms:OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink"
                      xlink:type="simple">
                      <xsl:attribute name="xlink:href">
                        <xsl:value-of select="$DEFAULTONLINERESOURCE" />
                      </xsl:attribute>
                    </wms:OnlineResource>
                  </xsl:otherwise>
                </xsl:choose>
              </wms:Post>
            </xsl:if>
          </wms:HTTP>
        </wms:DCPType>
      </wms:GetMap>
      <xsl:if test="./wms:GetFeatureInfo">
        <wms:GetFeatureInfo>
          <xsl:choose>
            <xsl:when test="./wms:GetFeatureInfo/wms:Format">
              <xsl:copy-of select="./wms:GetFeatureInfo/wms:Format" />
            </xsl:when>
            <xsl:otherwise>
              <wms:Format>application/vnd.ogc.gml</wms:Format>
              <wms:Format>text/html</wms:Format>
            </xsl:otherwise>
          </xsl:choose>
          <wms:DCPType>
            <wms:HTTP>
              <wms:Get>
                <xsl:choose>
                  <xsl:when
                    test="wms:GetFeatureInfo/wms:DCPType/wms:HTTP/wms:Get/wms:OnlineResource/@xlink:href">
                    <wms:OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink"
                      xlink:type="simple">
                      <xsl:attribute name="xlink:href">
                        <xsl:value-of
                          select="./wms:GetFeatureInfo/wms:DCPType/wms:HTTP/wms:Get/wms:OnlineResource/@xlink:href" />
                      </xsl:attribute>
                    </wms:OnlineResource>
                  </xsl:when>
                  <xsl:otherwise>
                    <wms:OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink"
                      xlink:type="simple">
                      <xsl:attribute name="xlink:href">
                        <xsl:value-of select="$DEFAULTONLINERESOURCE" />
                      </xsl:attribute>
                    </wms:OnlineResource>
                  </xsl:otherwise>
                </xsl:choose>
              </wms:Get>
              <xsl:copy-of select="./wms:GetFeatureInfo/wms:DCPType/wms:HTTP/wms:Post" />
            </wms:HTTP>
          </wms:DCPType>
        </wms:GetFeatureInfo>
      </xsl:if>
      <xsl:if test="./sld:GetLegendGraphic">
        <sld:GetLegendGraphic>
          <xsl:choose>
            <xsl:when test="./sld:GetLegendGraphic/wms:Format">
              <xsl:copy-of select="./sld:GetLegendGraphic/wms:Format" />
            </xsl:when>
            <xsl:otherwise>
              <wms:Format>image/png</wms:Format>
              <wms:Format>image/jpeg</wms:Format>
            </xsl:otherwise>
          </xsl:choose>
          <wms:DCPType>
            <wms:HTTP>
              <wms:Get>
                <xsl:choose>
                  <xsl:when
                    test="wms:GetFeatureInfo/wms:DCPType/wms:HTTP/wms:Get/wms:OnlineResource/@xlink:href">
                    <wms:OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink"
                      xlink:type="simple">
                      <xsl:attribute name="xlink:href">
                        <xsl:value-of
                          select="wms:GetFeatureInfo/wms:DCPType/wms:HTTP/wms:Get/wms:OnlineResource/@xlink:href" />
                      </xsl:attribute>
                    </wms:OnlineResource>
                  </xsl:when>
                  <xsl:otherwise>
                    <wms:OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink"
                      xlink:type="simple">
                      <xsl:attribute name="xlink:href">
                        <xsl:value-of select="$DEFAULTONLINERESOURCE" />
                      </xsl:attribute>
                    </wms:OnlineResource>
                  </xsl:otherwise>
                </xsl:choose>
              </wms:Get>
              <xsl:copy-of select="./sld:GetLegendGraphic/wms:DCPType/wms:HTTP/wms:Post" />
            </wms:HTTP>
          </wms:DCPType>
        </sld:GetLegendGraphic>
      </xsl:if>
      <xsl:if test="sld:DescribeLayer">
      	<sld:DescribeLayer>
      		<wms:Format>application/vnd.ogc.wms_xml</wms:Format>
      		<wms:DCPType>
      			<wms:HTTP>
      				<wms:Get>
      					<wms:OnlineResource
      						xmlns:xlink="http://www.w3.org/1999/xlink"
      						xlink:type="simple">
      						<xsl:choose>
      							<xsl:when
      								test="sld:DescribeLayer/wms:DCPType/wms:HTTP/wms:Get/wms:OnlineResource/@xlink:href">
      								<xsl:attribute
      									name="xlink:href">
      									<xsl:value-of
      										select="sld:DescribeLayer/wms:DCPType/wms:HTTP/wms:Get/wms:OnlineResource/@xlink:href" />
      								</xsl:attribute>
      							</xsl:when>
      							<xsl:otherwise>
      								<xsl:attribute
      									name="xlink:href">
      									<xsl:value-of
      										select="$DEFAULTONLINERESOURCE" />
      								</xsl:attribute>
      							</xsl:otherwise>
      						</xsl:choose>
      					</wms:OnlineResource>
      				</wms:Get>
      			</wms:HTTP>
      		</wms:DCPType>
      	</sld:DescribeLayer>
      </xsl:if>
      <xsl:if test="./sld:GetStyles">
        <sld:GetStyles>
          <wms:Format>application/vnd.ogc.wms_xml</wms:Format>
          <wms:DCPType>
            <wms:HTTP>
              <wms:Get>
                <wms:OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink"
                  xlink:type="simple">
                  <xsl:attribute name="xlink:href">
                    <xsl:value-of
                      select="./sld:GetStyles/wms:DCPType/wms:HTTP/wms:Get/wms:OnlineResource/@xlink:href" />
                  </xsl:attribute>
                </wms:OnlineResource>
              </wms:Get>
              <xsl:copy-of select="./sld:GetStyles/wms:DCPType/wms:HTTP/wms:Post" />
            </wms:HTTP>
          </wms:DCPType>
        </sld:GetStyles>
      </xsl:if>
      <xsl:if test="./sld:PutStyles">
        <sld:PutStyles>
          <Format>application/vnd.ogc.wms_xml</Format>
          <wms:DCPType>
            <wms:HTTP>
              <wms:Get>
                <wms:OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink"
                  xlink:type="simple">
                  <xsl:attribute name="xlink:href">
                    <xsl:value-of
                      select="./sld:PutStyles/wms:DCPType/wms:HTTP/wms:Get/wms:OnlineResource/@xlink:href" />
                  </xsl:attribute>
                </wms:OnlineResource>
              </wms:Get>
              <xsl:copy-of select="./wms:PutStyles/wms:DCPType/wms:HTTP/wms:Post" />
            </wms:HTTP>
          </wms:DCPType>
        </sld:PutStyles>
      </xsl:if>
    </wms:Request>
  </xsl:template>

  <xsl:template match="wms:Layer">
    <wms:Layer>
      <xsl:if test="./@queryable">
        <xsl:attribute name="queryable">
          <xsl:value-of select="./@queryable" />
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="./@cascaded">
        <xsl:attribute name="cascaded">
          <xsl:value-of select="./@cascaded" />
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="./@opaque">
        <xsl:attribute name="opaque">
          <xsl:value-of select="./@opaque" />
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="./@noSubsets">
        <xsl:attribute name="noSubsets">
          <xsl:value-of select="./@noSubsets" />
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="./@fixedWidth">
        <xsl:attribute name="fixedWidth">
          <xsl:value-of select="./@fixedWidth" />
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="./@fixedHeight">
        <xsl:attribute name="fixedHeight">
          <xsl:value-of select="./@fixedHeight" />
        </xsl:attribute>
      </xsl:if>
      <xsl:copy-of select="wms:Name" />
      <xsl:copy-of select="wms:Title" />
      <xsl:copy-of select="wms:Abstract" />
      <xsl:copy-of select="wms:KeywordList" />
      <xsl:copy-of select="wms:MinScaleDenominator" />
      <xsl:copy-of select="wms:MaxScaleDenominator" />
      <xsl:choose>
        <xsl:when test="./wms:CRS">
          <xsl:copy-of select="./wms:CRS" />
        </xsl:when>
        <xsl:otherwise>
          <!-- use EPSG:4326 as default reference system -->
          <wms:CRS>EPSG:4326</wms:CRS>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:choose>
        <xsl:when test="./wms:EX_GeographicBoundingBox">
          <xsl:copy-of select="./wms:EX_GeographicBoundingBox" />
        </xsl:when>
      </xsl:choose>
      <xsl:copy-of select="wms:BoundingBox" />
      <xsl:copy-of select="wms:Dimension" />
      <xsl:copy-of select="wms:Attribution" />
      <xsl:copy-of select="wms:AuthorityURL" />
      <xsl:copy-of select="wms:Identifier" />
      <xsl:copy-of select="wms:MetadataURL" />
      <xsl:copy-of select="wms:DataURL" />
      <xsl:copy-of select="wms:FeatureListURL" />
      <xsl:if test="wms:Name">
        <xsl:if test="deegree:DataSource">
          <!-- just a layer having a requestable name can have a style -->
          <xsl:choose>
            <xsl:when test="wms:Style">
              <xsl:for-each select="wms:Style">
                <xsl:call-template name="wms:Style">
                  <xsl:with-param name="pStyle" select="." />
                <xsl:with-param name="bStyle" select="true()" />
                </xsl:call-template>
              </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="wms:Style">
                <xsl:with-param name="pStyle" select="/wms:WMS_Capabilities" />
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:if>
      </xsl:if>
      <xsl:apply-templates select="wms:Layer" />
      <xsl:if test="wms:Name">
        <!-- just a layer having a requestable name can have a datasource -->
        <xsl:choose>
          <xsl:when test="deegree:DataSource">
            <xsl:for-each select="deegree:DataSource">
              <xsl:call-template name="datasource">
                <xsl:with-param name="pDs" select="." />
              </xsl:call-template>
            </xsl:for-each>
          </xsl:when>
          <!--                     <xsl:otherwise>
            <xsl:call-template name="datasource">
            <xsl:with-param name="pDs" select="."/>
            </xsl:call-template>
            </xsl:otherwise>-->
        </xsl:choose>
      </xsl:if>
    </wms:Layer>
  </xsl:template>
  
  <xsl:template match="wms:Style" name="wms:Style">
    <xsl:param name="pStyle" />
    <xsl:param name="bStyle" />
    <xsl:variable name="STYLENAME">
      <xsl:choose>
        <xsl:when test="$pStyle/wms:Name">
          <xsl:copy-of select="$pStyle/wms:Name" />
        </xsl:when>
        <xsl:otherwise>
          <wms:Name>default:<xsl:value-of select="./wms:Name" /></wms:Name>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <wms:Style>
      <wms:Name>
        <xsl:value-of select="$STYLENAME" />
      </wms:Name>
      <xsl:choose>
        <xsl:when test="$pStyle/wms:Title">
          <xsl:copy-of select="$pStyle/wms:Title" />
        </xsl:when>
        <xsl:otherwise>
          <wms:Title>
            <xsl:value-of select="$STYLENAME" />
          </wms:Title>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:copy-of select="$pStyle/wms:Abstract" />
      <xsl:choose>
        <xsl:when test="$pStyle/wms:LegendURL">
          <xsl:copy-of select="$pStyle/wms:LegendURL" />
        </xsl:when>
        <xsl:otherwise>
          <!-- use a GetLegendGraphic request as default if no LegendURL has been defined -->
          <wms:LegendURL width="10" height="10">
            <wms:Format>image/gif</wms:Format>
            <wms:OnlineResource xlink:type="simple">
            	<xsl:variable name="getlegendversion">
            		<xsl:choose>
            			<xsl:when test="boolean(/wms:WMS_Capabilities/deegree:DeegreeParam/deegree:SupportedVersion[text() = '1.3.0'])">1.3.0</xsl:when>
            			<xsl:otherwise>1.1.1</xsl:otherwise>
            		</xsl:choose>
            	</xsl:variable>
               <xsl:choose>
                <xsl:when test="boolean($bStyle)">
                  <xsl:attribute name="xlink:href"><xsl:value-of select="$DEFAULTONLINERESOURCE" />?request=GetLegendGraphic&amp;version=<xsl:value-of select="$getlegendversion" />&amp;width=10&amp;height=10&amp;format=image/gif&amp;layer=<xsl:value-of select="../wms:Name" />&amp;style=<xsl:value-of select="$STYLENAME" /></xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:attribute name="xlink:href"><xsl:value-of select="$DEFAULTONLINERESOURCE" />?request=GetLegendGraphic&amp;version=<xsl:value-of select="$getlegendversion" />&amp;width=10&amp;height=10&amp;format=image/gif&amp;layer=<xsl:value-of select="wms:Name" />&amp;style=<xsl:value-of select="$STYLENAME" /></xsl:attribute>
                 </xsl:otherwise>
              </xsl:choose>
            </wms:OnlineResource>
          </wms:LegendURL>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:copy-of select="$pStyle/wms:StyleSheetURL" /><xsl:copy-of select="$pStyle/wms:StyleURL" /><xsl:copy-of select="$pStyle/deegree:StyleResource" />
    </wms:Style>
  </xsl:template>
  <xsl:template match="deegree:DataSource" name="datasource">
    <xsl:param name="pDs" />
    <deegree:DataSource>
      <xsl:if test="$pDs/@queryable">
        <xsl:attribute name="queryable">
          <xsl:value-of select="$pDs/@queryable" />
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="$pDs/@failOnException">
        <xsl:attribute name="failOnException">
          <xsl:value-of select="$pDs/@failOnException" />
        </xsl:attribute>
      </xsl:if>
      <xsl:choose>
        <xsl:when test="$pDs/deegree:Name">
          <xsl:copy-of select="$pDs/deegree:Name" />
        </xsl:when>
        <xsl:otherwise>
          <!-- use the layers name as default name for the datasource -->
          <deegree:Name>
            <xsl:value-of select="./Name" />
          </deegree:Name>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:choose>
        <xsl:when test="$pDs/deegree:Type">
          <xsl:copy-of select="$pDs/deegree:Type" />
        </xsl:when>
        <xsl:otherwise>
          <!-- use LOCALWFS as default datasource type -->
          <deegree:Type>LOCALWFS</deegree:Type>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:copy-of select="$pDs/deegree:GeometryProperty" />
      <xsl:copy-of select="$pDs/jdbc:JDBCConnection"/>
      <xsl:copy-of select="$pDs/deegree:GeometryField"/>
      <xsl:copy-of select="$pDs/deegree:SQLTemplate"/>
      <xsl:copy-of select="$pDs/deegree:NativeCRS"/>
      <xsl:copy-of select="$pDs/deegree:CustomSQLAllowed" />
      <xsl:copy-of select="$pDs/deegree:DimensionProperty" />
      <xsl:copy-of select="$pDs/deegree:StaticGetFeatureInfoFile" />
      <xsl:choose>
        <xsl:when test="$pDs/deegree:OWSCapabilities">
          <xsl:copy-of select="$pDs/deegree:OWSCapabilities" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:choose>
            <xsl:when test="$pDs/deegree:Type = 'LOCALWCS' ">
              <!-- if datasource type is LOCALWCS use reference to LOCALWCS_capabilities.xml 
                as default -->
              <deegree:OWSCapabilities>
                <deegree:OnlineResource xlink:type="simple" xlink:href="LOCALWCS_capabilities.xml" />
              </deegree:OWSCapabilities>
            </xsl:when>
            <xsl:otherwise>
              <!-- use reference to LOCALWFS_capabilities.xml -->
              <deegree:OWSCapabilities>
                <deegree:OnlineResource xlink:type="simple" xlink:href="LOCALWFS_capabilities.xml" />
              </deegree:OWSCapabilities>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:if test="$pDs/deegree:FilterCondition">
        <xsl:copy-of select="$pDs/deegree:FilterCondition" />
      </xsl:if>
      <xsl:if test="$pDs/deegree:Type = 'LOCALWCS' or $pDs/deegree:Type = 'REMOTEWCS' ">
        <xsl:copy-of select="$pDs/deegree:TransparentColors" />
      </xsl:if>
      <xsl:if test="$pDs/deegree:Type = 'REMOTEWMS' ">
        <xsl:copy-of select="$pDs/deegree:TransparentColors" />
      </xsl:if>
      <xsl:copy-of select="$pDs/deegree:ScaleHint" />
      <xsl:copy-of select="$pDs/deegree:FeatureInfoTransformation" />
      <xsl:copy-of select="$pDs/deegree:ValidArea" />
      <xsl:copy-of select="$pDs/deegree:RequestTimeLimit" />
    </deegree:DataSource>
  </xsl:template>
</xsl:stylesheet>
