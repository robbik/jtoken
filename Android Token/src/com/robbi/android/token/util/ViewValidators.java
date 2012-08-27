package com.robbi.android.token.util;

import com.robbi.android.token.R;

import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public abstract class ViewValidators {

	public static void cannotBeEmptyOrWhitespaceOnly(EditText view) {
		view.setImeActionLabel("", EditorInfo.IME_ACTION_NEXT);
		
		view.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId != EditorInfo.IME_ACTION_NEXT) {
					if (v.getText().toString().trim().length() == 0) {
						v.setError(v.getContext().getString(R.string.field_cannot_be_blank));
					}
				}
				
				return false;
			}
		});
	}

	public static void cannotBeEmpty(EditText view) {
		view.setImeActionLabel("", EditorInfo.IME_ACTION_NEXT);
		
		view.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId != EditorInfo.IME_ACTION_NEXT) {
					if (v.getText().toString().length() == 0) {
						v.setError(v.getContext().getString(R.string.field_cannot_be_blank));
					}
				}
				
				return false;
			}
		});
	}
}
