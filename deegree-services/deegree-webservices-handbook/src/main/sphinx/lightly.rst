.. _anchor-lightly:

===============
Getting started
===============

In the previous chapter, you learned how to install and start deegree webservices. In this chapter, we will introduce the service console and learn how to perform basic tasks such as downloading and activating example configurations. In deegree terminology, a configuration for a deegree instance is called "deegree workspace". A workspace is a well-defined directory of configuration files that defines the different aspects of a deegree instance (active web services, data access, layers, etc). The next chapter describes the inner structure of a deegree workspace, but for the remainder of this chapter, it is sufficient to understand that it is the configuration of a deegree webservice instance.

-------------------------------
Using deegree's service console
-------------------------------

The service console is a web-based administration interface for configuring your deegree webservices installation. If deegree webservices are running on your machine, you can usually access the console from your browser via http://localhost:8080

.. figure:: images/browser.png
   :figwidth: 60%
   :width: 50%
   :target: _images/browser.png

   deegree webservices console

.. tip::
  If you're not running the ZIP version, but deployed the WAR version into a web container, you most probably will have to use a different URL for accessing the console, e.g. http://localhost:8080/deegree-webservices-3.2-pre8. The port number and webapp name depend on your installation/deployment.

.. tip::
  You can access the service console from other machines on your network by exchanging *localhost* with the name or IP address of the machine that runs the deegree webservices.

Before you are logged in, you can only access two links in the menu on the left:

* **send requests**: Access a simple user interface for sending raw OGC XML requests to the service (useful for testing). Until you activate some services, this is not very useful, though.
* **see layers**: Shows the layers provided by the WMS in a simple web client. Until you activate the WMS, only an OpenStreetMap base layer is available.

These links do not allow to change any settings of your deegree installation. In order to gain access to the administration tasks, you need to login first. 

^^^^^^^^^^
Logging in
^^^^^^^^^^

The default password is **deegree**. Enter it into the password field and click 'Log In'. The view will change now and give access to all configuration sections:

.. figure:: images/browser.png
   :figwidth: 60%
   :width: 50%
   :target: _images/browser.png

   Logged in

The links on the left allow to configure different configuration aspects of your installation. Most of them will be introduced in the next chapter. In the remainder of this chapter, the relevant menu items are in the **general** category:

* workspace: Download and activate example configurations
* proxy: Control proxy settings that deegree uses for accessing the internet

.. tip::
  If the machine running deegree webservices uses a proxy to access the internet and you have trouble downloading example configurations, you will probably have to configure the proxy settings. Ask your network administrator for details.

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Activating example workspaces
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Click on the *workspace* link:

.. figure:: images/browser.png
   :figwidth: 60%
   :width: 50%
   :target: _images/browser.png

   Workspace section

The bottom of the workspace view lists the available example workspace. You should see following:

* **deegree utahDemo**
* **deegree inspireNode**
* **deegree CSW demo**
* **deegree WPS demo**

If you click on **Import**, the corresponding example workspace will be fetched from deegree's official workspace repository and extracted in your deegree configuration folder. Depending on the workspace and your internet connection, this may take a while.

After downloading has completed, the workspace will be shown under "Inactive workspaces":

.. figure:: images/browser.png
   :figwidth: 60%
   :width: 50%
   :target: _images/browser.png

   Added inactive workspace

You can now activate the downloaded workspace by clicking on "Start". Again, this may take some time, as it may require some initialization (e.g. building of indexes). The workspace will be removed from the list of inactive workspaces, instead, the workspace will be marked as "Active" (at the top of the view). Your deegree instance is now running the service configuration that is contained in the downloaded workspace.

.. _anchor-workspace-utah:

----------------------------------------
Example workspace 1: Webmapping for Utah
----------------------------------------

This workspace demonstrates a web mapping setup based on data from Utah. It contains a WMS with some raster and vector layers and some nice render styles. Raster data is read from GeoTIFF files, vector data is backed by shapefiles. Additionally, a WFS is configured that allows to access the raw vector data in GML format.

