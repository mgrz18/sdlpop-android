package com.mgrz18.sdlpop;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import org.libsdl.app.SDLActivity;

public class TouchOverlayView extends View {

    private static final int KEY_UP = KeyEvent.KEYCODE_DPAD_UP;
    private static final int KEY_DOWN = KeyEvent.KEYCODE_DPAD_DOWN;
    private static final int KEY_LEFT = KeyEvent.KEYCODE_DPAD_LEFT;
    private static final int KEY_RIGHT = KeyEvent.KEYCODE_DPAD_RIGHT;
    private static final int KEY_LSHIFT = KeyEvent.KEYCODE_SHIFT_LEFT;
    private static final int KEY_ESC = KeyEvent.KEYCODE_ESCAPE;
    private static final int KEY_ENTER = KeyEvent.KEYCODE_ENTER;
    private static final int KEY_SPACE = KeyEvent.KEYCODE_SPACE;

    private static final int ZONE_NONE = 0;
    private static final int ZONE_UP = 1;
    private static final int ZONE_DOWN = 2;
    private static final int ZONE_LEFT = 3;
    private static final int ZONE_RIGHT = 4;
    private static final int ZONE_SHIFT = 5;
    private static final int ZONE_ESC = 6;
    private static final int ZONE_ENTER = 7;
    private static final int ZONE_CHEATS = 8;
    private static final int ZONE_CHEAT_CLOSE = 9;
    private static final int ZONE_UP_LEFT = 10;
    private static final int ZONE_UP_RIGHT = 11;
    private static final int ZONE_TIME = 12;
    private static final int ZONE_COUNT = 13;
    private static final int ZONE_CHEAT_FIRST = 100;

    private static final Cheat[] CHEATS = {
        new Cheat("Shift+L", "Next lvl",  new int[]{ KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_L }),
        new Cheat("Shift+T", "Big potion", new int[]{ KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_T }),
        new Cheat("Shift+S", "Heal +1",   new int[]{ KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_S }),
        new Cheat("Shift+W", "Feather",   new int[]{ KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_W }),
        new Cheat("Shift+I", "Invert",    new int[]{ KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_I }),
        new Cheat("Shift+B", "Blind",     new int[]{ KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_B }),
        new Cheat("R",       "Resurrect", new int[]{ KeyEvent.KEYCODE_R }),
        new Cheat("K",       "Kill grd",  new int[]{ KeyEvent.KEYCODE_K }),
        new Cheat("+",       "+1 min",    new int[]{ KeyEvent.KEYCODE_NUMPAD_ADD }),
        new Cheat("-",       "-1 min",    new int[]{ KeyEvent.KEYCODE_NUMPAD_SUBTRACT }),
    };

    private static final class Cheat {
        final String label;
        final String sub;
        final int[] keys;
        Cheat(String l, String s, int[] k) { label = l; sub = s; keys = k; }
    }

    private final Paint capPaint;
    private final Paint shadowPaint;
    private final Paint bevelLightPaint;
    private final Paint bevelDarkPaint;
    private final Paint labelPaint;
    private final Paint subLabelPaint;
    private final float density;

    private final SparseIntArray pointerZones = new SparseIntArray();
    private final boolean[] zonePressed = new boolean[ZONE_COUNT];
    private final boolean[] cheatPressed = new boolean[CHEATS.length];

    private boolean menuShown;
    private boolean cheatsEnabled;
    private boolean cheatsPanelOpen;

    private long lastTapTime;
    private int tapCount;
    private static final long TRIPLE_TAP_WINDOW_MS = 500L;
    private final Handler statePoller = new Handler(Looper.getMainLooper());
    private final Runnable pollState = new Runnable() {
        @Override
        public void run() {
            boolean menu, cheats;
            try {
                menu = PoPNative.isMenuShown();
                cheats = PoPNative.isCheatsEnabled();
            } catch (UnsatisfiedLinkError e) {
                menu = false; cheats = false;
            }
            boolean changed = false;
            if (menu != menuShown) { menuShown = menu; changed = true; }
            if (cheats != cheatsEnabled) {
                cheatsEnabled = cheats;
                if (!cheats) cheatsPanelOpen = false;
                changed = true;
            }
            if (changed) invalidate();
            statePoller.postDelayed(this, 120);
        }
    };

