<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of deegree, for copyright/license information, please visit http://www.deegree.org/license. -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:cntxt="http://www.opengis.net/context" xmlns:sld="http://www.opengis.net/sld" xmlns:deegree="http://www.deegree.org/context" xmlns:xlink="http://www.w3.org/1999/xlink">
    <!-- global variable/constant declarations -->
    <xsl:variable name="vMapWidth">
        <xsl:value-of select="./cntxt:ViewContext/cntxt:General/cntxt:Window/@width"/>
    </xsl:variable>
    <xsl:variable name="vMapHeight">
        <xsl:value-of select="./cntxt:ViewContext/cntxt:General/cntxt:Window/@height"/>
    </xsl:variable>
    <xsl:variable name="vMinx">
        <xsl:value-of select="./cntxt:ViewContext/cntxt:General/cntxt:BoundingBox/@minx"/>
    </xsl:variable>
    <xsl:variable name="vMiny">
        <xsl:value-of select="./cntxt:ViewContext/cntxt:General/cntxt:BoundingBox/@miny"/>
    </xsl:variable>
    <xsl:variable name="vMaxx">
        <xsl:value-of select="./cntxt:ViewContext/cntxt:General/cntxt:BoundingBox/@maxx"/>
    </xsl:variable>
    <xsl:variable name="vMaxy">
        <xsl:value-of select="./cntxt:ViewContext/cntxt:General/cntxt:BoundingBox/@maxy"/>
    </xsl:variable>
    <xsl:variable name="vCRS">
        <xsl:value-of select="./cntxt:ViewContext/cntxt:General/cntxt:BoundingBox/@SRS"/>
    </xsl:variable>
    <xsl:variable name="vNorthHeight">30</xsl:variable>
    <xsl:variable name="vSouthHeight">25</xsl:variable>
    <xsl:variable name="vWestWidth">0</xsl:variable>
    <xsl:variable name="vEastWidth">100</xsl:variable>
    <xsl:variable name="vVerticalAdjustment">230</xsl:variable>
    <xsl:variable name="vHorizontalAdjustment">50</xsl:variable>
    <!-- ==================================================================== -->
    <!-- 
        include different transformation scripts 
        - viewcontext: root-element  and creation of the HTML header section including most of the 
                                 JavaScript code (especially the Controller).
        - frontend: creates table structure for arraging footer, header, north, east, south, west and 
                          center section of the client.
       - module: fills the structure
       - init_windows: basic layout based on extJs windows 
    -->
    <xsl:include href="mappanel_windows.xsl"/>
    <xsl:include href="init_windows.xsl"/>
    <xsl:include href="viewcontext.xsl"/>
    <!-- ==================================================================== -->
    <!-- templates -->
    <!-- common java script files -->
    <xsl:template match="cntxt:General/cntxt:Extension/deegree:Frontend/deegree:CommonJS/deegree:Name">
        <SCRIPT LANGUAGE="JavaScript1.2" TYPE="text/javascript">
            <xsl:attribute name="src"><xsl:value-of select="."/></xsl:attribute>
        </SCRIPT>
    </xsl:template>
    <!-- module specific java script files -->
    <xsl:template match="cntxt:General/cntxt:Extension/deegree:Frontend/deegree:*/deegree:Module/deegree:ModuleJS">
        <SCRIPT LANGUAGE="JavaScript1.2" TYPE="text/javascript">
            <xsl:attribute name="src"><xsl:value-of select="."/></xsl:attribute>
        </SCRIPT>
    </xsl:template>
</xsl:stylesheet>
