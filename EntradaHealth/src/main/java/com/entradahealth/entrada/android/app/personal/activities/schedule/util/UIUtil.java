package com.entradahealth.entrada.android.app.personal.activities.schedule.util;

import com.entradahealth.entrada.android.R;
import com.entradahealth.entrada.android.app.personal.activities.schedule.model.Resource;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils.TruncateAt;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


/**
 * A Utility class for UI.
 *
 */
public class UIUtil {

	/**
	 * creates popup window
	 */
	public static TableLayout initPopupWindow(Activity activity, final View bindedView, final PopupWindow popupWindow) {
		TableLayout tableLayout = new TableLayout(activity) {
			@Override
			protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
				setMinimumWidth(bindedView.getWidth());
				super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			}
		};
		tableLayout.setBackgroundColor(Color.WHITE);
		return tableLayout;
	}

	/**
	 * creates a table row for resource names
	 */
	public static TableRow createTableRow(Activity activity, Resource title) {
		Paint paint = null;
		if (paint == null) {
			paint = new Paint();
			paint.setColor(Color.LTGRAY);
		}

		final Paint finalPaint = paint;
		final TableRow tableRow = new TableRow(activity) {
			@Override
			protected void dispatchDraw(Canvas canvas) {
				super.dispatchDraw(canvas);
				canvas.drawLine(0, getHeight() - 1, getWidth(),getHeight() - 1, finalPaint);
				
			
			}
		};
		TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.WRAP_CONTENT);
		tableRow.setLayoutParams(tableParams);
		
		// Creating a check box image., but we are not adding it to row as of now.
		ImageView imgView = new ImageView(activity);
		imgView.setImageResource(R.drawable.un_checkd);
		imgView.setPadding(0, 20, 20, 20);
		//Creating a Text View
		TextView textView = new TextView(activity);
		textView.setText(title.getResourceName());
		textView.setPadding(20, 20,imgView.getDrawable().getIntrinsicWidth() + 10, 20);
		textView.setTextColor(Color.BLACK);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
		textView.setEllipsize(TruncateAt.END);
		TableRow.LayoutParams tableParamss = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.WRAP_CONTENT);
		tableParamss.weight = 1;
		textView.setLayoutParams(tableParamss);
		
		textView.setTag(title);
		tableRow.addView(textView);

		return tableRow;
	}


	/**
	 * This method convets dp unit to equivalent device specific value in
	 * pixels.
	 * 
	 * @param dp
	 *            A value in dp(Device independent pixels) unit. Which we need
	 *            to convert into pixels
	 * @param context
	 *            Context to get resources and device specific display metrics
	 * @return A float value to represent Pixels equivalent to dp according to
	 *         device
	 */
	public static float convertDpToPixel(float dp, Context context) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float px = dp * (metrics.densityDpi / 160f);
		return px;
	}
}
