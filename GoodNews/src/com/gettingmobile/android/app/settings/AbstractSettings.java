package com.gettingmobile.android.app.settings;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.*;
import java.util.regex.Pattern;

public abstract class AbstractSettings implements OnSharedPreferenceChangeListener {
    public static final String PREFERENCES_SUFFIX = "_preferences";
    protected static final String LOG_TAG = "goodnews.Settings";
	protected final Context context;
	protected final SharedPreferences prefs;
    private final Map<String, Set<OnSettingChangeListener>> listeners = new HashMap<String, Set<OnSettingChangeListener>>();
    private final Set<OnSharedPreferenceChangeListener> allChangesListeners = new HashSet<OnSharedPreferenceChangeListener>();

    public static String getPreferencesName(Context context, String suffix) {
        return context.getPackageName() + suffix;
    }

    public static SharedPreferences getPreferences(Context context, String suffix) {
        return context.getSharedPreferences(getPreferencesName(context, suffix), Context.MODE_PRIVATE);
    }

    public static SharedPreferences getDefaultPreferences(Context context) {
        return getPreferences(context, PREFERENCES_SUFFIX);
    }
    
	public AbstractSettings(Context context) {
        this.context = context;
        prefs = getDefaultPreferences(context);
        prefs.registerOnSharedPreferenceChangeListener(this);

        /*
         * register internal listeners
         */
	}

    public SharedPreferences getSharedPreferences() {
        return prefs;
    }

	public void registerChangeListener(String key, OnSettingChangeListener listener) {
        Set<OnSettingChangeListener> keyListeners = listeners.get(key);
        if (keyListeners == null) {
            keyListeners = new HashSet<OnSettingChangeListener>();
            listeners.put(key, keyListeners);
        }
		keyListeners.add(listener);
	}
	
	public void unregisterChangeListener(String key, OnSettingChangeListener listener) {
        final Set<OnSettingChangeListener> keyListeners = listeners.get(key);
        if (keyListeners != null) {
            keyListeners.remove(listener);
            if (keyListeners.isEmpty()) {
                listeners.remove(key);
            }
        }
	}

    public void registerChangeListener(OnSharedPreferenceChangeListener listener) {
        allChangesListeners.add(listener);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        /*
         * call preference specific listeners listeners
         */
        final Set<OnSettingChangeListener> keyListeners = listeners.get(key);
        if (keyListeners != null) {
            for (OnSettingChangeListener l : keyListeners) {
                l.onSettingChanged(this, sharedPreferences, key);
            }
        }

        /*
         * call unspecific listeners
         */
        for (OnSharedPreferenceChangeListener l : allChangesListeners) {
            l.onSharedPreferenceChanged(sharedPreferences, key);
        }
    }

    /*
     * dynamic initialization of settings
     */

    public <T extends Enum<T>> void setDefaultValue(String key, T value) {
        if (getString(key) == null) {
            setString(key, value.name());
        }
    }


    /*
     * helper
     */

    public boolean getBoolean(String key, boolean defaultValue) {
        return prefs.getBoolean(key, defaultValue);
    }

	public boolean getBoolean(String key) {
		return getBoolean(key, false);
	}

    public boolean getBooleanOverride(String overrideKey, String key, boolean compatibilityMode) {
        BooleanOverride override;
        try {
            override = getValue(BooleanOverride.class, overrideKey, BooleanOverride.GLOBAL);
        } catch (ClassCastException ex) {
            if (!compatibilityMode)
                throw ex;
            
            final boolean oldBooleanValue = getBoolean(overrideKey);
            final SharedPreferences.Editor editor = prefs.edit();
            editor.remove(overrideKey);
            
            override = oldBooleanValue ? BooleanOverride.TRUE : BooleanOverride.GLOBAL;
            editor.putString(overrideKey, override.toString());
            editor.commit();
        }
        return override != BooleanOverride.GLOBAL ? override == BooleanOverride.TRUE : getBoolean(key);
    }
    
    public boolean getBooleanOverride(String overrideKey, String key) {
        return getBooleanOverride(overrideKey, key, false);
    }

