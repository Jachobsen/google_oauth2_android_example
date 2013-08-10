package com.example.oauth.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class SelectAccountDialogFragment extends DialogFragment {
	private SelectAccountDialogProtocol mDelegate = null;
	
	public interface SelectAccountDialogProtocol {
		public void gotAccount(int index);
	}
	
	public static SelectAccountDialogFragment newInstance(int title, String[] accountNames) {
		SelectAccountDialogFragment fragment = new SelectAccountDialogFragment();
        Bundle args = new Bundle();
        
        args.putInt("title", title);
        args.putStringArray("account_names", accountNames);
        
        fragment.setArguments(args);
        return fragment;
    }
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int title = getArguments().getInt("title");
        String[] accountNames = getArguments().getStringArray("account_names");

        return new AlertDialog.Builder(getActivity())
						               .setTitle(title)
						               .setItems(accountNames, new DialogInterface.OnClickListener() {
						                    public void onClick(DialogInterface dialog, int which) {
						                    	mDelegate.gotAccount(which);
						                      }
						                    })
						               .create();
    }
	
	@Override
	public void onAttach(Activity activity) {
		try {
        	mDelegate = (SelectAccountDialogProtocol)activity;
        } catch(ClassCastException e) {}
		
		super.onAttach(activity);
	}
	
	@Override
	public void onDetach() {
		mDelegate = null;
		super.onDetach();
	}
}
