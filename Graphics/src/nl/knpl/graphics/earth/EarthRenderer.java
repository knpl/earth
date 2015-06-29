package nl.knpl.graphics.earth;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import nl.knpl.graphics.earth.R;

public class EarthRenderer implements Renderer {
	
	private static final int SIZEOF_FLOAT = 4,
							 SIZEOF_SHORT = 2;

	private final float[] lpos;
	
	/* Eye position in spherical coordinates */
	private float erho, ephi, ethe;
	
	/* Light position in sphereical coordinates */
	private float lrho, lphi, lthe;
	
	/* Eye and Light displacements */
	private float derho, dephi, dethe;
	private float dlthe, dlphi, dlrho;
	
	private float width, height;
	
	private int vboh, iboh;
	
	private final String vshader,
						 fshader;
	
	private float[] viewMat, projMat;
	
	private int ph, viewh, lightposh;
	
	private int nindices;
	
	public EarthRenderer(String vshader, String fshader) {
		this.vshader = vshader;
		this.fshader = fshader;
		
		erho = lrho = 2;
		ephi = lphi = (float)(.5*Math.PI);
		ethe = lthe = (float)Math.PI;
		
		derho = dlrho = 1;
		dlthe = dlphi = 0;
		dethe = dephi = 0;
		
		lpos = new float[4];
		viewMat = new float[16];
		projMat = new float[16];
		
		nindices = 0;
	}
	
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		GLES20.glClearColor(0, 0, 0, 1);
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glEnable(GLES20.GL_TEXTURE_2D);
		
		/* Compile vertex shaders from source. */
		int vsh = createShader(GLES20.GL_VERTEX_SHADER, vshader);
		int fsh = createShader(GLES20.GL_FRAGMENT_SHADER, fshader);
		ph = createProgram(vsh, fsh, "pos", "tang", "tex");
		
		/* Get uniform locations. */
		viewh = GLES20.glGetUniformLocation(ph, "view");
		lightposh = GLES20.glGetUniformLocation(ph, "lightpos");
		
		GLES20.glUseProgram(ph);
		
		/* Initialize textures and geometry. */
		initTextures();
		initGeometry();
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		this.width = width;
		this.height = height;
		
		GLES20.glViewport(0, 0, width, height);
		float ar = (float)width/height;
		Matrix.perspectiveM(projMat, 0, 90, ar, .125f, 32f);
		
		int handle = GLES20.glGetUniformLocation(ph, "proj");
		GLES20.glUniformMatrix4fv(handle, 1, false, projMat, 0);
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
		
		/* r = rho * sin(phi)
		 * x = r * sin(theta)
		 * y = rho * cos(phi)
		 * z = r * cos(theta)
		 */
		float er, ex, ey, ez;
		er = (float) (erho * derho * Math.sin(ephi + dephi));
		ex = (float) (er * Math.sin(ethe + dethe));
		ey = (float) (erho * derho * Math.cos(ephi + dephi));
		ez = (float) (er * Math.cos(ethe + dethe));
		Matrix.setLookAtM(viewMat, 0, ex, ey, ez, 0, 0, 0, 0, 1, 0);
		
		float lr = (float) (lrho * dlrho * Math.sin(lphi + dlphi));
		lpos[0] = (float) (lr * Math.sin(lthe + dlthe));
		lpos[1] = (float) (lrho * dlrho * Math.cos(lphi + dlphi));
		lpos[2] = (float) (lr * Math.cos(lthe + dlthe));
		lpos[3] = 1f;
		
