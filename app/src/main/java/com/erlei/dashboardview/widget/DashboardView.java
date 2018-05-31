package com.erlei.dashboardview.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.erlei.dashboardview.R;

@SuppressWarnings("unused")
public class DashboardView extends View {

    /**
     * 内圆盘的半径 radius * mDiscFraction
     */
    private float mDiscFraction = 0.7F;
    /**
     * 渐变的起始颜色
     */
    private int mStartColor = Color.RED;
    /**
     * 渐变的结束颜色
     */
    private int mEndColor = Color.BLUE;
    /**
     * 默认的刻度线颜色
     */
    private int mDefaultColor = Color.GRAY;
    /**
     * 起始角度
     */
    private float mStartAngle = 180;
    /**
     * 绘制角度
     */
    private float mSweepAngle = 180F;
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
    private int mSection = 20;
    /**
     * 一个mSection等分份数
     */
    private int mPortion = 3;
    private Paint mInsideDiscPaint;
    private Paint mTickMarksPaint;
    private Paint mPointerPaint;
    private Paint mTextPaint;

    private RectF mInsideDiscRectF;

    private float mCenterX;
    private float mCenterY;
    private Line mPointerLine;
    private float mProgress = 50;
    private int mTextColor = Color.WHITE;
    private float mTextSize = dp2px(60);
    private Rect mRectText;
    private int mMax = 100;
    private android.animation.ArgbEvaluator mArgbEvaluator;


    public DashboardView(Context context) {
        this(context, null);
    }

    public DashboardView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DashboardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray typeArray = getContext().obtainStyledAttributes(attrs, R.styleable.DashboardView);

        mDiscFraction = typeArray.getFloat(R.styleable.DashboardView_disc_fraction, 0.7F);
        mTextSize = typeArray.getDimension(R.styleable.DashboardView_text_size, sp2px(30));
        mTextColor = typeArray.getColor(R.styleable.DashboardView_text_color, Color.WHITE);
        mStartColor = typeArray.getColor(R.styleable.DashboardView_start_color, Color.RED);
        mEndColor = typeArray.getColor(R.styleable.DashboardView_end_color, Color.BLUE);
        mDefaultColor = typeArray.getColor(R.styleable.DashboardView_default_color, Color.GRAY);

        mLongLine = new Line(
                typeArray.getDimensionPixelSize(R.styleable.DashboardView_long_line_width, dp2px(3)),
                typeArray.getDimensionPixelSize(R.styleable.DashboardView_long_line_height, dp2px(10)));
        mShortLine = new Line(
                typeArray.getDimensionPixelSize(R.styleable.DashboardView_short_line_width, dp2px(3)),
                typeArray.getDimensionPixelSize(R.styleable.DashboardView_short_line_height, dp2px(5)));
        mPointerLine = new Line(
                typeArray.getDimensionPixelSize(R.styleable.DashboardView_pointer_width, dp2px(4)),
                typeArray.getDimensionPixelSize(R.styleable.DashboardView_pointer_height, dp2px(20)));

        mSection = typeArray.getInteger(R.styleable.DashboardView_section, 20);
        mPortion = typeArray.getInteger(R.styleable.DashboardView_portion, 3);


        typeArray.recycle();
        mArgbEvaluator = new android.animation.ArgbEvaluator();

        //内圆盘画笔
        mInsideDiscPaint = initInsideDiscPaint();
        mInsideDiscRectF = new RectF();

        //刻度画笔
        mTickMarksPaint = initTickMarksPaint();

        //指针画笔
        mPointerPaint = initPointerPaint();

