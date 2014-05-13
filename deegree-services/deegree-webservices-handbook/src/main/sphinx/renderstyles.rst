.. _anchor-configuration-renderstyles:

==========
Map styles
==========

Style resources are used to obtain information on how to render geo objects (mostly features, but also coverages) into maps. The most common use case is to reference them from a layer configuration, in order to describe how the layer is to be rendered. This chapter assumes the reader is familiar with basic SLD/SE terms. The style configurations do not depend on any other resource.

In contrast to other deegree configurations the style configurations do not have a custom format. You can use standard SLD or SE documents (1.0.0 and 1.1.0 are supported), with a couple of deegree specific extensions, which are described below. Please refer to the SLD_ and SE_ specifications for reference. Additionally this page contains specific examples below.

.. _SLD: http://www.opengeospatial.org/standards/sld
.. _SE: http://www.opengeospatial.org/standards/se

In deegree terms, each SLD or SE file will create a *style store*. In case of an SE file (usually beginning at the FeatureTypeStyle or CoverageStyle level) the style store only contains one style, in case of an SLD file the style store may contain multiple styles, each identified by the layer (only NamedLayers make sense here) and the name of the style (only UserStyles make sense) when referenced later.

.. figure:: images/workspace-overview-style.png
   :figwidth: 80%
   :width: 80%
   :target: _images/workspace-overview-style.png

   Style resources define how geo objects are rendered

.. tip::
  When defining styles, take note of the log file. Upon startup the log will warn you about potential problems or errors during parsing, and upon rendering warnings will be emitted when rendering is unsuccessful eg. because you had a typo in a geometry property name. When you're seeing an empty map when expecting a fancy one, check the log before reporting a bug. deegree will tolerate a lot of syntactical errors in your style files, but you're more likely to get a good result when your files validate and you have no warnings in the log.

^^^^^^^^
Overview
^^^^^^^^

From the point of view of the Symbology Encoding Standard, there are 5 kinds of symbolizations, which can be present in a map image:
 * **Point symbolizations**
 * **Line symbolizations**
 * **Polygon symbolizations**
 * **Text symbolizations**
 * **Raster symbolizations**

The first 4 symbolizations usually represent vector feature objects. Raster symbolization is used to visualize raster data. This documentation chapter describes, how those
symbolizations can be realized using OGC symbology encoding. It will lead from the underlying basics to some more complex constructions for map visulization.
 
^^^^^^
Basics
^^^^^^

________________
General Layout
________________

The general structure of an SE-Style contains:

.. code-block:: xml

    <FeatureTypeStyle>
    <FeatureTypeName> 
    <Rule> 

It is constructed like this:

.. code-block:: xml

    <FeatureTypeStyle xmlns="http://www.opengis.net/se" xmlns:ogc="http://www.opengis.net/ogc" xmlns:sed="http://www.deegree.org/se" xmlns:deegreeogc="http://www.deegree.org/ogc" xmlns:plan="http://www.deegree.org/plan" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.opengis.net/se http://schemas.opengis.net/se/1.1.0/FeatureStyle.xsd http://www.deegree.org/se http://schemas.deegree.org/se/1.1.0/Symbolizer-deegree.xsd">
        <FeatureTypeName>plan:yourFeatureType</FeatureTypeName>
        <Rule>
            ...
        </Rule>
    </FeatureTypeStyle>

.. Tip:: Before you start, always remember that every style is read top-down. So be aware the second <Rule> will overpaint the first one, the third overpaints the second and so on

___________________
Symbolization Rules
___________________

Every specific map visualization needs its own symbolization rule. Rules are defined within the **<Rule>** element. Each rule can consist of at least one symbolizer.
Every rule has its own name and description elements. The description elements are used to create the legend caption from it.

Depending on the type of symbolization to create, one of the following symbolizers can be used:

 * <PointSymbolizer>
 * <LineSymbolizer>
 * <PolygonSymbolizer>
 * <TextSymbolizer>
 * <RasterSymbolizer>

