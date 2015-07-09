Title: 3D Earth Demo
Author: Rene van Klink (rene@knpl.nl)

HAH, YOU'RE READING ME!

This is a simple OpenGL ES demonstration I created in my spare time (which I have had a lot of. For some reason...).
It was supposed to refresh what limited memories I had from OpenGL programming, make myself more familiar with 
Android programming, using git and publishing something on the app-store of a well known search-engine for the web. 
It will with overwhelming probability remain more-or-less a thousand lines code. 

The Android code demonstrates usage of a single screen-filling GLSurfaceView, passing touch-events to it, those sorts
of things. The OpenGL code (most of EarthRenderer.java) demonstrates the use of modern OpenGL using what hopefully 
resembles Blinn-Phong shading and bump/normal mapping. I think it looks alright, I won't guarantee its correctness.
The shader code can be found in the assets folder.

By touching the screen you control the position of either the camera or a light (based on a toggle in the action bar) 
in a spherical fashion. Drag to move, pinch to zoom. There's also a small menu that lets you control the ambient, 
diffuse and specular components of the (white) light, if that makes any sense.

I took the Earth textures from http://celestiamotherlode.net/ which I think got their images from NASA's Blue Marble 
project (http://earthobservatory.nasa.gov/Features/BlueMarble/) and modified them. I suppose they are in the public 
domain... AFAIK. I'll replace them with hand drawn ones on the first sign that they aren't. What I'm saying is:
Please, just don't sue me.

I'm basically not a very mature person. One day I will read this again and cringe.
