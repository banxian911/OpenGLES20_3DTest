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
package com.sv3458.spacerace;

import android.app.Activity;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.android.opengl.R;

public class OpenGLES20Activity extends Activity {

    private GLSurfaceView mGLView;
    private MediaPlayer mediaPlayer;

    private long lastTime = 0;
    private int i=0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity
        mGLView = new MyGLSurfaceView(this);
        setContentView(mGLView);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mediaPlayer = MediaPlayer.create(this, R.raw.themesong3);
        mediaPlayer.setLooping(true);
        mediaPlayer.setVolume(0.2f, 0.2f);
        mediaPlayer.start();


//        SpaceRaceApp app = ((SpaceRaceApp)this.getApplication());
//        app.myRandomNum = (int)(Math.random()*100.0);

    }

    @Override
    protected void onPause() {
        super.onPause();
        // The following call pauses the rendering thread.
        // If your OpenGL application is memory intensive,
        // you should consider de-allocating objects that
        // consume significant memory here.
        mGLView.onPause();
        mediaPlayer.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // The following call resumes a paused rendering thread.
        // If you de-allocated graphic objects for onPause()
        // this is a good place to re-allocate them.
        mGLView.onResume();
        mediaPlayer.start();
    }
    
    @Override
    public void onBackPressed() {
    	// TODO Auto-generated method stub
    	/*long currentTime = System.currentTimeMillis();
		if(currentTime-lastTime > 100){
			lastTime = currentTime;
			Toast.makeText(this,R.string.back_toast, Toast.LENGTH_SHORT).show();
			return;
		}*/
    	if (i <= 5) {
			i++;
			//Toast.makeText(this,R.string.back_toast, Toast.LENGTH_SHORT).show();
			return;
		}
    	super.onBackPressed();
    	
    }
}