    public void setBoolean(String key, boolean value) {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

	public int getInt(String key) {
		return prefs.getInt(key, 0);
	}

    public void setInt(String key, int value) {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public long getLong(String key) {
        return prefs.getLong(key, 0);
    }

    public void setLong(String key, long value) {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(key, value);
        editor.commit();
    }

	public int getIntFromString(String key, int defValue) {
        final String str = prefs.getString(key, null);
        return str == null || str.length() == 0 ? defValue : Integer.parseInt(str);
	}
	
	public int getIntFromString(String key) {
		return getIntFromString(key, 0);
	}
	
	public String getString(String key) {
		return prefs.getString(key, null);
	}

    public void setString(String key, String value) {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public Calendar getTimestamp(String key) {
        final long timestamp = getLong(key);
        if (timestamp == 0)
            return null;
        
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        return c;
    }

    public void setTimestamp(String key, Calendar timestamp) {
        setLong(key, timestamp.getTimeInMillis());
    }

    public void setTimestamp(String key, long timestamp) {
        setLong(key, timestamp);
    }

    public void setTimestampToNow(String key) {
        setTimestamp(key, Calendar.getInstance());
    }

    public void setTimestampFromNow(String key, int field, int amount) {
        final Calendar c = Calendar.getInstance();
        c.add(field, amount);
        setTimestamp(key, c);
    }

    public Set<String> getStringSet(String key) {
        final String json = getString(key);
        if (json == null)
            return null;

        try {
            final JSONArray jsonArray = new JSONArray(json);
            final Set<String> strings = new HashSet<String>(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); ++i) {
                strings.add(jsonArray.getString(i));
            }
            return strings;
        } catch (JSONException ex) {
            Log.e(LOG_TAG, "Failed to deserialize string set as JSON object", ex);
            return null;
        }
    }

    public void setStringSet(String key, Set<String> strings) {
        if (strings != null) {
            final JSONArray jsonArray = new JSONArray();
            for (String s : strings) {
                jsonArray.put(s);
            }
            setString(key, jsonArray.toString());
        } else {
            setString(key, null);
        }
    }

    public void extendStringSet(String key, Set<String> strings) {
        final Set<String> prevStrings = getStringSet(key);
        if (prevStrings != null) {
            prevStrings.addAll(strings);
            setStringSet(key, prevStrings);
        } else {
            setStringSet(key, strings);
        }

    }

    public <T extends Enum<T>> T getValue(Class<T> type, String key) {
        final String value = getString(key);
        try {
            return value != null ?  T.valueOf(type, value) : null;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public <T extends Enum<T>> T getValue(Class<T> type, String key, T defaultValue) {
        final T value = getValue(type, key);
        return value != null ? value : defaultValue;
    }

    public <T extends Enum<T>> T getValueOverride(Class<T> type, String overrideKey, String key, T defaultValue) {
        final T override = getValue(type, overrideKey);
        return override != null ? override : getValue(type, key, defaultValue);
    }

    public <T extends Enum<T>> void setValue(String key, T value) {
        setString(key, value.name());
    }

    private Set<String> findMatchingEntryKeys(Pattern keyPattern, Pattern valuePattern) {
        final Set<String> matchingKeyEntries = new HashSet<String>();
        for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
            if (entry.getValue() instanceof String) {
                final String key = entry.getKey();
                final String value = (String) entry.getValue();
                if (keyPattern.matcher(key).matches() && valuePattern.matcher(value).matches()) {
                    matchingKeyEntries.add(key);
                }
            }
        }
        return matchingKeyEntries;
    }

    private void updateValues(Set<String> keys, String value) {
        final SharedPreferences.Editor editor = prefs.edit();
        for (String key : keys) {
            Log.i(LOG_TAG, "Updating setting '" + key + "' to " + value);
            editor.putString(key, value);
        }
        editor.commit();
    }

    protected void updateValues(String keyPattern, String valuePattern, String newValue) {
        final Set<String> matchingKeyEntries = findMatchingEntryKeys(
                Pattern.compile(keyPattern), Pattern.compile(valuePattern));
        updateValues(matchingKeyEntries, newValue);
    }
}
