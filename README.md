Export-To-Earth
===============

This plugin exports gephi graphs with geocoordinates to KMZ files for Google Earth or other GIS software. To install it, use the Gephi Plugin interface.

To use this plugin, go to Preview, then choose File > Export, and select "KMZ" from the dropdown menu. You can customize the fields exported, or choose which fields represent "lat" and "lon" with the Options button.

This plugin attempts to preserve the appearance of your graph preview as best as possible (aside from moving the nodes and edges to the appropriate geocoordinates). Node colors are preserved from how they have been assigned in Gephi.

This plugin was dveloped during the NEH Summer Institute for Advanced Topics in Digital Humanities on Digital Cultural Mapping (http://hypercities.com/NEH/). Thanks to Ryan Cordell, Zoe Borovsky, and Paula Lupkin for being the impetus behind the development of the plugin and the alpha testers.


Dependencies
============

This plugin depends on JAK, the Java API for KML (http://code.google.com/p/javaapiforkml/). JAK itself depends on JAXB.
