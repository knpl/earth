package nl.knpl.graphics.earth;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

public class OGLUtils {
	
	public static void initTexture(Resources res, BitmapFactory.Options opts, int resource, int handle) {
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
