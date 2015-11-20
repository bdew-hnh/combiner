### About

This is a tools for manipulating map data from Haven and Hearth custom clients.

Map tiles are stored in either a folder for each set, with files named **tile_{x}_{y}.png** or in a combined file with ".mpk" extension.

### Normal operation mode

**combiner [input1 [input2] [input3] ...]**

In this mode all inputs will be merged in memory, and stitched images will be generated in the first input directory.

In case no inputs are specified - a directory named "map" in the current working directory will be used.

### Merge mode

**combiner --merge \<output> input1 [input2] [input3] ...**

In this mode all inputs will be merged, with the merged tile sets and stitched images written out to the output location.

Output location must not exists.

### Images mode

**combiner --images \<directory>**

In this mod all tile sets in the directory will be stitched into images, without any automated merging.

### Combine mode

**combiner --combine \<input1> \<x1> \<y1> \<input2> \<x2> \<y2> \<output>**

Manually combines two tile sets and merges tiles and fingerprints.

input1 and input2 should be paths to tile sets (either directories or .mpk files) with (x1,y1) and (x2,y2) representing the same tile in both sets.

Output location must not exists.

### Online map generation mod

**combiner --gmap \<input> \<output>**

This mode is used to generate data for online map display systems (google maps, leaflet, etc.) that need pre-generated zoom levels.

Input should be a path of a tile set (either a directory with images or an .mpk file). Output should not exist.

Special options for this mode:

 * **--nulltiles** - will force generation of tiles that have no data (fully transparent)
 * **--minzoom** - specifies the minimum zoom level to generate (default is 0)
 * **--interpolation {bilinear|bicubic|nearest}** - chooses what interpolation method to use for resizing (default is bilinear)
 * **--tilesize** - sets tile size of the highest zoom level, e.g. 5 will make each base tile be 5x5 game map tiles (500x500 pixels)

### General command line options

These options can appear anywhere on the command line

* **--coords** - Adds coordinates to each tile on stitched images
* **--grid** - Draws grid on stitched images
* **--noimg** - Will not generate stitched images (only useable with --merge)
* **--nompk** - Forces generation of directory tile sets instead of .mpk files (only useable with --merge and --combine)
* **--time** - Shows timing data
