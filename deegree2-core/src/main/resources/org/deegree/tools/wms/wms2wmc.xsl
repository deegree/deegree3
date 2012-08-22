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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:wmc="http://www.opengis.net/context" xmlns:xlink="http://www.w3.org/1999/xlink">
	<xsl:variable name="ONLINE">
		<xsl:value-of select="/WMT_MS_Capabilities/Capability/Request/GetCapabilities/DCPType/HTTP/Get/OnlineResource/@xlink:href"/>
	</xsl:variable>
	<xsl:variable name="VERSION">
		<xsl:value-of select="WMT_MS_Capabilities/@version"/>
	</xsl:variable>
	<xsl:variable name="TITLE">
		<xsl:value-of select="WMT_MS_Capabilities/Service/Title"/>
	</xsl:variable>
	<xsl:template match="WMT_MS_Capabilities">
		<wmc:ViewContext version="1.0.0" id="String">
			<wmc:General>
				<wmc:Window width="500" height="500"/>
				<wmc:BoundingBox SRS="EPSG:4326" minx="3" miny="46" maxx="18" maxy="56"/>
				<wmc:Title>deegree iGeoPortal</wmc:Title>
				<wmc:KeywordList>
					<wmc:Keyword>deegree</wmc:Keyword>
					<wmc:Keyword>iGeoPortal</wmc:Keyword>
					<wmc:Keyword>lat/lon</wmc:Keyword>
				</wmc:KeywordList>
				<wmc:DescriptionURL format="text/html">
					<wmc:OnlineResource xlink:type="simple" xlink:href="http://www.deegree.org"/>
				</wmc:DescriptionURL>
				<wmc:ContactInformation>
					<wmc:ContactPersonPrimary>
						<wmc:ContactPerson>Andreas Poth</wmc:ContactPerson>
						<wmc:ContactOrganization>lat/lon</wmc:ContactOrganization>
					</wmc:ContactPersonPrimary>
					<wmc:ContactPosition>developer</wmc:ContactPosition>
					<wmc:ContactAddress>
						<wmc:AddressType>postal</wmc:AddressType>
						<wmc:Address>Aennchenstr. 19</wmc:Address>
						<wmc:City>Bonn</wmc:City>
						<wmc:StateOrProvince>NRW</wmc:StateOrProvince>
						<wmc:PostCode>53177</wmc:PostCode>
						<wmc:Country>Germany</wmc:Country>
					</wmc:ContactAddress>
					<wmc:ContactVoiceTelephone>++49 228 184960</wmc:ContactVoiceTelephone>
					<wmc:ContactElectronicMailAddress>poth@lat-lon.de</wmc:ContactElectronicMailAddress>
				</wmc:ContactInformation>
				<wmc:Extension xmlns:deegree="http://www.deegree.org/context">
					<deegree:Mode>ZOOMIN</deegree:Mode>
					<deegree:MapParameter>
						<deegree:MinScale>1</deegree:MinScale>
						<deegree:MaxScale>100000</deegree:MaxScale>
					</deegree:MapParameter>
				</wmc:Extension>
			</wmc:General>
			<wmc:LayerList>
				<xsl:apply-templates select="Capability/Layer"/>
			</wmc:LayerList>
		</wmc:ViewContext>
	</xsl:template>
	<xsl:template match="Capability/Layer | Layer">
		<xsl:if test="boolean( Name )">
			<wmc:Layer hidden="0">
				<xsl:if test="./@queryable">
					<xsl:attribute name="queryable"><xsl:value-of select="./@queryable"/></xsl:attribute>
				</xsl:if>
				<wmc:Server service="OGC:WMS" version="1.1.1">
					<xsl:attribute name="title"><xsl:value-of select="$TITLE"/></xsl:attribute>
					<xsl:attribute name="version"><xsl:value-of select="$VERSION"/></xsl:attribute>
					<wmc:OnlineResource xlink:type="simple">
						<xsl:attribute name="xlink:href"><xsl:value-of select="$ONLINE"/></xsl:attribute>
					</wmc:OnlineResource>
				</wmc:Server>
				<wmc:Name>
					<xsl:value-of select="Name"/>
				</wmc:Name>
				<wmc:Title>
					<xsl:value-of select="Title"/>
				</wmc:Title>
				<xsl:for-each select="SRS">
					<wmc:SRS>
						<xsl:value-of select="."/>
					</wmc:SRS>
				</xsl:for-each>
				<wmc:FormatList>
					<wmc:Format current="1">image/jpeg</wmc:Format>
					<xsl:for-each select="/WMT_MS_Capabilities/Capability/Request/GetMap/Format">
						<xsl:if test=". != 'image/jpeg' ">
							<wmc:Format>
								<xsl:value-of select="."/>
							</wmc:Format>
						</xsl:if>
					</xsl:for-each>
				</wmc:FormatList>
				<wmc:StyleList>
					<xsl:choose>
						<xsl:when test="boolean( Style )">
							<xsl:for-each select="Style">
								<wmc:Style>
									<xsl:if test="position() = 1">
										<xsl:attribute name="current">1</xsl:attribute>
									</xsl:if>
									<wmc:Name>
										<xsl:value-of select="Name"/>
									</wmc:Name>
									<wmc:Title>
										<xsl:value-of select="Title"/>
									</wmc:Title>
								</wmc:Style>
							</xsl:for-each>
						</xsl:when>
						<xsl:otherwise>
							<wmc:Style current="1">
								<wmc:Name>default</wmc:Name>
								<wmc:Title>default</wmc:Title>
							</wmc:Style>
						</xsl:otherwise>
					</xsl:choose>
				</wmc:StyleList>
			</wmc:Layer>
		</xsl:if>
		<xsl:apply-templates select="Layer"/>
	</xsl:template>
</xsl:stylesheet>
<!-- ==================================================================================
Changes to this class. What the people have been up to:
$Log$
Revision 1.2  2006/08/06 19:53:18  poth
file header and footer added


 ====================================================================================== -->