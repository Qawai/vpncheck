package com.qawai.vpncheck;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

// Спидометр с анимацией стрелки для вкладки замера скорости.
public class GaugeView extends View {

    private final Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint goodPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint needlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int accent = 0xFF4FC3F7, good = 0xFF66BB6A;
    private double speed = 0;
    private double target = 0;
    private double max = 100;

    private final Handler h = new Handler(Looper.getMainLooper());
    private final Runnable animator = new Runnable() {
        @Override public void run() {
            if (Math.abs(target - speed) > max * 0.02) {
                speed += (target - speed) * 0.15;
                invalidate();
                h.postDelayed(this, 16);
            } else {
                speed = target;
                invalidate();
            }
        }
    };

    public GaugeView(Context c) { super(c); init(); }

    private void init() {
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);
        goodPaint.setStyle(Paint.Style.STROKE);
        goodPaint.setStrokeCap(Paint.Cap.ROUND);
        needlePaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setTheme(int accent, int good, int warn) {
        this.accent = accent;
        this.good = good;
        arcPaint.setColor(accent);
        goodPaint.setColor(good);
        needlePaint.setColor(accent);
        dotPaint.setColor(accent);
    }

    public void setMax(double m) { this.max = Math.max(1, m); }

    public void setSpeed(double mbps) {
        target = Math.max(0, Math.min(mbps, max));
        h.removeCallbacks(animator);
        h.post(animator);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth(), hgt = getHeight();
        if (w <= 0 || hgt <= 0) return;
        float cx = w / 2f, cy = hgt * 0.88f;
        float r = Math.min(w, hgt) * 0.42f;
        float sw = Math.max(6, r * 0.12f);

        arcPaint.setStrokeWidth(sw);
        goodPaint.setStrokeWidth(sw);

        RectF oval = new RectF(cx - r, cy - r, cx + r, cy + r);
        canvas.drawArc(oval, 180, 180, false, arcPaint);
        canvas.drawArc(oval, 315, 45, false, goodPaint);

        float frac = (float) (speed / max);
        double ang = Math.toRadians(180 + 180 * frac);
        float len = r * 0.78f;
        needlePaint.setStrokeWidth(sw * 0.55f);
        canvas.drawLine(cx, cy,
                (float) (cx + len * Math.cos(ang)),
                (float) (cy + len * Math.sin(ang)), needlePaint);
        canvas.drawCircle(cx, cy, sw * 0.9f, dotPaint);
    }
}
