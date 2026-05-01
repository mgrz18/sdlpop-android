package com.mgrz18.sdlpop;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;

import org.libsdl.app.SDLActivity;

public class TouchOverlayView extends View {

    private static final int SCAN_UP = 82;
    private static final int SCAN_DOWN = 81;
    private static final int SCAN_LEFT = 80;
    private static final int SCAN_RIGHT = 79;
    private static final int SCAN_LSHIFT = 225;
    private static final int SCAN_ESC = 41;

    private static final int ZONE_NONE = 0;
    private static final int ZONE_UP = 1;
    private static final int ZONE_DOWN = 2;
    private static final int ZONE_LEFT = 3;
    private static final int ZONE_RIGHT = 4;
    private static final int ZONE_SHIFT = 5;
    private static final int ZONE_ESC = 6;

    private final Paint fillPaint;
    private final Paint strokePaint;
    private final Paint glyphPaint;
    private final float density;

    private final SparseIntArray pointerZones = new SparseIntArray();

    public TouchOverlayView(Context context) {
        super(context);
        density = getResources().getDisplayMetrics().density;
        setWillNotDraw(false);

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setColor(Color.argb(70, 255, 255, 255));
        fillPaint.setStyle(Paint.Style.FILL);

        strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setColor(Color.argb(160, 255, 255, 255));
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(2f * density);

        glyphPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glyphPaint.setColor(Color.argb(220, 255, 255, 255));
        glyphPaint.setTextAlign(Paint.Align.CENTER);
        glyphPaint.setTextSize(22f * density);
        glyphPaint.setFakeBoldText(true);
    }

    private float dpadRadius() { return 110f * density; }
    private float shiftRadius() { return 90f * density; }
    private float escSize() { return 56f * density; }
    private float margin() { return 28f * density; }

    private float dpadCenterX() { return margin() + dpadRadius(); }
    private float dpadCenterY() { return getHeight() - margin() - dpadRadius(); }
    private float shiftCenterX() { return getWidth() - margin() - shiftRadius(); }
    private float shiftCenterY() { return getHeight() - margin() - shiftRadius(); }
    private float escLeft() { return getWidth() - margin() - escSize(); }
    private float escTop() { return margin(); }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float dx = dpadCenterX();
        float dy = dpadCenterY();
        float r = dpadRadius();
        canvas.drawCircle(dx, dy, r, fillPaint);
        canvas.drawCircle(dx, dy, r, strokePaint);
        canvas.drawText("▲", dx, dy - r * 0.55f + glyphPaint.getTextSize() / 3f, glyphPaint);
        canvas.drawText("▼", dx, dy + r * 0.55f + glyphPaint.getTextSize() / 3f, glyphPaint);
        canvas.drawText("◀", dx - r * 0.55f, dy + glyphPaint.getTextSize() / 3f, glyphPaint);
        canvas.drawText("▶", dx + r * 0.55f, dy + glyphPaint.getTextSize() / 3f, glyphPaint);

        float sx = shiftCenterX();
        float sy = shiftCenterY();
        float sr = shiftRadius();
        canvas.drawCircle(sx, sy, sr, fillPaint);
        canvas.drawCircle(sx, sy, sr, strokePaint);
        canvas.drawText("SHIFT", sx, sy + glyphPaint.getTextSize() / 3f, glyphPaint);

        float el = escLeft();
        float et = escTop();
        float es = escSize();
        canvas.drawRect(el, et, el + es, et + es, fillPaint);
        canvas.drawRect(el, et, el + es, et + es, strokePaint);
        canvas.drawText("ESC", el + es / 2f, et + es / 2f + glyphPaint.getTextSize() / 3f, glyphPaint);
    }

    private int hitTest(float x, float y) {
        float ex = escLeft();
        float ey = escTop();
        float es = escSize();
        if (x >= ex && x <= ex + es && y >= ey && y <= ey + es) {
            return ZONE_ESC;
        }

        float sx = shiftCenterX();
        float sy = shiftCenterY();
        float sr = shiftRadius();
        if (dist(x, y, sx, sy) <= sr) {
            return ZONE_SHIFT;
        }

        float dx = dpadCenterX();
        float dy = dpadCenterY();
        float r = dpadRadius();
        float dd = dist(x, y, dx, dy);
        if (dd <= r) {
            float ax = x - dx;
            float ay = y - dy;
            if (Math.abs(ax) > Math.abs(ay)) {
                return ax < 0 ? ZONE_LEFT : ZONE_RIGHT;
            } else {
                return ay < 0 ? ZONE_UP : ZONE_DOWN;
            }
        }
        return ZONE_NONE;
    }

    private static float dist(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private static int scanForZone(int zone) {
        switch (zone) {
            case ZONE_UP: return SCAN_UP;
            case ZONE_DOWN: return SCAN_DOWN;
            case ZONE_LEFT: return SCAN_LEFT;
            case ZONE_RIGHT: return SCAN_RIGHT;
            case ZONE_SHIFT: return SCAN_LSHIFT;
            case ZONE_ESC: return SCAN_ESC;
            default: return 0;
        }
    }

    private void zoneDown(int zone) {
        int sc = scanForZone(zone);
        if (sc != 0) SDLActivity.onNativeKeyDown(sc);
    }

    private void zoneUp(int zone) {
        int sc = scanForZone(zone);
        if (sc != 0) SDLActivity.onNativeKeyUp(sc);
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
                if (zone != ZONE_NONE) {
                    pointerZones.put(pid, zone);
                    zoneDown(zone);
                    return true;
                }
                return false;
            }
            case MotionEvent.ACTION_MOVE: {
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
                if (zone != ZONE_NONE) {
                    zoneUp(zone);
                    pointerZones.delete(pid);
                }
                return true;
            }
            default:
                return false;
        }
    }
}
