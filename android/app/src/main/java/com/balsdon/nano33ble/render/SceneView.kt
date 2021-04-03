package com.balsdon.nano33ble.render

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class SceneView(context: Context, attributeSet: AttributeSet? = null) : GLSurfaceView(context, attributeSet) {
    val renderer: Renderer
    init {
        // Create an OpenGL ES 2.0 context.  CHANGED to 3.0  JW.
        setEGLContextClientVersion(3)
        //fix for error No Config chosen, but I don't know what this does.
        super.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        // Set the Renderer for drawing on the GLSurfaceView
        renderer = SurfaceRenderer()
        setRenderer(renderer)
    }
}