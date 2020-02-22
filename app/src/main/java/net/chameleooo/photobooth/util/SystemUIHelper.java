package net.chameleooo.photobooth.util;

import android.os.Build;
import android.util.Log;

public class SystemUIHelper {

    private static boolean UI_ENABLED = true;

    public static void swicthUI(){
        if (UI_ENABLED) {
            killSystemUi();
        } else {
            enableSystemUI();
        }
    }

    private static void enableSystemUI() {
        Process proc = null;
        try {
            proc = Runtime.getRuntime().exec(new String[]{"su", "-c", "am startservice -n com.android.systemui/.SystemUIService"});
            UI_ENABLED = true;
        } catch (Exception e) {
            Log.w("Main", "Failed to kill task bar (1).");
            e.printStackTrace();
        }
        try {
            proc.waitFor();
        } catch (Exception e) {
            Log.w("Main", "Failed to kill task bar (2).");
            e.printStackTrace();
        }
    }

    private static void killSystemUi() {
        Process proc = null;

        String ProcID = "42";

        try {
            proc = Runtime.getRuntime().exec(new String[]{"su", "-c", "service call activity " + ProcID + " s16 com.android.systemui"});
            UI_ENABLED = false;
        } catch (Exception e) {
            Log.w("Main", "Failed to kill task bar (1).");
            e.printStackTrace();
        }
        try {
            proc.waitFor();
        } catch (Exception e) {
            Log.w("Main", "Failed to kill task bar (2).");
            e.printStackTrace();
        }
    }
}
