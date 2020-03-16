// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.request.network;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpCookie;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.TreeMap;

/**
 * <p>
 * Http header.
 * </p>
 */
public class Headers {

    /**
     * The constant TIME_FORMAT_HTTP.
     */
    public static final String TIME_FORMAT_HTTP = "EEE, dd MMM y HH:mm:ss 'GMT'";
    /**
     * The constant TIME_ZONE_GMT.
     */
    public static final TimeZone TIME_ZONE_GMT = TimeZone.getTimeZone("GMT");

    /**
     * The constant KEY_ACCEPT.
     */
    public static final String KEY_ACCEPT = "Accept";
    /**
     * The constant VALUE_ACCEPT_ALL.
     */
    public static final String VALUE_ACCEPT_ALL = "*/*";
    /**
     * The constant KEY_ACCEPT_ENCODING.
     */
    public static final String KEY_ACCEPT_ENCODING = "Accept-Encoding";
    /**
     * The constant VALUE_ACCEPT_ENCODING.
     */
    public static final String VALUE_ACCEPT_ENCODING = "gzip, deflate";
    /**
     * The constant KEY_ACCEPT_LANGUAGE.
     */
    public static final String KEY_ACCEPT_LANGUAGE = "Accept-Language";
    /**
     * The constant KEY_ACCEPT_RANGE.
     */
    public static final String KEY_ACCEPT_RANGE = "Accept-Range";
    /**
     * The constant KEY_COOKIE.
     */
    public static final String KEY_COOKIE = "Cookie";
    /**
     * The constant KEY_CONTENT_DISPOSITION.
     */
    public static final String KEY_CONTENT_DISPOSITION = "Content-Disposition";
    /**
     * The constant KEY_CONTENT_ENCODING.
     */
    public static final String KEY_CONTENT_ENCODING = "Content-Encoding";
    /**
     * The constant KEY_CONTENT_LENGTH.
     */
    public static final String KEY_CONTENT_LENGTH = "Content-Length";
    /**
     * The constant KEY_CONTENT_RANGE.
     */
    public static final String KEY_CONTENT_RANGE = "Content-Range";
    /**
     * The constant KEY_CONTENT_TYPE.
     */
    public static final String KEY_CONTENT_TYPE = "Content-Type";
    /**
     * The constant VALUE_APPLICATION_URLENCODED.
     */
    public static final String VALUE_APPLICATION_URLENCODED = "application/x-www-form-urlencoded";
    /**
     * The constant VALUE_APPLICATION_FORM.
     */
    public static final String VALUE_APPLICATION_FORM = "multipart/form-data";
    /**
     * The constant VALUE_APPLICATION_STREAM.
     */
    public static final String VALUE_APPLICATION_STREAM = "application/octet-stream";
    /**
     * The constant VALUE_APPLICATION_JSON.
     */
    public static final String VALUE_APPLICATION_JSON = "application/json";
    /**
     * The constant VALUE_APPLICATION_XML.
     */
    public static final String VALUE_APPLICATION_XML = "application/xml";
    /**
     * The constant KEY_CACHE_CONTROL.
     */
    public static final String KEY_CACHE_CONTROL = "Cache-Control";
    /**
     * The constant KEY_CONNECTION.
     */
    public static final String KEY_CONNECTION = "Connection";
    /**
     * The constant VALUE_KEEP_ALIVE.
     */
    public static final String VALUE_KEEP_ALIVE = "keep-alive";
    /**
     * The constant VALUE_CLOSE.
     */
    public static final String VALUE_CLOSE = "close";
    /**
     * The constant KEY_DATE.
     */
    public static final String KEY_DATE = "Date";
    /**
     * The constant KEY_EXPIRES.
     */
    public static final String KEY_EXPIRES = "Expires";
    /**
     * The constant KEY_E_TAG.
     */
    public static final String KEY_E_TAG = "ETag";
    /**
     * The constant KEY_HOST.
     */
    public static final String KEY_HOST = "Host";
    /**
     * The constant KEY_IF_MODIFIED_SINCE.
     */
    public static final String KEY_IF_MODIFIED_SINCE = "If-Modified-Since";
    /**
     * The constant KEY_IF_NONE_MATCH.
     */
    public static final String KEY_IF_NONE_MATCH = "If-None-Match";
    /**
     * The constant KEY_LAST_MODIFIED.
     */
    public static final String KEY_LAST_MODIFIED = "Last-Modified";
    /**
     * The constant KEY_LOCATION.
     */
    public static final String KEY_LOCATION = "Location";
    /**
     * The constant KEY_RANGE.
     */
    public static final String KEY_RANGE = "Range";
    /**
     * The constant KEY_SET_COOKIE.
     */
    public static final String KEY_SET_COOKIE = "Set-Cookie";
    /**
     * The constant KEY_USER_AGENT.
     */
    public static final String KEY_USER_AGENT = "User-Agent";

