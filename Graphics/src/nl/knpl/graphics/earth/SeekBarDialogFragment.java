package nl.knpl.graphics.earth;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;

public class SeekBarDialogFragment extends DialogFragment {
	
	public interface SeekBarDialogListener {
		public void onSeekBarDialogResult(int reqcode, double result);
	}
	
	public static final String REQCODE_KEY = "nl.knpl.graphics.earth.reqcode",
							   FROM_KEY = "nl.knpl.graphics.earth.from",
							   TO_KEY = "nl.knpl.graphics.earth.to",
							   DEFAULT_KEY = "nl.knpl.graphics.earth.default";
	
	private SeekBarDialogListener listener;
	
	public static final SeekBarDialogFragment createSeekBarDialogFragment(
			int reqcode, double from, double to, double defaultt) {
		
		SeekBarDialogFragment dialog = new SeekBarDialogFragment();
		
		Bundle args = new Bundle();
		args.putInt(REQCODE_KEY, reqcode);
		args.putDouble(FROM_KEY, from);
		args.putDouble(TO_KEY, to);
		args.putDouble(DEFAULT_KEY, defaultt);
		dialog.setArguments(args);
		
		return dialog;
	}

	@Override
	@NonNull
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		
		Bundle args = getArguments();
		final int reqcode = args.getInt(REQCODE_KEY);
		final double from = args.getDouble(FROM_KEY),
					 to = args.getDouble(TO_KEY),
					 defaultt = args.getDouble(DEFAULT_KEY);
		
		View layout = inflater.inflate(R.layout.seekbardialog_layout, null);
		final SeekBar seekbar = (SeekBar) layout.findViewById(R.id.seekbar);
		int progress = (int) (100 * (defaultt - from)/(to - from));
		if (progress < 0) 
			progress = 0;
		else if (progress > 100) 
			progress = 100;
		seekbar.setProgress(progress);
		
		builder.setView(layout);
		builder.setTitle(getTag());
		builder.setPositiveButton("OK", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				double result = from + 0.01 * (to - from) * seekbar.getProgress();
				listener.onSeekBarDialogResult(reqcode, result);
			}
		});
		builder.setNegativeButton("Cancel", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		
		return builder.create();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (!(activity instanceof SeekBarDialogListener)) {
			throw new ClassCastException("Activity must implement SeekBarDialogListener");
		}
		listener = (SeekBarDialogListener) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}
}
