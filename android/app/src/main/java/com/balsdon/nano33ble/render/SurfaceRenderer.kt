package com.balsdon.nano33ble.render
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.balsdon.nano33ble.render.shape.Cube
import com.balsdon.nano33ble.render.shape.Square
import com.balsdon.nano33ble.render.shape.Triangle
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

//https://github.com/JimSeker/opengl/blob/master/OpenGL30Cube/app/src/main/java/edu/cs4730/opengl30cube/myRenderer.java

class SurfaceRenderer : GLSurfaceView.Renderer {
    private lateinit var triangle: Triangle
    private lateinit var square: Square
    lateinit var cube: Cube

    private val modelViewProjectionMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val rotationMatrix = FloatArray(16)

    var angle = 0f
    var transX = 0f
    var transY = 0f
    var transZ = 1.0f

    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        triangle = Triangle()
        square = Square()
        cube = Cube()
    }

    override fun onDrawFrame(unused: GL10) {
        // Clear the color buffer  set above by glClearColor.
        // Clear the color buffer  set above by glClearColor.
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        //need this otherwise, it will over-write stuff and the cube will look wrong!
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)

        // Set the camera position (View matrix)  note Matrix is an include, not a declared method.
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)

        // Create a rotation and translation for the cube
        Matrix.setIdentityM(rotationMatrix, 0)

        //move the cube up/down and left/right
        Matrix.translateM(rotationMatrix, 0, transX, transY, transZ)

        //mangle is how fast, x,y,z which directions it rotates.
        Matrix.rotateM(rotationMatrix, 0, 0f, 1.0f, 1.0f, 1.0f)

        // combine the model with the view matrix
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, viewMatrix, 0, rotationMatrix, 0)

        // combine the model-view with the projection matrix
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewProjectionMatrix, 0)

        //square.draw(modelViewProjectionMatrix)
        //triangle.draw(modelViewProjectionMatrix)
//        cube.rotationY += 0.4f
//        cube.rotationZ += 0.4f
        cube.draw(modelViewProjectionMatrix)
        //change the angle, so the cube will spin.

    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES30.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
    }

    companion object {
        private const val TAG = "SurfaceRenderer"

        fun loadShader(type: Int, shaderCode: String?): Int {
            val shader = GLES30.glCreateShader(type)
            GLES30.glShaderSource(shader, shaderCode)
            GLES30.glCompileShader(shader)
            return shader
        }

        fun checkGlError(glOperation: String) {
            var error: Int
            while (GLES30.glGetError().also { error = it } != GLES30.GL_NO_ERROR) {
                Log.e(TAG, "$glOperation: glError $error")
                throw RuntimeException("$glOperation: glError $error")
            }
        }
    }
}