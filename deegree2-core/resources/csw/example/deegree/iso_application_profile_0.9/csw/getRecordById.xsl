<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:gml="http://www.opengis.net/gml" xmlns:ogc="http://www.opengis.net/ogc" xmlns:csw="http://www.opengis.net/cat/csw" xmlns:wfs="http://www.opengis.net/wfs" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
    <xsl:template match="csw:GetRecordById">
        <!-- will be used for GetRecordById requests -->
        <wfs:GetFeature maxFeatures="-1" outputFormat="text/xml; subtype=gml/3.1.1" xmlns:gml="http://www.opengis.net/gml" xmlns:app="http://www.deegree.org/app">
            <wfs:Query>
                <xsl:attribute name="typeName">app:MD_Metadata</xsl:attribute>
                <ogc:Filter>
                    <xsl:choose>
                        <xsl:when test="count(./csw:Id) > 1">
                            <ogc:Or>
                                <xsl:for-each select="./csw:Id">
                                    <ogc:PropertyIsEqualTo>
                                        <ogc:PropertyName>app:fileidentifier</ogc:PropertyName>
                                        <ogc:Literal>
                                            <xsl:value-of select="."/>
                                        </ogc:Literal>
                                    </ogc:PropertyIsEqualTo>
                                </xsl:for-each>
                            </ogc:Or>
                        </xsl:when>
                        <xsl:otherwise>
                            <ogc:PropertyIsEqualTo>
                                <ogc:PropertyName>app:fileidentifier</ogc:PropertyName>
                                <ogc:Literal>
                                    <xsl:value-of select="./csw:Id"/>
                                </ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                        </xsl:otherwise>
                    </xsl:choose>
                </ogc:Filter>
            </wfs:Query>
        </wfs:GetFeature>
    </xsl:template>

</xsl:stylesheet>
