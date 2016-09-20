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
import android.opengl.Matrix;

import com.example.android.opengl.R;
import com.sv3458.spacerace.Util.RawResourceReader;
import com.sv3458.spacerace.Util.ShaderHelper;
import com.sv3458.spacerace.Util.TextureHelper;
import com.sv3458.spacerace.game.GameRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class ScreenText {
    private final FloatBuffer vertexBuffer;
    private final FloatBuffer texcoordBuffer;
    private final ShortBuffer drawListBuffer;
    private int mProgram;
    private int mTextureDataHandle;
    private Context activityContext;

    private String textToDisplay;
    private int drawOrderLength;
    private float[] orthoProjectionM;

    public ScreenText(Context pActivityContext, String pText) {
        float vertexCoords[], textureCoords[];
        short drawOrder[];
        textToDisplay = pText;
        activityContext = pActivityContext;

        float letterWidth = 0.1f;
        float letterHeight = 0.2f;

        int numLetters = textToDisplay.length();
        vertexCoords = new float[numLetters*4*3];   // 4 vertexes by 3 dimensions per letter
        textureCoords = new float[numLetters*4*2];   // 4 vertexes by 2 texture coordinates per letter
        drawOrder = new short[numLetters*3*2];     // 3 vertexes for 2 triangles per letter
        float charX,charY, coordIncrement = 1f/16f;
        for(int i=0; i<numLetters; i++) {
            vertexCoords[4*3*i+0] = letterWidth*i + 0;
            vertexCoords[4*3*i+1] = letterHeight;
            vertexCoords[4*3*i+2] = 0f;
            vertexCoords[4*3*i+3] = letterWidth*i + 0;
            vertexCoords[4*3*i+4] = 0;
            vertexCoords[4*3*i+5] = 0f;
            vertexCoords[4*3*i+6] = letterWidth*i + letterWidth;
            vertexCoords[4*3*i+7] = 0;
            vertexCoords[4*3*i+8] = 0f;
            vertexCoords[4*3*i+9] = letterWidth*i + letterWidth;
            vertexCoords[4*3*i+10] = letterHeight;
            vertexCoords[4*3*i+11] = 0f;

            charX = (float)(((int) textToDisplay.charAt(i)-32)%16);
            charY = (float)(((int) textToDisplay.charAt(i)-32)/16);
            textureCoords[4*2*i+0] = charX*coordIncrement + 0;
            textureCoords[4*2*i+1] = charY*coordIncrement + 0;
            textureCoords[4*2*i+2] = charX*coordIncrement + 0;
            textureCoords[4*2*i+3] = charY*coordIncrement + coordIncrement;
            textureCoords[4*2*i+4] = charX*coordIncrement + coordIncrement;
            textureCoords[4*2*i+5] = charY*coordIncrement + coordIncrement;
            textureCoords[4*2*i+6] = charX*coordIncrement + coordIncrement;
            textureCoords[4*2*i+7] = charY*coordIncrement + 0;

            drawOrder[3*2*i+0] =  (short)(0+(i*4));
            drawOrder[3*2*i+1] =  (short)(1+(i*4));
            drawOrder[3*2*i+2] =  (short)(2+(i*4));
            drawOrder[3*2*i+3] =  (short)(0+(i*4));
            drawOrder[3*2*i+4] =  (short)(2+(i*4));
            drawOrder[3*2*i+5] =  (short)(3+(i*4));
        }
        drawOrderLength = drawOrder.length;

        // initialize vertex byte buffer for shape coordinates
        vertexBuffer = ByteBuffer.allocateDirect(vertexCoords.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(vertexCoords).position(0);

        texcoordBuffer = ByteBuffer.allocateDirect(textureCoords.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        texcoordBuffer.put(textureCoords).position(0);

        drawListBuffer = ByteBuffer.allocateDirect(drawOrder.length * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
        drawListBuffer.put(drawOrder).position(0);

        orthoProjectionM = new float[16];
        Matrix.orthoM(orthoProjectionM, 0, -1f, 1f, -1f, 1f, -1f, 1f);
    }

    public void prepareGLcontext() {
        // prepare shaders and OpenGL program
        int vertexShader = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, RawResourceReader.readTextFileFromRawResource(activityContext, R.raw.racefloor_vertex_shader));
        int fragmentShader = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, RawResourceReader.readTextFileFromRawResource(activityContext, R.raw.racefloor_fragment_shader));
        mProgram = ShaderHelper.createAndLinkProgram(vertexShader, fragmentShader, new String[] {});

        mTextureDataHandle = TextureHelper.loadTexture(activityContext, R.drawable.textfont1);
    }

    public void draw() {
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        // Positions
        int mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        vertexBuffer.position(0);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(
                mPositionHandle, 3,
                GLES20.GL_FLOAT, false,
                3*4, vertexBuffer);


        // Pass in the texture coordinate information
        int mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgram, "a_TexCoordinate");
        texcoordBuffer.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, 2, GLES20.GL_FLOAT, false,
                2*4, texcoordBuffer);
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

        // Textures
        int mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "u_Texture");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
        GLES20.glUniform1i(mTextureUniformHandle, 0);


        // Transformation
        int mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GameRenderer.checkGlError("glGetUniformLocation");
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, orthoProjectionM, 0);
        GameRenderer.checkGlError("glUniformMatrix4fv");


        // Draw the square
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, drawOrderLength,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

}