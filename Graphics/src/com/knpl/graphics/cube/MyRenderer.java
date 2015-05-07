package com.knpl.graphics.cube;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.view.MotionEvent;

public class MyRenderer implements Renderer {
	private static final float[] CUBE_CORNER_VERTS = {
		-1,-1, 1, 1,
		 1,-1, 1, 1,
		 1, 1, 1, 1,
		-1, 1, 1, 1,
		-1,-1,-1, 1,
		 1,-1,-1, 1,
		 1, 1,-1, 1,
		-1, 1,-1, 1
	};
	
	private static final float[] CUBE_FACE_NORMALS = {
		0, 0, 1, // front
		1, 0, 0, // right
		0, 1, 0, // top
		0, 0,-1, // back
	   -1, 0, 0, // left
	    0,-1, 0  // bottom
	};
	
	private static final float[] CUBE_FACE_COLORS = {
		1, 0, 0, 1, // front
		0, 1, 0, 1, // right
		1, 1, 0, 1, // top
		1, .65f, 0, 1, // back
	    0, 0, 1, 1, // left
	    1, 1, 1, 1, // bottom
	};
	
	private static final int SIZEOF_FLOAT = 4;
	
	private float[] cubeData;
	private final float[] lightPos = {0f, 0f, 3f, 1f};
	
	private final float[] eyeSC = {5, (float)(Math.PI/2), 0}; // {rho, phi, theta}
	private final float[] lightSC = {5, (float)(Math.PI/2), 0}; // {rho, phi, theta}
	private float deyetheta, deyephi,
				  dlighttheta, dlightphi;
	
	private float width, height;
	private float anchorx, anchory;
	
	private final FloatBuffer cubeVertices;
	
	private final String vshader,
						 fshader;
	
	private float[] modelMat, viewMat, projMat;
	
	private int modelh, viewh, projh,
				posh, normalh, colorh,
				lightposh;
	
	public MyRenderer(String vshader, String fshader) {
		this.vshader = vshader;
		this.fshader = fshader;
		
		width = height = 0;
		deyetheta = deyephi = 0;
		dlighttheta = dlightphi = 0;
		
		cubeData = new float[6*6*11];
		buildCube();
		
		ByteBuffer buf = ByteBuffer.allocateDirect(cubeData.length * SIZEOF_FLOAT);
		buf.order(ByteOrder.nativeOrder());
		cubeVertices = buf.asFloatBuffer();
		cubeVertices.put(cubeData).position(0);
		
		modelMat = new float[16];
		viewMat = new float[16];
		projMat = new float[16];
	}
	
	private void buildVertex(int face, int offset, int a) {
		cubeData[offset + 0] = CUBE_CORNER_VERTS[4*a + 0];
		cubeData[offset + 1] = CUBE_CORNER_VERTS[4*a + 1];
		cubeData[offset + 2] = CUBE_CORNER_VERTS[4*a + 2];
		cubeData[offset + 3] = CUBE_CORNER_VERTS[4*a + 3];
		cubeData[offset + 4] = CUBE_FACE_NORMALS[3*face + 0];
		cubeData[offset + 5] = CUBE_FACE_NORMALS[3*face + 1];
		cubeData[offset + 6] = CUBE_FACE_NORMALS[3*face + 2];
		cubeData[offset + 7] = CUBE_FACE_COLORS[4*face + 0];
		cubeData[offset + 8] = CUBE_FACE_COLORS[4*face + 1];
		cubeData[offset + 9] = CUBE_FACE_COLORS[4*face + 2];
		cubeData[offset + 10] = CUBE_FACE_COLORS[4*face + 3];
	}
	
	private void buildFace(int face, int a, int b, int c, int d) {
		int offset = 11 * 6 * face;
		buildVertex(face, offset +  0, a);
		buildVertex(face, offset + 11, b);
		buildVertex(face, offset + 22, c);
		buildVertex(face, offset + 33, c);
		buildVertex(face, offset + 44, d);
		buildVertex(face, offset + 55, a);
	}
	