Symbolizers can have an uom-attribute (units of measure), which determines the unit of all values set inside the Symbolizer. The following values for UoM are supported within deegree:

 * uom="pixel"
 * uom="meter"
 * uom="mm"

The default value is "pixel".

Within every symbolizer (except rastersymbolizers), a geometry property used for the rendering, can be specified with the **<Geometry>** element.
If there is no geometry specified the first geometry property of the FeatureType will be used.

Each of the (Vector-)Symbolizer-elements has its dimensions, which are described in more detail below:

 * **<LineSymbolizer>** has only one dimension: the <Stroke>-element (to style the stroke).
 * **<PolygonSymbolizer>** has two dimensions: the <Stroke> (to sytle the stroke of the polygon) and the <Fill>-element (to style the inside of the polygon).
 * **<PointSymbolizer>** can also contain both dimensions: the <Stroke> (to style the stroke of the point) and the <Fill>-element (to style the inside of the point).
 * **<TextSymbolizer>** has three dimensions: the <Label> (to set the property, which is to be styled), the <Font> (to style the font) and the <Fill>-element (to style the inside of the font).

------
Stroke
------

To describe a <Stroke>, a number of different <SvgParameter> can be used.

 * name="stroke" ==> The stroke (color) is defined by the hex color code (e.g. black ==> #000000).
 * name="opacity" ==> Opacity can be set by a percentage number, written as decimal (e.g. 0,25 ==> 25% opacity).
 * name="with" ==> Wide or thin, set your stroke-width however you want.
 * name="linecap" ==> For linecap (ending) a stroke you can choose the following types: round, edged, square, butt.
 * name="linejoin" ==> Also there are different types of linejoin possibilities: round, mitre, bevel.
 * name="dasharray" ==> The dasharray defines where the stroke is painted and where not (e.g. "1 1" ==> - - - ).

.. code-block:: xml

    <LineSymbolizer uom="meter">
      <Geometry>
        <ogc:PropertyName>layer:position</ogc:PropertyName>
      </Geometry>
      <Stroke>
        <SvgParameter name="stroke">#000000</SvgParameter>
        <SvgParameter name="stroke-opacity">0.5</SvgParameter>
        <SvgParameter name="stroke-width">1</SvgParameter>
        <SvgParameter name="stroke-linecap">round</SvgParameter>
        <SvgParameter name="stroke-linejoin">round</SvgParameter>
        <SvgParameter name="stroke-dasharray">1 1</SvgParameter>
      </Stroke>
    </LineSymbolizer>

----
Fill
----

For the visualization of polygons, points and texts, the <Fill> element can be used additional to styling the <Stroke>. You can set the following <SvgParameter>:

 * name="fill" (color)
 * name="fill-opacity"

These two <SvgParameter> are working like those from <Stroke>.

.. code-block:: xml

    <PolygonSymbolizer uom="meter">
      <Geometry>
        <...>
      </Geometry>
      <Fill>
        <SvgParameter name="fill">#000000</SvgParameter>
        <SvgParameter name="fill-opacity">0.5</SvgParameter>
      </Fill>
      <Stroke>
        <...>
      </Stroke>
    </PolygonSymbolizer>

----
Font
----

For the creation of a <TextSymbolizer>, certain parameters for the displayed text have to be set.
Every <TextSymbolizer> needs a <Label> to be specified.
The <Font> to be used for the text symbolization can be set with <SvgParameter> elements. These are the possible <SvgParameter>:

 * name="font-family" ==> Possible types are: e.g. Arial, Times Roman, Sans-Serif
 * name="font-weight" ==> Possible types are: normal, bold, bolder, lighter
 * name="font-size"

With a <Fill>-element a color and opacity of the font can be defined. This method is used to show text which is stored in your database.

.. code-block:: xml

    <TextSymbolizer uom="meter">
      <Geometry>
        <...>
      </Geometry>
      <Label>
        <ogc:PropertyName>layer:displayedProperty</ogc:PropertyName>
      </Label>
      <Font>
        <SvgParameter name="font-family">Arial</SvgParameter>
        <SvgParameter name="font-family">Sans-Serif</SvgParameter>
        <SvgParameter name="font-weight">bold</SvgParameter>
        <SvgParameter name="font-size">3</SvgParameter>
      </Font>
      <Fill>
        <...>
      </Fill>
    </TextSymbolizer>

______________________
Advanced symbolization
______________________
 
There are numerous possibilities for advanced symbolization. This chapter describes the basic components of advanced map stylings using symbology encoding.

--------------
Using Graphics
--------------

There are different ways to use graphical symbols as a base for map symbolizations. <Mark> elements can be used to specify well known graphics, <ExternalGraphic> elements can be used to have
external graphic files as a base for a symbolization rule.

**Mark**

With Marks it is possible to use wellkown objects for symboliation as well as user-generated content like SVGs. 
It is possible to use all of these for <PointSymbolizer>, <LineSymbolizer> and <PolygonSymbolizer>. 

For a <PointSymbolizer> the use of a Mark looks like the following:

.. code-block:: xml

    <PointSymbolizer uom="meter">
      <Geometry>
        ...
      </Geometry>
      <Graphic>
        <Mark>
          ...

For <LineSymbolizer> and <PolygonSymbolizer> it works like this:

.. code-block:: xml

    <Geometry>
      ...
    </Geometry>
    <Stroke>
      <GraphicStroke>
        <Graphic>
          <Mark>
            ...
 
The following wellknown objects can be used within Marks:
 * circle
 * triangle
 * star
 * square
 * x ==> creates a cross

.. code-block:: xml

    <Mark>
      <WellKnownName>triangle</WellKnownName>
      <Fill>
        ...
      </Fill>
    </Mark>

Including an SVG graphic within a mark might look like this:

.. code-block:: xml

    <Mark>
      <OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple"
        xlink:href="/filepath/symbol.svg" />
      <Format>svg</Format>
      <Fill>
        ...
      </Fill>
      <Stroke>
        ...
      </Stroke>
    </Mark>

**ExternalGraphic**

<ExternalGraphic>-elements can be used to embed graphics, taken from a graphic-file (e.g. SVGs or PNGs). The <OnlineResource> sub-element gives the URL of the graphic-file.

.. tip:: Make sure you don't forget the MIME-type in the <Format>-sub-element (e.g. "image/svg" or "image/png").

.. code-block:: xml

    <Graphic>
      <ExternalGraphic>
        <OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink"
          xlink:type="simple" xlink:href="/filepath/symbol.svg" />
        <Format>image/svg</Format>
      </ExternalGraphic>
     <Size>10</Size>
      ...
    </Graphic>

----
Size
----

Of course everything has its own <Size>. 
The size is defined directly after <Mark> or <ExternalGraphic>.

.. code-block:: xml

    <Mark>
      <WellKnownName>triangle</WellKnownName>
      <Fill>
        <SvgParameter name="fill">#000000</SvgParameter>
      </Fill>
    </Mark>
    <Size>3</Size>

---
Gap
---
It is possible to define Gaps for graphics within <LineSymbolizer> or <PolygonSymbolizer>. For this the <Gap>-element can be used like this:

.. code-block:: xml

    <GraphicStroke>
      <Graphic>
        <Mark>
          ...
        </Mark>
        ...
      </Graphic>
      <Gap>20</Gap>
    </GraphicStroke>l

--------
Rotation
--------

Symbology Encoding enables the possibility to rotate every graphic around its center with the <Rotation>-element. This goes from zero to 360 degrees. The rotation is clockwise unless it's negative, then it's counter-clockwise.

.. code-block:: xml

    <Graphic>
      <Mark>
        ...
      </Mark>
      <Size>3</Size>
      <Rotation>180</Rotation>
    </Graphic>

------------
Displacement
------------

The <Displacement>-element allows to paint a graphic displaced from his given position. Negative and positive values are possible. THe displacement must be set via the X and Y displacement elements.

.. code-block:: xml

    <Graphic>
      <Mark>
        ...
      </Mark>
      ...
      <Displacement>
        <DisplacementX>5</DisplacementX>
        <DisplacementY>5</DisplacementY>
      </Displacement>
    </Graphic>

----
Halo
----

A nice possibility to highlight your font, is the <Halo>-element. 
The <Radius>-sub-element defines the size of the border.

.. code-block:: xml

    <TextSymbolizer uom="meter">
        <Geometry>
            <ogc:PropertyName>xplan:position</ogc:PropertyName>
        </Geometry>
        <Label>
            ...
        </Label>
        <Font>
            ...
        </Font>
        <LabelPlacement>
            ...
        </LabelPlacement>
        <Halo>
            <Radius>1.0</Radius>
            <Fill>
                ...
            </Fill>
        </Halo>
        ...
    </TextSymbolizer>

^^^^^^^^^^^^^
Using Filters
^^^^^^^^^^^^^

Within symbolization rules, it is possible to use Filter Encoding expressions.
How construct those expressions is explained within the :ref:`anchor-configuration-filter` chapter

^^^^^^^^^^^^^^
Basic Examples
^^^^^^^^^^^^^^
________________
Point Symbolizer
________________

.. code-block:: xml

    <FeatureTypeStyle
    xmlns="http://www.opengis.net/se"
    xmlns:app="http://www.deegree.org/app"
    xmlns:ogc="http://www.opengis.net/ogc"
    xmlns:sed="http://www.deegree.org/se"
    xmlns:deegreeogc="http://www.deegree.org/ogc"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.opengis.net/se http://schemas.opengis.net/se/1.1.0/FeatureStyle.xsd http://www.deegree.org/se http://schemas.deegree.org/se/1.1.0/Symbolizer-deegree.xsd">
     <Name>Weatherstations</Name>
     <Rule>
        <Name>Weatherstations</Name>
        <Description>
          <Title>Weatherstations in Utah</Title>
        </Description>
        <ogc:Filter>
          <ogc:PropertyIsEqualTo>
            <ogc:PropertyName>SomeProperty</ogc:PropertyName>
            <ogc:Literal>100</ogc:Literal>
          </ogc:PropertyIsEqualTo>
          </ogc:Filter>
          <PointSymbolizer>
            <Graphic>
              <Mark
                <WellKnownName>square</WellKnownName>
                <Fill>
                  <SvgParameter name="fill">#FF0000</SvgParameter>
                </Fill>
                <Stroke>
                  <SvgParameter name="stroke">#000000</SvgParameter>
                  <SvgParameter name="stroke-width">1</SvgParameter>
                </Stroke>
              </Mark>
              <Size>13</Size>
            </Graphic>
          </PointSymbolizer>
      </Rule> 
    </FeatureTypeStyle>

________________
Line Symbolizer
________________

.. code-block:: xml

    <FeatureTypeStyle
    xmlns="http://www.opengis.net/se"
    xmlns:app="http://www.deegree.org/app"
    xmlns:ogc="http://www.opengis.net/ogc"
    xmlns:sed="http://www.deegree.org/se"
    xmlns:deegreeogc="http://www.deegree.org/ogc"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.opengis.net/se http://schemas.opengis.net/se/1.1.0/FeatureStyle.xsd http://www.deegree.org/se http://schemas.deegree.org/se/1.1.0/Symbolizer-deegree.xsd">
      <Name>Railroads</Name>
      <Rule>
        <Name>Railroads</Name>
        <LineSymbolizer>
          <Stroke>
            <SvgParameter name="stroke">#000000</SvgParameter>
            <SvgParameter name="stroke-opacity">1.0</SvgParameter>
            <SvgParameter name="stroke-width">0.3</SvgParameter>
          </Stroke>
          <PerpendicularOffset>1.5</PerpendicularOffset>
        </LineSymbolizer>
        <LineSymbolizer>
          <Stroke>
            <SvgParameter name="stroke">#ffffff</SvgParameter>
            <SvgParameter name="stroke-opacity">1.0</SvgParameter>
            <SvgParameter name="stroke-width">1.5</SvgParameter>
          </Stroke>
        </LineSymbolizer>
        <LineSymbolizer>
          <Stroke>
            <SvgParameter name="stroke">#000000</SvgParameter>
            <SvgParameter name="stroke-opacity">1.0</SvgParameter>
            <SvgParameter name="stroke-width">0.3</SvgParameter>
          </Stroke>
          <PerpendicularOffset>-1.5</PerpendicularOffset>
        </LineSymbolizer>
      </Rule>    
    </FeatureTypeStyle>

__________________
Polygon Symbolizer
__________________

.. code-block:: xml

    <FeatureTypeStyle
     xmlns="http://www.opengis.net/se"
     xmlns:app="http://www.deegree.org/app"
     xmlns:ogc="http://www.opengis.net/ogc"
     xmlns:sed="http://www.deegree.org/se"
     xmlns:deegreeogc="http://www.deegree.org/ogc"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://www.opengis.net/se http://schemas.opengis.net/se/1.1.0/FeatureStyle.xsd http://www.deegree.org/se http://schemas.deegree.org/se/1.1.0/Symbolizer-deegree.xsd">
      <Name>LandslideAreas</Name>
      <Rule>
        <Name>LandslideAreas</Name>
        <Description>
          <Title>LandslideAreas</Title>
        </Description>
        <PolygonSymbolizer>
          <Fill>
            <SvgParameter name="fill">#cc3300</SvgParameter>
            <SvgParameter name="fill-opacity">0.3</SvgParameter>
          </Fill>
          <Stroke>
            <SvgParameter name="stroke">#000000</SvgParameter>
            <SvgParameter name="stroke-opacity">1.0</SvgParameter>
            <SvgParameter name="stroke-width">1</SvgParameter>
          </Stroke>
        </PolygonSymbolizer>
      </Rule>
    </FeatureTypeStyle>

_______________
Text Symbolizer
_______________

.. code-block:: xml

    <FeatureTypeStyle
     xmlns="http://www.opengis.net/se"
     xmlns:app="http://www.deegree.org/app"
     xmlns:ogc="http://www.opengis.net/ogc"
     xmlns:sed="http://www.deegree.org/se"
     xmlns:deegreeogc="http://www.deegree.org/ogc"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://www.opengis.net/se http://schemas.opengis.net/se/1.1.0/FeatureStyle.xsd http://www.deegree.org/se http://schemas.deegree.org/se/1.1.0/Symbolizer-deegree.xsd">
      <Name>Municipalities</Name>
      <Rule>
        <Name>Municipalities</Name>
        <Description>
          <Title>Municipalities</Title>
        </Description>
        <MaxScaleDenominator>200000</MaxScaleDenominator>
        <TextSymbolizer>
          <Label>
            <ogc:PropertyName>app:NAME</ogc:PropertyName>
          </Label>
          <Font>
            <SvgParameter name="font-family">Arial</SvgParameter>
            <SvgParameter name="font-family">Sans-Serif</SvgParameter>
            <SvgParameter name="font-weight">bold</SvgParameter>
            <SvgParameter name="font-size">12</SvgParameter>
          </Font>
          <Halo>
            <Radius>1</Radius>
            <Fill>
              <SvgParameter name="fill-opacity">1.0</SvgParameter>
              <SvgParameter name="fill">#fefdC3</SvgParameter>
            </Fill>
          </Halo>
          <Fill>
            <SvgParameter name="fill">#000000</SvgParameter>
          </Fill>
        </TextSymbolizer>
      </Rule>
    </FeatureTypeStyle>

^^^^^^^^^^^^^^^^^^^^^
SLD/SE clarifications
^^^^^^^^^^^^^^^^^^^^^

This chapter is meant to clarify deegree's behaviour when using standard SLD/SE constructs.

________________________________________
Perpendicular offset/polygon orientation
________________________________________

For polygon rendering, the orientation is always fixed, and will be corrected if a feature store yields inconsistent geometries. The outer ring is always oriented counter clockwise, inner rings are oriented clockwise.

A positive perpendicular offset setting results in an offset movement in the outer direction, a negative setting moves the offset into the interior. For inner rings the effect is flipped (a positive setting moves into the interior of the inner ring, a negative setting moves into the exterior of the inner ring).

_________________
ScaleDenominators
_________________

The use of MinScaleDenominators and MaxScaleDenominators within SLD/SE files can easily be misunderstood because of the meaning of a high or a low scale. Therefore, this is clarified here according to the standard.
In general the MinScaleDenominator is always a smaller number than the MaxScaleDenominator. The following example explains, how it works:

.. code-block:: xml

    <MinScaleDenominator>25000</MinScaleDenominator>
    <MaxScaleDenominator>50000</MaxScaleDenominator>

This means, that the Symbolizer is being used for scales between 1:25000 and 1:50000.

^^^^^^^^^^^^^^^^^^^^^^^^^^^
deegree specific extensions
^^^^^^^^^^^^^^^^^^^^^^^^^^^

deegree supports some extensions of SLD/SE and filter encoding to enable more sophisticated styling. The following sections describe the respective extensions for SLD/SE and filter encoding.
For several specific extensions, there is a deegree SE XML Schema_.

.. _Schema: http://schemas.deegree.org/se

_________________
SLD/SE extensions
_________________


---------------------------------
Use of TTF files as Mark symbols
---------------------------------

You can use TrueType font files to use custom vector symbols in a ``Mark`` element:

.. code-block:: xml

    <Mark>
      <OnlineResource xlink:href="filepath/yousans.ttf" />
      <Format>ttf</Format>
      <MarkIndex>99</MarkIndex>
      <Fill>
        <SvgParameter name="fill">#000000</SvgParameter>
        ...
      </Fill>
      <Stroke>
        <SvgParameter name="stroke-opacity">0</SvgParameter>
        ...
      </Stroke>
    </Mark>

To find out what index you need to access, have a look at this post_ on the mailinglist which explains it very well.

.. _post: http://osgeo-org.1560.n6.nabble.com/SE-Styling-MarkIndex-glyph-index-tt5022210.html#a5026571

-------------------
Label AutoPlacement
-------------------

deegree has an option for SE LabelPlacement to automatically place labels on the map.
To enable AutoPlacement, you can simply set the "auto" attribute to "true".

.. code-block:: xml

    <LabelPlacement>
      <PointPlacement auto="true">
        <Displacement>
          <DisplacementX>0</DisplacementX>
          <DisplacementY>0</DisplacementY>
        </Displacement>
        <Rotation>0</Rotation>
      </PointPlacement>
    </LabelPlacement> 

.. tip::
  AutoPlacement for labels only works for PointPlacement. AutoPlacement for LinePlacement is not implemented yet.
  
------------------------
LinePlacement extensions
------------------------

There are additional deegree specific LinePlacement parameters available to enable more sophisticated
text rendering along lines:

+-----------------------+------------+---------+-----------------------------------------------------------------+
| Option                | Value      | Default | Description                                                     |
+=======================+============+=========+=================================================================+
| PreventUpsideDown     | Boolean    | false   | Avoids upside down placement of text                            |
+-----------------------+------------+---------+-----------------------------------------------------------------+ 
| Center                | Boolean    | false   | Places the text in the center of the line                       |
+-----------------------+------------+---------+-----------------------------------------------------------------+ 
| WordWise              | Boolean    | true    | Tries to place individual words instead of individual characters| 
+-----------------------+------------+---------+-----------------------------------------------------------------+

^^^^^^^
Example
^^^^^^^ 

.. code-block:: xml

    <LinePlacement>
	    <IsRepeated>false</IsRepeated>
	    <InitialGap>10</InitialGap>
	    <PreventUpsideDown>true</PreventUpsideDown>
	    <Center>true</Center>
	    <WordWise>false</WordWise>
    </LinePlacement>

__________________________
SE & FE Functions
__________________________

There are a couple of deegree specific functions which can be expressed as standard OGC function expressions in SLD/SE. Additionally deegree has support for all the unctions defined within the SE standard.

------------
FormatNumber
------------

This function is needed to format number attributes. It can be used like in the following example:

.. code-block:: xml

    <FormatNumber xmlns:ogc="http://www.opengis.net/ogc" xmlns:app="http://www.deegree.org/app" xmlns="http://www.opengis.net/se" fallbackValue="">
      <NumericValue>
        <ogc:PropertyName>app:SHAPE_LEN</ogc:PropertyName>
      </NumericValue>
      <Pattern>############.00</Pattern>
    </FormatNumber>

----------
FormatDate
----------

This function is fully supported, although not fully tested with all available schema types mentioned in the spec.

.. code-block:: xml

    <FormatDate xmlns:ogc="http://www.opengis.net/ogc" xmlns:app="http://www.deegree.org/app" xmlns="http://www.opengis.net/se" fallbackValue="">
      <DateValue>
        <ogc:PropertyName>app:TIMESTAMP</ogc:PropertyName>
      </DateValue>
      <Pattern>DD</Pattern>
    </FormatDate>


----------
ChangeCase
----------

This function is used to change the case of property values.

.. code-block:: xml

    <ChangeCase xmlns:ogc="http://www.opengis.net/ogc" xmlns:app="http://www.deegree.org/app" xmlns="http://www.opengis.net/se" fallbackValue="" direction="toUpper">
      <StringValue>
        <ogc:PropertyName>app:text</ogc:PropertyName>
      </StringValue>
    </ChangeCase>

-----------
Concatenate
-----------

With the concatenate function it is possible to merge the values of more than one property to a chain.

.. code-block:: xml

    <Concatenate xmlns:ogc="http://www.opengis.net/ogc" xmlns:app="http://www.deegree.org/app" xmlns="http://www.opengis.net/se" fallbackValue="">
      <StringValue>
        <ogc:PropertyName>app:text1</ogc:PropertyName>
      </StringValue>
      <StringValue>
        <ogc:PropertyName>app:text2</ogc:PropertyName>
      </StringValue>
      <StringValue>
        <ogc:PropertyName>app:text3</ogc:PropertyName>
      </StringValue>
    </Concatenate>

----
Trim
----

The trim function is used to trim string property values.

.. code-block:: xml

    <Trim xmlns:ogc="http://www.opengis.net/ogc" xmlns:app="http://www.deegree.org/app" xmlns="http://www.opengis.net/se" fallbackValue="" stripOffPosition="both">
      <StringValue>
        <ogc:PropertyName>app:text</ogc:PropertyName>
      </StringValue>
    </Trim>


------------
StringLength
------------

With the StringLength function it is possible to calculate the length of string property values.

.. code-block:: xml

    <StringLength xmlns:ogc="http://www.opengis.net/ogc" xmlns:app="http://www.deegree.org/app" xmlns="http://www.opengis.net/se" fallbackValue="">
      <StringValue>
        <ogc:PropertyName>app:text</ogc:PropertyName>
      </StringValue>
    </StringLength>

---------
Substring
---------

With the substring function it is possible to only get a specific substring of a string property.

.. code-block:: xml

    <Substring xmlns:ogc="http://www.opengis.net/ogc" xmlns:app="http://www.deegree.org/app" xmlns="http://www.opengis.net/se" fallbackValue="">
      <StringValue>
        <ogc:PropertyName>app:text</ogc:PropertyName>
      </StringValue>
      <Position>1</Position>
      <Length>
        <ogc:Sub>
          <StringPosition fallbackValue="" searchDirection="frontToBack">
            <LookupString>-</LookupString>
            <StringValue>
              <ogc:PropertyName>app:text</ogc:PropertyName>
            </StringValue>
          </StringPosition>
          <ogc:Literal>1</ogc:Literal>
        </ogc:Sub>
      </Length>
    </Substring>

---------------
StringPosition
---------------

The StringPosition function is made to get the literal at a specific position from a string property.

.. code-block:: xml

    <StringPosition xmlns:app="http://www.deegree.org/app" xmlns="http://www.opengis.net/se" fallbackValue="" searchDirection="frontToBack">
      <LookupString>-</LookupString>
      <StringValue>
        <ogc:PropertyName xmlns:ogc="http://www.opengis.net/ogc">app:text</ogc:PropertyName>
      </StringValue>
    </StringPosition>


-------------------------------
Categorize, Interpolate, Recode
-------------------------------

These functions can operate both on alphanumeric properties of features and on raster data. For color values we extended the syntax a bit to allow for an alpha channel: #99ff0000 is a red value with an alpha value of 0x99. This allows the user to create eg. an interpolation from completely transparent to a completely opaque color value. To work on raster data you'll have to replace the PropertyName values with Rasterdata.

For Interpolate only linear interpolation is currently supported.

.. code-block:: xml

    <Categorize xmlns:app="http://www.deegree.org/app" xmlns="http://www.opengis.net/se" xmlns:ogc="http://www.opengis.net/ogc" fallbackValue="#fefdC3">
      <LookupValue>
        <ogc:PropertyName>app:POP2000</ogc:PropertyName>
      </LookupValue>
      <Value>#FFE9D8</Value>
      <Threshold>1000</Threshold>
      <Value>#FBCFAC</Value>
      <Threshold>10000</Threshold>
      <Value>#FAAC6F</Value>
      <Threshold>25000</Threshold>
      <Value>#FD913D</Value>
      <Threshold>100000</Threshold>
      <Value>#FF7000</Value>
    </Categorize>

.. code-block:: xml

    <Interpolate xmlns:ogc="http://www.opengis.net/ogc" xmlns:app="http://www.deegree.org/app" xmlns="http://www.opengis.net/se" fallbackValue="#005C29" method="color">
      <LookupValue>
        <ogc:PropertyName>app:CODE</ogc:PropertyName>
      </LookupValue>
      <InterpolationPoint>
        <Data>-1</Data>
        <Value>#005C29</Value>
      </InterpolationPoint>
      <InterpolationPoint>
        <Data>100</Data>
        <Value>#067A3A</Value>
      </InterpolationPoint>
      <InterpolationPoint>
        <Data>300</Data>
        <Value>#03A64C</Value>
      </InterpolationPoint>
      <InterpolationPoint>
        <Data>500</Data>
        <Value>#00CF5D</Value>
      </InterpolationPoint>
      <InterpolationPoint>
        <Data>1000</Data>
        <Value>#ffffff</Value>
      </InterpolationPoint>
    </Interpolate>

.. code-block:: xml

    <Recode xmlns:app="http://www.deegree.org/app" xmlns="http://www.opengis.net/se" fallbackValue="">
      <LookupValue>app:code</LookupValue>
      <MapItem>
        <Data>1000</Data>
        <Value>water</Value>
      </MapItem>
      <MapItem>
        <Data>2000</Data>
        <Value>nuclear</Value>
      </MapItem>
      <MapItem>
        <Data>3000</Data>
        <Value>solar</Value>
      </MapItem>
      <MapItem>
        <Data>4000</Data>
        <Value>wind</Value>
      </MapItem>
    </Recode>


-----------------------
General XPath functions
-----------------------

Many useful things can be done by simply using standard XPath 1.0 functions in PropertyName elements.

Access the (local) name of an element (e.g. the name of a referenced feature / subfeature).

.. code-block:: xml

    <PropertyName xmlns:app="http://www.deegree.org/app">app:subfeature/*/local-name()</PropertyName>



