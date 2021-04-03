package com.balsdon.nano33ble.render.shape

import android.opengl.Matrix


abstract class Shape() {
    companion object {
        const val coordinatesPerVertex = 3
    }

    protected abstract val programReference: Int

    var translationX: Float = 0f
    var translationY: Float = 0f
    var translationZ: Float = 0f
    var rotationX: Float = 0f
    var rotationY: Float = 0f
    var rotationZ: Float = 0f

    protected open val vertexShaderCode =  // This matrix member variable provides a hook to manipulate
        // the coordinates of the objects that use this vertex shader
        "uniform mat4 uMVPMatrix;" +
                "attribute vec4 vPosition;" +
                "void main() {" +  // The matrix must be included as a modifier of gl_Position.
                // Note that the uMVPMatrix factor *must be first* in order
                // for the matrix multiplication product to be correct.
                "  gl_Position = uMVPMatrix * vPosition;" +
                "}"
    protected open val fragmentShaderCode = ("precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}")

    protected fun applyRotationTranslation(modelViewProjectionMatrix:FloatArray) : FloatArray {
        val rotationMatrix = FloatArray(16)
        val shapeMatrix = FloatArray(16)
        Matrix.setIdentityM(rotationMatrix, 0)
        Matrix.translateM(rotationMatrix, 0, translationX, translationY, translationZ)
        Matrix.rotateM(rotationMatrix, 0, rotationX, 1.0f, 0.0f, 0.0f)
        Matrix.rotateM(rotationMatrix, 0, rotationY, 0.0f, 1.0f, 0.0f)
        Matrix.rotateM(rotationMatrix, 0, rotationZ, 0.0f, 0.0f, 1.0f)
        Matrix.multiplyMM(shapeMatrix, 0, modelViewProjectionMatrix, 0, rotationMatrix, 0)
        return shapeMatrix
    }

    protected abstract fun render(modelViewProjectionMatrix:FloatArray)

    fun draw(modelViewProjectionMatrix:FloatArray) =
        render(applyRotationTranslation(modelViewProjectionMatrix))
}