package dev.onn.poc;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.media.tv.TvContract;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.List;
import java.util.TreeMap;

/** PoC desechable: un boton por pregunta de la PoC; resultados en pantalla y en logcat (ONNPOC). */
public class MainActivity extends Activity {
    static final String TAG = "ONNPOC";
    static final String LX = "com.google.android.apps.tv.launcherx";
    TextView out;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(64, 40, 64, 40);
        TextView title = new TextView(this);
        title.setText("ONN PoC - prueba de concepto (desechable)");
        title.setTextSize(28);
        root.addView(title);
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(row);
        addBtn(row, "Perfiles", this::openProfiles);
        addBtn(row, "Notificaciones", () -> tryStart("notif-panel", new Intent("android.app.action.OPEN_NOTIFICATION_HANDLER_PANEL")));
        addBtn(row, "Ajustes", () -> tryStart("settings", new Intent(Settings.ACTION_SETTINGS)));
        addBtn(row, "Google Play", this::openPlay);
        addBtn(row, "Recomendaciones", this::readRecs);
        addBtn(row, "Apps", this::listApps);
        ScrollView sv = new ScrollView(this);
        out = new TextView(this);
        out.setTextSize(18);
        sv.addView(out);
        root.addView(sv);
        setContentView(root);
        runAction(getIntent());
    }

    @Override protected void onNewIntent(Intent i) {
        super.onNewIntent(i);
        runAction(i);
    }

    /** Permite lanzar cada prueba por ADB: am start -n dev.onn.poc/.MainActivity --es poc recs */
    void runAction(Intent i) {
        String a = i == null ? null : i.getStringExtra("poc");
        if (a == null) return;
        log("== accion " + a);
        switch (a) {
            case "recs": readRecs(); break;
            case "apps": listApps(); break;
            case "settings": tryStart("settings", new Intent(Settings.ACTION_SETTINGS)); break;
            case "play": openPlay(); break;
            case "profiles": openProfiles(); break;
            case "notif-a11y":
                HomeInterceptorService svc = HomeInterceptorService.instance;
                log("notif-a11y: " + (svc == null ? "servicio no conectado" : "performGlobalAction=" + svc.openNotifications()));
                break;
            default: log("accion desconocida");
        }
    }

    @Override protected void onResume() {
        super.onResume();
        HomeInterceptorService.lastResumeUptime = SystemClock.uptimeMillis();
        long t = HomeInterceptorService.lastInterceptUptime;
        if (t > 0) {
            log("Home interceptado #" + HomeInterceptorService.interceptCount
                    + " - intercept a onResume " + (SystemClock.uptimeMillis() - t) + " ms");
        }
    }

    void addBtn(LinearLayout row, String label, Runnable r) {
        Button btn = new Button(this);
        btn.setText(label);
        btn.setOnClickListener(v -> { try { r.run(); } catch (Exception e) { log(label + " ERROR " + e); } });
        row.addView(btn);
    }

    void openProfiles() {
        Intent[] tries = {
            new Intent().setComponent(new ComponentName(LX, LX + ".profile.chooser.ProfileChooserActivity")),
            new Intent().setComponent(new ComponentName(LX, LX + ".profile.managedprofile.delegate.ProxyProfileChooserActivity")),
            new Intent("com.google.android.gms.account.ProfilePickerDelegation"),
        };
        for (int i = 0; i < tries.length; i++) if (tryStart("profiles#" + i, tries[i])) return;
    }

    void openPlay() {
        Intent i = getPackageManager().getLeanbackLaunchIntentForPackage("com.android.vending");
        if (i == null) i = getPackageManager().getLaunchIntentForPackage("com.android.vending");
        if (i == null) { log("play: sin intent de lanzamiento"); return; }
        tryStart("play", i);
    }

    boolean tryStart(String name, Intent i) {
        try {
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            log(name + ": OK " + i);
            return true;
        } catch (Exception e) {
            log(name + ": FALLA " + e.getClass().getSimpleName() + " " + e.getMessage());
            return false;
        }
    }

    void readRecs() {
        countByPkg("channels", TvContract.Channels.CONTENT_URI, TvContract.Channels.COLUMN_PACKAGE_NAME);
        countByPkg("preview_programs", TvContract.PreviewPrograms.CONTENT_URI, TvContract.PreviewPrograms.COLUMN_PACKAGE_NAME);
        countByPkg("watch_next", TvContract.WatchNextPrograms.CONTENT_URI, TvContract.WatchNextPrograms.COLUMN_PACKAGE_NAME);
        // Solo el esquema de las URIs de imagen (local o http), sin registrar contenido.
        try (Cursor c = getContentResolver().query(TvContract.PreviewPrograms.CONTENT_URI,
                new String[]{TvContract.PreviewPrograms.COLUMN_POSTER_ART_URI}, null, null, null)) {
            TreeMap<String, Integer> schemes = new TreeMap<>();
            while (c != null && c.moveToNext()) {
                String u = c.getString(0);
                String s = u == null ? "null" : String.valueOf(Uri.parse(u).getScheme());
                schemes.merge(s, 1, Integer::sum);
            }
            log("poster schemes: " + schemes);
        } catch (Exception e) { log("poster schemes ERROR " + e); }
    }

    void countByPkg(String name, Uri uri, String pkgCol) {
        try (Cursor c = getContentResolver().query(uri, new String[]{pkgCol}, null, null, null)) {
            TreeMap<String, Integer> m = new TreeMap<>();
            while (c != null && c.moveToNext()) m.merge(String.valueOf(c.getString(0)), 1, Integer::sum);
            log(name + ": " + (c == null ? "cursor null" : m.toString()));
        } catch (Exception e) {
            log(name + ": ERROR " + e.getClass().getSimpleName() + " " + e.getMessage());
        }
    }

    void listApps() {
        Intent q = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        List<ResolveInfo> l = getPackageManager().queryIntentActivities(q, 0);
        StringBuilder sb = new StringBuilder("apps leanback=" + l.size() + ": ");
        for (ResolveInfo r : l) sb.append(r.activityInfo.packageName).append(" ");
        log(sb.toString());
    }

    void log(String m) {
        Log.i(TAG, m);
        out.append(m + "\n");
    }
}
