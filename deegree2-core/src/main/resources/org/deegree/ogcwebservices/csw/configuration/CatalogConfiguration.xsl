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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:wcs="http://www.opengis.net/wcs" xmlns:gml="http://www.opengis.net/gml" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:deegree="http://www.deegree.org">
    <!-- variables declaration -->
    <xsl:variable name="vRootDir">
        <xsl:value-of select="./wcs:WCS_Capabilities/deegree:deegreeParam/deegree:RootDirectory"/>
    </xsl:variable>
    <xsl:variable name="vDefOnlineRes">
        <xsl:value-of select="./wcs:WCS_Capabilities/deegree:deegreeParam/deegree:DefaultOnlineResource/@xlink:href"/>
    </xsl:variable>
    <!-- ROOT element -->
    <xsl:template match="wcs:WCS_Capabilities">
        <WCS_Capabilities xmlns="http://www.opengis.net/wcs" xmlns:gml="http://www.opengis.net/gml" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:deegree="http://www.deegree.org">
            <xsl:attribute name="version"><xsl:value-of select="@version"/></xsl:attribute>
            <xsl:if test="@updateSequence != ''">
                <xsl:attribute name="updateSequence"><xsl:value-of select="@updateSequence"/></xsl:attribute>
            </xsl:if>
            <xsl:apply-templates select="deegree:deegreeParam"/>
            <xsl:apply-templates select="wcs:Service"/>
            <xsl:choose>
                <xsl:when test="wcs:Capability != ''">
                    <xsl:apply-templates select="wcs:Capability"/>
                </xsl:when>
                <xsl:otherwise>
                    <!-- set default Capability section -->
                    <Capability>
                        <xsl:if test="@updateSequence != ''">
                            <xsl:attribute name="updateSequence"><xsl:value-of select="@updateSequence"/></xsl:attribute>
                        </xsl:if>
                        <xsl:attribute name="version"><xsl:value-of select="@version"/></xsl:attribute>
                        <Request>
                            <GetCapabilities>
                                <DCPType>
                                    <HTTP>
                                        <Get>
                                            <OnlineResource xlink:type="simple">
                                                <xsl:attribute name="xlink:href"><xsl:value-of select="$vDefOnlineRes"/></xsl:attribute>
                                            </OnlineResource>
                                        </Get>
                                    </HTTP>
                                </DCPType>
                            </GetCapabilities>
                            <DescribeCoverage>
                                <DCPType>
                                    <HTTP>
                                        <Get>
                                            <OnlineResource xlink:type="simple">
                                                <xsl:attribute name="xlink:href"><xsl:value-of select="$vDefOnlineRes"/></xsl:attribute>
                                            </OnlineResource>
                                        </Get>
                                    </HTTP>
                                </DCPType>
                            </DescribeCoverage>
                            <GetCoverage>
                                <DCPType>
                                    <HTTP>
                                        <Get>
                                            <OnlineResource xlink:type="simple">
                                                <xsl:attribute name="xlink:href"><xsl:value-of select="$vDefOnlineRes"/></xsl:attribute>
                                            </OnlineResource>
                                        </Get>
                                    </HTTP>
                                </DCPType>
                            </GetCoverage>
                        </Request>
                        <Exception>
                            <Format>application/vnd.ogc.se_xml</Format>
                        </Exception>
                        <VendorSpecificCapabilities/>
                    </Capability>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:choose>
                <xsl:when test="wcs:ContentMetadata">
                    <xsl:apply-templates select="wcs:ContentMetadata"/>
                </xsl:when>
                <xsl:otherwise>
                    <!-- todo -->
                </xsl:otherwise>
            </xsl:choose>
        </WCS_Capabilities>
    </xsl:template>
    <!-- global deegree specific parameters -->
    <xsl:template match="deegree:deegreeParam">
        <deegree:deegreeParam>
            <deegree:RootDirectory>
                <xsl:value-of select="$vRootDir"/>
            </deegree:RootDirectory>
        </deegree:deegreeParam>
        <deegree:DefaultOnlineResource xlink:type="simple">
            <xsl:attribute name="xlink:href"><xsl:value-of select="$vDefOnlineRes"/></xsl:attribute>
        </deegree:DefaultOnlineResource>
        <xsl:choose>
            <xsl:when test="deegree:CacheSize != ''">
                <xsl:copy-of select="deegree:CacheSize"/>
            </xsl:when>
            <xsl:otherwise>
                <deegree:CacheSize>100</deegree:CacheSize>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:choose>
            <xsl:when test="deegree:RequestTimeLimit != ''">
                <xsl:copy-of select="deegree:RequestTimeLimit"/>
            </xsl:when>
            <xsl:otherwise>
                <deegree:RequestTimeLimit>5</deegree:RequestTimeLimit>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:choose>
            <xsl:when test="deegree:DataDirectoryList != ''">
                <xsl:choose>
                    <xsl:when test="deegree:DataDirectoryList/deegree:DataDirectory != ''">
                        <xsl:copy-of select="deegree:DataDirectoryList"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <deegree:DataDirectoryList>
                            <deegree:DataDirectory>
                                <xsl:value-of select="$vRootDir"/>/WEB-INF/data</deegree:DataDirectory>
                        </deegree:DataDirectoryList>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <deegree:DataDirectoryList>
                    <deegree:DataDirectory>
                        <xsl:value-of select="$vRootDir"/>/WEB-INF/data</deegree:DataDirectory>
                </deegree:DataDirectoryList>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!-- Service Section -->
    <xsl:template match="wcs:Service">
        <Service>
            <xsl:if test="../@updateSequence != ''">
                <xsl:attribute name="updateSequence"><xsl:value-of select="../@updateSequence"/></xsl:attribute>
            </xsl:if>
            <xsl:attribute name="version"><xsl:value-of select="../@version"/></xsl:attribute>
            <xsl:copy-of select="wcs:metadataLink"/>
            <xsl:copy-of select="wcs:description"/>
            <xsl:choose>
                <xsl:when test="wcs:name != ''">
                    <xsl:copy-of select="wcs:name"/>
                </xsl:when>
                <xsl:otherwise>
                    <name>deegreewcs</name>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:choose>
                <xsl:when test="wcs:label != ''">
                    <xsl:copy-of select="wcs:label"/>
                </xsl:when>
                <xsl:otherwise>
                    <label>deegreewcs</label>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:copy-of select="wcs:keywords"/>
            <xsl:apply-templates select="wcs:responsibleParty"/>
            <xsl:choose>
                <xsl:when test="wcs:fees != ''">
                    <xsl:copy-of select="wcs:fees"/>
                </xsl:when>
                <xsl:otherwise>
                    <fees>NONE</fees>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:choose>
                <xsl:when test="wcs:accessConstraints != ''">
                    <xsl:copy-of select="wcs:accessConstraints"/>
                </xsl:when>
                <xsl:otherwise>
                    <accessConstraints>NONE</accessConstraints>
                </xsl:otherwise>
            </xsl:choose>
        </Service>
    </xsl:template>
    <!-- Service/ResponsibleParty Section -->
    <xsl:template match="wcs:responsibleParty">
        <responsibleParty>
            <xsl:choose>
                <xsl:when test="wcs:individualName != ''">
                    <xsl:copy-of select="wcs:individualName"/>
                </xsl:when>
                <xsl:otherwise>
                    <individualName>deegree</individualName>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:copy-of select="wcs:organisationName"/>
            <xsl:copy-of select="wcs:positionName"/>
            <xsl:copy-of select="wcs:contactInfo"/>
        </responsibleParty>
    </xsl:template>
    <!-- Capability Section -->
    <xsl:template match="wcs:Capability">
        <Capability>
            <xsl:if test="../@updateSequence != ''">
                <xsl:attribute name="updateSequence"><xsl:value-of select="../@updateSequence"/></xsl:attribute>
            </xsl:if>
            <xsl:attribute name="version"><xsl:value-of select="../@version"/></xsl:attribute>
            <Request>
                <xsl:choose>
                    <xsl:when test="wcs:Request/wcs:GetCapabilities/wcs:DCPType/wcs:HTTP/wcs:Get/wcs:OnlineResource">
                        <xsl:copy-of select="wcs:Request/wcs:GetCapabilities"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <GetCapabilities>
                            <DCPType>
                                <HTTP>
                                    <Get>
                                        <OnlineResource xlink:type="simple">
                                            <xsl:attribute name="xlink:href"><xsl:value-of select="$vDefOnlineRes"/></xsl:attribute>
                                        </OnlineResource>
                                    </Get>
                                    <xsl:if test="wcs:Request/wcs:GetCapabilities/wcs:DCPType/wcs:HTTP/wcs:Post/wcs:OnlineResource">
                                        <xsl:copy-of select="wcs:Request/wcs:GetCapabilities/wcs:DCPType/wcs:HTTP/wcs:Post"/>
                                    </xsl:if>
                                </HTTP>
                            </DCPType>
                        </GetCapabilities>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:choose>
                    <xsl:when test="wcs:Request/wcs:DescribeCoverage/wcs:DCPType/wcs:HTTP/wcs:Get/wcs:OnlineResource">
                        <xsl:copy-of select="wcs:Request/wcs:DescribeCoverage"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <DescribeCoverage>
                            <DCPType>
                                <HTTP>
                                    <Get>
                                        <OnlineResource xlink:type="simple">
                                            <xsl:attribute name="xlink:href"><xsl:value-of select="$vDefOnlineRes"/></xsl:attribute>
                                        </OnlineResource>
                                    </Get>
                                    <xsl:if test="wcs:Request/wcs:DescribeCoverage/wcs:DCPType/wcs:HTTP/wcs:Post/wcs:OnlineResource">
                                        <xsl:copy-of select="wcs:Request/wcs:DescribeCoverage/wcs:DCPType/wcs:HTTP/wcs:Post"/>
                                    </xsl:if>
                                </HTTP>
                            </DCPType>
                        </DescribeCoverage>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:choose>
                    <xsl:when test="wcs:Request/wcs:GetCoverage/wcs:DCPType/wcs:HTTP/wcs:Get/wcs:OnlineResource">
                        <xsl:copy-of select="wcs:Request/wcs:GetCoverage"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <GetCoverage>
                            <DCPType>
                                <HTTP>
                                    <Get>
                                        <OnlineResource xlink:type="simple">
                                            <xsl:attribute name="xlink:href"><xsl:value-of select="$vDefOnlineRes"/></xsl:attribute>
                                        </OnlineResource>
                                    </Get>
                                    <xsl:if test="wcs:Request/wcs:GetCoverage/wcs:DCPType/wcs:HTTP/wcs:Post/wcs:OnlineResource">
                                        <xsl:copy-of select="wcs:Request/wcs:GetCoverage/wcs:DCPType/wcs:HTTP/wcs:Post"/>
                                    </xsl:if>
                                </HTTP>
                            </DCPType>
                        </GetCoverage>
                    </xsl:otherwise>
                </xsl:choose>
            </Request>
            <xsl:choose>
                <xsl:when test="wcs:Exception">
                    <xsl:copy-of select="wcs:Exception"/>
                </xsl:when>
                <xsl:otherwise>
                    <Exception>
                        <Format>application/vnd.ogc.se_xml</Format>
                    </Exception>
                </xsl:otherwise>
            </xsl:choose>
        </Capability>
    </xsl:template>
    <!-- contentmetadata section -->
    <xsl:template match="wcs:ContentMetadata">
        <ContentMetadata>
            <xsl:if test="../@updateSequence != ''">
                <xsl:attribute name="updateSequence"><xsl:value-of select="../@updateSequence"/></xsl:attribute>
            </xsl:if>
            <xsl:attribute name="version"><xsl:value-of select="../@version"/></xsl:attribute>
            <!-- first aply all defined CoverageOfferings -->
            <xsl:apply-templates select="wcs:CoverageOfferingBrief"/>
            <!-- then parse the registered data directories -->
            <xsl:choose>
                <xsl:when test="../deegree:deegreeParam/deegree:DataDirectoryList/deegree:DataDirectory">
                    <!-- parse DataDirectory list for not registered coverages -->
                    <xsl:for-each select="../deegree:deegreeParam/deegree:DataDirectoryList/deegree:DataDirectory">
                        <uzu/>
                    </xsl:for-each>
                </xsl:when>
                <xsl:otherwise>
                    <!-- have a look to the default data directory -->
                </xsl:otherwise>
            </xsl:choose>
        </ContentMetadata>
    </xsl:template>
    <xsl:template match="wcs:CoverageOfferingBrief">
        <xsl:copy-of select="."/>
    </xsl:template>
</xsl:stylesheet>
