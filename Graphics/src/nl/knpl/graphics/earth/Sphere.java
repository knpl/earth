package nl.knpl.graphics.earth;

public class Sphere {
	
	public static final int N = 8;
	
	private static float[] data = null;
	private static short[] idx = null;
	
	public static float[] getVertices() {
		return data;
	}
	
	public static short[] getIndices() {
		return idx;
	}
	
	public static void w(int index, double the, double phi) {
		double sinphi, cosphi, sinthe, costhe;
		sinphi = Math.sin(phi); cosphi = Math.cos(phi);
		sinthe = Math.sin(the); costhe = Math.cos(the);
		
		/* normals */
		data[N * index + 0] = (float) (sinphi * sinthe);
		data[N * index + 1] = (float) cosphi;
		data[N * index + 2] = (float) (sinphi * costhe);
		
		/* tangents */
		data[N * index + 3] = (float) costhe;
		data[N * index + 4] = (float) 0;
		data[N * index + 5] = (float) -sinthe;
		
		/* tex coordinates */
		data[N * index + 6] = (float) (the / (2 * Math.PI));
		data[N * index + 7] = (float) (phi / Math.PI);
	}

	public static void initSphere(int n) {
		double phi, the;
		final double halfstep = Math.PI / n;
		final double step = halfstep + halfstep;
		int nvecs = (n+1)*(n+1);
		
		// build data
		int k = 0;
		data = new float[N * nvecs];
		phi = 0.0;
		for (int i = 0; i <= n; ++i) {
			the = 0.0;
			for (int j = 0; j <= n; ++j) {
				w(k++, the, phi);
				the += step;
			}
			phi += halfstep;
		}
		
		// build indices
		k = 0;
		idx = new short[6 * n * n];
		for (int i = 0; i < n; ++i) {
			int u = i * (n + 1);
			int d = (i + 1) * (n + 1);
			for (int j = 0; j < n; ++j) {
				idx[k++] = (short) (u+j); idx[k++] =   (short) (d+j); idx[k++] = (short) (d+j+1);
				idx[k++] = (short) (u+j); idx[k++] = (short) (d+j+1); idx[k++] = (short) (u+j+1);
			}
		}
	}
}
