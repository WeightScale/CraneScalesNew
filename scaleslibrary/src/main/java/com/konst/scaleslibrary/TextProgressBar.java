package com.konst.scaleslibrary;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.util.AttributeSet;
import android.widget.ProgressBar;

/**
 * @author Kostya 21.11.2016.
 */
public class TextProgressBar extends ProgressBar {
    private String text="";
    private final Paint textPaint;

    /*public TextProgressBar(Context context) {
        super(context);
        text = "HP";
        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
    }*/

    public TextProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray attributesArray = context.obtainStyledAttributes(attrs, R.styleable.texProgressBar, R.attr.textProgressBarStyle, 0);
        //text = "HP";
        textPaint = new Paint();
        textPaint.setColor(attributesArray.getColor(R.styleable.texProgressBar_textColorProgressBar, Color.BLACK));
        textPaint.setTextSize(attributesArray.getDimension(R.styleable.texProgressBar_textSizeProgressBar, 12));
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
    }

    /*public TextProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        text = "HP";
        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
    }*/

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        // First draw the regular progress bar, then custom draw our text
        super.onDraw(canvas);
        Rect bounds = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), bounds);
        int x = getWidth() / 2 - bounds.centerX();
        int y = getHeight() / 2 - bounds.centerY();
        canvas.drawText(text, x, y, textPaint);
    }

    public synchronized void setText(String text) {
        this.text = text;
        drawableStateChanged();
    }

    public void setTextColor(int color) {
        textPaint.setColor(color);
        drawableStateChanged();
    }

    public void setHpBar(int a) {
        setProgress(a);
        setText(a + "/" + getMax());
    }
}
