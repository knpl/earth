package com.knpl.graphics.cube;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;
import android.opengl.Matrix;

public class MyRenderer implements Renderer {
	
	private static final int SIZEOF_FLOAT = 4,
							 SIZEOF_SHORT = 2;

	private final float[] lightPos = {0f, 0f, 3f, 1f};
	
	private float[] sphereData;
	private short[] sphereIndexData;
	
	private final float[] eyeSC = {5, (float)(Math.PI/2), 0}; // {rho, phi, theta}
	private final float[] lightSC = {5, (float)(Math.PI/2), 0}; // {rho, phi, theta}
	private float deyetheta, deyephi, deyerho,
				  dlighttheta, dlightphi, dlightrho;
	
	private float width, height;
	
	private final FloatBuffer sphereVertices;
	private final ShortBuffer sphereIndices;
	
	private int vboh, iboh, texh;
	
	private final String vshader,
						 fshader;
	
	private float[] viewMat, projMat;
	
	private int viewh, projh,
				posh, texattrh,
				samplerh,
				lightposh;
	
	public MyRenderer(String vshader, String fshader) {
		this.vshader = vshader;
		this.fshader = fshader;
		
		width = height = 0;
		deyetheta = deyephi = 0;
		dlighttheta = dlightphi = 0;
		deyerho = dlightrho = 1;
		
		Sphere.initSphere(40);
		sphereData = Sphere.indexSphere();
		ByteBuffer buf = ByteBuffer.allocateDirect(sphereData.length * SIZEOF_FLOAT);
		buf.order(ByteOrder.nativeOrder());
		sphereVertices = buf.asFloatBuffer();
		sphereVertices.put(sphereData).position(0);
		
		sphereIndexData = Sphere.indices();
		buf = ByteBuffer.allocateDirect(sphereIndexData.length * SIZEOF_SHORT);
		buf.order(ByteOrder.nativeOrder());
		sphereIndices = buf.asShortBuffer();
		sphereIndices.put(sphereIndexData).position(0);
		
		viewMat = new float[16];
		projMat = new float[16];
	}
	
