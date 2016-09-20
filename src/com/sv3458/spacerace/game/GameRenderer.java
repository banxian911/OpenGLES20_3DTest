package com.sv3458.spacerace.game;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.sv3458.spacerace.SpaceRaceApp;
import com.sv3458.spacerace.models.PlatformTrack;
import com.sv3458.spacerace.models.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GameRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "GameRenderer";
    private Triangle mTriangle;
    private RaceFloor   raceFloorModel;
    private SkyBox      skyBoxBackgroundModel;
    private TrackModel  trackModel;
    private GamePlay    gamePlay;
    private ScreenText  debugText;

    private Activity activityContext;

    private final float[] mProjectionMatrix = new float[16];

    private float mAngle;

    float playerOrientationY = 0;
    float playerPitchAngle = 0;
    float playerRollAngle = 0;


    private final FloatBuffer axisPoints;

    public GameRenderer(Activity pActivityContext) {
        activityContext = pActivityContext;

        // axis lines
        axisPoints = ByteBuffer.allocateDirect(18*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        axisPoints.put(new float[] {
                0.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f
        }).position(0);

//        SpaceRaceApp app = ((SpaceRaceApp)pActivityContext.getApplication());
//        app.myRandomNum = (int)(Math.random()*100.0);


        mTriangle = new Triangle();
        skyBoxBackgroundModel = new SkyBox(activityContext);
        raceFloorModel = new RaceFloor(activityContext);

        trackModel = new TrackModel(activityContext);

        gamePlay = new GamePlay(activityContext, trackModel);

        SpaceRaceApp app = ((SpaceRaceApp)activityContext.getApplication());

        debugText = new ScreenText(activityContext, Integer.toString(app.myRandomNum));

    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        mTriangle.prepareGLcontext();
        skyBoxBackgroundModel.prepareGLcontext();
        raceFloorModel.prepareGLcontext();
        trackModel.prepareGLcontext();
        debugText.prepareGLcontext();
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        float[] viewMatrix = new float[16];
        float[] skyboxMatrix;
        float[] mMVPMatrix;

        playerPitchAngle = (playerPitchAngle + 0.009f) % 360.0f;

        gamePlay.animateFrame();
        playerOrientationY = gamePlay.getPlayerOrientationAngle();
        playerRollAngle = gamePlay.getPlayerRollAngle();

        /***--[ game play angle ]--**/ /**/
        Matrix.setLookAtM(viewMatrix, 0, 0, 0f, 0f, 0f, 0f, -1f, 0f, 1.0f, 0.0f);
        skyboxMatrix = viewMatrix.clone();


        Matrix.rotateM(viewMatrix, 0, (float)(Math.sin(Math.toRadians(playerPitchAngle))*4), 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(skyboxMatrix, 0, (float)(Math.sin(Math.toRadians(playerPitchAngle))*4), 1.0f, 0.0f, 0.0f);

        Matrix.rotateM(viewMatrix, 0, (float)(Math.sin(Math.toRadians(playerRollAngle))*5), 0.0f, 0.0f, 1.0f);
        Matrix.rotateM(skyboxMatrix, 0, (float)(Math.sin(Math.toRadians(playerRollAngle))*5), 0.0f, 0.0f, 1.0f);

        Matrix.rotateM(skyboxMatrix, 0, playerOrientationY-180, 0.0f, 1.0f, 0.0f);

        Matrix.translateM(viewMatrix, 0, 0, -5f+(float)(Math.sin(playerPitchAngle*20)*0.05), 0);

        Matrix.multiplyMM(viewMatrix, 0, mProjectionMatrix, 0, viewMatrix, 0);
        Matrix.multiplyMM(skyboxMatrix, 0, mProjectionMatrix, 0, skyboxMatrix, 0);
        mMVPMatrix = viewMatrix.clone();
        /**/

        /***--[ debug angle ]--**/ /*
        Matrix.setLookAtM(viewMatrix, 0, 0, 0f, 0f, 0f, 0f, -1f, 0f, 1.0f, 0.0f);
        skyboxMatrix = viewMatrix.clone();


//        Matrix.rotateM(viewMatrix, 0, 45, 0.0f, 1.0f, 0.0f);
  //      Matrix.rotateM(skyboxMatrix, 0, 45, 0.0f, 1.0f, 0.0f);

        Matrix.translateM(viewMatrix, 0, 0f, 0f, -80f);

        Matrix.rotateM(viewMatrix, 0, 90, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(skyboxMatrix, 0, 90, 1.0f, 0.0f, 0.0f);

        Matrix.multiplyMM(viewMatrix, 0, mProjectionMatrix, 0, viewMatrix, 0);
        Matrix.multiplyMM(skyboxMatrix, 0, mProjectionMatrix, 0, skyboxMatrix, 0);
        mMVPMatrix = viewMatrix.clone();
        */


        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        skyBoxBackgroundModel.draw(skyboxMatrix);

        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        raceFloorModel.draw(mMVPMatrix, 0);



        float[] trackMatrix = gamePlay.getAnimationMatrix();
        Matrix.multiplyMM(trackMatrix, 0, viewMatrix, 0, trackMatrix, 0);
        trackModel.draw(trackMatrix);



        // origin coordinates

        GLES20.glUseProgram(mTriangle.mProgram);

        int mvpMatrix = GLES20.glGetUniformLocation(mTriangle.mProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mvpMatrix, 1, false, mMVPMatrix, 0);

        int posHandle = GLES20.glGetAttribLocation(mTriangle.mProgram, "vPosition");
        axisPoints.position(0);
        GLES20.glVertexAttribPointer(
                posHandle,
                3,
                GLES20.GL_FLOAT,
                false,
                0,
                axisPoints);
        GLES20.glEnableVertexAttribArray(posHandle);

        GLES20.glLineWidth(10.0f);

        int colHandle = GLES20.glGetUniformLocation(mTriangle.mProgram, "vColor");

        GLES20.glUniform4f(colHandle, 1.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2);

        GLES20.glUniform4f(colHandle, 0.0f, 1.0f, 0.0f, 1.0f);
        GLES20.glDrawArrays(GLES20.GL_LINES, 2, 2);

        GLES20.glUniform4f(colHandle, 0.0f, 0.0f, 1.0f, 1.0f);
        GLES20.glDrawArrays(GLES20.GL_LINES, 4, 2);


/*
        // screen text
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        debugText.draw();

        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
*/
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;
        /*

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 0.2f, 100);
*/

        float fovy = (float)(2 * Math.atan(30f / 2f / 16f));
        Matrix.perspectiveM(mProjectionMatrix, 0, fovy * 180.0f / 3.14f, ratio, 0.2f, 150f);

    }

    /**
     * Utility method for compiling a OpenGL shader.
     *
     * <p><strong>Note:</strong> When developing shaders, use the checkGlError()
     * method to debug shader coding errors.</p>
     *
     * @param type - Vertex or fragment shader type.
     * @param shaderCode - String containing the shader code.
     * @return - Returns an id for the shader.
     */
    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    /**
    * Utility method for debugging OpenGL calls. Provide the name of the call
    * just after making it:
    *
    * <pre>
    * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
    * GameRenderer.checkGlError("glGetUniformLocation");</pre>
    *
    * If the operation is not successful, the check throws an error.
    *
    * @param glOperation - Name of the OpenGL call to check.
    */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    /**
     * Returns the rotation angle of the triangle shape (mTriangle).
     *
     * @return - A float representing the rotation angle.
     */
    public float getAngle() {
        return mAngle;
    }

    /**
     * Sets the rotation angle of the triangle shape (mTriangle).
     */
    public void setAngle(float angle) {
        mAngle = angle;
    }

}