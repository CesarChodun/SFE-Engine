package main.java.core.resources;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Class for obtaining configuration data from a JSON file.
 *
 * @author Cezary Chodun
 * @since 23.10.2019
 */
public class ConfigAsset {

    /** The JSON object with configuration data. */
    protected JSONObject data;

    /** Creates a new configuration data from the asset, and given its path within the asset. */
    public ConfigAsset(JSONObject obj) {
        this.data = obj;
    }

    /**
     * Obtains a configuration asset from the configuration file.
     *
     * @param cfgAssetName Name of the configuration asset.
     * @return The configuration asset.
     */
    public ConfigAsset getCfgAsset(String cfgAssetName) {
        if (data.isNull(cfgAssetName)) {
            data.put(cfgAssetName, new JSONObject());
        }

        return new ConfigAsset(data.getJSONObject(cfgAssetName));
    }

    /**
     * Obtains an array of JSON objects.
     *
     * @param listName Key for the array.
     * @return The array(converted to list).
     */
    public List<ConfigAsset> getCfgList(String listName) {
        if (data.isNull(listName)) {
            data.put(listName, new JSONArray());
        }

        JSONArray arr = data.getJSONArray(listName);

        List<ConfigAsset> cfgs = new ArrayList<ConfigAsset>();
        for (int i = 0; i < arr.length(); i++) {
            cfgs.add(new ConfigAsset(arr.getJSONObject(i)));
        }

        return cfgs;
    }

    /**
     * Obtains flags with names from the configuration data. Flags values are taken from the
     * 'source' class. And if the configuration data doesn't contain any value for the key, the
     * default value is used instead.
     *
     * @param source Class for the flags integer values.
     * @param key The key to the string with flag names.
     * @param defaultFlag The default flag name.
     * @return Combined flags(using or operator).
     * @throws NoSuchFieldException If a field with the specified name is not
     *     found.SecurityException.
     * @throws SecurityException If a security manager, s, is present and the caller'sclass loader
     *     is not the same as or an ancestor of the class loader for the current class and
     *     invocation of s.checkPackageAccess() denies access to the package of this
     *     class.IllegalArgumentException.
     * @throws IllegalArgumentException If the specified object is not an instance of the class or
     *     interface declaring the underlying field (or a subclass or implementor thereof), or if
     *     the field value cannot be converted to the type int by a widening.
     * @throws IllegalAccessException If this Field object is enforcing Java language access control
     *     and the underlying field is inaccessible.
     */
    public int getFlags(Class<?> source, String key, String defaultFlag)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException,
                    IllegalAccessException {

        List<String> defaultValue = new ArrayList<String>();
        defaultValue.add(defaultFlag);

        List<String> flagNames = getArray(key, defaultValue);
        List<Integer> flagValues = ResourceUtil.getStaticIntValuesFromClass(source, flagNames);
        int flags = 0;
        for (int i = 0; i < flagValues.size(); i++) {
            flags |= flagValues.get(i);
        }

        return flags;
    }

    /**
     * Obtains flags with names from the configuration data. Flags values are taken from the
     * 'source' class.
     *
     * @param source Class for the flags integer values.
     * @param key The key to the string with flag names.
     * @return Combined flags(using or operator). If no flags are present '0' is returned.
     * @throws NoSuchFieldException If a field with the specified name is not
     *     found.SecurityException.
     * @throws SecurityException If a security manager, s, is present and the caller'sclass loader
     *     is not the same as or an ancestor of the class loader for the current class and
     *     invocation of s.checkPackageAccess() denies access to the package of this
     *     class.IllegalArgumentException.
     * @throws IllegalArgumentException If the specified object is not an instance of the class or
     *     interface declaring the underlying field (or a subclass or implementor thereof), or if
     *     the field value cannot be converted to the type int by a widening.
     * @throws IllegalAccessException If this Field object is enforcing Java language access control
     *     and the underlying field is inaccessible.
     */
    public int getFlags(Class<?> source, String key)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException,
                    IllegalAccessException {
        return getFlags(source, key, new ArrayList<String>());
    }

    /**
     * Obtains flags with names from the configuration data. Flags values are taken from the
     * 'source' class. And if the configuration data doesn't contain any value for the key, the
     * default value is used instead.
     *
     * @param source Class for the flags integer values.
     * @param key The key to the string with flag names.
     * @param defaultValue The default list of flag names.
     * @return Combined flags(using or operator).
     * @throws NoSuchFieldException If a field with the specified name is not
     *     found.SecurityException.
     * @throws SecurityException If a security manager, s, is present and the caller'sclass loader
     *     is not the same as or an ancestor of the class loader for the current class and
     *     invocation of s.checkPackageAccess() denies access to the package of this
     *     class.IllegalArgumentException.
     * @throws IllegalArgumentException If the specified object is not an instance of the class or
     *     interface declaring the underlying field (or a subclass or implementor thereof), or if
     *     the field value cannot be converted to the type int by a widening.
     * @throws IllegalAccessException If this Field object is enforcing Java language access control
     *     and the underlying field is inaccessible.
     */
    public int getFlags(Class<?> source, String key, List<String> defaultValue)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException,
                    IllegalAccessException {

        List<String> flagNames = getArray(key, defaultValue);
        List<Integer> flagValues = ResourceUtil.getStaticIntValuesFromClass(source, flagNames);
        int flags = 0;
        for (int i = 0; i < flagValues.size(); i++) {
            flags |= flagValues.get(i);
        }

        return flags;
    }

