package com.mycode.base.viewexposurechecker;

import android.graphics.Rect;
import android.view.View;

import androidx.core.view.ViewCompat;

/**
 * Created by kyunghoon on 2019-03-20
 */
public abstract class ExposureChecker {

    /**
     * 기본 적인 로직입니다. 경우에 따라 재정의해서 쓰시면 됩니다.
     */
    protected boolean isExposed(View view) {
        Rect clippingRect = new Rect();
        boolean isShown = view.getGlobalVisibleRect(clippingRect);
        if (!isShown) {
            return false;
        }

        if (!ViewCompat.isAttachedToWindow(view)) {
            return false;
        }

        final float totalArea = view.getWidth() * view.getHeight();
        if (totalArea == 0) {
            return false;
        }

        final float exposureArea = clippingRect.width() * clippingRect.height();
        if (exposureArea/totalArea >= getExposureRateInternal()) {
            return true;
        }

        return false;
    }

    private float getExposureRateInternal() {
        float exposureRate = getViewExposureRate();
        if (exposureRate < 0f) {
            exposureRate = 0f;
        }
        if (exposureRate > 1.0f) {
            exposureRate = 1.0f;
        }
        return exposureRate;
    }

    /**
     * @return View의 몇 %를 노출시켜야 노출로 판정할 것인가? (Of ~ 1.0f)
     */
    public abstract float getViewExposureRate();

    /**
     * @return 얼마나 자주 폴링 체크를 할 것인가?
     */
    public abstract int getAutoExposureCheckPeriodMillis();

    /**
     * @return View가 얼마동안 오래 노출되어야 노출되었다고 판정할 것인가?
     */
    public abstract int getExposureTimeMillisForNotify();


    boolean isFullyDisappeared(View view) {
        return !isExposedSlightly(view);
    }

    boolean isExposedSlightly(View view) {
        Rect clippingRect = new Rect();
        boolean isShown = view.getGlobalVisibleRect(clippingRect);
        if (!isShown) {
            return false;
        }

        if (!ViewCompat.isAttachedToWindow(view)) {
            return false;
        }

        return true;
    }

}
