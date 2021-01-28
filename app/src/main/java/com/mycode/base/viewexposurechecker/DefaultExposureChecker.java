package com.mycode.base.viewexposurechecker;

/**
 * Created by kyunghoon on 2019-03-20
 */
public class DefaultExposureChecker extends ExposureChecker {

    @Override
    public float getViewExposureRate() {
        return 1.0f;
    }

    @Override
    public int getAutoExposureCheckPeriodMillis() {
        return 100;
    }

    @Override
    public int getExposureTimeMillisForNotify() {
        return 1000;
    }

}
