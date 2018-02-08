async-opengl-renderer
----------------

OpenGL renderer with asynchronous loading of assets (experiment).

The application first starts with rendering quad without texture.

![https://i.imgur.com/7mzQGNy.png](https://i.imgur.com/7mzQGNy.png)

After 1 second a load request is dispatched to *content loading 
system*, which asynchronously loads file from disk, decodes it and 
uploads image data to GPU without blocking or stopping the main
thread, so the OpenGL application continues to run smoothly
(without any fps drop) even when data is being uploaded to GPU.

The whole thing is based on creating another OpenGL context in 
**uploader thread**, which shares resources with the **main render
thread**. The communication is synchronized using GL [fences](https://www.khronos.org/opengl/wiki/Sync_Object#Fence).

![https://i.imgur.com/q8iqMb2.png](https://i.imgur.com/q8iqMb2.png)