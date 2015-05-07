package com.knpl.graphics.cube;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.content.res.AssetManager;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class MyGLSurfaceView extends GLSurfaceView implements OnTouchListener {

	private final MyRenderer renderer;
	private boolean moveEye;
	
	public MyGLSurfaceView(Context context) throws Exception {
		super(context);
		ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        ConfigurationInfo ci = am.getDeviceConfigurationInfo();
        
    	AssetManager asm = getContext().getAssets();
        String vsource = readFile(asm.open("vertex.vs"));
        String fsource = readFile(asm.open("fragment.fs"));
        if (ci.reqGlEsVersion < 0x20000) {
        	throw new RuntimeException("OpenGL ES 2.0 not supported");
        }
        setEGLContextClientVersion(2);
    	renderer = new MyRenderer(vsource, fsource);
    	setRenderer(renderer);
    	
    	moveEye = true;
    	setOnTouchListener(this);
	}

	private static String readFile(InputStream is) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        
        while (true) {
        	int bytes = is.read(buffer);
        	if (bytes == -1) {
        		break;
        	}
        	baos.write(buffer,0, bytes);
        }
        
		return baos.toString("UTF-8");
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		final int action = event.getActionMasked();
		final float x = event.getX();
		final float y = event.getY();
		
		if (action == MotionEvent.ACTION_DOWN) {
			v.performClick();
		}
		queueEvent(new Runnable() {
			@Override
			public void run() {
				renderer.doSomething(action, x, y, moveEye);
			}
		});
		return true;
	}
	
	public void toggleMode() {
		this.moveEye = !moveEye;
	}

	@Override
	public boolean performClick() {
		return super.performClick();
	}
}
