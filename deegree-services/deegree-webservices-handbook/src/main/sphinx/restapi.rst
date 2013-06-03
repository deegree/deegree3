.. _anchor-configuration-restapi:

====================================
deegree REST interface
====================================

deegree offers a REST like web interface to access and configure the deegree workspace. You can use it to alter configuration, restart workspaces or resources and start a different workspace.


------------------------
Setting up the interface
------------------------

The servlet that handles the REST interface is already running if you use the standard ``web.xml`` deployment descriptor. For security reasons, you'll need to add a user with the role ``deegree`` to your Tomcat configuration, eg. by adding an appropriate line to the ``conf/tomcat-users.xml`` file.

Once you did that, you can get an overview of available 'commands' by requesting ``http://localhost:8080/deegree-webservices/config``. You'll need to provide the username/password you configured in your Tomcat configuration.

Here's an example output::

   No action specified.

   Available actions:
   GET /config/download[/path]                                  - download currently running workspace or file in workspace
   GET /config/download/wsname[/path]                           - download workspace with name <wsname> or file in workspace
   GET /config/restart                                          - restart currently running workspace
   GET /config/restart/wsname                                   - restart with workspace <wsname>
   GET /config/listworkspaces                                   - list available workspace names
   GET /config/list[/path]                                      - list currently running workspace or directory in workspace
   GET /config/list/wsname[/path]                               - list workspace with name <wsname> or directory in workspace
   GET /config/invalidate/datasources/tile/id/matrixset[?bbox=] - invalidate part or all of a tile store cache's tile matrix set
   GET /config/crs/list                                         - list available CRS definitions
   POST /config/crs/getcodes with wkt=<wkt>                     - retrieves a list of CRS codes corresponding to the WKT (POSTed KVP)
   GET /config/crs/<code>                                       - checks if a CRS definition is available, returns true/false
   PUT /config/upload/wsname.zip                                - upload workspace <wsname>
   PUT /config/upload/path/file                                 - upload file into current workspace
   PUT /config/upload/wsname/path/file                          - upload file into workspace with name <wsname>
   DELETE /config/delete[/path]                                 - delete currently running workspace or file in workspace
   DELETE /config/delete/wsname[/path]                          - delete workspace with name <wsname> or file in workspace

   HTTP response codes used:
   200 - ok
   403 - if you tried something you shouldn't have
   404 - if a file or directory needed to fulfill a request was not found
   500 - if something serious went wrong on the server side

----------------------------
Detailed explanation
----------------------------

Let's see how the commands work in detail. In general, you can specify a path relative to the workspace almost anywhere. With no path given, you act on the workspace, with a path given, you act on that part of the workspace.

__________________
Downloading
__________________

In order to download the complete workspace, you request ``http://localhost:8080/deegree-webservices/config/download``. Since the workspace is made up of many files, you get a ``.zip`` file. If you just want to download the featurestore configuration named ``inspire``, you request ``http://localhost:8080/deegree-webservices/config/download/datasources/feature/inspire.xml``.

To use a different workspace instead of the currently running one, use ``http://localhost:8080/deegree-webservices/config/download/otherworkspace`` (you may also specify a file within that workspace).

_____________________
Restarting
_____________________

You can restart the currently running workspace using ``http://localhost:8080/deegree-webservices/config/restart``, or start another workspace using ``http://localhost:8080/deegree-webservices/config/restart/anotherworkspace``.

____________________
Listing
____________________

You can see what workspaces are available to the deegree installation by running ``http://localhost:8080/deegree-webservices/config/listworkspaces``.

You can also browse through a workspace's files by requesting eg. ``http://localhost:8080/deegree-webservices/config/list/datasources/``, or to see the files in a workspace other than the one currently running ``http://localhost:8080/deegree-webservices/config/list/someworkspace/services/``.

__________________
Storing
__________________

You can update or add files in a workspace, or upload a completely new workspace by sending a HTTP PUT request.

To upload a new workspace, send a ``.zip`` file with the workspace contents to ``http://localhost:8080/deegree-webservices/config/upload/someworkspace.zip``. This will extract the workspace as ``someworkspace``. Note that there should not be a parent directory in the ``.zip``, it should contain folders like ``datasources`` or ``service`` directly.

To upload individual files send requests against ``http://localhost:8080/deegree-webservices/config/upload/path/to/file.xml``, or with a workspace name prefix as usual (``http://localhost:8080/deegree-webservices/config/upload/someworkspace/and/the/path/file.xml``).

_____________
Deleting
_____________

Deletion works just like storing, except you send HTTP DELETE requests and instead of the ``upload`` path component you use ``delete``. You can also delete whole directories with content by specifying just the path to the directory. Deleting workspaces is also possible, just specify the workspace name (without a ``.zip`` suffix).

________________________________
Invalidating tile store caches
________________________________

This is a special operation only possible for ``CachingTileStore`` resources. You can invalidate the whole cache, or just a part of it by requesting ``http://localhost:8080/deegree-webservices/config/invalidate/datasources/tile/configname/matrixsetname``. You can specify a bounding box by appending it in the form ``?bbox=minx,miny,maxx,maxy`` (just like in WMS requests).

________________
CRS queries
________________

You can get a list of all available CRS definitions by requesting ``http://localhost:8080/deegree-webservices/config/crs/list``. Check if a specific CRS is configured in deegree by requesting ``http://localhost:8080/deegree-webservices/config/crs/EPSG:12345``. The response will be the text ``true`` or ``false``, depending whether the CRS is defined or not. If you have a WKT CRS definition, you can POST against ``http://localhost:8080/deegree-webservices/config/crs/getcodes`` to get a list of corresponding identifiers (experimental). Use the ``wkt`` parameter when posting to send the WKT definition.
