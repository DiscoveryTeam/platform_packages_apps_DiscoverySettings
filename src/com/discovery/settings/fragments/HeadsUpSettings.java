package com.discovery.settings.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.discovery.settings.preference.GlobalSettingSwitchPreference;
import com.discovery.settings.preference.PackageListAdapter;
import com.discovery.settings.preference.PackageListAdapter.PackageItem;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeadsUpSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceClickListener {

    private static final int DIALOG_BLACKLIST_APPS = 0;
    private static final int DIALOG_WHITELIST_APPS = 1;
    private static final String KEY_HEADS_UP_NOTIFICATIONS_ENABLED = "heads_up_notifications_enabled"; 

    private PackageListAdapter mPackageAdapter;
    private PackageManager mPackageManager;
    private PreferenceGroup mBlacklistPrefList;
    private PreferenceGroup mWhitelistPrefList;
    private Preference mAddBlacklistPref;
    private Preference mAddWhitelistPref;

    private String mBlacklistPackageList;
    private String mWhitelistPackageList;
    private Map<String, Package> mBlacklistPackages;
    private Map<String, Package> mWhitelistPackages;

    private GlobalSettingSwitchPreference mHeadsUpNotificationsEnabled; 

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get launch-able applications
        addPreferencesFromResource(R.xml.heads_up_settings);
        mPackageManager = getPackageManager();
        mPackageAdapter = new PackageListAdapter(getActivity());

        mBlacklistPrefList = (PreferenceGroup) findPreference("blacklist_applications");
        mBlacklistPrefList.setOrderingAsAdded(false);

        mWhitelistPrefList = (PreferenceGroup) findPreference("whitelist_applications");
        mWhitelistPrefList.setOrderingAsAdded(false);

        mBlacklistPackages = new HashMap<String, Package>();
        mWhitelistPackages = new HashMap<String, Package>();

        mAddBlacklistPref = findPreference("add_blacklist_packages");
        mAddWhitelistPref = findPreference("add_whitelist_packages");

        mAddBlacklistPref.setOnPreferenceClickListener(this);
        mAddWhitelistPref.setOnPreferenceClickListener(this);

        mHeadsUpNotificationsEnabled = (GlobalSettingSwitchPreference) findPreference(KEY_HEADS_UP_NOTIFICATIONS_ENABLED);
        updatePrefs();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshCustomApplicationPrefs();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.DISCOVERY_SETTINGS;
    }

    @Override
    public int getDialogMetricsCategory(int dialogId) {
        if (dialogId == DIALOG_BLACKLIST_APPS || dialogId == DIALOG_WHITELIST_APPS) {
            return MetricsProto.MetricsEvent.DISCOVERY_SETTINGS;
        }
        return 0;
    }

    /**
     * Utility classes and supporting methods
     */
    @Override
    public Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final Dialog dialog;
        final ListView list = new ListView(getActivity());
        list.setAdapter(mPackageAdapter);

        builder.setTitle(R.string.profile_choose_app);
        builder.setView(list);
        dialog = builder.create();

        switch (id) {
            case DIALOG_BLACKLIST_APPS:
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent,
                            View view, int position, long id) {
                        PackageItem info = (PackageItem) parent.getItemAtPosition(position);
                        addCustomApplicationPref(info.packageName, mBlacklistPackages);
                        dialog.cancel();
                    }
                });
                 break;
            case DIALOG_WHITELIST_APPS:
                list.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // Add empty application definition, the user will be able to edit it later
                        PackageItem info = (PackageItem) parent.getItemAtPosition(position);
                        addCustomApplicationPref(info.packageName, mWhitelistPackages);
                        dialog.cancel();
                    }
                });
        }
        return dialog;
    }

    /**
     * Application class
     */
    private static class Package {
        public String name;
        /**
         * Stores all the application values in one call
         * @param name
         */
        public Package(String name) {
            this.name = name;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(name);
            return builder.toString();
        }

        public static Package fromString(String value) {
            if (TextUtils.isEmpty(value)) {
                return null;
            }

            try {
                Package item = new Package(value);
                return item;
            } catch (NumberFormatException e) {
                return null;
            }
        }

    };

    private void refreshCustomApplicationPrefs() {
        if (!parsePackageList()) {
            return;
        }

        // Add the Application Preferences
        if (mBlacklistPrefList != null && mWhitelistPrefList != null) {
            mBlacklistPrefList.removeAll();
            mWhitelistPrefList.removeAll();

            for (Package pkg : mBlacklistPackages.values()) {
                try {
                    Preference pref = createPreferenceFromInfo(pkg);
                    mBlacklistPrefList.addPreference(pref);
                } catch (PackageManager.NameNotFoundException e) {
                    // Do nothing
                }
            }

            for (Package pkg : mWhitelistPackages.values()) {
                try {
                    Preference pref = createPreferenceFromInfo(pkg);
                    mWhitelistPrefList.addPreference(pref);
                } catch (PackageManager.NameNotFoundException e) {
                    // Do nothing
                }
            }
        }

        // Keep these at the top
        mAddBlacklistPref.setOrder(0);
        mAddWhitelistPref.setOrder(0);
        // Add 'add' options
        mBlacklistPrefList.addPreference(mAddBlacklistPref);
        mWhitelistPrefList.addPreference(mAddWhitelistPref);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == mAddBlacklistPref) {
            showDialog(DIALOG_BLACKLIST_APPS);
        } else if (preference == mAddWhitelistPref) {
            showDialog(DIALOG_WHITELIST_APPS);
        } else {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.dialog_delete_title)
                    .setMessage(R.string.dialog_delete_message)
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (preference == mBlacklistPrefList.findPreference(preference.getKey())) {
                                 removeApplicationPref(preference.getKey(), mBlacklistPackages);
                            } else if (preference == mWhitelistPrefList.findPreference(preference.getKey())) {
                                 removeApplicationPref(preference.getKey(), mWhitelistPackages);
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null);

        builder.show();
        }
        updatePrefs();
        return true;
    }

     private void addCustomApplicationPref(String packageName, Map<String,Package> map) {
        Package pkg = map.get(packageName);
        if (pkg == null) {
            pkg = new Package(packageName);
            map.put(packageName, pkg);
            savePackageList(false, map);
            refreshCustomApplicationPrefs();
        }
    }

    private Preference createPreferenceFromInfo(Package pkg)
            throws PackageManager.NameNotFoundException {
        PackageInfo info = mPackageManager.getPackageInfo(pkg.name,
                PackageManager.GET_META_DATA);
        Preference pref =
                new Preference(getActivity());

        pref.setKey(pkg.name);
        pref.setTitle(info.applicationInfo.loadLabel(mPackageManager));
        pref.setIcon(info.applicationInfo.loadIcon(mPackageManager));
        pref.setPersistent(false);
        pref.setOnPreferenceClickListener(this);
        return pref;
    }

    private void removeApplicationPref(String packageName, Map<String,Package> map) {
        if (map.remove(packageName) != null) {
            savePackageList(false, map);
            refreshCustomApplicationPrefs();
        }
    }

    private boolean parsePackageList() {

        boolean parsed = false;
 
        final String blacklistString = Settings.System.getString(getContentResolver(),
                Settings.System.HEADS_UP_BLACKLIST_VALUES);
  
        final String whitelistString = Settings.System.getString(getContentResolver(),
                Settings.System.HEADS_UP_WHITELIST_VALUES);

        if (!TextUtils.equals(mBlacklistPackageList, blacklistString)) {
            mBlacklistPackageList = blacklistString;
            mBlacklistPackages.clear();
            parseAndAddToMap(blacklistString, mBlacklistPackages);
            parsed = true;
        }
        if (!TextUtils.equals(mWhitelistPackageList, whitelistString)) {
            mWhitelistPackageList = whitelistString;
            mWhitelistPackages.clear();
            parseAndAddToMap(whitelistString, mWhitelistPackages);
            parsed = true;
        }
 
        return parsed;
    }

    private void parseAndAddToMap(String baseString, Map<String,Package> map) {
        if (baseString == null) {
            return;
        }

        final String[] array = TextUtils.split(baseString, "\\|");
        for (String item : array) {
            if (TextUtils.isEmpty(item)) {
                continue;
            }
            Package pkg = Package.fromString(item);
            map.put(pkg.name, pkg);
        }
    }
  
    private void savePackageList(boolean preferencesUpdated, Map<String,Package> map) {
        String setting = map == mBlacklistPackages
                ? Settings.System.HEADS_UP_BLACKLIST_VALUES
                : Settings.System.HEADS_UP_WHITELIST_VALUES;
        List<String> settings = new ArrayList<String>();
        for (Package app : map.values()) {
            settings.add(app.toString());
        }
        final String value = TextUtils.join("|", settings);
        if (preferencesUpdated) {
            if (TextUtils.equals(setting, Settings.System.HEADS_UP_BLACKLIST_VALUES)) {
                mBlacklistPackageList = value;
            } else {
                mWhitelistPackageList = value;
            }
        }
        Settings.System.putString(getContentResolver(),
                setting, value);
    }

    private void updatePrefs() {
          ContentResolver resolver = getActivity().getContentResolver();
          boolean enabled = (Settings.System.getInt(resolver,
                  Settings.System.STATUS_BAR_SHOW_TICKER, 0) == 1) ||
                  (Settings.System.getInt(resolver,
                  Settings.System.STATUS_BAR_SHOW_TICKER, 0) == 2);
        if (enabled) {
            Settings.Global.putInt(resolver,
                Settings.Global.HEADS_UP_NOTIFICATIONS_ENABLED, 0);
            mBlacklistPrefList.setEnabled(false);
            mWhitelistPrefList.setEnabled(false);            
            mHeadsUpNotificationsEnabled.setEnabled(false);
        }
    }

}