After downloading and activating the "deegree utahDemo" workspace, you can click on the "see layers" link, which opens a simple web map client that displays a base map (not rendered by deegree, but loaded from the OpenStreetMap servers).

.. figure:: images/browser.png
   :figwidth: 60%
   :width: 50%
   :target: _images/browser.png

   Map client showing base map

Click on the "+" icon on the right side to see a list of available layers. Tick one (e.g. ) to enable it in the client. It will be generated by your deegree instance.

.. figure:: images/browser.png
   :figwidth: 60%
   :width: 50%
   :target: _images/browser.png

   Selecting WMS layers to be displayed

.. tip::
You can drag the map by holding the mouse button and moving your mouse. Zooming can be done by the controls on the left or using the mouse wheel. 
Alternatively, you can open a rectangle by holding the SHIFT key and clicking the mouse button in the map area.

In order to send some requests against the WFS, you may use the "send requests" link in the service console (go back first to the console). A simple interface for sending XML requests will open up. This interface is meant for testing the behaviour of your web service on the protocol level and contains some reasonable example requests.

.. figure:: images/browser.png
   :figwidth: 60%
   :width: 50%
   :target: _images/browser.png

   Sending example requests

You may select example requests from the drop-down menu and click on the "Send" button. The server response will be displayed.

.. figure:: images/browser.png
   :figwidth: 60%
   :width: 50%
   :target: _images/browser.png

   Sending example requests

.. tip::
  Instead of using the built-in layer preview or the generic XML client, you may use any compliant OGC client for accessing the WMS and WFS. Successfully tested desktop clients include Quantum GIS (install WFS plugin for accessing WFS), uDig, OpenJUMP and deegree iGeoDesktop. The service address to enter in your client is: http://localhost:8080/services.

.. figure:: images/browser.png
   :figwidth: 60%
   :width: 50%
   :target: _images/browser.png

   Quantum GIS displaying some WMS layers from the utahDemo


.. _anchor-workspace-inspire:

--------------------------------------
Example workspace 2: INSPIRE in action
--------------------------------------

This workspace demonstrates a basic INSPIRE View and Discovery Service setup. It contains a transactional WFS configured for Annex I Data Themes and a WMS that is configured to display some of the Data Themes. The workspace is configured to store the INSPIRE features in memory, but can easily be changed to use PostGIS or Oracle as storage backend (TBD describe this).

After downloading and activating the "deegree inspireNode" workspace, you can click on the "see layers" link, which opens a simple web map client that displays a base map (not rendered by deegree, but loaded from the OpenStreetMap servers).

.. figure:: images/browser.png
   :figwidth: 60%
   :width: 50%
   :target: _images/browser.png

   Map client showing base map

You can now activate the INSPIRE layers, but nothing will be rendered, as the configured storage (memory) doesn't contain any features yet.

.. figure:: images/browser.png
   :figwidth: 60%
   :width: 50%
   :target: _images/browser.png

   INSPIRE layers are empty

In order to insert some INSPIRE features, use the "send requests" link in the service console:

.. figure:: images/browser.png
   :figwidth: 60%
   :width: 50%
   :target: _images/browser.png

   INSPIRE layers are empty

Use the right-most drop-down menu to select an example request. The last entry "blabla.xml" can be used to insert some INSPIRE Address features using a WFS insert request:

.. figure:: images/browser.png
   :figwidth: 60%
   :width: 50%
   :target: _images/browser.png

   Insert request

After successful insertion (click "Send"), the internal storage contains some addresses, and you may move back to the layer overview ("see layers"). After activating the Address layer, you should see some addresses.

The example requests also contain a lot of examples for the query possibilities of the deegree WFS, e.g. the requesting of INSPIRE Addresses by street name:

.. figure:: images/browser.png
   :figwidth: 60%
   :width: 50%
   :target: _images/browser.png

   Query examples

.. _anchor-workspace-csw:

----------------------------------------
Example config 3: An ISO catalogue setup
----------------------------------------

.. _anchor-workspace-wps:

------------------------------------
Example config 4: Processing service
------------------------------------


