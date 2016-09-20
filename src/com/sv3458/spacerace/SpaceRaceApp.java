package com.sv3458.spacerace;

import android.app.Application;

import java.util.HashMap;

public class SpaceRaceApp extends Application {

    public int myRandomNum;
    private HashMap<String, Integer> glPrograms;
    private HashMap<String, Integer> glTextures;

    public SpaceRaceApp() {
        super();

        myRandomNum = 10;

        glPrograms = new HashMap<String, Integer>();
        glTextures = new HashMap<String, Integer>();
    }

    public void putGLTexture(String key, int val) {
        glTextures.put(key, new Integer(val));
    }

    public void putGLProgram(String key, int val) {
        glPrograms.put(key, new Integer(val));
    }

    public int getGLTexture(String key) {
        return (Integer)glTextures.get(key).intValue();
    }

    public int getGLProgram(String key) {
        return (Integer)glPrograms.get(key).intValue();
    }
}
