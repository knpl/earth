package nl.knpl.graphics.earth;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import nl.knpl.graphics.earth.R;
import nl.knpl.graphics.earth.SeekBarDialogFragment.SeekBarDialogListener;

public class MainActivity extends ActionBarActivity implements SeekBarDialogListener {

	public static final int AMBIENT_REQCODE = 0x01,
							DIFFUSE_REQCODE = 0x02,
							SPECULAR_REQCODE = 0x03,
							SHININESS_REQCODE = 0x04;
	
	public static final float DEFAULT_AMBIENT = .125f,
							  DEFAULT_DIFFUSE = .75f,
							  DEFAULT_SPECULAR = .25f,
							  DEFAULT_SHININESS = 20f;
	
	private float ambient, diffuse, specular, shininess;
	
	private EarthGLSurfaceView surface;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
        	surface = new EarthGLSurfaceView(this);
        }
        catch (Exception ex) {
        	ex.printStackTrace();
        	finish();
        }
        
        surface.setAmbient(ambient = DEFAULT_AMBIENT);
        surface.setDiffuse(diffuse = DEFAULT_DIFFUSE);
        surface.setSpecular(specular = DEFAULT_SPECULAR);
        surface.setShininess(shininess = DEFAULT_SHININESS);
        
        setContentView(surface);
    }

	@Override
	protected void onPause() {
		super.onPause();
		surface.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		surface.onResume();
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	SeekBarDialogFragment frag;
        switch (item.getItemId()) {
        case R.id.action_ambient_slider:
        	frag = SeekBarDialogFragment.createSeekBarDialogFragment(AMBIENT_REQCODE, 0, 1, ambient);
        	frag.show(getSupportFragmentManager(), "ambient");
        	break;
        case R.id.action_diffuse_slider:
        	frag = SeekBarDialogFragment.createSeekBarDialogFragment(DIFFUSE_REQCODE, 0, 1, diffuse);
        	frag.show(getSupportFragmentManager(), "diffuse");
        	break;
        case R.id.action_specular_slider:
        	frag = SeekBarDialogFragment.createSeekBarDialogFragment(SPECULAR_REQCODE, 0, 1, specular);
        	frag.show(getSupportFragmentManager(), "specular");
        	break;
        case R.id.action_shininess_slider:
        	frag = SeekBarDialogFragment.createSeekBarDialogFragment(SHININESS_REQCODE, 0, 100, shininess);
        	frag.show(getSupportFragmentManager(), "shininess");
        	break;
        case R.id.action_mode_toggle:
        	surface.toggleMode();
        	break;
        default:
        	/* Do nothing */
        }
        return super.onOptionsItemSelected(item);
    }

	@Override
	public void onSeekBarDialogResult(int reqcode, double result) {
		float f = (float) result;
		switch (reqcode) {
		case AMBIENT_REQCODE:
			ambient = f;
			surface.setAmbient(f);
			break;
		case DIFFUSE_REQCODE:
			diffuse = f;
			surface.setDiffuse(f);
			break;
		case SPECULAR_REQCODE:
			specular = f;
			surface.setSpecular(f);
			break;
		case SHININESS_REQCODE:
			shininess = f;
			surface.setShininess(f);
			break;
		default:
			/* Do nothing */
		}
	}
}
