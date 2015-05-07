package com.knpl.graphics.cube;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.knpl.graphics.cube.R;

public class MainActivity extends ActionBarActivity {

	private MyGLSurfaceView surface;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
        	surface = new MyGLSurfaceView(this);
	        setContentView(surface);	
        }
        catch (Exception ex) {
        	ex.printStackTrace();
        	finish();
        }
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
        int id = item.getItemId();
        if (id == R.id.action_mode_toggle) {
        	surface.toggleMode();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
