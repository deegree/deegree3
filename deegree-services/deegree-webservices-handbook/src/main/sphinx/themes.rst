.. _anchor-configuration-themes:

==========
Map themes
==========

A theme defines a tree like hierarchy, which at each node can contain a number of layers. For people familiar with WMS, a theme is basically a layer tree without the actual layer definition.

In deegree it is used to define a structure with layers to be used in service configurations, notably WMS and WMTS. The concept originated from the WMTS 1.0.0 specification, with a strong hunch that it might be used in subsequent WMS specifications as well (namely WMS 2.0.0).

To configure a theme, you should already have a couple of layers configured. Right now there are two types of theme configurations available. The most commonly used is the 'standard' theme configuration, where you manually configure the structure. Another is a configuration which extracts a theme from a remote WMS resource's layer tree.

A theme always has exactly one root node (theme). A theme can contain zero or more sub-themes, and zero or more layers.

.. figure:: images/workspace-overview-theme.png
   :figwidth: 80%
   :width: 80%
   :target: _images/workspace-overview-theme.png

   Theme resources group layers into trees

---------------
Standard themes
---------------

The standard theme configuration is used to manually configure themes. One configuration can contain one or more themes. A theme configuration makes use of the common :ref:`description` and :ref:`spatial` elements described in the layer chapter. If the metadata is not specified, it will be copied from layers within the same node.

In order to reference layers, the theme configuration needs to know layer stores. That's why the first thing you need to specify are the layer stores you intend to use:

.. code-block:: xml

  <Themes configVersion="3.2.0" xmlns="http://www.deegree.org/themes/standard"
                                xmlns:d="http://www.deegree.org/metadata/description"
                                xmlns:s="http://www.deegree.org/metadata/spatial">

    <LayerStoreId>layerstore</LayerStoreId>
    <LayerStoreId>layerstore2</LayerStoreId>
    <Theme>
    ...
    </Theme>
    ...
  </Themes>

Let's have a look at the actual theme configuration. First, you have the choice to give the theme an identifier or not. Then you can specify the description and spatial metadata (only the ``Title`` element is mandatory here). If it does not have an identifier, it will not be requestable in the service configuration:

.. code-block:: xml

  <Theme>
    <Identifier>roads</Identifier>
    <!-- common description elements here -->
    <!-- common spatial metadata elements here -->
    ...
  </Theme>

After that, you can add layers and subthemes as required to the theme:

.. code-block:: xml

  <Theme>
    ...
    <Layer>roads</Layer>
    <Layer layerStore='layerstore2'>highways</Layer>
    <Theme>
      ...
      <Theme>
        ...
      </Theme>
    </Theme>
  </Theme>

As you can see, you can optionally specify which layer store a given layer comes from. This can be useful if you have multiple layer stores offering a layer with the same name.

Since the names of the layers are not used when using WMS, this mechanism can be used to combine multiple layers (configuration wise) into one (WMS wise, in deegree terms it would be one theme with multiple layers).

-----------------
Remote WMS themes
-----------------

The remote WMS theme configuration can be used to extract a theme from a remote WMS resource's layer tree. This is most commonly used when trying to cascade a whole WMS.

The configuration is very simple, you only need to specify the remote WMS resource you want to use, and the layer store from which layers should be extracted:

.. code-block:: xml

  <RemoteWMSThemes xmlns="http://www.deegree.org/themes/remotewms" configVersion="3.1.0">
    <RemoteWMSId>d3</RemoteWMSId>
    <LayerStoreId>d3</LayerStoreId>
  </RemoteWMSThemes>

deegree will automatically add layers to the theme, if a corresponding layer exists in the layer store. In case the layer store is also configured based on the remote WMS used here, there will be a corresponding layer for each requestable layer from the remote WMS.

Using this kind of configuration, you can duplicate a complete WMS using 15 lines of configuration (3 for the remote WMS, 3 for the remote WMS layer store, 4 for the theme and 5 for the WMS).

