package com.balsdon.nano33ble.render.shape

import android.graphics.Color
import android.opengl.GLES30
import com.balsdon.nano33ble.colourToFloatArray
import com.balsdon.nano33ble.render.SurfaceRenderer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class Cube: Shape() {
    override val programReference: Int
    private var modelViewProjectionHandle = 0
    private var colourHandle = 0
    private val vertices: FloatBuffer

    //initial size of the cube.  set here, so it is easier to change later.
    var scale = 0.4f

    //this is the initial data, which will need to translated into the mVertices variable in the consturctor.
    var verticesData =
        floatArrayOf(
            -scale, scale, scale,  // top-left
            -scale, -scale, scale,  // bottom-left
            scale, -scale, scale,  // bottom-right
            scale, -scale, scale,  // bottom-right
            scale, scale, scale,  // top-right
            -scale, scale, scale,  // top-left
            -scale, scale, -scale,  // top-left
            -scale, -scale, -scale,  // bottom-left
            scale, -scale, -scale,  // bottom-right
            scale, -scale, -scale,  // bottom-right
            scale, scale, -scale,  // top-right
            -scale, scale, -scale,  // top-left
            -scale, scale, -scale,  // top-left
            -scale, -scale, -scale,  // bottom-left
            -scale, -scale, scale,  // bottom-right
            -scale, -scale, scale,  // bottom-right
            -scale, scale, scale,  // top-right
            -scale, scale, -scale,  // top-left
            scale, scale, -scale,  // top-left
            scale, -scale, -scale,  // bottom-left
            scale, -scale, scale,  // bottom-right
            scale, -scale, scale,  // bottom-right
            scale, scale, scale,  // top-right
            scale, scale, -scale,  // top-left
            -scale, scale, -scale,  // top-left
            -scale, scale, scale,  // bottom-left
            scale, scale, scale,  // bottom-right
            scale, scale, scale,  // bottom-right
            scale, scale, -scale,  // top-right
            -scale, scale, -scale,  // top-left
            -scale, -scale, -scale,  // top-left
            -scale, -scale, scale,  // bottom-left
            scale, -scale, scale,  // bottom-right
            scale, -scale, scale,  // bottom-right
            scale, -scale, -scale,  // top-right
            -scale, -scale, -scale // top-left
        )
    var colorCyan: FloatArray = Color.CYAN.colourToFloatArray()
    var colorBlue: FloatArray = Color.BLUE.colourToFloatArray()
    var colorRed: FloatArray = Color.RED.colourToFloatArray()
    var colorMagenta: FloatArray = Color.MAGENTA.colourToFloatArray()
    var colorGreen: FloatArray = Color.GREEN.colourToFloatArray()
    var colorYellow: FloatArray = Color.YELLOW.colourToFloatArray()

    //vertex shader code
    override val vertexShaderCode = """
        #version 300 es 			  
        uniform mat4 uMVPMatrix;     
        in vec4 vPosition;           
        void main()                  
        {                            
           gl_Position = uMVPMatrix * vPosition;  
        }                            
        """

    //fragment shader code.
    override val fragmentShaderCode = """
        #version 300 es		 			          	
        precision mediump float;					  	
        uniform vec4 vColor;	 			 		  	
        out vec4 fragColor;	 			 		  	
        void main()                                  
        {                                            
          fragColor = vColor;                    	
        }                                            
        """
    var TAG = "Cube"
    override fun render(mvpMatrix: FloatArray) {

        // Use the program object
        GLES30.glUseProgram(programReference)

        // get handle to shape's transformation matrix
        modelViewProjectionHandle = GLES30.glGetUniformLocation(programReference, "uMVPMatrix")
        //myRenderer.checkGlError("glGetUniformLocation")

        // get handle to fragment shader's vColor member
        colourHandle = GLES30.glGetUniformLocation(programReference, "vColor")


        // Apply the projection and view transformation
        GLES30.glUniformMatrix4fv(modelViewProjectionHandle, 1, false, mvpMatrix, 0)
        //myRenderer.checkGlError("glUniformMatrix4fv")
        val vertexPositionIndex = 0
        vertices.position(vertexPositionIndex) //just in case.  We did it already though.

        //add all the points to the space, so they can be correct by the transformations.
        //would need to do this even if there were no transformations actually.
        GLES30.glVertexAttribPointer(
            vertexPositionIndex, 3, GLES30.GL_FLOAT,
            false, 0, vertices
        )
        GLES30.glEnableVertexAttribArray(vertexPositionIndex)

        //Now we are ready to draw the cube finally.
        var startPos = 0
        val verticesPerFace = 6

        //draw front face
        GLES30.glUniform4fv(colourHandle, 1, colorBlue, 0)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, startPos, verticesPerFace)
        startPos += verticesPerFace

        //draw back face
        GLES30.glUniform4fv(colourHandle, 1, colorCyan, 0)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, startPos, verticesPerFace)
        startPos += verticesPerFace

        //draw left face
        GLES30.glUniform4fv(colourHandle, 1, colorRed, 0)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, startPos, verticesPerFace)
        startPos += verticesPerFace

        //draw right face
        GLES30.glUniform4fv(colourHandle, 1, colorMagenta, 0)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, startPos, verticesPerFace)
        startPos += verticesPerFace

        //draw top face
        GLES30.glUniform4fv(colourHandle, 1, colorGreen, 0)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, startPos, verticesPerFace)
        startPos += verticesPerFace

        //draw bottom face
        GLES30.glUniform4fv(colourHandle, 1, colorYellow, 0)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, startPos, verticesPerFace)
        //last face, so no need to increment.
    }

    //finally some methods
    //constructor
    init {
        //first setup the mVertices correctly.
        vertices = ByteBuffer
            .allocateDirect(verticesData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(verticesData)
        vertices.position(0)

        //setup the shaders
        val linked = IntArray(1)

        // Load the vertex/fragment shaders
        val vertexShader: Int = SurfaceRenderer.loadShader(GLES30.GL_VERTEX_SHADER,
            vertexShaderCode
        )
        val fragmentShader: Int = SurfaceRenderer.loadShader(GLES30.GL_FRAGMENT_SHADER,
            fragmentShaderCode
        )

        // Create the program object
        val programObject: Int = GLES30.glCreateProgram()
//        if (programObject == 0) {
//            Log.e(TAG, "So some kind of error, but what?")
//            return
//        }
        GLES30.glAttachShader(programObject, vertexShader)
        GLES30.glAttachShader(programObject, fragmentShader)

        // Bind vPosition to attribute 0
        GLES30.glBindAttribLocation(programObject, 0, "vPosition")

        // Link the program
        GLES30.glLinkProgram(programObject)

        // Check the link status
        GLES30.glGetProgramiv(programObject, GLES30.GL_LINK_STATUS, linked, 0)
//        if (linked[0] == 0) {
//            Log.e(TAG, "Error linking program:")
//            Log.e(TAG, GLES30.glGetProgramInfoLog(programObject))
//            GLES30.glDeleteProgram(programObject)
//            return
//        }

        // Store the program object
        programReference = programObject

        //now everything is setup and ready to draw.
    }
}