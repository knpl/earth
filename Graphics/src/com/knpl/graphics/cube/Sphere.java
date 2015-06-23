package com.knpl.graphics.cube;

public class Sphere {
	
	private static final int N = 5;
	
	private static float[] data = null;
	private static short[] idx = null;
	
	public static float[] indexSphere() {
		return data;
	}
	
	public static short[] indices() {
		return idx;
	}
	
	public static void w(int index, double x, double y, double z, double the, double phi) {
		// position
		data[N * index + 0] = (float) x;
		data[N * index + 1] = (float) y;
		data[N * index + 2] = (float) z;
		
		data[N * index + 3] = (float) (the / (2 * Math.PI));
		data[N * index + 4] = (float) (phi / Math.PI);
	}

	public static float[] initSphere(int n) {
		double phi, the, sinphi, cosphi, sinthe, costhe;
		final double step = (2 * Math.PI) / n;
		final double halfstep = .5 * step;
		int nvecs = (n + 1) * (n-1) + 2;
		
		// build data
		int k = 0;
		data = new float[N * (nvecs)];
		w(k++, 0, 1, 0, Math.PI, 0);
		phi = step;
		for (int i = 0; i < n-1; ++i) {
			the = 0;
			for (int j = 0; j <= n; ++j) {
				sinphi = Math.sin(phi);
				cosphi = Math.cos(phi);
				sinthe = Math.sin(the);
				costhe = Math.cos(the);
				w(k++, sinphi * sinthe, cosphi, sinphi * costhe, the, phi);
				the += step;
			}
			phi += halfstep;
		}
		w(k++, 0, -1, 0, Math.PI, Math.PI);
		
		// build indices
		k = 0;
		idx = new short[6 * n * (n - 1)];
		
		// upper triangle fan
		for (int i = 0; i < n; ++i) {
			idx[k++] = 0;
			idx[k++] = (short) (i+1); 
			idx[k++] = (short) (i+2);
		}
		
		// strips
		for (int i = 1; i < n-1; ++i) {
			int u = (1 + (i-1) * (n + 1));
			int d = (1 +  i * (n + 1));
			for (int j = 0; j < n; ++j) {
				idx[k++] = (short) (u+j); idx[k++] =   (short) (d+j); idx[k++] = (short) (d+j+1);
				idx[k++] = (short) (u+j); idx[k++] = (short) (d+j+1); idx[k++] = (short) (u+j+1);
			}
		}
		
		// lower triangle fan
		int u = 1 + (n-2) * (n+1);
		int d = 1 + (n-1) * (n+1);
		for (int i = 0; i < n; ++i) { 
			idx[k++] = (short) (u + i);
			idx[k++] = (short) d;
			idx[k++] = (short) (u + 1 + i);
		}
		
		return data;
	}
}