        //文字画笔
        mTextPaint = initTextPaint();
        mRectText = new Rect();
    }

    /**
     * 文字画笔
     */
    private Paint initTextPaint() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(mTextColor);
        paint.setTextSize(mTextSize);
        return paint;
    }

    private Paint initPointerPaint() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(mPointerLine.width);
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
        int radius = (width - (getPaddingLeft() + getPaddingRight())) / 2;
        mInsideDiscRadius = (int) (radius * mDiscFraction);
        int height = radius + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(width, height);

        mCenterX = (getMeasuredWidth() - getPaddingLeft() - getPaddingRight()) / 2f;
        mCenterY = (getMeasuredHeight() - getPaddingTop() - getPaddingRight());
        //计算内圆盘的位置
        int insideDiscSize = mInsideDiscRadius * 2;
        int left = (width - insideDiscSize) / 2;
        int top = height - mInsideDiscRadius;
        mInsideDiscRectF.set(left, top, insideDiscSize + left, insideDiscSize + top);
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
        drawPointer(canvas);
        drawInsideDisc(canvas);
        drawTickMarks(canvas);
        drawText(canvas);
    }

    /**
     * 画文字
     */
    private void drawText(Canvas canvas) {
        String value = String.valueOf((int) mProgress);
        mTextPaint.getTextBounds(value, 0, value.length(), mRectText);
        canvas.drawText(value, mCenterX, (getMeasuredHeight() - mInsideDiscRadius / 2 + mRectText.height() / 2), mTextPaint);
    }

    /**
     * 画指针
     */
    private void drawPointer(Canvas canvas) {
        canvas.save();
        float v = mSweepAngle * (mProgress / mMax);
        float x0 = ((getMeasuredWidth() - mInsideDiscRadius * 2) / 2) - mPointerLine.height;
        float y0 = getMeasuredHeight();
        float x1 = ((getMeasuredWidth() - mInsideDiscRadius * 2) / 2);
        float y1 = getMeasuredHeight();
        canvas.rotate(v, mCenterX, mCenterY);
        float fraction = (v / mSweepAngle);
        int evaluate = (int) mArgbEvaluator.evaluate(fraction, mStartColor, mEndColor);
        mPointerPaint.setColor(evaluate);
        canvas.drawLine(x0, y0, x1, y1, mPointerPaint);
        canvas.restore();
    }

    /**
     * 画仪表刻度线
     */
    private void drawTickMarks(Canvas canvas) {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
        float v = mSweepAngle * (mProgress / mMax);//指针角度
        //长刻度
        mTickMarksPaint.setStyle(Paint.Style.STROKE);
        mTickMarksPaint.setStrokeWidth(mLongLine.width);
        mTickMarksPaint.setColor(mLongLine.color);
        float x0 = mLongLine.width * 2;
        float x1 = mLongLine.height + x0;
        canvas.save();
        canvas.translate(0, -mLongLine.width);
        mTickMarksPaint.setColor(mStartColor);
        canvas.drawLine(x0, (float) height, x1, (float) height, mTickMarksPaint);
        float angle = mSweepAngle / mSection;
        for (int i = 0; i < mSection; i++) {
            canvas.rotate(angle, mCenterX, mCenterY);
            float fraction = (angle * (i + 1) / mSweepAngle);
            if (angle * (i + 1) > v) {
                mTickMarksPaint.setColor(mDefaultColor);
            } else {
                int evaluate = (int) mArgbEvaluator.evaluate(fraction, mStartColor, mEndColor);
                mTickMarksPaint.setColor(evaluate);
            }
            canvas.drawLine(x0, (float) height, x1, (float) height, mTickMarksPaint);
        }
        canvas.restore();


        //画短刻度
        canvas.save();
        canvas.translate(0, -mLongLine.width);
        mTickMarksPaint.setStrokeWidth(mShortLine.width);
        mTickMarksPaint.setColor(mShortLine.color);
        float x2 = mShortLine.height + x0;
        //canvas.drawLine(x0, y0, x2, y2, mTickMarksPaint);//重合线 ， 不用绘制
        angle = mSweepAngle / (mSection * mPortion);
        for (int i = 1; i < mSection * mPortion; i++) {
            canvas.rotate(angle, mCenterX, mCenterY);
            if (i % mPortion == 0) { // 避免与长刻度画重合
                continue;
            }
            float fraction = (angle * (i + 1) / mSweepAngle);
            if (angle * i > v) {
                mTickMarksPaint.setColor(mDefaultColor);
            } else {
                int evaluate = (int) mArgbEvaluator.evaluate(fraction, mStartColor, mEndColor);
                mTickMarksPaint.setColor(evaluate);
            }
            canvas.drawLine(x0, (float) height, x2, (float) height, mTickMarksPaint);
        }
        canvas.restore();
    }


    private Paint initInsideDiscPaint() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.GRAY);
        paint.setStyle(Paint.Style.FILL);
        return paint;
    }

    /**
     * @param discFraction 设置内圆盘的的半径 , 值为 1.0 - 0.0 , 外圈圆的半径 * discFraction
     */
    public void setDiscFraction(float discFraction) {
        mDiscFraction = discFraction;
        invalidate();
    }

    public void setMax(int max) {
        mMax = max;
        invalidate();
    }

    /**
     * @param defaultColor 设置默认颜色 不设置则为 Color.GRAY
     */
    public void setDefaultColor(int defaultColor) {
        mDefaultColor = defaultColor;
        invalidate();
    }

    /**
     * @param progress 设置进度
     */
    public void setProgress(int progress) {
        mProgress = progress;
        invalidate();
    }

    /**
     * @param textColor 文字颜色
     */
    public void setTextColor(int textColor) {
        mTextColor = textColor;
        invalidate();
    }

    /**
     * @param w 长刻度线宽度
     * @param h 长刻度线宽度
     */
    public void setLongLine(int w, int h) {
        mShortLine = new Line(w, h);
        invalidate();
    }

    /**
     * @param w 短刻度线宽度
     * @param h 短刻度线宽度
     */
    public void setShortLine(int w, int h) {
        mShortLine = new Line(w, h);
        invalidate();
    }

    /**
     * @param textSize 文字大小
     */
    public void setTextSize(float textSize) {
        mTextSize = textSize;
        invalidate();
    }

    /**
     * @param startColor 起始颜色
     */
    public void setStartColor(int startColor) {
        mStartColor = startColor;
        invalidate();
    }

    /**
     * @param endColor 结束颜色
     */
    public void setEndColor(int endColor) {
        mEndColor = endColor;
        invalidate();
    }

    /**
     * @param portion 每个大刻度里面分为多少小刻度 默认为 3
     */
    public void setPortion(int portion) {
        mPortion = portion;
        invalidate();
    }

    /**
     * @param section 整个仪表有多少大刻度 默认为 20
     */
    public void setSection(int section) {
        mSection = section;
        invalidate();
    }

    private class Line {
        int height;
        int width;
        int color;

        Line(int width, int height) {
            this.width = width;
            this.height = height;
        }

    }

    private static int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                Resources.getSystem().getDisplayMetrics());
    }

    private static int sp2px(int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                Resources.getSystem().getDisplayMetrics());
    }
}
