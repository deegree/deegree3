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

@version $Revision: 9339 $
@author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
@author last edited by: $Author: apoth $

@version 1.0. $Revision: 9339 $, $Date: 2007-12-27 13:31:52 +0100 (Do, 27 Dez 2007) $
                 
====================================================================================== -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" omit-xml-declaration="no" indent="yes" />
    
    <xsl:param name="indent-increment" select="'   '" />

    <xsl:template match="*">
        <xsl:param name="indent" select="'&#xA;'" />
        <xsl:value-of select="$indent" />
        <xsl:copy>
            <xsl:copy-of select="@*" />
            <xsl:apply-templates>
                <xsl:with-param name="indent" select="concat($indent, $indent-increment)" />
            </xsl:apply-templates>
            <xsl:if test="*">
                <xsl:value-of select="$indent" />
            </xsl:if>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="comment()|processing-instruction()">
    </xsl:template>

    <!-- WARNING: this is dangerous. Handle with care -->
    <xsl:template match="text()[normalize-space(.)='']" />

</xsl:stylesheet>
