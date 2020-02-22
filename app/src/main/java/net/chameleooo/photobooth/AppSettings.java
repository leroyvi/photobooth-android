/**
 * Copyright 2013 Nils Assbeck, Guersel Ayaz and Michael Zoech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.chameleooo.photobooth;

import android.content.Context;
import android.content.SharedPreferences;

public class AppSettings {

    private final SharedPreferences prefs;

    public AppSettings(Context context) {
        prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE);
    }

    public boolean isGalleryOrderReversed() {
        return prefs.getBoolean("internal.gallery.reverse_order", false);
    }

    public void setGalleryOrderReversed(boolean reversed) {
        prefs.edit().putBoolean("internal.gallery.reverse_order", reversed).apply();
    }

    public int getCapturedPictureSampleSize() {
        return getIntFromStringPreference("memory.picture_sample_size", 1);
    }

    private int getIntFromStringPreference(String key, int defaultValue) {
        try {
            String value = prefs.getString(key, null);
            if (value != null) {
                return Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
            // nop
        }
        return defaultValue;
    }
}
