package com.yixin.tinode.tinode;

import android.os.Build;
import android.util.Log;

import com.yixin.tinode.db.tinode.BaseDb;
import com.zuozhan.app.BaseIP;

import java.util.Locale;

import co.tinode.tinodesdk.Tinode;
import co.tinode.tinodesdk.model.PrivateType;
import co.tinode.tinodesdk.model.VCard;

//import com.google.firebase.iid.FirebaseInstanceId;

/**
 * Shared resources.
 */
public class Cache {
    private static final String TAG = "Cache";
    //    public static String HOST_NAME = "api.tinode.co"; // remote host
//    public static final String HOST_NAME = "47.104.98.219:6060"; // local host
//    public static String HOST_NAME = "123.56.100.207:6060"; // local host
//    public static String HOST_NAME = "47.96.101.159:6060"; // local host
    public static String HOST_NAME = BaseIP.IM_IP; // local host

    private static final String API_KEY = "AQEAAAABAAD_rAp4DJh05a1HAwFT3A6K";


    private static Tinode sTinode;
    private static int sVisibleCount = 0;
    private static int sUniqueCounter = 100;

    public static Tinode getTinode() {
        if (sTinode == null) {
            Log.d(TAG, "Tinode instantiated");

            sTinode = new Tinode("Tindroid/0.15", API_KEY, BaseDb.getInstance().getStore(), null);
            sTinode.setOsString(Build.VERSION.RELEASE);

            // Default types for parsing Public, Private fields of messages
            sTinode.setDefaultTypeOfMetaPacket(VCard.class, PrivateType.class);
            sTinode.setMeTypeOfMetaPacket(VCard.class);
            sTinode.setFndTypeOfMetaPacket(VCard.class);

            // Set device language
            sTinode.setLanguage(Locale.getDefault().getLanguage());
            sTinode.setAutologin(true);
        }

//        sTinode.setDeviceToken(FirebaseInstanceId.getInstance().getToken());
        return sTinode;
    }

    // Invalidate and reinitialize existing cache.
    public static void invalidate() {
        if (sTinode != null) {
            sTinode.logout();
            sTinode = null;
        }
    }

    /**
     * Keep counter of visible activities
     *
     * @param visible true if some activity became visible
     * @return
     */
    public static int activityVisible(boolean visible) {
        sVisibleCount += visible ? 1 : -1;
        // Log.d(TAG, "Visible count: " + sVisibleCount);
        return sVisibleCount;
    }

    /**
     * @return true if any activity is visible to the user
     */
    public static boolean isInForeground() {
        return sVisibleCount > 0;
    }

    public synchronized static int getUniqueCounter() {
        return ++sUniqueCounter;
    }
}
