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

@version $Revision: 9345 $
@author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
@author last edited by: $Author: apoth $

@version 1.0. $Revision: 9345 $, $Date: 2007-12-27 17:22:25 +0100 (Do, 27 Dez 2007) $
                 
====================================================================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:wcs="http://www.opengis.net/wcs" xmlns:gml="http://www.opengis.net/gml" xmlns:deegree="http://www.deegree.org">
    <xsl:variable name="vType">
        <xsl:value-of select="wcs:CoverageDescription/wcs:CoverageOffering/deegree:Extension/@type"/>
    </xsl:variable>
    <!-- ROOT element -->
    <xsl:template match="wcs:CoverageDescription">
        <CoverageDescription xmlns="http://www.opengis.net/wcs" xmlns:gml="http://www.opengis.net/gml" xmlns:deegree="http://www.deegree.org">
            <xsl:attribute name="version"><xsl:value-of select="@version"/></xsl:attribute>
            <xsl:if test="@updateSequence != ''">
                <xsl:attribute name="updateSequence"><xsl:value-of select="@updateSequence"/></xsl:attribute>
            </xsl:if>
            <xsl:apply-templates select="wcs:CoverageOffering"/>
        </CoverageDescription>
    </xsl:template>
    <!-- CoverageOffering -->
    <xsl:template match="wcs:CoverageOffering">
        <CoverageOffering>
            <xsl:copy-of select="wcs:metadataLink"/>
            <xsl:copy-of select="wcs:description"/>
            <xsl:copy-of select="wcs:name"/>
            <xsl:choose>
                <xsl:when test="wcs:label != ''">
                    <xsl:copy-of select="wcs:label"/>
                </xsl:when>
                <xsl:otherwise>
                    <label>
                        <xsl:value-of select="wcs:name"/>
                    </label>
                </xsl:otherwise>
            </xsl:choose>
        </CoverageOffering>
        <xsl:copy-of select="wcs:lonLatEnvelope"/>
        <xsl:copy-of select="wcs:keywords"/>
        <xsl:copy-of select="wcs:domainSet"/>
        <xsl:apply-templates select="wcs:rangeSet"/>
        <xsl:copy-of select="wcs:supportedCRSs"/>
        <xsl:choose>
            <xsl:when test="wcs:supportedFormats">
                <xsl:copy-of select="wcs:supportedFormats"/>
            </xsl:when>
            <xsl:otherwise>
                <supportedFormats>
                    <formats>image/jpeg</formats>
                    <formats>image/tiff</formats>
                    <formats>image/png</formats>
                    <formats>image/bmp</formats>
                </supportedFormats>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:choose>
            <xsl:when test="wcs:supportedInterpolations">
                <xsl:copy-of select="wcs:supportedInterpolations"/>
            </xsl:when>
            <xsl:otherwise>
                <supportedInterpolations default="nearest neighbor">
                    <interpolationMethod>nearest neighbor</interpolationMethod>
                </supportedInterpolations>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:copy-of select="deegree:Extension"/>
    </xsl:template>
    <!-- rangeSet -->
    <xsl:template match="wcs:rangeSet">
        <rangeSet>
            <xsl:apply-templates select="wcs:RangeSet"/>
        </rangeSet>
    </xsl:template>
    <!-- RangeSet -->
    <xsl:template match="wcs:RangeSet">
        <RangeSet>
            <xsl:copy-of select="wcs:metadataLink"/>
            <xsl:copy-of select="wcs:description"/>
            <xsl:copy-of select="wcs:name"/>
            <xsl:choose>
                <xsl:when test="wcs:label != ''">
                    <xsl:copy-of select="wcs:label"/>
                </xsl:when>
                <xsl:otherwise>
                    <label>
                        <xsl:value-of select="wcs:name"/>
                    </label>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:apply-templates select="wcs:axisDescription"/>
            <xsl:choose>
                <xsl:when test="wcs:nullValues">
                    <xsl:copy-of select="wcs:nullValues"/>
                </xsl:when>
                <xsl:otherwise>
                    <nullValues type="xs:integer" semantic="http://www.deegree.org">
                        <singleValue>-9999</singleValue>
                    </nullValues>
                </xsl:otherwise>
            </xsl:choose>
        </RangeSet>
    </xsl:template>
    <!-- axisDescription -->
    <xsl:template match="wcs:axisDescription">
        <axisDescription>
            <xsl:apply-templates select="wcs:AxisDescription"/>
        </axisDescription>
    </xsl:template>
    <!-- AxisDescription -->
    <xsl:template match="wcs:AxisDescription">
        <AxisDescription>
            <xsl:copy-of select="wcs:metadataLink"/>
            <xsl:copy-of select="wcs:description"/>
            <xsl:copy-of select="wcs:name"/>
            <xsl:choose>
                <xsl:when test="wcs:label != ''">
                    <xsl:copy-of select="wcs:label"/>
                </xsl:when>
                <xsl:otherwise>
                    <label>
                        <xsl:value-of select="wcs:name"/>
                    </label>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:copy-of select="wcs:values"/>
        </AxisDescription>
    </xsl:template>
</xsl:stylesheet>
