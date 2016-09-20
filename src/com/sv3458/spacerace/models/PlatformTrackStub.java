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
import com.sv3458.spacerace.game.GameRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * A two-dimensional square for use as a drawn object in OpenGL ES 2.0.
 */
public class PlatformTrackStub implements PlatformTrackInterface {
    public PlatformTrackStub() {
    }

    public void prepareGLcontext() {
    }

    public void draw(float[] mvpMatrix) {
    }

    public void permTransform(float[] pPermTransform) {
    }

    public short getTrackType() {
        return PlatformTrack.TRACK_STRAIGHT;
    }
}