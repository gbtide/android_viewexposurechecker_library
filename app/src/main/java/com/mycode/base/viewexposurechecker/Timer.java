package com.mycode.base.viewexposurechecker;

/**
 * Created by kyunghoon on 2019-03-20
 */
class Timer {
    private static final long NOT_STARTED = -1;

    private long mStartTimeMillis = NOT_STARTED;

    public void start() {
        this.mStartTimeMillis = System.currentTimeMillis();
    }

    public boolean notStartYet() {
        return mStartTimeMillis == NOT_STARTED;
    }

    public void reset() {
        mStartTimeMillis = NOT_STARTED;
    }

    public boolean isOverOrEqual(long desiredTimeMillis) {
        if (mStartTimeMillis == NOT_STARTED) {
            return false;
        }

        return System.currentTimeMillis() - mStartTimeMillis >= desiredTimeMillis;
    }
}
