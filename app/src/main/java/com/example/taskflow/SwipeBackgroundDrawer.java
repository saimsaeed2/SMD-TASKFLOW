package com.example.taskflow;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

// Draws rounded colored swipe-action backgrounds with icon and label behind swiped RecyclerView cards.
public class SwipeBackgroundDrawer {

    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF bgRect = new RectF();
    private final float cornerRadius;
    private final int iconSize;
    private final int horizontalPadding;
    private final int iconTextGap;

    public SwipeBackgroundDrawer(Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        this.cornerRadius = 16f * density;
        this.iconSize = Math.round(22f * density);
        this.horizontalPadding = Math.round(20f * density);
        this.iconTextGap = Math.round(8f * density);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(14f * density);
        textPaint.setFakeBoldText(true);
    }

    public void draw(Canvas canvas, View itemView, float dX,
                     @ColorRes int bgColorRes, @DrawableRes int iconRes, String label) {
        if (dX == 0f) return;

        Context context = itemView.getContext();
        float top = itemView.getTop();
        float bottom = itemView.getBottom();
        float left;
        float right;

        if (dX > 0) {
            left = itemView.getLeft();
            right = Math.min(itemView.getRight(), itemView.getLeft() + dX + cornerRadius);
        } else {
            left = Math.max(itemView.getLeft(), itemView.getRight() + dX - cornerRadius);
            right = itemView.getRight();
        }

        if (right <= left) return;

        canvas.save();
        if (dX > 0) {
            canvas.clipRect(itemView.getLeft(), top, itemView.getLeft() + dX, bottom);
        } else {
            canvas.clipRect(itemView.getRight() + dX, top, itemView.getRight(), bottom);
        }

        bgPaint.setColor(ContextCompat.getColor(context, bgColorRes));
        bgRect.set(itemView.getLeft(), top, itemView.getRight(), bottom);
        canvas.drawRoundRect(bgRect, cornerRadius, cornerRadius, bgPaint);

        Drawable icon = ContextCompat.getDrawable(context, iconRes);
        float centerY = (top + bottom) / 2f;
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textBaseline = centerY - (fm.ascent + fm.descent) / 2f;
        float textWidth = textPaint.measureText(label);

        if (dX > 0) {
            int iconLeft = itemView.getLeft() + horizontalPadding;
            int iconTop = Math.round(centerY - iconSize / 2f);
            drawTintedIcon(canvas, icon, iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize);
            canvas.drawText(label, iconLeft + iconSize + iconTextGap, textBaseline, textPaint);
        } else {
            int iconRight = itemView.getRight() - horizontalPadding;
            int iconLeft = iconRight - iconSize;
            int iconTop = Math.round(centerY - iconSize / 2f);
            drawTintedIcon(canvas, icon, iconLeft, iconTop, iconRight, iconTop + iconSize);
            canvas.drawText(label, iconLeft - iconTextGap - textWidth, textBaseline, textPaint);
        }

        canvas.restore();
    }

    private void drawTintedIcon(Canvas canvas, Drawable drawable,
                                int left, int top, int right, int bottom) {
        if (drawable == null) return;
        Drawable wrapped = DrawableCompat.wrap(drawable.mutate());
        DrawableCompat.setTint(wrapped, Color.WHITE);
        wrapped.setBounds(left, top, right, bottom);
        wrapped.draw(canvas);
    }
}
