Export-To-Earth
===============

This plugin exports gephi graphs with geocoordinates to KMZ files for Google Earth or other GIS software.

** To get just the plugin, download com-hypercities-exporttoearth.nbm **

To use this plugin, go to Preview, then choose File > Export, and select "KMZ" from the dropdown menu. It should do the rest.

Before running this, make sure your Nodes table has geocoordinate columns. The plugin looks for columns in the following order:
* For latitude: columns named "latitude," columns named "lat," columns with the words "latitude" or "lat" in them. 
* For longitude: columns named "longitude," columns named "lon," columns named "lng", columns with the words "longitude," "lon," or "lng" in them.

All comparisons are case-insensitive. The columns must also have a numerical type like float, decimal, or double.

This plugin attempts to preserve the appearance of your graph preview as best as possible (aside from moving the nodes and edges to the appropriate geocoordinates). Node colors are preserved from how they have been assigned in Gephi. Edges are always white.

This plugin was dveloped during the NEH Summer Institute for Advanced Topics in Digital Humanities on Digital Cultural Mapping (http://hypercities.com/NEH/). Thanks to Ryan Cordell, Zoe Borovsky, and Paula Lupkin for being the impetus behind the development of the plugin and the alpha testers.


Dependencies
============

This plugin depends on JAK, the Java API for KML (http://code.google.com/p/javaapiforkml/). JAK itself depends on JAXB.