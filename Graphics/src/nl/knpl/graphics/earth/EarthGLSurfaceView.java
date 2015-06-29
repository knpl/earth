package nl.knpl.graphics.earth;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.content.res.AssetManager;
import android.opengl.GLSurfaceView;

public class EarthGLSurfaceView extends GLSurfaceView implements DragPinchOnTouchListener.Listener {

	private final EarthRenderer renderer;
	private boolean moveEye;
	
	public EarthGLSurfaceView(Context context) throws Exception {
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
    	renderer = new EarthRenderer(vsource, fsource);
    	setRenderer(renderer);
    	
    	setOnTouchListener(new DragPinchOnTouchListener(this));
    	
    	moveEye = true;
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
	
	public void toggleMode() {
		this.moveEye = !moveEye;
	}

	@Override
	public void startDrag(float x, float y) {
	}

	@Override
	public void drag(float x0, float y0, float x1, float y1) {
		final float xx0 = x0, yy0 = y0, xx1 = x1, yy1 = y1;
		queueEvent(new Runnable() {
			@Override
			public void run() {
				if (moveEye)
					renderer.dragEye(xx0, yy0, xx1, yy1);
				else 
					renderer.dragLight(xx0, yy0, xx1, yy1);
			}
		});
	}

	@Override
	public void stopDrag(float x0, float y0, float x1, float y1) {
		final float xx0 = x0, yy0 = y0, xx1 = x1, yy1 = y1;
		queueEvent(new Runnable() {
			@Override
			public void run() {
				if (moveEye)
					renderer.stopDragEye(xx0, yy0, xx1, yy1);
				else 
					renderer.stopDragLight(xx0, yy0, xx1, yy1);
			}
		});
	}

	@Override
	public void startPinch(float x, float y, float r) {
	}

	@Override
	public void pinch(float x, float y, float r0, float r1) {
		final float xx = x, yy = y, rr0 = r0, rr1 = r1;
		queueEvent(new Runnable() {
			@Override
			public void run() {
				if (moveEye)
					renderer.pinchEye(xx, yy, rr0, rr1);
				else 
					renderer.pinchLight(xx, yy, rr0, rr1);
			}
		});
	}

	@Override
	public void stopPinch(float x, float y, float r0, float r1) {
		final float xx = x, yy = y, rr0 = r0, rr1 = r1;
		queueEvent(new Runnable() {
			@Override
			public void run() {
				if (moveEye)
					renderer.stopPinchEye(xx, yy, rr0, rr1);
				else 
					renderer.stopPinchLight(xx, yy, rr0, rr1);
			}
		});
	}
	
	@Override
	public boolean performClick() {
		return super.performClick();
	}
}