    public TouchOverlayView(Context context) {
        super(context);
        density = getResources().getDisplayMetrics().density;
        setWillNotDraw(false);

        capPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        capPaint.setStyle(Paint.Style.FILL);
        capPaint.setColor(Color.argb(32, 70, 70, 76));

        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setColor(Color.argb(24, 0, 0, 0));

        bevelLightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bevelLightPaint.setStyle(Paint.Style.STROKE);
        bevelLightPaint.setStrokeCap(Paint.Cap.SQUARE);

        bevelDarkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bevelDarkPaint.setStyle(Paint.Style.STROKE);
        bevelDarkPaint.setStrokeCap(Paint.Cap.SQUARE);

        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(Color.argb(85, 235, 235, 240));
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTextSize(28f * density);
        labelPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));

        subLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subLabelPaint.setColor(Color.argb(68, 235, 235, 240));
        subLabelPaint.setTextAlign(Paint.Align.CENTER);
        subLabelPaint.setTextSize(13f * density);
        subLabelPaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        statePoller.post(pollState);
    }

    @Override
    protected void onDetachedFromWindow() {
        statePoller.removeCallbacks(pollState);
        super.onDetachedFromWindow();
    }

    private float keySize() { return 64f * density; }
    private float keyGap() { return 8f * density; }
    private float shiftWidth() { return 150f * density; }
    private float shiftHeight() { return 70f * density; }
    private float escSize() { return 56f * density; }
    private float margin() { return 24f * density; }
    private float keyRadius() { return 6f * density; }
    private float shadowOffset() { return 4f * density; }

    private RectF zoneRect(int zone, RectF out) {
        float ks = keySize();
        float gap = keyGap();
        float dpadLeft = margin();
        float dpadBottom = getHeight() - margin();
        float downCx = dpadLeft + ks + gap + ks / 2f;
        float downCy = dpadBottom - ks / 2f;

        switch (zone) {
            case ZONE_UP: {
                float cx = downCx;
                float cy = downCy - ks - gap;
                out.set(cx - ks / 2f, cy - ks / 2f, cx + ks / 2f, cy + ks / 2f);
                return out;
            }
            case ZONE_UP_LEFT: {
                float cx = downCx - ks - gap;
                float cy = downCy - ks - gap;
                out.set(cx - ks / 2f, cy - ks / 2f, cx + ks / 2f, cy + ks / 2f);
                return out;
            }
            case ZONE_UP_RIGHT: {
                float cx = downCx + ks + gap;
                float cy = downCy - ks - gap;
                out.set(cx - ks / 2f, cy - ks / 2f, cx + ks / 2f, cy + ks / 2f);
                return out;
            }
            case ZONE_DOWN: {
                out.set(downCx - ks / 2f, downCy - ks / 2f, downCx + ks / 2f, downCy + ks / 2f);
                return out;
            }
            case ZONE_LEFT: {
                float cx = downCx - ks - gap;
                out.set(cx - ks / 2f, downCy - ks / 2f, cx + ks / 2f, downCy + ks / 2f);
                return out;
            }
            case ZONE_RIGHT: {
                float cx = downCx + ks + gap;
                out.set(cx - ks / 2f, downCy - ks / 2f, cx + ks / 2f, downCy + ks / 2f);
                return out;
            }
            case ZONE_SHIFT: {
                float w = shiftWidth();
                float h = shiftHeight();
                float r = getWidth() - margin();
                float b = getHeight() - margin();
                out.set(r - w, b - h, r, b);
                return out;
            }
            case ZONE_ENTER: {
                float w = shiftWidth();
                float h = shiftHeight();
                float es = keySize();
                float r = getWidth() - margin() - w - keyGap();
                float b = getHeight() - margin();
                out.set(r - es, b - h, r, b);
                return out;
            }
            case ZONE_ESC: {
                float s = escSize();
                float r = getWidth() - margin();
                float t = margin();
                out.set(r - s, t, r, t + s);
                return out;
            }
            case ZONE_TIME: {
                float s = escSize();
                float r = getWidth() - margin();
                float t = margin() + s + keyGap();
                out.set(r - s, t, r, t + s);
                return out;
            }
            case ZONE_CHEATS: {
                float s = escSize();
                float l = margin();
                float t = margin();
                out.set(l, t, l + s * 1.6f, t + s);
                return out;
            }
            case ZONE_CHEAT_CLOSE: {
                float s = escSize();
                float r = getWidth() - margin();
                float t = margin();
                out.set(r - s, t, r, t + s);
                return out;
            }
            default:
                out.set(0, 0, 0, 0);
                return out;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        RectF r = new RectF();

        drawKey(canvas, zoneRect(ZONE_UP_LEFT, r), "↖", null, zonePressed[ZONE_UP_LEFT]);
        drawKey(canvas, zoneRect(ZONE_UP, r), "↑", null, zonePressed[ZONE_UP]);
        drawKey(canvas, zoneRect(ZONE_UP_RIGHT, r), "↗", null, zonePressed[ZONE_UP_RIGHT]);
        drawKey(canvas, zoneRect(ZONE_DOWN, r), "↓", null, zonePressed[ZONE_DOWN]);
        drawKey(canvas, zoneRect(ZONE_LEFT, r), "←", null, zonePressed[ZONE_LEFT]);
        drawKey(canvas, zoneRect(ZONE_RIGHT, r), "→", null, zonePressed[ZONE_RIGHT]);
        drawKey(canvas, zoneRect(ZONE_SHIFT, r), "SHIFT", "ACTION", zonePressed[ZONE_SHIFT]);
        if (menuShown) {
            drawKey(canvas, zoneRect(ZONE_ENTER, r), "↵", "ENTER", zonePressed[ZONE_ENTER]);
        }
        drawKey(canvas, zoneRect(ZONE_ESC, r), "ESC", null, zonePressed[ZONE_ESC]);
        if (cheatsEnabled && !cheatsPanelOpen) {
            drawKey(canvas, zoneRect(ZONE_CHEATS, r), "CHEATS", null, zonePressed[ZONE_CHEATS]);
        }
        if (cheatsPanelOpen) {
            drawCheatsPanel(canvas);
        }
    }

    private void drawCheatsPanel(Canvas canvas) {
        Paint dim = new Paint();
        dim.setColor(Color.argb(180, 0, 0, 0));
        canvas.drawRect(0, 0, getWidth(), getHeight(), dim);

        RectF r = new RectF();
        for (int i = 0; i < CHEATS.length; i++) {
            cheatRect(i, r);
            drawKey(canvas, r, CHEATS[i].label, CHEATS[i].sub, cheatPressed[i]);
        }
        drawKey(canvas, zoneRect(ZONE_CHEAT_CLOSE, r), "X", null, zonePressed[ZONE_CHEAT_CLOSE]);
    }

    private RectF cheatRect(int idx, RectF out) {
        int cols = 5;
        int row = idx / cols;
        int col = idx % cols;
        float w = 130f * density;
        float h = 60f * density;
        float gap = 10f * density;
        float totalW = cols * w + (cols - 1) * gap;
        float totalH = 2 * h + gap;
        float startX = (getWidth() - totalW) / 2f;
        float startY = (getHeight() - totalH) / 2f;
        float l = startX + col * (w + gap);
        float t = startY + row * (h + gap);
        out.set(l, t, l + w, t + h);
        return out;
    }

    private void drawKey(Canvas canvas, RectF rect, String label, String sublabel, boolean pressed) {
        float so = shadowOffset();
        float kr = keyRadius();
        if (pressed) {
            RectF sunk = new RectF(rect.left, rect.top + so * 0.6f,
                                    rect.right, rect.bottom + so * 0.6f);
            canvas.drawRoundRect(sunk, kr, kr, capPaint);
            rect = sunk;
        } else {
            RectF shadow = new RectF(rect.left + so, rect.top + so, rect.right + so, rect.bottom + so);
            canvas.drawRoundRect(shadow, kr, kr, shadowPaint);
            canvas.drawRoundRect(rect, kr, kr, capPaint);
        }

        float bw = 2f * density;
        bevelLightPaint.setStrokeWidth(bw);
        bevelDarkPaint.setStrokeWidth(bw);
        bevelLightPaint.setColor(Color.argb(50, 240, 240, 245));
        bevelDarkPaint.setColor(Color.argb(62, 0, 0, 0));

        float bInset = bw / 2f;
        float l = rect.left + bInset;
        float t = rect.top + bInset;
        float r = rect.right - bInset;
        float b = rect.bottom - bInset;
        Paint topLeft  = pressed ? bevelDarkPaint  : bevelLightPaint;
        Paint botRight = pressed ? bevelLightPaint : bevelDarkPaint;
        canvas.drawLine(l, t, r, t, topLeft);
        canvas.drawLine(l, t, l, b, topLeft);
        canvas.drawLine(r, t, r, b, botRight);
        canvas.drawLine(l, b, r, b, botRight);

        float bw2 = 1f * density;
        bevelLightPaint.setStrokeWidth(bw2);
        bevelDarkPaint.setStrokeWidth(bw2);
        bevelLightPaint.setColor(Color.argb(25, 240, 240, 245));
        bevelDarkPaint.setColor(Color.argb(32, 0, 0, 0));
        Paint topLeft2  = pressed ? bevelDarkPaint  : bevelLightPaint;
        Paint botRight2 = pressed ? bevelLightPaint : bevelDarkPaint;
        float i2 = bw + bw2;
        canvas.drawLine(rect.left + i2, rect.top + i2, rect.right - i2, rect.top + i2, topLeft2);
        canvas.drawLine(rect.left + i2, rect.top + i2, rect.left + i2, rect.bottom - i2, topLeft2);
        canvas.drawLine(rect.right - i2, rect.top + i2, rect.right - i2, rect.bottom - i2, botRight2);
        canvas.drawLine(rect.left + i2, rect.bottom - i2, rect.right - i2, rect.bottom - i2, botRight2);

        float cx = rect.centerX();
        float cy = rect.centerY();
        if (sublabel == null) {
            Paint.FontMetrics fm = labelPaint.getFontMetrics();
            float baseline = cy - (fm.ascent + fm.descent) / 2f;
            canvas.drawText(label, cx, baseline, labelPaint);
        } else {
            Paint.FontMetrics fm = labelPaint.getFontMetrics();
            float baseline = cy - (fm.ascent + fm.descent) / 2f - 6f * density;
            canvas.drawText(label, cx, baseline, labelPaint);
            canvas.drawText(sublabel, cx, baseline + 16f * density, subLabelPaint);
        }
    }

    private int hitTest(float x, float y) {
        RectF r = new RectF();
        if (cheatsPanelOpen) {
            zoneRect(ZONE_CHEAT_CLOSE, r);
            if (r.contains(x, y)) return ZONE_CHEAT_CLOSE;
            for (int i = 0; i < CHEATS.length; i++) {
                cheatRect(i, r);
                if (r.contains(x, y)) return ZONE_CHEAT_FIRST + i;
            }
            return ZONE_NONE;
        }
        int[] dpadZones = { ZONE_UP_LEFT, ZONE_UP_RIGHT, ZONE_UP, ZONE_DOWN, ZONE_LEFT, ZONE_RIGHT };
        int[] sideZones = menuShown
            ? new int[] { ZONE_ESC, ZONE_ENTER, ZONE_SHIFT }
            : new int[] { ZONE_ESC, ZONE_SHIFT };
        for (int z : sideZones) {
            zoneRect(z, r);
            if (r.contains(x, y)) return z;
        }
        if (cheatsEnabled) {
            zoneRect(ZONE_CHEATS, r);
            if (r.contains(x, y)) return ZONE_CHEATS;
        }
        for (int z : dpadZones) {
            zoneRect(z, r);
            if (r.contains(x, y)) return z;
        }
        return ZONE_NONE;
    }

    private static final int[] EMPTY_KEYS = new int[0];
    private static final int[] KEYS_UP = { KEY_UP };
    private static final int[] KEYS_DOWN = { KEY_DOWN };
    private static final int[] KEYS_LEFT = { KEY_LEFT };
    private static final int[] KEYS_RIGHT = { KEY_RIGHT };
    private static final int[] KEYS_SHIFT = { KEY_LSHIFT };
    private static final int[] KEYS_ESC = { KEY_ESC };
    private static final int[] KEYS_ENTER = { KEY_ENTER };
    private static final int[] KEYS_UP_LEFT = { KEY_UP, KEY_LEFT };
    private static final int[] KEYS_UP_RIGHT = { KEY_UP, KEY_RIGHT };
    private static final int[] KEYS_TIME = { KEY_SPACE };

    private static int[] keysForZone(int zone) {
        switch (zone) {
            case ZONE_UP: return KEYS_UP;
            case ZONE_DOWN: return KEYS_DOWN;
            case ZONE_LEFT: return KEYS_LEFT;
            case ZONE_RIGHT: return KEYS_RIGHT;
            case ZONE_SHIFT: return KEYS_SHIFT;
            case ZONE_ESC: return KEYS_ESC;
            case ZONE_ENTER: return KEYS_ENTER;
            case ZONE_UP_LEFT: return KEYS_UP_LEFT;
            case ZONE_UP_RIGHT: return KEYS_UP_RIGHT;
            case ZONE_TIME: return KEYS_TIME;
            default: return EMPTY_KEYS;
        }
    }

    private void zoneDown(int zone) {
        for (int kc : keysForZone(zone)) {
            SDLActivity.onNativeKeyDown(kc);
        }
        if (zone >= 0 && zone < zonePressed.length) {
            zonePressed[zone] = true;
            invalidate();
        }
    }

    private void zoneUp(int zone) {
        for (int kc : keysForZone(zone)) {
            SDLActivity.onNativeKeyUp(kc);
        }
        if (zone >= 0 && zone < zonePressed.length) {
            zonePressed[zone] = false;
            invalidate();
        }
    }

    private void firePulse(final int kc) {
        SDLActivity.onNativeKeyDown(kc);
        statePoller.postDelayed(new Runnable() {
            @Override public void run() { SDLActivity.onNativeKeyUp(kc); }
        }, 60L);
    }

    private void fireCheat(final int idx) {
        final int[] keys = CHEATS[idx].keys;
        for (int i = 0; i < keys.length; i++) {
            final int kc = keys[i];
            statePoller.postDelayed(new Runnable() {
                @Override public void run() { SDLActivity.onNativeKeyDown(kc); }
            }, i * 30L);
        }
        long releaseStart = keys.length * 30L + 60L;
        for (int i = keys.length - 1; i >= 0; i--) {
            final int kc = keys[i];
            statePoller.postDelayed(new Runnable() {
                @Override public void run() { SDLActivity.onNativeKeyUp(kc); }
            }, releaseStart);
            releaseStart += 30L;
        }
        statePoller.postDelayed(new Runnable() {
            @Override public void run() {
                cheatPressed[idx] = false;
                invalidate();
            }
        }, releaseStart + 50L);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        int idx;
        int pid;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                idx = event.getActionIndex();
                pid = event.getPointerId(idx);
                int zone = hitTest(event.getX(idx), event.getY(idx));
                if (zone == ZONE_CHEATS) {
                    cheatsPanelOpen = true;
                    invalidate();
                    return true;
                }
                if (zone == ZONE_CHEAT_CLOSE) {
                    zonePressed[ZONE_CHEAT_CLOSE] = true;
                    pointerZones.put(pid, zone);
                    invalidate();
                    return true;
                }
                if (zone >= ZONE_CHEAT_FIRST) {
                    int idxCheat = zone - ZONE_CHEAT_FIRST;
                    cheatPressed[idxCheat] = true;
                    invalidate();
                    fireCheat(idxCheat);
                    return true;
                }
                if (zone != ZONE_NONE) {
                    pointerZones.put(pid, zone);
                    zoneDown(zone);
                    tapCount = 0;
                } else {
                    long now = System.currentTimeMillis();
                    tapCount = (now - lastTapTime <= TRIPLE_TAP_WINDOW_MS) ? tapCount + 1 : 1;
                    lastTapTime = now;
                    if (tapCount >= 3) {
                        tapCount = 0;
                        firePulse(KEY_SPACE);
                    }
                }
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                if (cheatsPanelOpen) return true;
                for (int i = 0; i < event.getPointerCount(); i++) {
                    pid = event.getPointerId(i);
                    int prev = pointerZones.get(pid, ZONE_NONE);
                    int now = hitTest(event.getX(i), event.getY(i));
                    if (prev != now) {
                        if (prev != ZONE_NONE) zoneUp(prev);
                        if (now != ZONE_NONE) {
                            zoneDown(now);
                            pointerZones.put(pid, now);
                        } else {
                            pointerZones.delete(pid);
                        }
                    }
                }
                return true;
            }
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                idx = event.getActionIndex();
                pid = event.getPointerId(idx);
                int zone = pointerZones.get(pid, ZONE_NONE);
                if (zone == ZONE_CHEAT_CLOSE) {
                    zonePressed[ZONE_CHEAT_CLOSE] = false;
                    pointerZones.delete(pid);
                    cheatsPanelOpen = false;
                    invalidate();
                    return true;
                }
                if (zone != ZONE_NONE) {
                    zoneUp(zone);
                    pointerZones.delete(pid);
                }
                return true;
            }
            default:
                return true;
        }
    }
}
