package com.sv3458.spacerace.models;

import android.content.Context;
import android.opengl.Matrix;

public class TrackModel {
    private static short ORIENT_NORTH   = 1;
    private static short ORIENT_EAST    = 2;
    private static short ORIENT_SOUTH   = 3;
    private static short ORIENT_WEST    = 4;

    private PlatformTrackInterface[][] platformTrack;
    private short curTrackOrientation;
    private short curTrackLane;
    private Context activityContext;

    public TrackModel(Context pActivityContext) {
        platformTrack = new PlatformTrackInterface[3][3];
        activityContext = pActivityContext;

        for(int i=0; i<platformTrack.length; i++) {
            for(int j=0; j<platformTrack[i].length; j++) {
                platformTrack[i][j] = null;
            }
        }

        curTrackOrientation = this.ORIENT_NORTH;
        curTrackLane = 1;   // center lane

        genNewTrack(false, false, 1);
        genNewTrack(false, true, 2);
    }

    public void prepareGLcontext() {
        for(int i=0; i<platformTrack.length; i++) {
            for(int j=0; j<platformTrack[i].length; j++) {
                if(platformTrack[i][j]!=null)
                    platformTrack[i][j].prepareGLcontext();
            }
        }
    }

    public void draw(float[] mvpMatrix) {
        for(int i=0; i<platformTrack.length; i++) {
            for(int j=0; j<platformTrack[i].length; j++) {
                if(platformTrack[i][j]!=null)
                    platformTrack[i][j].draw(mvpMatrix);
            }
        }
    }

    public void permTransform(float[] pPermTransform) {
        for(int i=0; i<platformTrack.length; i++) {
            for(int j=0; j<platformTrack[i].length; j++) {
                if(platformTrack[i][j]!=null)
                    platformTrack[i][j].permTransform(pPermTransform);
            }
        }
    }

    private void genNewTrack() {
        genNewTrack(true, true, 2);
    }

    private void genNewTrack(boolean prepGL, boolean randomTrack, int gridPos) {
        if(randomTrack) {
            if(platformTrack[0][gridPos-1]==null)
                platformTrack[0][gridPos] = new PlatformTrack(activityContext, PlatformTrack.TRACK_STRAIGHT);
            else
                if(platformTrack[0][gridPos-1].getTrackType()==PlatformTrack.TRACK_TURN_LEFT)
                    // no track piece after turn
                    platformTrack[0][gridPos] = null;
                else {
                    if (Math.random() > 0.75)
                        platformTrack[0][gridPos] = new PlatformTrack(activityContext, PlatformTrack.TRACK_TURN_LEFT);
                    else
                        platformTrack[0][gridPos] = new PlatformTrack(activityContext, PlatformTrack.TRACK_STRAIGHT);
                }

            platformTrack[1][gridPos] = new PlatformTrack(activityContext, PlatformTrack.TRACK_STRAIGHT);

            if(platformTrack[2][gridPos-1]==null)
                platformTrack[2][gridPos] = new PlatformTrack(activityContext, PlatformTrack.TRACK_STRAIGHT);
            else
                if(platformTrack[2][gridPos-1].getTrackType()==PlatformTrack.TRACK_TURN_RIGHT)
                    // no track piece after turn
                    platformTrack[2][gridPos] = null;
                else {
                    if (Math.random() > 0.75)
                        platformTrack[2][gridPos] = new PlatformTrack(activityContext, PlatformTrack.TRACK_TURN_RIGHT);
                    else
                        platformTrack[2][gridPos] = new PlatformTrack(activityContext, PlatformTrack.TRACK_STRAIGHT);
                }
        } else {
            platformTrack[0][gridPos] = new PlatformTrack(activityContext, PlatformTrack.TRACK_STRAIGHT);
            platformTrack[1][gridPos] = new PlatformTrack(activityContext, PlatformTrack.TRACK_STRAIGHT);
            platformTrack[2][gridPos] = new PlatformTrack(activityContext, PlatformTrack.TRACK_STRAIGHT);
        }

        if(prepGL) {
            if(platformTrack[0][gridPos]!=null) platformTrack[0][gridPos].prepareGLcontext();
            if(platformTrack[1][gridPos]!=null) platformTrack[1][gridPos].prepareGLcontext();
            if(platformTrack[2][gridPos]!=null) platformTrack[2][gridPos].prepareGLcontext();
        }

        float laneShift=0, advanceShift = 0;
        if(curTrackLane==0) laneShift = PlatformTrack.platformSpacing;
        if(curTrackLane==1) laneShift = 0;
        if(curTrackLane==2) laneShift = -PlatformTrack.platformSpacing;

        if(gridPos==2) advanceShift=PlatformTrack.platformLength;

        float[] matrix = new float[16];
        Matrix.setIdentityM(matrix, 0);
        Matrix.translateM(matrix, 0, -PlatformTrack.platformSpacing + laneShift, 0, -advanceShift);
        if(platformTrack[0][gridPos]!=null) platformTrack[0][gridPos].permTransform(matrix);

        Matrix.setIdentityM(matrix, 0);
        Matrix.translateM(matrix, 0, 0 + laneShift, 0, -advanceShift);
        if(platformTrack[1][gridPos]!=null) platformTrack[1][gridPos].permTransform(matrix);

        Matrix.setIdentityM(matrix, 0);
        Matrix.translateM(matrix, 0, PlatformTrack.platformSpacing + laneShift, 0, -advanceShift);
        if(platformTrack[2][gridPos]!=null) platformTrack[2][gridPos].permTransform(matrix);
    }

    public void advanceTrack() {
        platformTrack[0][0] = platformTrack[0][1];
        platformTrack[1][0] = platformTrack[1][1];
        platformTrack[2][0] = platformTrack[2][1];

        platformTrack[0][1] = platformTrack[0][2];
        platformTrack[1][1] = platformTrack[1][2];
        platformTrack[2][1] = platformTrack[2][2];

        genNewTrack();
    }
    public void rotateTrackCCW() {
        platformTrack[0][0] = platformTrack[0][1];
        platformTrack[1][0] = null;
        platformTrack[2][0] = null;

        platformTrack[0][1] = new PlatformTrackStub();
        platformTrack[1][1] = null;
        platformTrack[2][1] = null;

        genNewTrack();
    }

    public void rotateTrackCW() {
        platformTrack[0][0] = null;
        platformTrack[1][0] = null;
        platformTrack[2][0] = platformTrack[2][1];

        platformTrack[0][1] = null;
        platformTrack[1][1] = null;
        platformTrack[2][1] = new PlatformTrackStub();

        genNewTrack();
    }

    public void mergeTrackLeft() { curTrackLane--; }

    public void mergeTrackRight() {
        curTrackLane++;
    }

    public short getTrackLanePos() {
        return curTrackLane;
    }

    public boolean isAllowedToMergeLeft() {
        if(curTrackLane==0)
            return false;
        else
            if(platformTrack[curTrackLane-1][1]!=null && platformTrack[curTrackLane-1][1].getTrackType()==PlatformTrack.TRACK_STRAIGHT)
                return true;
            else
                return false;
    }

    public boolean isAllowedToMergeRight() {
        if(curTrackLane==2)
            return false;
        else
            if(platformTrack[curTrackLane+1][1]!=null && platformTrack[curTrackLane+1][1].getTrackType()==PlatformTrack.TRACK_STRAIGHT)
                return true;
            else
                return false;
    }

    public PlatformTrackInterface getTrackObj(int pos) {
        return platformTrack[pos][1];
    }

}