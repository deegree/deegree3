<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:citygml="http://www.citygml.org/citygml/1/0/0" xmlns:gml="http://www.opengis.net/gml"
    xmlns:wfs="http://www.opengis.net/wfs" xmlns:app="http://www.deegree.org/app">

    <xsl:template match="wfs:FeatureCollection | citygml:CityModel | /">
        <wfs:FeatureCollection xmlns:citygml="http://www.citygml.org/citygml/1/0/0"
            xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:gml="http://www.opengis.net/gml"
            xmlns:ogc="http://www.opengis.net/ogc" xmlns:wfs="http://www.opengis.net/wfs">
            <!-- xsl:apply-templates select="./*/citygml:Building"></xsl:apply-templates-->
            <xsl:apply-templates select=".//citygml:Building"/>
        </wfs:FeatureCollection>
    </xsl:template>

    <xsl:template match="citygml:Building">
        <gml:featureMember>
            <citygml:Building>
                <xsl:if test="boolean( gml:name )">
                    <citygml:name>
                        <xsl:value-of select="gml:name"></xsl:value-of>
                    </citygml:name>
                </xsl:if>
                <xsl:if test="boolean( citygml:function )">
                    <citygml:function>
                        <xsl:value-of select="citygml:function"></xsl:value-of>
                    </citygml:function>
                </xsl:if>
                <xsl:if test="boolean( citygml:usage )">
                    <citygml:usage>
                        <xsl:value-of select="citygml:usage"></xsl:value-of>
                    </citygml:usage>
                </xsl:if>
                <xsl:if test="boolean( citygml:creationDate )">
                    <citygml:creation>
                        <xsl:value-of select="citygml:creationDate"></xsl:value-of>
                    </citygml:creation>
                </xsl:if>
                <xsl:if test="boolean( citygml:terminationDate )">
                    <citygml:deletion>
                        <xsl:value-of select="citygml:terminationDate"></xsl:value-of>
                    </citygml:deletion>
                </xsl:if>
                <xsl:if test="boolean( citygml:roofType )">
                    <citygml:roofType>
                        <xsl:value-of select="citygml:roofType"></xsl:value-of>
                    </citygml:roofType>
                </xsl:if>
                <xsl:if test="boolean( citygml:yearOfConstruction )">
                    <citygml:yearOfConstruction>
                        <xsl:value-of select="citygml:yearOfConstruction"></xsl:value-of>
                    </citygml:yearOfConstruction>
                </xsl:if>
                <xsl:if test="boolean( citygml:measuredHeight )">
                    <citygml:measuredheight>
                        <xsl:value-of select="citygml:measuredHeight"></xsl:value-of>
                    </citygml:measuredheight>
                </xsl:if>
                <xsl:if test="boolean( citygml:storeysAboveGround )">
                    <citygml:storeysaboveground>
                        <xsl:value-of select="citygml:storeysAboveGround"></xsl:value-of>
                    </citygml:storeysaboveground>
                </xsl:if>
                <xsl:if test="boolean( citygml:storeysBelowGround )">
                    <citygml:storeysbelowground>
                        <xsl:value-of select="citygml:storeysBelowGround"></xsl:value-of>
                    </citygml:storeysbelowground>
                </xsl:if>
                <xsl:if test="boolean( citygml:storeyHeightsAboveGround )">
                    <citygml:storeyheightsaboveground>
                        <xsl:value-of select="citygml:storeyHeightsAboveGround"></xsl:value-of>
                    </citygml:storeyheightsaboveground>
                </xsl:if>
                <xsl:if test="boolean( citygml:storeyHeightsBelowGround )">
                    <citygml:storeyheightsbelowground>
                        <xsl:value-of select="citygml:storeyHeightsBelowGround"></xsl:value-of>
                    </citygml:storeyheightsbelowground>
                </xsl:if>
                <app:geometry>
                    <gml:MultiSurface>
                    <!-- 
                    removed to avoid problems with citygml files having no srsName attribute for geometries
                    	<xsl:attribute name="srsName"><xsl:value-of select="//@srsName[1]"></xsl:value-of></xsl:attribute>
                     -->
                        <xsl:for-each select=".//gml:Polygon | .//gml:Surface">
                            <gml:surfaceMember>
                                <xsl:copy-of select="."></xsl:copy-of>
                            </gml:surfaceMember>
                        </xsl:for-each>
                    </gml:MultiSurface>
                </app:geometry>
            </citygml:Building>
        </gml:featureMember>
    </xsl:template>

</xsl:stylesheet>
