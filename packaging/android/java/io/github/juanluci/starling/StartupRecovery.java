package io.github.juanluci.starling;

import android.content.Context;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import org.json.JSONObject;

/** Persist the attempt before loading native code; cleared only after a presented frame. */
final class StartupRecovery {
    private static File marker(Context context) { return new File(context.getFilesDir(), "starling-starting"); }
    static void begin(Context context) {
        try { new FileOutputStream(marker(context)).close(); }
        catch (java.io.IOException error) { android.util.Log.w("starling", "Cannot record startup", error); }
    }
    static void shown(Context context) { marker(context).delete(); }
    static boolean interrupted(Context context) {
        if (marker(context).exists()) return true;
        File session = new File(context.getFilesDir(), "performance/current-session.json");
        try (FileInputStream stream = new FileInputStream(session)) {
            byte[] data = new byte[16384];
            int size = stream.read(data);
            if (size <= 0) return false;
            JSONObject json = new JSONObject(new String(data, 0, size, "UTF-8"));
            String phase = json.optString("phase");
            return !json.optBoolean("clean_exit") && ("native_start".equals(phase)
                    || "renderer_init".equals(phase) || "resource_loading".equals(phase));
        } catch (Exception ignored) { return false; }
    }
}
