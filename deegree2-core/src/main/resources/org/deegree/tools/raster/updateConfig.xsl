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

@version $Revision: 12869 $
@author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
@author last edited by: $Author: rbezema $

@version 1.0. $Revision: 12869 $, $Date: 2008-07-14 15:21:09 +0200 (Mo, 14 Jul 2008) $
                 
====================================================================================== -->
<xsl:stylesheet version="1.0" xmlns="http://www.opengis.net/wcs" xmlns:gml="http://www.opengis.net/gml" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:deegree="http://www.deegree.org/wcs" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:wcs="http://www.opengis.net/wcs">
	<xsl:output method="xml" indent="yes"/>
	<xsl:param name="srs" select="'EPSG:4326'"/>
	<xsl:param name="srsPre" select="'EPSG'"/>
	<xsl:param name="upperleftll" select="'x_ll1 y_ll1'"/>
	<xsl:param name="lowerrightll" select="'x_ll2 y_ll2'"/>
	<xsl:param name="upperleft" select="'x1 y1'"/>
	<xsl:param name="lowerright" select="'x2 y2'"/>
	<xsl:param name="dataDir" select=""/>
	<xsl:param name="name" select="'name'"/>
	<xsl:param name="label" select="'label'"/>
	<xsl:param name="keywords" select="'key1 key2'"/>
	<xsl:param name="description" select="'description'"/>
	<xsl:param name="resolutions" select="'resolutions'"/>
	<xsl:param name="mimeType" select="'mimeType'"/>
	<xsl:variable name="epsg">
		<srs epsg="4326" name="WGS84(DD)"/>
	</xsl:variable>
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="wcs:name">
		<xsl:element name="name">
			<xsl:value-of select="$name"/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="wcs:description">
		<xsl:element name="description">
			<xsl:value-of select="$description"/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="wcs:label">
		<xsl:element name="label">
			<xsl:value-of select="$label"/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="wcs:keywords">
		<xsl:element name="wcs:keywords">
			<xsl:call-template name="Split">
				<xsl:with-param name="strInput" select="$keywords"/>
				<xsl:with-param name="strDelimiter" select="','"/>
			</xsl:call-template>
		</xsl:element>
	</xsl:template>
	<xsl:template match="wcs:lonLatEnvelope">
		<xsl:element name="lonLatEnvelope">
			<xsl:attribute name="srsName">WGS84(DD)</xsl:attribute>
			<!-- constrained to WGS84 geographic co-ordinate system decimal deegrees -->
			<!-- xsl:value-of select="document('')/*/xsl:variable[@name='epsg']/wcs:srs[@epsg= '4326']/@name"/></xsl:attribute-->
			<xsl:element name="gml:pos">
				<xsl:attribute name="dimension">2</xsl:attribute>
				<xsl:value-of select="$upperleftll"/>
			</xsl:element>
			<xsl:element name="gml:pos">
				<xsl:attribute name="dimension">2</xsl:attribute>
				<xsl:value-of select="$lowerrightll"/>
			</xsl:element>
		</xsl:element>
	</xsl:template>
	<xsl:template match="wcs:spatialDomain">
		<xsl:element name="spatialDomain">
			<xsl:element name="gml:Envelope">
				<xsl:attribute name="srsName"><xsl:value-of select="concat('HTTP://WWW.OPENGIS.NET/GML/SRS/EPSG.XML#', $srs)"/></xsl:attribute>
				<xsl:element name="gml:pos">
					<xsl:attribute name="dimension">2</xsl:attribute>
					<xsl:value-of select="$upperleft"/>
				</xsl:element>
				<xsl:element name="gml:pos">
					<xsl:attribute name="dimension">2</xsl:attribute>
					<xsl:value-of select="$lowerright"/>
				</xsl:element>
			</xsl:element>
		</xsl:element>
	</xsl:template>
	<xsl:template name="Split">
		<!--This template will recursively break apart a comma-delimited string into child elements-->
		<xsl:param name="strInput" select="''"/>
		<xsl:param name="strDelimiter" select="','"/>
		<xsl:variable name="strNextItem" select="substring-before($strInput, $strDelimiter)"/>
		<xsl:variable name="strOutput" select="substring-after($strInput, $strDelimiter)"/>
		<xsl:variable name="strLen" select="string-length($strNextItem)"/>
		<xsl:choose>
			<xsl:when test="contains($strInput,$strDelimiter)">
				<wcs:keyword>
					<xsl:value-of select="normalize-space($strNextItem)"/>
				</wcs:keyword>
				<!-- At this point, the template will recursively call itself until the last comma is found -->
				<xsl:call-template name="Split">
					<xsl:with-param name="strInput" select="$strOutput"/>
					<xsl:with-param name="strDelimiter" select="$strDelimiter"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<!-- The otherwise clause will be reached when a comma is not located using contains() -->
				<wcs:keyword>
					<xsl:value-of select="normalize-space($strInput)"/>
				</wcs:keyword>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="wcs:requestCRSs">
		<requestCRSs>EPSG:4326 <xsl:value-of select="concat( $srsPre, $srs )"/></requestCRSs>
	</xsl:template>
	<xsl:template match="wcs:requestResponseCRSs">
		<requestResponseCRSs>
          EPSG:4326 <xsl:value-of select="concat( $srsPre, $srs )"/>
		</requestResponseCRSs>
	</xsl:template>
	<xsl:template match="wcs:nativeCRSs">
		<nativeCRSs>
			<xsl:value-of select="concat( $srsPre, $srs )"/>
		</nativeCRSs>
	</xsl:template>
	<xsl:template match="wcs:supportedFormats">
		<supportedFormats nativeFormat="{$mimeType}">
			<xsl:copy-of select="./*"/>
		</supportedFormats>
	</xsl:template>
	<!-- add extras here for each of the Deegree extensions -->
	<xsl:template match="deegree:Extension[@type='shapeIndexed']">
		<deegree:Extension type="shapeIndexed">
			<xsl:for-each select="deegree:Resolution">
				<xsl:choose>
					<xsl:when test="contains($dataDir, concat(./deegree:Shape, '/'))">
						<!-- do nothing -->
					</xsl:when>
					<xsl:otherwise>
						<xsl:copy-of select="."/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
			<xsl:call-template name="writeShpResolutions"/>
		</deegree:Extension>
	</xsl:template>
	<xsl:template name="writeShpResolutions">
		<!--This template will recursively break apart a comma-delimited string resolutions-->
		<xsl:param name="resolutions" select="$resolutions"/>
		<xsl:param name="strDelimiter" select="','"/>
		<xsl:param name="max" select="'99999999'"/>
		<xsl:param name="tileProperty" select="'FILENAME'"/>
		<xsl:param name="directoryProperty" select="'FOLDER'"/>
		<xsl:variable name="strNextItem" select="substring-before($resolutions, $strDelimiter)"/>
		<xsl:variable name="strOutput" select="substring-after($resolutions, $strDelimiter)"/>
		<xsl:choose>
			<xsl:when test="contains($resolutions,$strDelimiter)">
				<xsl:variable name="min" select="normalize-space($strNextItem)"/>
				<xsl:element name="deegree:Resolution">
					<xsl:attribute name="max"><xsl:value-of select="normalize-space($max)"/></xsl:attribute>
					<xsl:attribute name="min"><xsl:value-of select="$min"/></xsl:attribute>
					<deegree:Range>
						<deegree:Name>default</deegree:Name>
					</deegree:Range>
					<xsl:element name="deegree:Shape">
						<xsl:attribute name="tileProperty"><xsl:value-of select="$tileProperty"/></xsl:attribute>
						<xsl:attribute name="directoryProperty"><xsl:value-of select="$directoryProperty"/></xsl:attribute>
						<xsl:attribute name="srsName"><xsl:value-of select="concat( $srsPre, $srs )"/></xsl:attribute>
						<xsl:value-of select="concat($dataDir, 'sh', $min)"/>
					</xsl:element>
				</xsl:element>
				<!-- At this point, the template will recursively call itself until the last comma is found -->
				<xsl:call-template name="writeShpResolutions">
					<xsl:with-param name="resolutions" select="normalize-space($strOutput)"/>
					<xsl:with-param name="strDelimiter" select="normalize-space($strDelimiter)"/>
					<xsl:with-param name="max" select="$min"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<!-- The otherwise clause will be reached when a comma is not located using contains() -->
				<xsl:element name="deegree:Resolution">
					<xsl:variable name="min" select="normalize-space($resolutions)"/>
					<xsl:attribute name="max"><xsl:value-of select="normalize-space($max)"/></xsl:attribute>
					<xsl:attribute name="min"><!--<xsl:value-of select="$min" />--><!-- force this to be 0 so that data is always returned --><xsl:value-of select="'0.0'"/></xsl:attribute>
					<deegree:Range>
						<deegree:Name>default</deegree:Name>
					</deegree:Range>
					<xsl:element name="deegree:Shape">
						<xsl:attribute name="tileProperty"><xsl:value-of select="$tileProperty"/></xsl:attribute>
						<xsl:attribute name="directoryProperty"><xsl:value-of select="$directoryProperty"/></xsl:attribute>
						<xsl:attribute name="srsName"><xsl:value-of select="concat( $srsPre, $srs )"/></xsl:attribute>
						<xsl:value-of select="concat($dataDir, 'sh', $min)"/>
					</xsl:element>
				</xsl:element>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>
