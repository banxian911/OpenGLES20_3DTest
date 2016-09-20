package com.sv3458.spacerace.game;

import android.app.Activity;
import android.opengl.Matrix;
import android.os.SystemClock;

import com.sv3458.spacerace.SpaceRaceApp;
import com.sv3458.spacerace.models.PlatformTrack;
import com.sv3458.spacerace.models.TrackModel;

public class GamePlay {
    public static float ANIM_GLIDE_SPEED = 0.3f;
    public static float ANIM_MERGE_SPEED = 0.1f;
    public static float ANIM_FLOOR_SPEED = 0.1f;

    private TrackModel trackModel;
    float[] animationMatrix;

    private boolean isTurningLeft = false;
    private boolean isTurningRight = false;
    private boolean isAdvancingForward = false;
    private boolean isMergingLeft = false;
    private boolean isMergingRight = false;
    private boolean isStayingInLane = false;
    private long lastAnimamtionTime = 0;
    private float playerOrientationAngle = 0;

    private float animLeftTurnAngle;
    private float animRightTurnAngle;
    private float animAdvanceForwardPos;
    private float animMergePos;
    private float animStayingInLaneTime;
//    private short debugStepCount = 0;

    public GamePlay(Activity pActivityContext, TrackModel pTrackModel) {
        animationMatrix = new float[16];

        trackModel = pTrackModel;
    }

    public void animateFrame() {
        boolean isAnimating = false;

        Matrix.setIdentityM(animationMatrix, 0);

        if(isTurningLeft) {
            animLeftTurnAngle+= this.ANIM_GLIDE_SPEED;
            if(animLeftTurnAngle>=90) {
                this.stopTurnLeft();
                trackModel.permTransform(this.genLeftTurnMatrix(90));
                trackModel.rotateTrackCCW();
                playerOrientationAngle-=90;
            } else {
                Matrix.multiplyMM(animationMatrix, 0, this.genLeftTurnMatrix(animLeftTurnAngle), 0, animationMatrix, 0);
                isAnimating = true;
            }
        }

        if(isTurningRight) {
            animRightTurnAngle+= this.ANIM_GLIDE_SPEED;
            if(animRightTurnAngle>=90) {
                this.stopTurnRight();
                trackModel.permTransform(this.genRightTurnMatrix(90));
                trackModel.rotateTrackCW();
                playerOrientationAngle+=90;
            } else {
                Matrix.multiplyMM(animationMatrix, 0, this.genRightTurnMatrix(animRightTurnAngle), 0, animationMatrix, 0);
                isAnimating = true;
            }
        }

        if(isAdvancingForward) {
            animAdvanceForwardPos+= this.ANIM_GLIDE_SPEED;
            if(animAdvanceForwardPos>=PlatformTrack.platformLength) {
                this.stopAdvanceForward();
                trackModel.permTransform(this.genAdvanceForwardMatrix(PlatformTrack.platformLength));
                trackModel.advanceTrack();
            } else {
                Matrix.multiplyMM(animationMatrix, 0, this.genAdvanceForwardMatrix(animAdvanceForwardPos), 0, animationMatrix, 0);
                isAnimating = true;
            }
        }

        if(isMergingLeft) {
            animMergePos+= this.ANIM_MERGE_SPEED;
            if(animMergePos>=PlatformTrack.platformSpacing) {
                this.stopMergingLeft();
                trackModel.permTransform(this.genMergeMatrix(PlatformTrack.platformSpacing));
                trackModel.mergeTrackLeft();
            } else {
                Matrix.multiplyMM(animationMatrix, 0, this.genMergeMatrix(animMergePos), 0, animationMatrix, 0);
                isAnimating = true;
            }
        }

        if(isMergingRight) {
            animMergePos+= this.ANIM_MERGE_SPEED;
            if(animMergePos>=PlatformTrack.platformSpacing) {
                this.stopMergingRight();
                trackModel.permTransform(this.genMergeMatrix(-PlatformTrack.platformSpacing));
                trackModel.mergeTrackRight();
            } else {
                Matrix.multiplyMM(animationMatrix, 0, this.genMergeMatrix(-animMergePos), 0, animationMatrix, 0);
                isAnimating = true;
            }
        }

        /************************************************************************/

        if(!(isTurningLeft || isTurningRight || isAdvancingForward)) {
            int targetLane = trackModel.getTrackLanePos();
            if(isMergingLeft) targetLane--;
            if(isMergingRight) targetLane++;
            int targetTrackType = trackModel.getTrackObj(targetLane).getTrackType();
            if(targetTrackType==PlatformTrack.TRACK_STRAIGHT)   startAdvanceForward();
            if(targetTrackType==PlatformTrack.TRACK_TURN_LEFT)  startTurnLeft();
            if(targetTrackType==PlatformTrack.TRACK_TURN_RIGHT)  startTurnRight();
//            if(Math.random()>0.9) {}
        }

        if(!(isMergingLeft || isMergingRight) && !(isTurningLeft || isTurningRight)) {
            if(isStayingInLane && animStayingInLaneTime>0) {
                animStayingInLaneTime-=0.5f;
                // do nothing
            } else
                if (isStayingInLane && animStayingInLaneTime<=0) {
                    isStayingInLane = false;
                    animStayingInLaneTime = 0;
                } else {
                    if(Math.random()>0.5) {
                        isStayingInLane = true;
                        animStayingInLaneTime = (float)(Math.random()*10);
                    } else {
                        if (!trackModel.isAllowedToMergeLeft()) {
                            if (trackModel.isAllowedToMergeRight()) startMergingRight();
                        } else
                            if (!trackModel.isAllowedToMergeRight()) {
                                if (trackModel.isAllowedToMergeLeft()) startMergingLeft();
                            } else {
                                if(Math.random()>0.5) {
                                    if (trackModel.isAllowedToMergeRight()) startMergingRight();
                                } else {
                                    if (trackModel.isAllowedToMergeLeft()) startMergingLeft();
                                }
                            }
                    }
                }
        }

        /*
        // for testing
        // if not animating at it's been more than 3 seconds, start new animation
        if(!isAnimating && (SystemClock.uptimeMillis() > (lastAnimamtionTime+3))) {
            startTurnLeft();
        }
        if(lastAnimamtionTime==0) {
            //startTurnLeft();
            startAdvanceForward();
        }
        */
/*
        if(debugStepCount<6 && !(isAdvancingForward || isTurningLeft || isMergingLeft)) {
            if(debugStepCount==0 && !isMergingLeft) startMergingLeft();
            if(debugStepCount==1 && !isAdvancingForward) startAdvanceForward();
            if(debugStepCount==2 && !isTurningLeft) startTurnLeft();
            if(debugStepCount==3 && !isAdvancingForward) startAdvanceForward();
            if(debugStepCount==4 && !isTurningLeft) startTurnLeft();
            if(debugStepCount==5 && !isAdvancingForward) startAdvanceForward();

            debugStepCount++;
        }
*/
        lastAnimamtionTime = SystemClock.uptimeMillis();
    }