    /**
     * Obtains a static int value from the given class. The static variable has the name equal to
     * the string that corresponds to the given key.
     *
     * @param source The class to obtain value from.
     * @param key Key to the string.
     * @param defaultValue Default value of the string.
     * @return Value from the class.
     * @throws AssertionError When failed to obtain the value.
     */
    public int getStaticIntFromClass(Class<?> source, String key, String defaultValue)
            throws AssertionError {

        String valueName = getString(key, defaultValue);
        int out = 0;

        try {
            out = ResourceUtil.getStaticIntValueFromClass(source, valueName);
        } catch (NoSuchFieldException
                | SecurityException
                | IllegalArgumentException
                | IllegalAccessException e) {
            e.printStackTrace();
            throw new AssertionError("Failed to load static value from a class!");
        }

        return out;
    }

    /**
     * Obtains a static Integer from the given class. The static variable has the name equal to the
     * string that corresponds to the given key.
     *
     * @param source The class to obtain value from.
     * @param key Key to the string.
     * @param defaultValue Default value of the string.
     * @return Value from the class.
     * @throws AssertionError When failed to obtain the value.
     */
    public Integer getStaticIntegerFromClass(Class<?> source, String key, String defaultValue)
            throws AssertionError {
        return new Integer(getStaticIntFromClass(source, key, defaultValue));
    }

    /**
     * Obtains an array from the JSON object.
     *
     * @param <T> Type of the objects in the list.
     * @param key The key(name) of the value.
     * @param defaultValue The default value for the key.
     * @return A list with the objects from the JSON data.
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public <T> List<T> getArray(String key, List<T> defaultValue) {
        if (data.isNull(key)) {
            data.put(key, defaultValue);
            return defaultValue;
        }

        JSONArray arr = data.getJSONArray(key);
        List<T> out = new ArrayList<T>();
        for (int i = 0; i < arr.length(); i++) {
            out.add((T) arr.get(i));
        }

        return out;
    }

    /**
     * Obtains an float array from the JSON object.
     *
     * @param key The key(name) of the value.
     * @param defaultValue The default value for the key.
     * @return A list with the objects from the JSON data.
     */
    public List<Float> getFloatArray(String key, List<Float> defaultValue) {
        if (data.isNull(key)) {
            data.put(key, defaultValue);
            return defaultValue;
        }

        JSONArray arr = data.getJSONArray(key);
        List<Float> out = new ArrayList<Float>();
        for (int i = 0; i < arr.length(); i++) {
            out.add(arr.getFloat(i));
        }

        return out;
    }

    /**
     * Obtains an string array from the JSON object.
     *
     * @param key The key(name) of the value.
     * @param defaultValue The default value for the key.
     * @return A list with the objects from the JSON data.
     */
    public List<String> getStringArray(String key, List<String> defaultValue) {
        if (data.isNull(key)) {
            data.put(key, defaultValue);
            return defaultValue;
        }

        JSONArray arr = data.getJSONArray(key);
        List<String> out = new ArrayList<String>();
        for (int i = 0; i < arr.length(); i++) {
            out.add(arr.getString(i));
        }

        return out;
    }

    /**
     * Obtains a String from the JSON data.
     *
     * @param key Key of the value.
     * @param defaultValue Default value.
     * @return Value corresponding to the key from the JSON object or the default value.
     */
    public String getString(String key, String defaultValue) {

        if (!data.isNull(key)) {
            return data.getString(key);
        }

        data.put(key, defaultValue);
        return defaultValue;
    }

    /**
     * Obtains a Integer from the JSON data.
     *
     * @param key Key of the value.
     * @param defaultValue Default value.
     * @return Value corresponding to the key from the JSON object or the default value.
     */
    public Integer getInteger(String key, Integer defaultValue) {

        if (!data.isNull(key)) {
            return data.getInt(key);
        }

        data.put(key, (int) defaultValue);
        return defaultValue;
    }

    /**
     * Obtains a Float from the JSON data.
     *
     * @param key Key of the value.
     * @param defaultValue Default value.
     * @return Value corresponding to the key from the JSON object or the default value.
     */
    public Float getFloat(String key, Float defaultValue) {

        if (!data.isNull(key)) {
            return data.getFloat(key);
        }

        data.put(key, (float) defaultValue);
        return defaultValue;
    }

    /**
     * Obtains a Double from the data.
     *
     * @param key Key of the value.
     * @param defaultValue Default value.
     * @return Value corresponding to the key from the JSON object or the default value.
     */
    public Double getDouble(String key, Double defaultValue) {

        if (!data.isNull(key)) {
            return data.getDouble(key);
        }

        data.put(key, (double) defaultValue);
        return defaultValue;
    }

    /**
     * Obtains a Boolean from the JSON data.
     *
     * @param key Key of the value.
     * @param defaultValue Default value.
     * @return Value corresponding to the key from the JSON object or the default value.
     */
    public Boolean getBoolean(String key, Boolean defaultValue) {

        if (!data.isNull(key)) {
            return data.getBoolean(key);
        }

        data.put(key, (boolean) defaultValue);
        return defaultValue;
    }

    /**
     * Returns a JSON object with the configuration data.
     *
     * @return the JSON object.
     */
    public JSONObject asJSON() {
        return data;
    }
}
