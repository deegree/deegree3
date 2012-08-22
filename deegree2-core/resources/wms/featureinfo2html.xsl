<?xml version="1.0" encoding="UTF-8"?>
<!--
    Default conversion script for transforming the GML response to a GetFeatureInfo
    request into a HTML-format.
    
    author: Andreas Poth
	last edited by: $Author: hrubach $
	version: $Revision: 19663 $, $Date: 2009-09-16 09:54:17 +0200 (Mi, 16 Sep 2009) $
    
    (c) deegree - LGPL
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ll="http://www.lat-lon.de" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:gml="http://www.opengis.net/gml">
<xsl:output method="html" omit-xml-declaration="yes" />
    <xsl:template match="ServiceExceptionReport">
        <html>
            <meta content=""/>
            <body bgcolor="#2E3363">
                <table align="center" bgcolor="#e0e9f9" border="0" cellspacing="10">
                    <tr>
                        <td>
                            <h2>
                                <font face="Helvetica, Arial, sans-serif">iGeoPortal FeatureInfo</font>
                            </h2>
                            <hr/>
                            <font face="Helvetica, Arial, sans-serif" size="-1">(c) deegree WMS 1.1.1
                                <script LANGUAGE="JavaScript">
                                    var now = new Date();
                                    document.write( now );
                                </script>
                            </font>
                        </td>
                    </tr>
                </table>
            </body>
        </html>
    </xsl:template>
    <xsl:template match="ll:FeatureCollection">
        <html>
            <meta content=""/>
            <body bgcolor="#2E3363">
                <table align="center" bgcolor="#e0e9f9" border="0" cellspacing="10">
                    <tr>
                        <td>
                            <h2>
                                <font face="Helvetica, Arial, sans-serif">FeatureInfo</font>
                            </h2>
                            <table border="0" width="500" cellpadding="0" cellspacing="0">
                                <xsl:apply-templates select="gml:featureMember"/>
                            </table>
                            <hr/>
                            <font face="Helvetica, Arial, sans-serif" size="-1">(c) deegree WMS 1.1.1
                                <script LANGUAGE="JavaScript">
                                    var now = new Date();
                                    document.write( now );
                                </script>
                            </font>
                        </td>
                    </tr>
                </table>
            </body>
        </html>
    </xsl:template>
    <xsl:template match="gml:featureMember">
        <tr>
            <xsl:if test="position() mod 2 > 0">
                <td bgcolor="#A0A5C5" width="30"/>
                <td bgcolor="#A0A5C5">
                    <br/>
                    <xsl:for-each select="./child::*">
                        <font face="Helvetica, Arial, sans-serif">
                            <h4><xsl:value-of select="local-name(.)"/></h4>
                            <!-- 
                            <xsl:for-each select="./child::*">
                                <xsl:value-of select="local-name( . )"/> = <xsl:value-of select="."/>
                                <br/>
                            </xsl:for-each>
                            -->
                             <xsl:for-each select="./child::*">
                                <xsl:variable name="VAL">
                                    <xsl:value-of select="."/>
                                </xsl:variable>
                                <xsl:choose>
                                    <xsl:when test="not($VAL = 'null')">
                                        <xsl:choose>
                                            <xsl:when test="starts-with( $VAL, 'http://') ">
                                                <xsl:value-of select="local-name( . )"/> =
                                                <a>
                                                <xsl:attribute name="href"><xsl:value-of select="$VAL"/></xsl:attribute>
                                                <xsl:value-of select="$VAL"/>
                                                </a>
                                            </xsl:when>
                                            <xsl:otherwise>
                                              <xsl:value-of select="local-name( . )"/> = <xsl:value-of select="$VAL"/>
                                              </xsl:otherwise>
                                       </xsl:choose>
                                   <br/>
                                   </xsl:when>
                                   <xsl:otherwise>
                                   </xsl:otherwise>
                              </xsl:choose>
                          </xsl:for-each>
                        </font>
                    </xsl:for-each>
                    <br/>
                </td>
            </xsl:if>
            <xsl:if test="position() mod 2 = 0">
                <td bgcolor="#FFFFFF" width="30"/>
                <td bgcolor="#FFFFFF">
                    <br/>
                    <xsl:for-each select="./child::*">
                        <font face="Helvetica, Arial, sans-serif">
                            <h4><xsl:value-of select="local-name(.)"/></h4>
                            <!-- 
                            <xsl:for-each select="./child::*">
                                <xsl:value-of select="local-name( . )"/> = <xsl:value-of select="."/>
                                <br/>
                            </xsl:for-each>
                            -->
                            <xsl:for-each select="./child::*">
                                <xsl:variable name="VAL">
                                    <xsl:value-of select="."/>
                                </xsl:variable>
                                <xsl:choose>
                                    <xsl:when test="not($VAL = 'null')">
                                        <xsl:choose>
                                            <xsl:when test="starts-with( $VAL, 'http://') ">
                                                <xsl:value-of select="local-name( . )"/> =
                                                <a>
                                                <xsl:attribute name="href"><xsl:value-of select="$VAL"/></xsl:attribute>
                                                <xsl:value-of select="$VAL"/>
                                                </a>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="local-name( . )"/> = <xsl:value-of select="$VAL"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                        <br/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:for-each>
                        </font>
                    </xsl:for-each>
                    <br/>
                </td>
            </xsl:if>
        </tr>
    </xsl:template>
</xsl:stylesheet>
