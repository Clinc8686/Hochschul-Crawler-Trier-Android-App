package de.clinc8686.hochschul_crawler;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

public class BootLoader {

    public void startBootLoader(Context context) {
        final ComponentName onBootReceiver = new ComponentName(context.getPackageName(), Crawler_Service.class.getName());
        if(context.getPackageManager().getComponentEnabledSetting(onBootReceiver) != PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
            context.getPackageManager().setComponentEnabledSetting(onBootReceiver,PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    public static void stopBootLoader(Context context) {
        final ComponentName onBootReceiver = new ComponentName(context.getPackageName(), Crawler_Service.class.getName());
        if(context.getPackageManager().getComponentEnabledSetting(onBootReceiver) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
            context.getPackageManager().setComponentEnabledSetting(onBootReceiver,PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }
}
