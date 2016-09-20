/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sv3458.spacerace.models;

import android.content.Context;
import android.opengl.GLES20;

import com.example.android.opengl.R;
import com.sv3458.spacerace.Util.RawResourceReader;
import com.sv3458.spacerace.Util.ShaderHelper;
import com.sv3458.spacerace.Util.TextureHelper;
import com.sv3458.spacerace.game.GameRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * A two-dimensional square for use as a drawn object in OpenGL ES 2.0.
 */
public class SkyBox {
    private final FloatBuffer vertexBuffer;
    private int mProgram;
    private int mPositionHandle;
    private int mMVPMatrixHandle;
    private int mTextureDataHandle;
    private Context activityContext;
/**/
    final float[] cubePositionData =
            {
                    // Front face
                    -1.0f, 1.0f, 1.0f,
                    -1.0f, -1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f,
                    -1.0f, -1.0f, 1.0f,
                    1.0f, -1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f,

                    // Right face
                    1.0f, 1.0f, 1.0f,
                    1.0f, -1.0f, 1.0f,
                    1.0f, 1.0f, -1.0f,
                    1.0f, -1.0f, 1.0f,
                    1.0f, -1.0f, -1.0f,
                    1.0f, 1.0f, -1.0f,

                    // Back face
                    1.0f, 1.0f, -1.0f,
                    1.0f, -1.0f, -1.0f,
                    -1.0f, 1.0f, -1.0f,
                    1.0f, -1.0f, -1.0f,
                    -1.0f, -1.0f, -1.0f,
                    -1.0f, 1.0f, -1.0f,

                    // Left face
                    -1.0f, 1.0f, -1.0f,
                    -1.0f, -1.0f, -1.0f,
                    -1.0f, 1.0f, 1.0f,
                    -1.0f, -1.0f, -1.0f,
                    -1.0f, -1.0f, 1.0f,
                    -1.0f, 1.0f, 1.0f,

                    // Top face
                    -1.0f, 1.0f, -1.0f,
                    -1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, -1.0f,
                    -1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, 1.0f,
                    1.0f, 1.0f, -1.0f,

                    // Bottom face
                    1.0f, -1.0f, -1.0f,
                    1.0f, -1.0f, 1.0f,
                    -1.0f, -1.0f, -1.0f,
                    1.0f, -1.0f, 1.0f,
                    -1.0f, -1.0f, 1.0f,
                    -1.0f, -1.0f, -1.0f,
            };
/**/


    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public SkyBox(Context pActivityContext) {
        activityContext = pActivityContext;

        // initialize vertex byte buffer for shape coordinates
        float[] scaledPositions = new float[cubePositionData.length];
        for(int i=0; i<cubePositionData.length; i++) {
            scaledPositions[i] = cubePositionData[i]*75;
        }
        vertexBuffer = ByteBuffer.allocateDirect(cubePositionData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(scaledPositions).position(0);
    }

    public void prepareGLcontext() {
        // prepare shaders and OpenGL program
        int vertexShader = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, RawResourceReader.readTextFileFromRawResource(activityContext, R.raw.skybox_vertex_shader));
        int fragmentShader = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, RawResourceReader.readTextFileFromRawResource(activityContext, R.raw.skybox_fragment_shader));

        mProgram = ShaderHelper.createAndLinkProgram(vertexShader, fragmentShader, new String[] {});

        mTextureDataHandle = TextureHelper.loadCubeMap(activityContext, new int[] {
                R.drawable.skybox1_right,
                R.drawable.skybox1_left,
                R.drawable.skybox1_top,
                R.drawable.skybox1_bottom,
                R.drawable.skybox1_front,
                R.drawable.skybox1_back
        });
    }

    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        // Positions
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(
                mPositionHandle, 3,
                GLES20.GL_FLOAT, false,
                0, vertexBuffer);

        // Textures
        int mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "u_Texture");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, mTextureDataHandle);
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        // Transformation
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GameRenderer.checkGlError("glGetUniformLocation");
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        GameRenderer.checkGlError("glUniformMatrix4fv");

        // Draw the square
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

}