	private static int loadTexture() {
		int[] textures = new int[1];
		GLES20.glGenTextures(1, textures, 0);
		if (textures[0] == 0) {
			throw new RuntimeException("Failed to generate texture");
		}
		
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inScaled = false;
		Bitmap bmp = BitmapFactory.decodeResource(
				GraphicsApp.getAppContext().getResources(),
				R.drawable.earth1110, opts);
		
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
		GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);

		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
				GLES20.GL_LINEAR_MIPMAP_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
				GLES20.GL_LINEAR);
		
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
		
		bmp.recycle();
		
		return textures[0];
	}
	
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		GLES20.glClearColor(0, 0, 0, 1);
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glEnable(GLES20.GL_TEXTURE_2D);
		
		int vsh = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
		if (vsh == 0) {
			throw new RuntimeException("Could not create vertex shader");
		}
		GLES20.glShaderSource(vsh, vshader);
		GLES20.glCompileShader(vsh);
		int[] status = new int[1];
		GLES20.glGetShaderiv(vsh, GLES20.GL_COMPILE_STATUS, status, 0);
		if (status[0] == 0) {
			String message = GLES20.glGetShaderInfoLog(vsh);
			GLES20.glDeleteShader(vsh);
			throw new RuntimeException("Could not compile vertex shader: "+message);
		}
		
		int fsh = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
		if (fsh == 0) {
			throw new RuntimeException("Could not create fragment shader");
		}
		GLES20.glShaderSource(fsh, fshader);
		GLES20.glCompileShader(fsh);
		GLES20.glGetShaderiv(fsh, GLES20.GL_COMPILE_STATUS, status, 0);
		if (status[0] == 0) {
			String message = GLES20.glGetShaderInfoLog(fsh);
			GLES20.glDeleteShader(fsh);
			throw new RuntimeException("Could not compile fragment shader: "+message);
		}
		
		int ph = GLES20.glCreateProgram();
		if (ph == 0) {
			throw new RuntimeException("Could not create program");
		}
		GLES20.glAttachShader(ph, vsh);
		GLES20.glAttachShader(ph, fsh);
		GLES20.glBindAttribLocation(ph, 0, "pos");
		GLES20.glBindAttribLocation(ph, 1, "tex");
		GLES20.glLinkProgram(ph);
		GLES20.glGetProgramiv(ph, GLES20.GL_LINK_STATUS, status, 0);
		if (status[0] == 0) {
			GLES20.glDeleteProgram(ph);
			throw new RuntimeException("Could not link program");
		}
		
		texh = loadTexture();
		samplerh = GLES20.glGetUniformLocation(ph, "sampler");
		viewh = GLES20.glGetUniformLocation(ph, "view");
		projh = GLES20.glGetUniformLocation(ph, "proj");
		lightposh = GLES20.glGetUniformLocation(ph, "lightpos");
		posh = GLES20.glGetAttribLocation(ph, "pos");
		texattrh = GLES20.glGetAttribLocation(ph, "tex");
		
		GLES20.glUseProgram(ph);
		
		int[] a = new int[1];
		GLES20.glGenBuffers(1, a, 0);
		vboh = a[0];
		GLES20.glGenBuffers(1, a, 0);
		iboh = a[0];
		
		if (vboh <= 0 || iboh <= 0) {
			throw new RuntimeException("Generating buffers failed.");
		}
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboh);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, sphereVertices.capacity() * SIZEOF_FLOAT,
							sphereVertices, GLES20.GL_STATIC_DRAW);
		
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, iboh);
		GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, sphereIndices.capacity() * SIZEOF_SHORT,
							sphereIndices, GLES20.GL_STATIC_DRAW);
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		this.width = width;
		this.height = height;
		
		GLES20.glViewport(0, 0, width, height);
		float ar = (float)width/height;
		Matrix.perspectiveM(projMat, 0, 90, ar, .125f, 32f);
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
		
		/* r = rho * sin(phi)
		 * x = r * sin(theta)
		 * y = rho * cos(phi)
		 * z = r * cos(theta)
		 */
		final float eyer = (float) (eyeSC[0] * deyerho * Math.sin(eyeSC[1] + deyephi)),
					eyex = (float) (eyer * Math.sin(eyeSC[2] + deyetheta)),
					eyey = (float) (eyeSC[0] * deyerho * Math.cos(eyeSC[1] + deyephi)),
					eyez = (float) (eyer * Math.cos(eyeSC[2] + deyetheta));
		
		Matrix.setLookAtM(viewMat, 0, eyex, eyey, eyez, 0, 0, 0, 0, 1, 0);
		
		final float lightr = (float) (lightSC[0] * dlightrho * Math.sin(lightSC[1] + dlightphi));
		lightPos[0] = (float) (lightr * Math.sin(lightSC[2] + dlighttheta));
		lightPos[1] = (float) (lightSC[0] * dlightrho * Math.cos(lightSC[1] + dlightphi));
		lightPos[2] = (float) (lightr * Math.cos(lightSC[2] + dlighttheta));
		
		GLES20.glUniformMatrix4fv(viewh, 1, false, viewMat, 0);
		GLES20.glUniformMatrix4fv(projh, 1, false, projMat, 0);
		GLES20.glUniform4fv(lightposh, 1, lightPos, 0);
		
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texh);
		GLES20.glUniform1i(samplerh, 0);
		
		final int stride = 5 * SIZEOF_FLOAT;
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboh);
		GLES20.glVertexAttribPointer(posh, 3, GLES20.GL_FLOAT, false, stride, 0);
		GLES20.glEnableVertexAttribArray(posh);
		GLES20.glVertexAttribPointer(texattrh, 2, GLES20.GL_FLOAT, false, stride, 3 * SIZEOF_FLOAT);
		GLES20.glEnableVertexAttribArray(texattrh);
		
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, iboh);
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, sphereIndices.capacity(), GLES20.GL_UNSIGNED_SHORT, 0);
		
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
	}
	
	public void dragEye(float x0, float y0, float x1, float y1) {
		deyetheta = (float) ((x0 - x1)/width * Math.PI);
		deyephi = (float) ((y0 - y1)/height * Math.PI);
	}
	
	public void stopDragEye(float x0, float y0, float x1, float y1) {
		eyeSC[1] += deyephi;
		eyeSC[2] += deyetheta;
		deyetheta = deyephi = 0;
	}
	
	public void pinchEye(float x, float y, float r0, float r1) {
		deyerho = r0/r1;
	}
	
	public void stopPinchEye(float x, float y, float r0, float r1) {
		eyeSC[0] *= r0/r1;
		deyerho = 1;
	}
	
	public void dragLight(float x0, float y0, float x1, float y1) {
		dlighttheta = (float) ((x1 - x0)/width * Math.PI);
		dlightphi = (float) ((y1 - y0)/height * Math.PI);
	}
	
	public void stopDragLight(float x0, float y0, float x1, float y1) {
		lightSC[1] += dlightphi;
		lightSC[2] += dlighttheta;
		dlighttheta = dlightphi = 0;
	}
	
	public void pinchLight(float x, float y, float r0, float r1) {
		dlightrho = r0/r1;
	}
	
	public void stopPinchLight(float x, float y, float r0, float r1) {
		lightSC[0] *= r0/r1;
		dlightrho = 1;
	}
}
