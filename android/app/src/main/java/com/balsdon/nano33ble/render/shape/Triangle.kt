package com.balsdon.nano33ble.render.shape

import android.opengl.GLES30
import com.balsdon.nano33ble.render.SurfaceRenderer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class Triangle: Shape() {

    override val programReference: Int
    private val vertexBuffer: FloatBuffer
    private var positionHandle = 0
    private var colourHandle = 0
    private var modelViewProjectionHandle = 0
    private val vertexCount = triangleCoords.size / coordinatesPerVertex
    private val vertexStride = coordinatesPerVertex * 4 // 4 bytes per vertex
    var color = floatArrayOf(0.63671875f, 0.76953125f, 0.22265625f, 0.0f)

    override fun render(mvpMatrix: FloatArray) {
        // Add program to OpenGL environment
        GLES30.glUseProgram(programReference)

        // get handle to vertex shader's vPosition member
        positionHandle = GLES30.glGetAttribLocation(programReference, "vPosition")

        // Enable a handle to the triangle vertices
        GLES30.glEnableVertexAttribArray(positionHandle)

        // Prepare the triangle coordinate data
        GLES30.glVertexAttribPointer(
            positionHandle, coordinatesPerVertex,
            GLES30.GL_FLOAT, false,
            vertexStride, vertexBuffer
        )

        // get handle to fragment shader's vColor member
        colourHandle = GLES30.glGetUniformLocation(programReference, "vColor")

        // Set color for drawing the triangle
        GLES30.glUniform4fv(colourHandle, 1, color, 0)

        // get handle to shape's transformation matrix
        modelViewProjectionHandle = GLES30.glGetUniformLocation(programReference, "uMVPMatrix")
        SurfaceRenderer.checkGlError("glGetUniformLocation")

        // Apply the projection and view transformation
        GLES30.glUniformMatrix4fv(modelViewProjectionHandle, 1, false, mvpMatrix, 0)
        SurfaceRenderer.checkGlError("glUniformMatrix4fv")

        // Draw the triangle
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount)

        // Disable vertex array
        GLES30.glDisableVertexAttribArray(positionHandle)
    }

    companion object {
        var triangleCoords = floatArrayOf( // in counterclockwise order:
            0.0f, 0.622008459f, 0.0f,  // top
            -0.5f, -0.311004243f, 0.0f,  // bottom left
            0.5f, -0.311004243f, 0.0f // bottom right
        )
    }

    init {
        // initialize vertex byte buffer for shape coordinates
        val byteBuffer = ByteBuffer.allocateDirect( // (number of coordinate values * 4 bytes per float)
            triangleCoords.size * 4
        )
        // use the device hardware's native byte order
        byteBuffer.order(ByteOrder.nativeOrder())

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = byteBuffer.asFloatBuffer().apply {
            // add the coordinates to the FloatBuffer
            put(triangleCoords)
            // set the buffer to read the first coordinate
            position(0)
        }

        // prepare shaders and OpenGL program
        val vertexShader = SurfaceRenderer.loadShader(
            GLES30.GL_VERTEX_SHADER, vertexShaderCode
        )
        val fragmentShader = SurfaceRenderer.loadShader(
            GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode
        )
        programReference = GLES30.glCreateProgram() // create empty OpenGL Program
        GLES30.glAttachShader(programReference, vertexShader) // add the vertex shader to program
        GLES30.glAttachShader(programReference, fragmentShader) // add the fragment shader to program
        GLES30.glLinkProgram(programReference) // create OpenGL program executables
    }
}