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

/**
 * A two-dimensional square for use as a drawn object in OpenGL ES 2.0.
 */
public class PlatformTrack implements PlatformTrackInterface{

    public static short TRACK_STRAIGHT = 1;
    public static short TRACK_TURN_LEFT = 2;
    public static short TRACK_TURN_RIGHT = 3;

    public static float platformWidth = 6.0f;
    public static float platformHeight = 2.0f;
    public static float platformLength = 45.0f;
    public static float platformSpacing = 10.0f;

    private int numSegments = 10;

    private final FloatBuffer vertexBuffer;
    private final ShortBuffer drawListBuffer;
    private int mProgram;
    private int mTextureDataHandle;
    private int drawOrderLength;
    private Context activityContext;

    private short trackType;

    float color[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public PlatformTrack(Context pActivityContext, short pTrackType) {
        float trackCoords[];
        short drawOrder[];
        trackType = pTrackType;
        activityContext = pActivityContext;

        if(pTrackType==PlatformTrack.TRACK_STRAIGHT) {
            trackCoords = new float[numSegments*4*3];   // 4 vertexes by 3 dimensions per section
            drawOrder = new short[numSegments*3*2];     // 3 vertexes for 2 triangles per section
            for(int i=0; i<numSegments; i++) {
                trackCoords[4*3*i+0] = -platformWidth/2;
                trackCoords[4*3*i+1] = platformHeight;
                trackCoords[4*3*i+2] = -(platformLength*(i+1)/numSegments);
                trackCoords[4*3*i+3] = -platformWidth/2;
                trackCoords[4*3*i+4] = platformHeight;
                trackCoords[4*3*i+5] = -(platformLength*(i)/numSegments);
                trackCoords[4*3*i+6] = platformWidth/2;
                trackCoords[4*3*i+7] = platformHeight;
                trackCoords[4*3*i+8] = -(platformLength*(i)/numSegments);
                trackCoords[4*3*i+9] = platformWidth/2;
                trackCoords[4*3*i+10] = platformHeight;
                trackCoords[4*3*i+11] = -(platformLength*(i+1)/numSegments);
                drawOrder[3*2*i+0] =  (short)(0+(i*4));
                drawOrder[3*2*i+1] =  (short)(1+(i*4));
                drawOrder[3*2*i+2] =  (short)(2+(i*4));
                drawOrder[3*2*i+3] =  (short)(0+(i*4));
                drawOrder[3*2*i+4] =  (short)(2+(i*4));
                drawOrder[3*2*i+5] =  (short)(3+(i*4));
            }
        } else if(pTrackType==PlatformTrack.TRACK_TURN_LEFT || pTrackType==PlatformTrack.TRACK_TURN_RIGHT)  {
            trackCoords = new float[numSegments*4*3+numSegments*4*3];   // 4 vertexes by 3 dimensions per section, and again for straight section
            drawOrder = new short[numSegments*3*2+numSegments*3*2];     // 3 vertexes for 2 triangles per section, and again for straight section
            float radMin = platformLength-platformWidth;
            float radMax = platformLength;
            int ixOffsetCoord = numSegments*4*3;
            int ixOffsetOrder = numSegments*3*2;
            float dirMul=1;
            if(pTrackType==PlatformTrack.TRACK_TURN_RIGHT) dirMul=-1;
            for(int i=0; i<numSegments; i++) {
                trackCoords[4*3*i+0] = dirMul*(-platformLength+(float)(Math.cos((Math.PI/2)*(i+1)/numSegments)*radMin) + platformWidth/2);
                trackCoords[4*3*i+1] = platformHeight;
                trackCoords[4*3*i+2] = -(float)(Math.sin((Math.PI / 2) * (i + 1) / numSegments)*radMin);
                trackCoords[4*3*i+3] = dirMul*(-platformLength+(float)(Math.cos((Math.PI/2)*(i)/numSegments)*radMin) + platformWidth/2);
                trackCoords[4*3*i+4] = platformHeight;
                trackCoords[4*3*i+5] = -(float)(Math.sin((Math.PI / 2) * (i) / numSegments)*radMin);
                trackCoords[4*3*i+6] = dirMul*(-platformLength+(float)(Math.cos((Math.PI/2)*(i)/numSegments)*radMax) + platformWidth/2);
                trackCoords[4*3*i+7] = platformHeight;
                trackCoords[4*3*i+8] = -(float)(Math.sin((Math.PI / 2) * (i) / numSegments)*radMax);
                trackCoords[4*3*i+9] = dirMul*(-platformLength+(float)(Math.cos((Math.PI/2)*(i+1)/numSegments)*radMax) + platformWidth/2);
                trackCoords[4*3*i+10] = platformHeight;
                trackCoords[4*3*i+11] = -(float)(Math.sin((Math.PI / 2) * (i + 1) / numSegments)*radMax);
                drawOrder[3*2*i+0] =  (short)(0+(i*4));
                drawOrder[3*2*i+1] =  (short)(1+(i*4));
                drawOrder[3*2*i+2] =  (short)(2+(i*4));
                drawOrder[3*2*i+3] =  (short)(0+(i*4));
                drawOrder[3*2*i+4] =  (short)(2+(i*4));
                drawOrder[3*2*i+5] =  (short)(3+(i*4));
            }
            for(int i=0; i<numSegments; i++) {
                trackCoords[ixOffsetCoord+4*3*i+0] = dirMul*(-platformLength + platformWidth/2 - (platformLength*(i+1)/numSegments));
                trackCoords[ixOffsetCoord+4*3*i+1] = platformHeight;
                trackCoords[ixOffsetCoord+4*3*i+2] = -radMin;
                trackCoords[ixOffsetCoord+4*3*i+3] = dirMul*(-platformLength + platformWidth/2 - (platformLength*(i)/numSegments));
                trackCoords[ixOffsetCoord+4*3*i+4] = platformHeight;
                trackCoords[ixOffsetCoord+4*3*i+5] = -radMin;
                trackCoords[ixOffsetCoord+4*3*i+6] = dirMul*(-platformLength + platformWidth/2 - (platformLength*(i)/numSegments));
                trackCoords[ixOffsetCoord+4*3*i+7] = platformHeight;
                trackCoords[ixOffsetCoord+4*3*i+8] = -radMax;
                trackCoords[ixOffsetCoord+4*3*i+9] = dirMul*(-platformLength + platformWidth/2 - (platformLength*(i+1)/numSegments));
                trackCoords[ixOffsetCoord+4*3*i+10] = platformHeight;
                trackCoords[ixOffsetCoord+4*3*i+11] = -radMax;
                drawOrder[ixOffsetOrder+3*2*i+0] =  (short)(0+(i*4)+(numSegments*4));
                drawOrder[ixOffsetOrder+3*2*i+1] =  (short)(1+(i*4)+(numSegments*4));
                drawOrder[ixOffsetOrder+3*2*i+2] =  (short)(2+(i*4)+(numSegments*4));
                drawOrder[ixOffsetOrder+3*2*i+3] =  (short)(0+(i*4)+(numSegments*4));
                drawOrder[ixOffsetOrder+3*2*i+4] =  (short)(2+(i*4)+(numSegments*4));
                drawOrder[ixOffsetOrder+3*2*i+5] =  (short)(3+(i*4)+(numSegments*4));
            }
        } else {
            throw new RuntimeException("Invalid argument");
        }
        drawOrderLength = drawOrder.length;

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
        // (# of coordinate values * 4 bytes per float)
                trackCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(trackCoords);
        vertexBuffer.position(0);



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
        int vertexShader = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, RawResourceReader.readTextFileFromRawResource(activityContext, R.raw.platformtrack_vertex_shader));
        int fragmentShader = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, RawResourceReader.readTextFileFromRawResource(activityContext, R.raw.platformtrack_fragment_shader));
        mProgram = ShaderHelper.createAndLinkProgram(vertexShader, fragmentShader, new String[]{"uMVPMatrix","vPosition","vColor"});

    }

    public void draw(float[] mvpMatrix) {
        int mPositionHandle, mColorHandle, mMVPMatrixHandle;
/*
        int vertexShader = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, RawResourceReader.readTextFileFromRawResource(activityContext, R.raw.platformtrack_vertex_shader));
        int fragmentShader = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, RawResourceReader.readTextFileFromRawResource(activityContext, R.raw.platformtrack_fragment_shader));
        mProgram = ShaderHelper.createAndLinkProgram(vertexShader, fragmentShader, new String[]{});
*/
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        // Positions
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
                GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(
                mPositionHandle, 3,
                GLES20.GL_FLOAT, false,
                3*4, vertexBuffer);


        // color
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
                GLES20.glUniform4fv(mColorHandle, 1, color, 0);


        // Transformation
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GameRenderer.checkGlError("glGetUniformLocation");
                GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
                GameRenderer.checkGlError("glUniformMatrix4fv");


        // Draw
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, drawOrderLength,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    public void permTransform(float[] pPermTransform) {
        float[] vertex = new float[4];
        float[] vertexNew = new float[4];
        vertexBuffer.rewind();
        for(int i=0; i<(vertexBuffer.capacity()/3); i++) {
            vertex[0] = vertexBuffer.get(i*3+0);
            vertex[1] = vertexBuffer.get(i*3+1);
            vertex[2] = vertexBuffer.get(i*3+2);
            vertex[3] = 1f;
            Matrix.multiplyMV(vertexNew, 0, pPermTransform, 0, vertex, 0);
            vertexBuffer.put(i*3+0, vertexNew[0]);
            vertexBuffer.put(i*3+1, vertexNew[1]);
            vertexBuffer.put(i*3+2, vertexNew[2]);
        }
    }

    public short getTrackType() {
        return trackType;
    }
}