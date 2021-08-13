package com.quartzy.redditclient;

import android.os.Bundle;
import android.text.InputType;

import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        EditTextPreference itemsPerPage = findPreference("itemsPerPage");
        if (itemsPerPage!=null){
            itemsPerPage.setOnBindEditTextListener(editText -> {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            });
        }
    }
}