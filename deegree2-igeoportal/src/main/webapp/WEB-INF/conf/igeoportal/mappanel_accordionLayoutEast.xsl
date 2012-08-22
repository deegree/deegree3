<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:cntxt="http://www.opengis.net/context" xmlns:sld="http://www.opengis.net/sld" xmlns:deegree="http://www.deegree.org/context" xmlns:xlink="http://www.w3.org/1999/xlink">

    <xsl:template name="MAPBORDER">
        layout: 'border',
        border: false,
        items: [                
            {
                region: 'north',
                margins: '0 5 5 5',
                height: <xsl:value-of select="$vNorthHeight"/>, 
                items: [ {
                        contentEl: 'menubar',
                        border: false
                    }]
            },
            {
                region: 'center',
                margins: '0 0 0 5',
                listeners: {
                    resize: {
                        fn: function(el, width, height) {                   
                            if ( controller != null ) { 
                                // do NOT change whitespaces in the following line
                                var t = new Ext.util.DelayedTask( controller.setMapSize,  controller, [width-2, height-30] );
                                t.delay( 500 );
                            }
                        }
                    }                       
                },
                items: [
                    <xsl:for-each select="//deegree:Center/deegree:Module">
                        <xsl:if test="./@hidden != 'true' and ./@overlay != 'true' ">
                            {
                            html: '<xsl:call-template name="CENTER"/>',
                            border: false
                            }
                            <xsl:if test="position() != count(//deegree:Center/deegree:Module[./@hidden != 'true' and ./@overlay != 'true'])">
                            ,
                            </xsl:if>                                               
                        </xsl:if>
                    </xsl:for-each>                                     
                ]
            },
            {
                region: 'south',
                layout: 'table',
                layoutConfig: {rows:1},
                margins: '5 5 5 5',
                height: <xsl:value-of select="$vSouthHeight"/>, 
                items: [
                    <xsl:for-each select="//deegree:South/deegree:Module">
                        <xsl:if test="./@hidden != 'true' and ./@overlay != 'true' ">
                            {
                            html: '<xsl:call-template name="SOUTH"/>',
                            //title: '<xsl:value-of select="deegree:Title"/>',
                            border: false
                            }
                            <xsl:if test="position() != count(//deegree:South/deegree:Module[./@hidden != 'true' and ./@overlay != 'true'])">
                            ,
                            </xsl:if>                                               
                        </xsl:if>
                    </xsl:for-each>
                ]
            },
             {
                region: 'east',
                title: 'East Side',
                collapsible: true,
                width: <xsl:value-of select="$vEastWidth"/>,
                margins: '0 5 0 5',
                layout: {
                    type: 'accordion',
                    fill: true,
                    animate: false
                },
                items:  [
                    /*{
                            html: '<img border="0" src="./images/portal_saltlake.jpg"></img> iGeoPortal hgjhg kjhg jhg jhg jhgjhgjhgjh kjhgj gjhg jkhgj hgjhg jhgjh gjhgjh gjhgjhg jkhg jhg jhg jkhgjh gjkhg jhgjkhg jkhgjkh gjkhg jhg jkgh jkhg jhg jg jkhg jhg jhg jhg jkg jhg jhg jkgjkkkkkkkkkkkkkkkkkk',
                            title: 'hallo',
                            border: false
                    },*/
                    <xsl:for-each select="//deegree:East/deegree:Module">
                        <xsl:if test="./@hidden != 'true' and ./@overlay != 'true' ">
                            {
                            html: '<xsl:call-template name="EAST"/>',
                            title: '<xsl:value-of select="deegree:Title"/>',
                            // autoscroll: true,
                            border: false,
                            listeners: {
                                    collapse: {
                                        fn: function(el, width, height) {
                                           if ( controller &amp;&amp; controller.vMeasurement ) {
                                             if ( '<xsl:value-of select="deegree:Name"/>' == 'Measurement' ) { 
                                                controller.vMeasurement.kill();
                                             }
                                           }
                                        }
                                    }
                               }
                            }
                            <xsl:if test="position() != count(//deegree:East/deegree:Module[./@hidden != 'true' and ./@overlay != 'true'])">
                            ,
                            </xsl:if>                                               
                        </xsl:if>
                    </xsl:for-each>                                     
                ]
            } 
        ]       
    </xsl:template>
    <xsl:template name="WINDOW">
        <iframe frameborder="0" height="600" marginheight="0" marginwidth="0" scrolling="no" width="350">
            <xsl:attribute name="id"><xsl:value-of select="concat('ID',./deegree:Name)"/></xsl:attribute>
            <xsl:attribute name="name"><xsl:value-of select="concat('ID',./deegree:Name)"/></xsl:attribute>
            <xsl:attribute name="src"><xsl:value-of select="./deegree:Content"/></xsl:attribute>
        </iframe>
    </xsl:template>    
    <xsl:template name="CENTER">
        <iframe frameborder="0" marginheight="0" marginwidth="0" >
            <xsl:attribute name="id"><xsl:value-of select="concat('ID',./deegree:Name)"/></xsl:attribute>
            <xsl:attribute name="name"><xsl:value-of select="concat('ID',./deegree:Name)"/></xsl:attribute>
            <xsl:attribute name="src"><xsl:value-of select="./deegree:Content"/></xsl:attribute>
            <xsl:choose>
                <xsl:when test="./@scrolling != ''">
                    <xsl:attribute name="scrolling"><xsl:value-of select="./@scrolling"/></xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="scrolling">auto</xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:choose>            
                <xsl:when test="./@width">
                    <xsl:attribute name="width"><xsl:value-of select="./@width"/></xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="width"><xsl:value-of select="$vMapWidth"/></xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:choose>
                <xsl:when test="./@height">
                    <xsl:attribute name="height"><xsl:value-of select="./@height"/></xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="height"><xsl:value-of select="$vMapHeight"/></xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>
        </iframe>
    </xsl:template>
    <xsl:template name="EAST">
        <iframe frameborder="0" marginheight="0" marginwidth="0" width="250">
            <xsl:attribute name="id"><xsl:value-of select="concat('ID',./deegree:Name)"/></xsl:attribute>
            <xsl:attribute name="name"><xsl:value-of select="concat('ID',./deegree:Name)"/></xsl:attribute>
            <xsl:attribute name="src"><xsl:value-of select="./deegree:Content"/></xsl:attribute>
            <xsl:choose>
                <xsl:when test="./@scrolling != ''">
                    <xsl:attribute name="scrolling"><xsl:value-of select="./@scrolling"/></xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="scrolling">auto</xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:choose>
                <xsl:when test="../@width">
                    <xsl:attribute name="width"><xsl:value-of select="../@width"/></xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="width"><xsl:value-of select="$vEastWidth"/></xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:if test="./@height">
                <xsl:attribute name="height"><xsl:value-of select="./@height"/></xsl:attribute>
            </xsl:if>
        </iframe>
    </xsl:template> 
    <xsl:template name="SOUTH">
        <iframe frameborder="0" marginheight="0" marginwidth="0">
            <xsl:attribute name="id"><xsl:value-of select="concat('ID',./deegree:Name)"/></xsl:attribute>
            <xsl:attribute name="name"><xsl:value-of select="concat('ID',./deegree:Name)"/></xsl:attribute>
            <xsl:attribute name="src"><xsl:value-of select="./deegree:Content"/></xsl:attribute>
            <xsl:choose>
                <xsl:when test="./@scrolling != ''">
                    <xsl:attribute name="scrolling"><xsl:value-of select="./@scrolling"/></xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="scrolling">auto</xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:choose>
                <xsl:when test="./@height">
                    <xsl:attribute name="height"><xsl:value-of select="./@height"/></xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="height"><xsl:value-of select="$vSouthHeight"/></xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:if test="./@width">
                <xsl:attribute name="width"><xsl:value-of select="./@width"/></xsl:attribute>
            </xsl:if>            
        </iframe>
    </xsl:template>
</xsl:stylesheet>
