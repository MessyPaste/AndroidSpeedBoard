package liutaw.com.understandview.component;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import liutaw.com.understandview.Utils;

/**
 * Created by Administrator on 2016/12/2.
 */

public class SpeedBoardView extends BaseView {

    public static final float QuaterSpeedAngle = 16.875f;//15码一个大刻度
    public static final float HalfQuaterSpeedAngle = QuaterSpeedAngle / 2;//半个刻度，用于测量小刻度
    public static final float StartAngle = 45.0f;//开始角度

    private Paint mBoardCirclePaint;//仪表盘外围扇形
    private Paint mBigScalePaint;//大刻度
    private Paint mBigScaleTextPaint;//大刻度的文字
    private Paint mSmallScalePaint;//小刻度
    private Paint mSpeedHandPaint;//速度指针
    private Paint mSpeedTextPaint;//速度文字

    private float realSpeed = 0;

    public SpeedBoardView(Context context) {
        super(context);
        initPaint();
    }

    public SpeedBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    private void initPaint() {

        mBoardCirclePaint = new Paint();
        mBoardCirclePaint.setColor(Color.parseColor("#000000"));
        mBoardCirclePaint.setStrokeWidth(dp2px(2));
        mBoardCirclePaint.setStyle(Paint.Style.STROKE);

        mBigScalePaint = new Paint();
        mBigScalePaint.setColor(Color.parseColor("#000000"));
        mBigScalePaint.setStrokeWidth(dp2px(2));
        mBigScalePaint.setStyle(Paint.Style.FILL);


        mSmallScalePaint = new Paint();
        mSmallScalePaint.setColor(Color.parseColor("#000000"));
        mSmallScalePaint.setStrokeWidth(dp2px(1));
        mSmallScalePaint.setStyle(Paint.Style.FILL);

        mBigScaleTextPaint = new Paint();
        mBigScaleTextPaint.setColor(Color.parseColor("#000000"));
        mBigScaleTextPaint.setTextSize(sp2px(8));

        mSpeedHandPaint = new Paint();
        mSpeedHandPaint.setColor(Color.parseColor("#000000"));
        mSpeedHandPaint.setStrokeWidth(dp2px(2));
        mSpeedHandPaint.setStyle(Paint.Style.FILL);

        mSpeedTextPaint = new Paint();
        mSpeedTextPaint.setColor(Color.parseColor("#000000"));
        mSpeedTextPaint.setTextSize(sp2px(16));


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int realWidth;
        int realHeight;
        if (widthMode == MeasureSpec.EXACTLY) {
            realWidth = widthSize;
        } else {
            realWidth = dp2px(150);
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            realHeight = heightSize;
        } else {
            realHeight = dp2px(150);
        }

        setMeasuredDimension(realWidth, realHeight);
    }

    private int recommentRadius = 0;
    private int recommentSpeedHandLength = 0;

    @Override
    protected void onDraw(final Canvas canvas) {
        canvas.save();
        canvas.translate(getMeasuredWidth() / 2, getMeasuredHeight() / 2);

        recommentRadius = Math.min(getMeasuredWidth(), getMeasuredHeight()) / 3;
        drawCircleLine(canvas);
        drawScale(canvas);

        if (realSpeed <= 0) {
            drawSpeedhandBySpeed(canvas, 0);
            drawSpeedText(canvas, 0);
        }
        if (realSpeed > 240) {
            drawSpeedhandBySpeed(canvas, 240);
            drawSpeedText(canvas, 240);
        } else {
            drawSpeedhandBySpeed(canvas, realSpeed);
            drawSpeedText(canvas, Math.round(realSpeed));
        }


    }

    private void drawCircleLine(Canvas canvas) {
        canvas.save();
        canvas.rotate(45);

        int radius = recommentRadius;
        RectF oval = new RectF();                     //RectF对象
        oval.left = -radius;                              //左边
        oval.top = -radius;                                   //上边
        oval.right = radius;                             //右边
        oval.bottom = radius;                                //下边
        canvas.drawArc(oval, 90, 270, false, mBoardCirclePaint);    //绘制圆弧

        canvas.restore();
    }