		GLES20.glUniformMatrix4fv(viewh, 1, false, viewMat, 0);
		GLES20.glUniform4fv(lightposh, 1, lpos, 0);
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, nindices, GLES20.GL_UNSIGNED_SHORT, 0);
	}
	
	public void dragEye(float x0, float y0, float x1, float y1) {
		dethe = (float) ((x0 - x1)/width * erho);
		dephi = (float) ((y0 - y1)/height * erho);
	}
	
	public void stopDragEye(float x0, float y0, float x1, float y1) {
		ephi += dephi;
		ethe += dethe;
		dethe = dephi = 0;
	}
	
	public void pinchEye(float x, float y, float r0, float r1) {
		derho = r0/r1;
	}
	
	public void stopPinchEye(float x, float y, float r0, float r1) {
		erho *= derho;
		derho = 1;
	}
	
	public void dragLight(float x0, float y0, float x1, float y1) {
		dlthe = (float) ((x1 - x0)/width * lrho);
		dlphi = (float) ((y1 - y0)/height * lrho);
	}
	
	public void stopDragLight(float x0, float y0, float x1, float y1) {
		lphi += dlphi;
		lthe += dlthe;
		dlthe = dlphi = 0;
	}
	
	public void pinchLight(float x, float y, float r0, float r1) {
		dlrho = r0/r1;
	}
	
	public void stopPinchLight(float x, float y, float r0, float r1) {
		lrho *= dlrho;
		dlrho = 1;
	}
	
	private void initTextures() {
		int[] textures = new int[3];
		GLES20.glGenTextures(3, textures, 0);
		for (int i = 0; i < textures.length; ++i) {
			if (textures[i] <= 0) {
				throw new RuntimeException("Failed to generate textures.");
			}
		}
		
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inScaled = false;
		Resources res = EarthApp.getAppContext().getResources();
		
		/* Upload textures. */
		loadTexture(res, opts, R.drawable.earth, textures[0]);
		loadTexture(res, opts, R.drawable.earth_normal, textures[1]);
		loadTexture(res, opts, R.drawable.earth_spec, textures[2]);
		
		/* Bind textures to uniforms. */
		int handle;
		handle = GLES20.glGetUniformLocation(ph, "texsamp");
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
		GLES20.glUniform1i(handle, 0);
		
		handle = GLES20.glGetUniformLocation(ph, "normsamp");
		GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[1]);
		GLES20.glUniform1i(handle, 1);
		
		handle = GLES20.glGetUniformLocation(ph, "specsamp");
		GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[2]);
		GLES20.glUniform1i(handle, 2);
	}
	
	private void initGeometry() {
		int[] a = new int[2];
		GLES20.glGenBuffers(2, a, 0);
		if (a[0] <= 0 || a[1] <= 0) {
			throw new RuntimeException("Generating buffers failed.");
		}
		vboh = a[0];
		iboh = a[1];
		
		/* Generate sphere vertex and index buffers. */
		Sphere.initSphere(40);
		float[] sphereData = Sphere.getVertices();
		ByteBuffer buf = ByteBuffer.allocateDirect(sphereData.length * SIZEOF_FLOAT);
		buf.order(ByteOrder.nativeOrder());
		FloatBuffer sphereVertices = buf.asFloatBuffer();
		sphereVertices.put(sphereData).position(0);
		
		short[] sphereIndexData = Sphere.getIndices();
		buf = ByteBuffer.allocateDirect(sphereIndexData.length * SIZEOF_SHORT);
		buf.order(ByteOrder.nativeOrder());
		ShortBuffer sphereIndices = buf.asShortBuffer();
		sphereIndices.put(sphereIndexData).position(0);
		
		nindices = sphereIndices.capacity();
		
		/* Upload sphere vertices. */
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboh);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, sphereVertices.capacity() * SIZEOF_FLOAT,
							sphereVertices, GLES20.GL_STATIC_DRAW);
		
		/* Upload sphere indices. */
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, iboh);
		GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, sphereIndices.capacity() * SIZEOF_SHORT,
							sphereIndices, GLES20.GL_STATIC_DRAW);
		
		/* Describe vertex attributes. */
		int handle;
		final int stride = Sphere.N * SIZEOF_FLOAT;
		
		handle = GLES20.glGetAttribLocation(ph, "pos");
		GLES20.glVertexAttribPointer(handle, 3, GLES20.GL_FLOAT, false, stride, 0);
		GLES20.glEnableVertexAttribArray(handle);
		
		handle = GLES20.glGetAttribLocation(ph, "tang");
		GLES20.glVertexAttribPointer(handle, 3, GLES20.GL_FLOAT, false, stride, 3 * SIZEOF_FLOAT);
		GLES20.glEnableVertexAttribArray(handle);
		
		handle = GLES20.glGetAttribLocation(ph, "tex");
		GLES20.glVertexAttribPointer(handle, 2, GLES20.GL_FLOAT, false, stride, 6 * SIZEOF_FLOAT);
		GLES20.glEnableVertexAttribArray(handle);
	}
	
	public static void loadTexture(Resources res, BitmapFactory.Options opts, int resource, int handle) {
		Bitmap bmp = BitmapFactory.decodeResource(res, resource, opts);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, handle);
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
		GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
				GLES20.GL_LINEAR_MIPMAP_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
				GLES20.GL_LINEAR);
		bmp.recycle();
	}

	public static int createShader(int type, String source) {
		String typeString;
		if (type == GLES20.GL_VERTEX_SHADER)
			typeString = "vertex";
		else if (type == GLES20.GL_FRAGMENT_SHADER)
			typeString = "fragment";
		else
			throw new RuntimeException("Unknown shader type");
		
		int sh = GLES20.glCreateShader(type);
		if (sh <= 0) {
			throw new RuntimeException("Could not create "+typeString+" shader");
		}
		GLES20.glShaderSource(sh, source);
		GLES20.glCompileShader(sh);
		int[] status = new int[1];
		GLES20.glGetShaderiv(sh, GLES20.GL_COMPILE_STATUS, status, 0);
		if (status[0] <= 0) {
			String message = GLES20.glGetShaderInfoLog(sh);
			GLES20.glDeleteShader(sh);
			throw new RuntimeException("Could not compile "+typeString+" shader: "+message);
		}
		
		return sh;
	}
	
	public static int createProgram(int vsh, int fsh, String... attributes) {
		int[] status = new int[1];
		int ph = GLES20.glCreateProgram();
		if (ph <= 0) {
			throw new RuntimeException("Could not create program");
		}
		GLES20.glAttachShader(ph, vsh);
		GLES20.glAttachShader(ph, fsh);
		for (int i = 0; i < attributes.length; ++i) {
			GLES20.glBindAttribLocation(ph, i, attributes[i]);
		}
		GLES20.glLinkProgram(ph);
		GLES20.glGetProgramiv(ph, GLES20.GL_LINK_STATUS, status, 0);
		if (status[0] == 0) {
			GLES20.glDeleteProgram(ph);
			throw new RuntimeException("Could not link program");
		}
		
		return ph;
	}
}