	private void buildCube() {
		buildFace(0, 0, 1, 2, 3); // front
		buildFace(1, 1, 5, 6, 2); // left
		buildFace(2, 2, 6, 7, 3); // top
		buildFace(3, 6, 5, 4, 7); // back
		buildFace(4, 3, 7, 4, 0); // right
		buildFace(5, 0, 4, 5, 1); // bottom	
	}
	
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		GLES20.glClearColor(.2f, .2f, .2f, 1f);
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		
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
		GLES20.glBindAttribLocation(ph, 1, "normal");
		GLES20.glBindAttribLocation(ph, 2, "color");
		GLES20.glLinkProgram(ph);
		GLES20.glGetProgramiv(ph, GLES20.GL_LINK_STATUS, status, 0);
		if (status[0] == 0) {
			GLES20.glDeleteProgram(ph);
			throw new RuntimeException("Could not link program");
		}
		
		modelh = GLES20.glGetUniformLocation(ph, "model");
		viewh = GLES20.glGetUniformLocation(ph, "view");
		projh = GLES20.glGetUniformLocation(ph, "proj");
		lightposh = GLES20.glGetUniformLocation(ph, "lightpos");
		posh = GLES20.glGetAttribLocation(ph, "pos");
		normalh = GLES20.glGetAttribLocation(ph, "normal");
		colorh = GLES20.glGetAttribLocation(ph, "color");
		
		GLES20.glUseProgram(ph);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		this.width = width;
		this.height = height;
		
		GLES20.glViewport(0, 0, width, height);
		float ar = (float)width/height;
		Matrix.frustumM(projMat, 0, -ar, ar, -1, 1, 1, 100);
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
		
		/* r = rho * sin(phi)
		 * x = r * sin(theta)
		 * y = rho * cos(phi)
		 * z = r * cos(theta)
		 */
		final float eyer = (float) (eyeSC[0] * Math.sin(eyeSC[1] + deyephi)),
					eyex = (float) (eyer * Math.sin(eyeSC[2] + deyetheta)),
					eyey = (float) (eyeSC[0] * Math.cos(eyeSC[1] + deyephi)),
					eyez = (float) (eyer * Math.cos(eyeSC[2] + deyetheta));
		
		Matrix.setLookAtM(viewMat, 0, eyex, eyey, eyez, 0, 0, 0, 0, 1, 0);
		
		final float lightr = (float) (lightSC[0] * Math.sin(lightSC[1] + dlightphi));
		lightPos[0] = (float) (lightr * Math.sin(lightSC[2] + dlighttheta));
		lightPos[1] = (float) (lightSC[0] * Math.cos(lightSC[1] + dlightphi));
		lightPos[2] = (float) (lightr * Math.cos(lightSC[2] + dlighttheta));
		
		Matrix.setIdentityM(modelMat, 0);
		
		int stride = 11 * SIZEOF_FLOAT;
		
		cubeVertices.position(0);
		GLES20.glVertexAttribPointer(posh, 4, GLES20.GL_FLOAT, false, stride, cubeVertices);
		GLES20.glEnableVertexAttribArray(posh);
		
		cubeVertices.position(4);
		GLES20.glVertexAttribPointer(normalh, 3, GLES20.GL_FLOAT, false, stride, cubeVertices);
		GLES20.glEnableVertexAttribArray(normalh);
		
		cubeVertices.position(7);
		GLES20.glVertexAttribPointer(colorh, 4, GLES20.GL_FLOAT, false, stride, cubeVertices);
		GLES20.glEnableVertexAttribArray(colorh);
		
		GLES20.glUniformMatrix4fv(modelh, 1, false, modelMat, 0);
		GLES20.glUniformMatrix4fv(viewh, 1, false, viewMat, 0);
		GLES20.glUniformMatrix4fv(projh, 1, false, projMat, 0);
		GLES20.glUniform4fv(lightposh, 1, lightPos, 0);
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
	}

	public void doSomething(int action, float x, float y, boolean moveEye) {
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			anchorx = x;
			anchory = y;
			break;
		case MotionEvent.ACTION_MOVE:
			if (moveEye) {
				deyetheta = (float) ((anchorx - x)/width * Math.PI);
				deyephi = (float) ((anchory - y)/height * Math.PI);
			}
			else {
				dlighttheta = (float) ((x - anchorx)/width * Math.PI);
				dlightphi = (float) ((y - anchory)/height * Math.PI);
			}
			break;
		case MotionEvent.ACTION_UP:
			if (moveEye) {
				eyeSC[1] += deyephi;
				eyeSC[2] += deyetheta;
			}
			else {
				lightSC[1] += dlightphi;
				lightSC[2] += dlighttheta;
			}
			deyetheta = deyephi = dlighttheta = dlightphi = 0;
			break;
		default:
			deyetheta = deyephi = dlighttheta = dlightphi = 0;
		}
	}
}