    private void drawScale(Canvas canvas) {
        canvas.save();


        for (int i = 0; i <= 16; i++) {
            float angle = StartAngle + QuaterSpeedAngle * i;
            if (i == 16) {
                drawSingleBigScale(canvas, 15 * i + "", angle, true);
            } else {
                drawSingleBigScale(canvas, 15 * i + "", angle, false);
            }

        }
        canvas.restore();

    }

    private void drawSingleBigScale(Canvas canvas, String text, float angle, boolean isLast) {
        canvas.save();
        canvas.rotate(angle);
        float startX = 0.0f;
        float startY = recommentRadius;
        float stopX = 0.0f;
        float stopY = recommentRadius - dp2px(8);
        canvas.drawLine(startX, startY, stopX, stopY, mBigScalePaint);

        if (!isLast)
            drawSingleSmallScale(canvas);

        float textY = stopY - dp2px(2) - getTextHeight(mBigScaleTextPaint, text);
        float textX = getTextWidth(mBigScaleTextPaint, text) / 2;

        if (recommentSpeedHandLength == 0) {
            recommentSpeedHandLength = recommentRadius * 3 / 4;
        }
        drawText(canvas, text, angle, textX, textY);

    }


    private void drawSingleSmallScale(Canvas canvas) {

        canvas.rotate(HalfQuaterSpeedAngle);
        float startX = 0.0f;
        float startY = recommentRadius;
        float stopX = 0.0f;
        float stopY = recommentRadius - dp2px(4);
        canvas.drawLine(startX, startY, stopX, stopY, mSmallScalePaint);


        //这里就不绘制小刻度了
    }

    private void drawText(Canvas canvas, String text, float angle, float textX, float textY) {
        canvas.restore();
        float transX = (float) (Math.sin(Math.toRadians(angle)) * textY);
        float transY = (float) (transX / (Math.tan(Math.toRadians((180 - angle) / 2))));

        //此处有一个特例，就是当tan趋向于无穷大的时候，需要手动调整，只会出现一次！
        if (transY == Float.POSITIVE_INFINITY) {
            transX = 0;
            transY = textY * 2;
        }
        Utils.log(this, "transX=" + transX + ",transY=" + transY);
        canvas.save();
        canvas.translate(-transX, -transY);
        canvas.drawText(text, -textX, textY, mBigScaleTextPaint);
        canvas.restore();

    }

    private void drawSpeedhandByAngle(Canvas canvas, float angle) {
        canvas.save();

        canvas.rotate(angle);
        canvas.drawLine(0.0f, 0.0f, 0.0f, recommentSpeedHandLength, mSpeedHandPaint);

        canvas.restore();
    }

    public void setSpeedWithAnimation(float speed) {
        ValueAnimator anim = ObjectAnimator.ofFloat(realSpeed, speed);
        anim.setDuration(2000);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
//                setRealSpeed((Float) animation.getAnimatedValue());
                setRealSpeed(value);

                Utils.log(this, "animation.getAnimatedValue()=" + animation.getAnimatedValue());
            }
        });
        anim.start();
    }

    private void drawSpeedhandBySpeed(Canvas canvas, float realSpeed) {
        canvas.save();

        canvas.rotate(realSpeed * 270 / 240 + StartAngle);
        canvas.drawLine(0.0f, 0.0f, 0.0f, recommentSpeedHandLength, mSpeedHandPaint);

        canvas.restore();
    }


    private void drawSpeedText(Canvas canvas, int speed) {
        canvas.save();

        String text = speed + " km/h";
        canvas.drawText(text, -getTextWidth(mSpeedTextPaint, text)/2, recommentRadius - getTextHeight(mSpeedTextPaint, text), mSpeedTextPaint);

        canvas.restore();

    }


    public float getRealSpeed() {
        return realSpeed;
    }

    public void setRealSpeed(float realSpeed) {
        this.realSpeed = realSpeed;
        invalidate();
    }
}
