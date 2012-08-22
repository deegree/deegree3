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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.opengis.net/wcs" xmlns:wcs="http://www.opengis.net/wcs" xmlns:gml="http://www.opengis.net/gml" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:deegree="http://www.deegree.org/wcs">
	<xsl:output method="xml" indent="yes"/>
	<xsl:param name="dataDirectory" select="'data_dir_here'"/>
	<xsl:param name="configFile" select="'file:///configuration.xml'"/>
	<xsl:param name="name" select="'name_here'"/>
	<xsl:param name="label" select="'label_here'"/>
	<xsl:param name="upperleftll" select="'x_ll1, y_ll1'"/>
	<xsl:param name="lowerrightll" select="'x_ll2, y_ll2'"/>
	<xsl:param name="keywords" select="'key1, key2'"/>
	<xsl:param name="description" select="'description'"/>
	<xsl:variable name="epsg">
		<srs epsg="4326" name="WGS84(DD)"/>
	</xsl:variable>
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="deegree:DataDirectoryList">
		<deegree:DataDirectoryList>
			<xsl:for-each select="deegree:DataDirectory">
				<xsl:choose>
					<xsl:when test=". = $dataDirectory">
						<!-- do nothing -->
					</xsl:when>
					<xsl:otherwise>
						<xsl:copy-of select="."/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
			<xsl:element name="deegree:DataDirectory">
				<xsl:value-of select="$dataDirectory"/>
			</xsl:element>
		</deegree:DataDirectoryList>
	</xsl:template>
	<xsl:template match="wcs:ContentMetadata">
		<xsl:element name="wcs:ContentMetadata">
			<xsl:for-each select="wcs:CoverageOfferingBrief">
				<xsl:choose>
					<xsl:when test="./wcs:name = $name">
						<!-- do nothing -->
					</xsl:when>
					<xsl:otherwise>
						<xsl:copy-of select="."/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
			<!-- update the current settings for lonLatEnvelope -->
			<CoverageOfferingBrief gml:id="ID000002">
				<description>
					<xsl:value-of select="$description"/>
				</description>
				<name>
					<xsl:value-of select="$name"/>
				</name>
				<label>
					<xsl:value-of select="./wcs:label"/>
				</label>
				<xsl:element name="lonLatEnvelope">
					<xsl:attribute name="srsName">WGS84(DD)</xsl:attribute>
					<xsl:element name="gml:pos">
						<xsl:attribute name="dimension">2</xsl:attribute>
						<xsl:value-of select="$upperleftll"/>
					</xsl:element>
					<xsl:element name="gml:pos">
						<xsl:attribute name="dimension">2</xsl:attribute>
						<xsl:value-of select="$lowerrightll"/>
					</xsl:element>
				</xsl:element>
				<xsl:element name="wcs:keywords">
					<xsl:call-template name="Split">
						<xsl:with-param name="strInput" select="$keywords"/>
						<xsl:with-param name="strDelimiter" select="','"/>
					</xsl:call-template>
				</xsl:element>
				<xsl:element name="deegree:Configuration"><xsl:value-of select="$configFile"/></xsl:element>
			</CoverageOfferingBrief>
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
</xsl:stylesheet>
