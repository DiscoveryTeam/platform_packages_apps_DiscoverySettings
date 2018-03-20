package com.discovery.settings.fragments;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.ContentResolver;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.text.TextUtils;
import android.view.IWindowManager;
import android.view.View;
import android.view.WindowManagerGlobal;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StatusBarSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {

    private static final String TAG = "StatusbarSettings";
    private static final String STATUS_BAR_SHOW_TICKER = "status_bar_show_ticker";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.statusbar_settings);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

    }

    @Override
    public int getMetricsCategory() {
      return MetricsProto.MetricsEvent.DISCOVERY_SETTINGS;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.statusbar_settings;
                    result.add(sir);
                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    ArrayList<String> result = new ArrayList<String>();
                    return result;
                }
            };
}
