package com.erlei.dashboardview.widget;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

public class DashboardView1 extends View {

    private static final String TAG = "DashboardView1";

    private float mFraction;
    private float mDiscFraction = 0.7F;
    private int mStartColor = Color.RED;
    private int mEndColor = Color.BLUE;
    private ArgbEvaluator mArgbEvaluator;
    /**
     * 起始角度
     */
    private float mStartAngle = 181;
    /**
     * 绘制角度
     */
    private float mSweepAngle = 178.5F;
    /**
     * 外圆半径
     */
    private int mRadius;
    /**
     * 内圆盘半径
     */
    private int mInsideDiscRadius;
    /**
     * 长刻度
     */
    private Line mLongLine;
    /**
     * 短刻度
     */
    private Line mShortLine;
    /**
     * 值域（mMax-mMin）等分份数
     */
    private int mSection = 10;
    /**
     * 一个mSection等分份数
     */
    private int mPortion = 3;
    private Paint mInsideDiscPaint;
    private Paint mTickMarksPaint;
    private Paint mPointerPaint;
    private RectF mInsideDiscRectF;
    private float mCenterX;
    private float mCenterY;
    private Line mPointerLine;
    private float mProgress = 50;


    public DashboardView1(Context context) {
        this(context, null);
    }

    public DashboardView1(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DashboardView1(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        mShortLine = new Line(dp2px(5), dp2px(2), Color.WHITE);
        mLongLine = new Line(dp2px(10), dp2px(2), Color.WHITE);
        mPointerLine = new Line(dp2px(20), dp2px(3F), Color.GREEN);

        mArgbEvaluator = new ArgbEvaluator();
        mArgbEvaluator.evaluate(mFraction, mStartColor, mEndColor);

        //初始化内圆盘画笔
        mInsideDiscPaint = initInsideDiscPaint();
        mInsideDiscRectF = new RectF();

        //刻度画笔
        mTickMarksPaint = initTickMarksPaint();

        //指针画笔
        mPointerPaint = initPointerPaint();

    }

    private Paint initPointerPaint() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(mLongLine.width);
        paint.setColor(mLongLine.color);
        return paint;
    }

    private Paint initTickMarksPaint() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        return paint;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = resolveSize(dp2px(200), widthMeasureSpec);
        mRadius = (width - (getPaddingLeft() + getPaddingRight())) / 2;
        mInsideDiscRadius = (int) (mRadius * mDiscFraction);
        int height = mRadius + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(width, height);

        mCenterX = mCenterY = getMeasuredWidth() / 2f;
        //计算内圆盘的位置
        int insideDiscSize = mInsideDiscRadius * 2;
        int left = (width - insideDiscSize) / 2;
        int top = height - mInsideDiscRadius;
        mInsideDiscRectF.set(left, top, insideDiscSize + left, insideDiscSize + top);
        Log.e(TAG, mInsideDiscRectF.toString());
        Log.e(TAG, "width = " + width + "\t\t height = " + height + "\t\t mRadius = " + mRadius + "\t\tmInsideDiscRadius = " + mInsideDiscRadius);
    }

    /**
     * 画内圆盘
     */
    private void drawInsideDisc(Canvas canvas) {
        canvas.drawArc(mInsideDiscRectF, 0, -180, true, mInsideDiscPaint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawInsideDisc(canvas);
        drawTickMarks(canvas);
        drawPointer(canvas);
        drawText(canvas);
    }

    /**
     * 画文字
     */
    private void drawText(Canvas canvas) {

    }

    /**
     * 画指针
     */
    private void drawPointer(Canvas canvas) {
        canvas.save();

        float v = mSweepAngle * (mProgress / 100);
        float x0 = ((getMeasuredWidth() - mInsideDiscRadius * 2) / 2) -  mPointerLine.height;
        float y0 = getMeasuredHeight();
        float x1 = ((getMeasuredWidth() - mInsideDiscRadius * 2) / 2);
        float y1 = getMeasuredHeight();
        canvas.rotate(v, mCenterX, mCenterY);
        canvas.drawLine(x0, y0, x1, y1, mPointerPaint);
        canvas.restore();
    }

    /**
     * 画仪表刻度线
     */
    private void drawTickMarks(Canvas canvas) {
        mTickMarksPaint.setStyle(Paint.Style.STROKE);
        mTickMarksPaint.setStrokeWidth(mLongLine.width);
        mTickMarksPaint.setColor(mLongLine.color);
        /*
          画长刻度
          画好起始角度的一条刻度后通过canvas绕着原点旋转来画剩下的长刻度
         */
        double cos = Math.cos(Math.toRadians(mStartAngle - 180));
        double sin = Math.sin(Math.toRadians(mStartAngle - 180));
        Log.e(TAG, "cos = " + cos + "\t\t sin = " + sin);
        float x0 = (float) (mLongLine.width + mRadius * (1 - cos));
        float y0 = (float) (mLongLine.width + mRadius * (1 - sin));
        float x1 = (float) (mLongLine.width + mRadius - (mRadius - mLongLine.height) * cos);
        float y1 = (float) (mLongLine.width + mRadius - (mRadius - mLongLine.height) * sin);
        Log.e(TAG, "x0 = " + x0 + "\t\t y0 = " + y0 + "\t\t x1 = " + x1 + "\t\ty1 = " + y1);
        canvas.save();
        canvas.drawLine(x0, y0, x1, y1, mTickMarksPaint);
        float angle = mSweepAngle / mSection;
        Log.e(TAG, "angle = " + angle);
        for (int i = 0; i < mSection; i++) {
            canvas.rotate(angle, mCenterX, mCenterY);
            canvas.drawLine(x0, y0, x1, y1, mTickMarksPaint);
        }
        canvas.restore();

        /*
          画短刻度
          同样采用canvas的旋转原理
         */
        canvas.save();
        mTickMarksPaint.setStrokeWidth(mShortLine.width);
        mTickMarksPaint.setColor(mShortLine.color);
        float x2 = (float) (mShortLine.width + mRadius - (mRadius - mShortLine.height / 2f) * cos);
        float y2 = (float) (mShortLine.width + mRadius - (mRadius - mShortLine.height / 2f) * sin);
        canvas.drawLine(x0, y0, x2, y2, mTickMarksPaint);
        angle = mSweepAngle / (mSection * mPortion);
        for (int i = 1; i < mSection * mPortion; i++) {
            canvas.rotate(angle, mCenterX, mCenterY);
            if (i % mPortion == 0) { // 避免与长刻度画重合
                continue;
            }
            canvas.drawLine(x0, y0, x2, y2, mTickMarksPaint);
        }
        canvas.restore();
    }


    private Paint initInsideDiscPaint() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.FILL);
        return paint;
    }


    private class Line {
        int height;
        int width;
        int color;

        public Line(int height, int width) {
            this.height = height;
            this.width = width;
        }

        public Line(int height, int width, int color) {
            this.height = height;
            this.width = width;
            this.color = color;
        }
    }

    private static int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                Resources.getSystem().getDisplayMetrics());
    }
}
