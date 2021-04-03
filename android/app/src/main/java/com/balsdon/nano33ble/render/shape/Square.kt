package com.balsdon.nano33ble.render.shape

import android.opengl.GLES30
import com.balsdon.nano33ble.render.SurfaceRenderer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class Square: Shape() {
    override val programReference: Int
    private val vertexBuffer: FloatBuffer
    private val drawListBuffer: ShortBuffer
    private var positionHandle = 0
    private var colourHandle = 0
    private var modelViewProjectionHandle = 0
    private val drawOrder = shortArrayOf(0, 1, 2, 0, 2, 3) // order to draw vertices
    private val vertexStride = coordinatesPerVertex * 4 // 4 bytes per vertex
    var color = floatArrayOf(0.2f, 0.709803922f, 0.898039216f, 1.0f)

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

        // Draw the square
        GLES30.glDrawElements(
            GLES30.GL_TRIANGLES, drawOrder.size,
            GLES30.GL_UNSIGNED_SHORT, drawListBuffer
        )

        // Disable vertex array
        GLES30.glDisableVertexAttribArray(positionHandle)
    }

    companion object {
        var squareCoords = floatArrayOf(
            -0.5f, 0.5f, 0.0f,  // top left
            -0.5f, -0.5f, 0.0f,  // bottom left
            0.5f, -0.5f, 0.0f,  // bottom right
            0.5f, 0.5f, 0.0f
        ) // top right
    }

    init {
        // initialize vertex byte buffer for shape coordinates
        val byteBuffer = ByteBuffer.allocateDirect( // (# of coordinate values * 4 bytes per float)
            squareCoords.size * 4
        ).apply {
            order(ByteOrder.nativeOrder())
        }
        vertexBuffer = byteBuffer.asFloatBuffer().apply {
            put(squareCoords)
            position(0)
        }

        // initialize byte buffer for the draw list
        val dlb = ByteBuffer.allocateDirect( // (# of coordinate values * 2 bytes per short)
            drawOrder.size * 2
        )
        dlb.order(ByteOrder.nativeOrder())
        drawListBuffer = dlb.asShortBuffer().apply {
            put(drawOrder)
            position(0)
        }

        // prepare shaders and OpenGL program
        val vertexShader = SurfaceRenderer.loadShader(
            GLES30.GL_VERTEX_SHADER,
            vertexShaderCode
        )
        val fragmentShader = SurfaceRenderer.loadShader(
            GLES30.GL_FRAGMENT_SHADER,
            fragmentShaderCode
        )
        programReference = GLES30.glCreateProgram() // create empty OpenGL Program
        GLES30.glAttachShader(programReference, vertexShader) // add the vertex shader to program
        GLES30.glAttachShader(programReference, fragmentShader) // add the fragment shader to program
        GLES30.glLinkProgram(programReference) // create OpenGL program executables
    }
}