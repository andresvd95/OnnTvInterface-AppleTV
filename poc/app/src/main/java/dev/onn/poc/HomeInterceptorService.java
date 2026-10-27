package dev.onn.poc;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

/** PoC: solo mira cambios de ventana del launcher de stock; no lee contenido. */
public class HomeInterceptorService extends AccessibilityService {
    static final String TAG = "ONNPOC";
    static final String STOCK_HOME = "com.google.android.apps.tv.launcherx.home.HomeActivity";
    static long lastInterceptUptime = 0;
    static int interceptCount = 0;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent e) {
        if (e.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;
        String cls = String.valueOf(e.getClassName());
        Log.i(TAG, "window pkg=" + e.getPackageName() + " class=" + cls);
        if (!STOCK_HOME.equals(cls)) return;
        interceptCount++;
        lastInterceptUptime = SystemClock.uptimeMillis();
        Log.i(TAG, "INTERCEPT #" + interceptCount + " eventDelayMs=" + (lastInterceptUptime - e.getEventTime()));
        bringToFront();
        scheduleCheck(1);
    }

    static volatile long lastResumeUptime = 0;
    final android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());

    void bringToFront() {
        Intent i = new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(i);
    }

    /** Reintento: si tras 500 ms nuestra pantalla no volvio a primer plano, se trae de nuevo (max 3). */
    void scheduleCheck(int attempt) {
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(() -> {
            if (lastResumeUptime >= lastInterceptUptime) return;
            Log.i(TAG, "RETRY #" + attempt + " tras intercept #" + interceptCount);
            bringToFront();
            if (attempt < 3) scheduleCheck(attempt + 1);
        }, 500);
    }

    static int keyHomeCount = 0;

    /** Atrapa la tecla Home antes de que el sistema abra Google TV (sin destello). */
    @Override
    protected boolean onKeyEvent(android.view.KeyEvent k) {
        if (k.getKeyCode() != android.view.KeyEvent.KEYCODE_HOME) return false;
        if (k.getAction() == android.view.KeyEvent.ACTION_UP) {
            keyHomeCount++;
            Log.i(TAG, "KEY HOME #" + keyHomeCount + " consumida");
            lastInterceptUptime = SystemClock.uptimeMillis();
            bringToFront();
            scheduleCheck(1);
        }
        return true;
    }

    static HomeInterceptorService instance;

    @Override protected void onServiceConnected() { instance = this; Log.i(TAG, "a11y conectado"); }

    @Override public boolean onUnbind(Intent i) { instance = null; return super.onUnbind(i); }

    boolean openNotifications() { return performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS); }

    @Override public void onInterrupt() {}
}