    private Map<String, List<String>> mSource;

    /**
     * Instantiates a new Headers.
     */
    public Headers() {
        mSource = new TreeMap<>();
    }

    /**
     * Add.
     *
     * @param key   the key
     * @param value the value
     */
    public void add(String key, String value) {
        if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
            if (!mSource.containsKey(key)) {
                mSource.put(key, new ArrayList<String>(1));
            }
            mSource.get(key).add(value);
        }
    }


    /**
     * Set.
     *
     * @param key   the key
     * @param value the value
     */
    public void set(String key, String value) {
        if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
            mSource.remove(key);
            add(key, value);
        }
    }

    /**
     * Add.
     *
     * @param key    the key
     * @param values the values
     */
    public void add(String key, List<String> values) {
        if (!TextUtils.isEmpty(key) && !values.isEmpty()) {
            for (String value : values) {
                add(key, value);
            }
        }
    }

    /**
     * Set.
     *
     * @param key    the key
     * @param values the values
     */
    public void set(String key, List<String> values) {
        if (!TextUtils.isEmpty(key) && !values.isEmpty()) {
            mSource.put(key, values);
        }
    }

    /**
     * Add.
     *
     * @param headers the headers
     */
    public void add(Headers headers) {
        for (Map.Entry<String, List<String>> entry : mSource.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();
            for (String value : values) {
                add(key, value);
            }
        }
    }

    /**
     * Remove list.
     *
     * @param key the key
     * @return the list
     */
    public List<String> remove(String key) {
        return mSource.remove(key);
    }

    /**
     * Get list.
     *
     * @param key the key
     * @return the list
     */
    public List<String> get(String key) {
        return mSource.get(key);
    }

    /**
     * Gets first.
     *
     * @param key the key
     * @return the first
     */
    public String getFirst(String key) {
        List<String> values = mSource.get(key);
        if (values != null && values.size() > 0) {
            return values.get(0);
        }
        return null;
    }

    /**
     * Contains key boolean.
     *
     * @param key the key
     * @return the boolean
     */
    public boolean containsKey(String key) {
        return mSource.containsKey(key);
    }

    /**
     * Replaces all.
     *
     * @param headers the headers
     */
    public void set(Headers headers) {
        for (Map.Entry<String, List<String>> entry : mSource.entrySet()) {
            set(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Get {@link Set} view of the mappings.
     *
     * @return a set view of the mappings.
     * @see Map#entrySet() Map#entrySet()
     */
    public Set<Map.Entry<String, List<String>>> entrySet() {
        return mSource.entrySet();
    }

    /**
     * Get {@link Set} view of the keys.
     *
     * @return the set
     */
    public Set<String> keySet() {
        return mSource.keySet();
    }

    /**
     * Get the count of key-values.
     *
     * @return the int
     */
    public int size() {
        return mSource.size();
    }

    /**
     * Map is empty.
     *
     * @return the boolean
     */
    public boolean isEmpty() {
        return mSource.isEmpty();
    }

    /**
     * Clear.
     */
    public void clear() {
        mSource.clear();
    }

    /**
     * To map map.
     *
     * @return the map
     */
    public Map<String, List<String>> toMap() {
        return mSource;
    }


    /**
     * {@value #KEY_CACHE_CONTROL}.
     *
     * @return CacheControl. cache control
     */
    public String getCacheControl() {
        List<String> cacheControls = get(KEY_CACHE_CONTROL);
        if (cacheControls == null) {
            cacheControls = Collections.emptyList();
        }
        return TextUtils.join(",", cacheControls);
    }

    /**
     * {@value KEY_CONTENT_DISPOSITION}.
     *
     * @return {@value KEY_CONTENT_DISPOSITION}.
     */
    public String getContentDisposition() {
        return getFirst(KEY_CONTENT_DISPOSITION);
    }

    /**
     * {@value #KEY_CONTENT_ENCODING}.
     *
     * @return ContentEncoding. content encoding
     */
    public String getContentEncoding() {
        return getFirst(KEY_CONTENT_ENCODING);
    }

    /**
     * {@value #KEY_CONTENT_LENGTH}.
     *
     * @return ContentLength. content length
     */
    public long getContentLength() {
        String contentLength = getFirst(KEY_CONTENT_LENGTH);
        if (TextUtils.isEmpty(contentLength)) {
            contentLength = "0";
        }
        return Long.parseLong(contentLength);
    }

    /**
     * {@value #KEY_CONTENT_TYPE}.
     *
     * @return ContentType. content type
     */
    public String getContentType() {
        return getFirst(KEY_CONTENT_TYPE);
    }

    /**
     * {@value #KEY_CONTENT_RANGE}.
     *
     * @return ContentRange. content range
     */
    public String getContentRange() {
        return getFirst(KEY_CONTENT_RANGE);
    }

    /**
     * {@value #KEY_DATE}.
     *
     * @return Date. date
     */
    public long getDate() {
        return getDateField(KEY_DATE);
    }

    /**
     * {@value #KEY_E_TAG}.
     *
     * @return ETag. e tag
     */
    public String getETag() {
        return getFirst(KEY_E_TAG);
    }

    /**
     * {@value #KEY_EXPIRES}.
     *
     * @return Expiration. expires
     */
    public long getExpires() {
        return getDateField(KEY_EXPIRES);
    }

    /**
     * {@value #KEY_LAST_MODIFIED}.
     *
     * @return LastModified. last modified
     */
    public long getLastModified() {
        return getDateField(KEY_LAST_MODIFIED);
    }

    /**
     * {@value #KEY_LOCATION}.
     *
     * @return Location. location
     */
    public String getLocation() {
        return getFirst(KEY_LOCATION);
    }

    /**
     * <p>
     * Returns the date value in milliseconds since 1970.1.1, 00:00h corresponding to the header field field. The
     * defaultValue will be returned if no such field can be found in the headers header.
     * </p>
     *
     * @param key the header field name.
     * @return the header field represented in milliseconds since January 1, 1970 GMT.
     */
    private long getDateField(String key) {
        String value = getFirst(key);
        if (!TextUtils.isEmpty(value)) {
            try {
                return formatGMTToMillis(value);
            } catch (ParseException ignored) {
            }
        }
        return 0;
    }

    /**
     * Formats to Hump-shaped words.
     *
     * @param key the key
     * @return the string
     */
    public static String formatKey(String key) {
        if (TextUtils.isEmpty(key)) {
            return null;
        }

        key = key.toLowerCase(Locale.ENGLISH);
        String[] words = key.split("-");

        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            String first = word.substring(0, 1);
            String end = word.substring(1);
            builder.append(first.toUpperCase(Locale.ENGLISH)).append(end).append("-");
        }
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.lastIndexOf("-"));
        }
        return builder.toString();
    }

    /**
     * From the json format String parsing out the {@code Map<String, List<String>>} data.
     *
     * @param jsonString the json string
     * @return the headers
     * @throws JSONException the json exception
     */
    public static Headers fromJSONString(String jsonString) throws JSONException {
        Headers headers = new Headers();
        JSONObject jsonObject = new JSONObject(jsonString);
        Iterator<String> keySet = jsonObject.keys();
        while (keySet.hasNext()) {
            String key = keySet.next();
            String value = jsonObject.optString(key);
            JSONArray values = new JSONArray(value);
            for (int i = 0; i < values.length(); i++) {
                headers.add(key, values.optString(i));
            }
        }
        return headers;
    }

    /**
     * Into a json format string.
     *
     * @param headers the headers
     * @return the string
     */
    public static String toJSONString(Headers headers) {
        JSONObject jsonObject = new JSONObject();
        Set<Map.Entry<String, List<String>>> entrySet = headers.entrySet();
        for (Map.Entry<String, List<String>> entry : entrySet) {
            String key = entry.getKey();
            List<String> values = entry.getValue();
            JSONArray value = new JSONArray(values);
            try {
                jsonObject.put(key, value);
            } catch (JSONException ignored) {
            }
        }
        return jsonObject.toString();
    }

    /**
     * Into a single key-value map.
     *
     * @param headers the headers
     * @return the request headers
     */
    public static Map<String, String> getRequestHeaders(Headers headers) {
        Map<String, String> headerMap = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            String key = entry.getKey();
            List<String> value = entry.getValue();
            String trueValue = TextUtils.join("; ", value);
            headerMap.put(key, trueValue);
        }
        return headerMap;
    }

    /**
     * All the cookies in header information.
     *
     * @param headers the headers
     * @return the http cookie list
     */
    public static List<HttpCookie> getHttpCookieList(Headers headers) {
        List<HttpCookie> cookies = new ArrayList<>();
        for (String key : headers.keySet()) {
            if (key.equalsIgnoreCase(KEY_SET_COOKIE)) {
                List<String> cookieValues = headers.get(key);
                for (String cookieStr : cookieValues) {
                    cookies.addAll(HttpCookie.parse(cookieStr));
                }
            }
        }
        return cookies;
    }

    /**
     * A value of the header information.
     *
     * @param content      like {@code text/html;charset=utf-8}.
     * @param key          like {@code charset}.
     * @param defaultValue list {@code utf-8}.
     * @return If you have a value key, you will return the parsed value if you don't return the default value.
     */
    public static String parseSubValue(String content, String key, String defaultValue) {
        if (!TextUtils.isEmpty(content) && !TextUtils.isEmpty(key)) {
            StringTokenizer stringTokenizer = new StringTokenizer(content, ";");
            while (stringTokenizer.hasMoreElements()) {
                String valuePair = stringTokenizer.nextToken();
                int index = valuePair.indexOf('=');
                if (index > 0) {
                    String name = valuePair.substring(0, index).trim();
                    if (key.equalsIgnoreCase(name)) {
                        defaultValue = valuePair.substring(index + 1).trim();
                        break;
                    }
                }
            }
        }
        return defaultValue;
    }

    /**
     * Parses the time in GMT format to milliseconds.
     *
     * @param gmtTime the gmt time
     * @return the long
     * @throws ParseException the parse exception
     */
    public static long formatGMTToMillis(String gmtTime) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(TIME_FORMAT_HTTP, Locale.US);
        formatter.setTimeZone(TIME_ZONE_GMT);
        Date date = formatter.parse(gmtTime);
        return date.getTime();
    }

    /**
     * Parses the time in milliseconds to GMT format.
     *
     * @param milliseconds the milliseconds
     * @return the string
     */
    public static String formatMillisToGMT(long milliseconds) {
        Date date = new Date(milliseconds);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TIME_FORMAT_HTTP, Locale.US);
        simpleDateFormat.setTimeZone(TIME_ZONE_GMT);
        return simpleDateFormat.format(date);
    }

    /**
     * Analyses the headers of the cache has valid time.
     *
     * @param headers http headers header.
     * @return Time corresponding milliseconds.
     */
    public static long analysisCacheExpires(Headers headers) {
        final long now = System.currentTimeMillis();

        long maxAge = 0;
        long staleWhileRevalidate = 0;

        String cacheControl = headers.getCacheControl();
        if (!TextUtils.isEmpty(cacheControl)) {
            StringTokenizer tokens = new StringTokenizer(cacheControl, ",");
            while (tokens.hasMoreTokens()) {
                String token = tokens.nextToken().trim().toLowerCase(Locale.getDefault());
                if ((token.equals("no-cache") || token.equals("no-store"))) {
                    return 0;
                } else if (token.startsWith("max-age=")) {
                    maxAge = Long.parseLong(token.substring(8)) * 1000L;
                } else if (token.startsWith("must-revalidate")) {
                    // If must-revalidate, It must be the server to validate expiration.
                    return 0;
                } else if (token.startsWith("stale-while-revalidate=")) {
                    staleWhileRevalidate = Long.parseLong(token.substring(23)) * 1000L;
                }
            }
        }

        long localExpire = now;// Local expiration time of cache.

        // in case of CacheControl.
        if (!TextUtils.isEmpty(cacheControl)) {
            localExpire += maxAge;
            if (staleWhileRevalidate > 0) {
                localExpire += staleWhileRevalidate;
            }

            return localExpire;
        }

        final long expires = headers.getExpires();
        final long date = headers.getDate();

        // 
        if (expires > date) {
            return now + expires - date;
        }
        return 0;
    }
}