    public float getPlayerOrientationAngle() {
        if(isTurningLeft) return playerOrientationAngle-animLeftTurnAngle;
        if(isTurningRight) return playerOrientationAngle+animRightTurnAngle;
        return playerOrientationAngle;
    }

    public float getPlayerRollAngle() {
        if(isMergingLeft) return -(animMergePos/PlatformTrack.platformSpacing)*180;
        if(isMergingRight) return (animMergePos/PlatformTrack.platformSpacing)*180;
        return 0;
    }

    public void startTurnLeft() {
        if (isTurningLeft) throw new RuntimeException("turn animation already engaged");
        this.stopTurnLeft();
        isTurningLeft = true;
    }
    public void stopTurnLeft() {
        animLeftTurnAngle = 0;
        isTurningLeft = false;
    }

    public void startTurnRight() {
        if (isTurningRight) throw new RuntimeException("turn animation already engaged");
        this.stopTurnRight();
        isTurningRight = true;
    }
    public void stopTurnRight() {
        animRightTurnAngle = 0;
        isTurningRight = false;
    }

    public void startAdvanceForward() {
        if (isAdvancingForward) throw new RuntimeException("forward animation already engaged");
        this.stopAdvanceForward();
        isAdvancingForward = true;
    }
    public void stopAdvanceForward() {
        animAdvanceForwardPos = 0;
        isAdvancingForward = false;
    }

    public void startMergingLeft() {
        if (isMergingLeft) throw new RuntimeException("left merge animation already engaged");
        this.stopMergingLeft();
        isMergingLeft = true;
    }
    public void stopMergingLeft() {
        animMergePos = 0;
        isMergingLeft = false;
    }

    public void startMergingRight() {
        if (isMergingRight) throw new RuntimeException("right merge animation already engaged");
        this.stopMergingRight();
        isMergingRight = true;
    }
    public void stopMergingRight() {
        animMergePos = 0;
        isMergingRight = false;
    }

    public float[] getAnimationMatrix() {
       return animationMatrix.clone();
    }

    public float getRotationAngle() {
        return animLeftTurnAngle;
    }

    private float[] genLeftTurnMatrix(float pAngle) {
        float[] matrix = new float[16];
        float distance = PlatformTrack.platformLength-(PlatformTrack.platformWidth/2);
        Matrix.setIdentityM(matrix, 0);
        Matrix.translateM(matrix, 0, -distance, 0, 0);
        Matrix.rotateM(matrix, 0, -pAngle, 0, 1, 0);
        Matrix.translateM(matrix, 0, distance, 0, 0);
        return matrix;
    }

    private float[] genRightTurnMatrix(float pAngle) {
        float[] matrix = new float[16];
        float distance = PlatformTrack.platformLength-(PlatformTrack.platformWidth/2);
        Matrix.setIdentityM(matrix, 0);
        Matrix.translateM(matrix, 0, distance, 0, 0);
        Matrix.rotateM(matrix, 0, pAngle, 0, 1, 0);
        Matrix.translateM(matrix, 0, -distance, 0, 0);
        return matrix;
    }

    private float[] genAdvanceForwardMatrix(float pPosition) {
        float[] matrix = new float[16];
        Matrix.setIdentityM(matrix, 0);
        Matrix.translateM(matrix, 0, 0, 0, pPosition);
        return matrix;
    }

    private float[] genMergeMatrix(float pPosition) {
        float[] matrix = new float[16];
        Matrix.setIdentityM(matrix, 0);
        Matrix.translateM(matrix, 0, pPosition, 0, 0);
        return matrix;
    }

}