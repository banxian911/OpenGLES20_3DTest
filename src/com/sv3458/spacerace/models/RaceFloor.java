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
import android.os.SystemClock;

import com.example.android.opengl.R;
import com.sv3458.spacerace.game.GamePlay;
import com.sv3458.spacerace.game.GameRenderer;
import com.sv3458.spacerace.Util.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * A two-dimensional square for use as a drawn object in OpenGL ES 2.0.
 */
public class RaceFloor {
    private final FloatBuffer vertexBuffer;
    private final FloatBuffer texcoordBuffer;
    private final ShortBuffer drawListBuffer;
    private int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;
    private int mTextureDataHandle;
    private Context activityContext;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float squareCoords[] = {
            -150f, -1.0f,  150f,   // top left
            -150f, -1.0f, -150f,   // bottom left
             150f, -1.0f, -150f,   // bottom right
             150f, -1.0f,  150f}; // top right

    private final short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    float color[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public RaceFloor(Context pActivityContext) {
        activityContext=pActivityContext;
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
        // (# of coordinate values * 4 bytes per float)
                squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);


        // tex coordinates
        texcoordBuffer = ByteBuffer.allocateDirect(4 * 2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        texcoordBuffer.put(new float[] {
                0.0f, 0.0f,
                50.0f, 0.0f,
                50.0f, 50.0f,
                0.0f, 50.0f
        }).position(0);


        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

    }

    public void prepareGLcontext() {

        // prepare shaders and OpenGL program
        int vertexShader = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, RawResourceReader.readTextFileFromRawResource(activityContext, R.raw.racefloor_vertex_shader));
        int fragmentShader = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, RawResourceReader.readTextFileFromRawResource(activityContext, R.raw.racefloor_fragment_shader));

        mProgram = ShaderHelper.createAndLinkProgram(vertexShader, fragmentShader, new String[] {});

        mTextureDataHandle = TextureHelper.loadTexture(activityContext, R.drawable.racefloor2);

    }

    public void draw(float[] mvpMatrix, float playerOrientationY) {
/*
        float xAdd = (float)(GamePlay.ANIM_FLOOR_SPEED * Math.cos(Math.toRadians(playerOrientationY)));
        float yAdd = (float)(GamePlay.ANIM_FLOOR_SPEED * Math.sin(Math.toRadians(playerOrientationY)));
        */
        float xAdd = GamePlay.ANIM_FLOOR_SPEED;
        float yAdd = 0;
        //xAdd=yAdd=0;

        texcoordBuffer.rewind();
        texcoordBuffer.put(0,texcoordBuffer.get(0)+xAdd);
        texcoordBuffer.put(2,texcoordBuffer.get(2)+xAdd);
        texcoordBuffer.put(4,texcoordBuffer.get(4)+xAdd);
        texcoordBuffer.put(6,texcoordBuffer.get(6)+xAdd);

        texcoordBuffer.put(1,texcoordBuffer.get(1)+yAdd);
        texcoordBuffer.put(3,texcoordBuffer.get(3)+yAdd);
        texcoordBuffer.put(5,texcoordBuffer.get(5)+yAdd);
        texcoordBuffer.put(7,texcoordBuffer.get(7)+yAdd);



        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        // Positions
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);


        // color
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);


        // Pass in the texture coordinate information
        int mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgram, "a_TexCoordinate");
        texcoordBuffer.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, 2, GLES20.GL_FLOAT, false,
                0, texcoordBuffer);
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

        // Textures
        int mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "u_Texture");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
        GLES20.glUniform1i(mTextureUniformHandle, 0);


        // Transformation
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GameRenderer.checkGlError("glGetUniformLocation");
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        GameRenderer.checkGlError("glUniformMatrix4fv");


        // Draw the square
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

}