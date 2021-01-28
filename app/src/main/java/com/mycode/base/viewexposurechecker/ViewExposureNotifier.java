package com.mycode.base.viewexposurechecker;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

/**
 * Created by kyunghoon on 2019-03-20
 */
public class ViewExposureNotifier {
    private static final int MSG_CHECK_EXPOSURE = 737;

    private View mView;
    private IExposureListener mExposureListener;
    private IDisappearListener mDisappearListener = null;
    private ExposureChecker mExposureChecker;

    private Timer mExposureTimeLengthChecker = new Timer();
    private boolean mDestroyed = false;
    private boolean mPaused = false;
    private boolean mUseInfiniteNotifier = true;
    private boolean mResetAllWhenOnPause = false;

    private boolean mFinishExposureNotify = false;
    private Boolean mFinishDisappearanceNotify = null;

    private Object mData;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CHECK_EXPOSURE:
                    if (isDestroyed()) {
                        return;
                    }
                    checkInternal();

                    repeatCheckIfPossible();
                    break;
            }
        }
    };

    /**
     * @param owner
     * @param targetView      : 노출 여부를 판정할 뷰
     * @param exposureChecker
     * @param listener
     */
    public ViewExposureNotifier(@NonNull LifecycleOwner owner,
                                @NonNull View targetView,
                                @NonNull ExposureChecker exposureChecker,
                                IExposureListener listener) {
        this.mView = targetView;
        this.mExposureChecker = exposureChecker;
        this.mExposureListener = listener;

        if (owner != null) {
            owner.getLifecycle().addObserver(new LifecycleObserverImple(owner));
        }
    }

    protected ViewExposureNotifier(@NonNull View targetView,
                                   @NonNull ExposureChecker exposureChecker,
                                   IExposureListener listener) {
        this.mView = targetView;
        this.mExposureChecker = exposureChecker;
        this.mExposureListener = listener;
    }

    @MainThread
    public void start() {
        mExposureTimeLengthChecker.reset();

        checkInternal();

        repeatCheckIfPossible();
    }

    private void checkInternal() {
        if (isDestroyed()) {
            return;
        }

        // 1. 노출 체크
        notifyExposureIfShould();

        // 2. 비노출 체크
        notifyDisappearanceIfShould();
    }

    private void notifyExposureIfShould() {
        if (mExposureListener == null) {
            return;
        }

        if (mExposureChecker.isExposed(mView)) {
            if (mFinishExposureNotify) {
                return;
            }

            if (mExposureTimeLengthChecker.notStartYet()) {
                mExposureTimeLengthChecker.start();
            }

            if (mExposureTimeLengthChecker.isOverOrEqual(mExposureChecker.getExposureTimeMillisForNotify())) {
                onExpose();
                mFinishExposureNotify = true;
                return;
            }

        } else {
            mExposureTimeLengthChecker.reset();
            mFinishExposureNotify = false;

        }
    }

    private void notifyDisappearanceIfShould() {
        if (mDisappearListener == null) {
            return;
        }

        if (!mExposureChecker.isExposed(mView)) {
//        if (mExposureChecker.isFullyDisappeared(mView)) {
            // memo. null 체크는 왜?
            // 최초 화면이 보여질 때, 화면에 완전히 안보여지고 있어도 notify 하면 안 됨
            if (mFinishDisappearanceNotify == null
                    || mFinishDisappearanceNotify) {
                return;
            }
            onDisappear();
            mFinishDisappearanceNotify = Boolean.TRUE;
            return;

        } else {
            mFinishDisappearanceNotify = Boolean.FALSE;

        }
    }

    protected void onExpose() {
        if (mExposureListener != null) {
            mExposureListener.onExpose(getData());
        }
    }

    protected void onDisappear() {
        if (mDisappearListener != null) {
            mDisappearListener.onDisappearFromScreen(getData());
        }
    }

    private void repeatCheckIfPossible() {
        if (shouldCheckAgain()) {
            mHandler.removeMessages(MSG_CHECK_EXPOSURE);
            mHandler.sendEmptyMessageDelayed(MSG_CHECK_EXPOSURE, mExposureChecker.getAutoExposureCheckPeriodMillis());
        }
    }

    protected boolean shouldCheckAgain() {
        return !mPaused &&
                (mUseInfiniteNotifier || !mFinishExposureNotify);
    }

    /**
     * @param data : 뷰 노출이 되었을 때, {@link IExposureListener#onExpose(Object)} 에서 받아 볼 수 있는 데이터
     */
    public void setData(Object data) {
        setDataInternal(data);
    }

    protected void setDataInternal(Object data) {
        this.mData = data;
    }

    protected Object getData() {
        return mData;
    }

    /**
     * @return false 일 때 : 뷰가 1회 노출되었다고 판정되면 더 이상 노출 여부를 알려주지 않습니다. PV 체크에 용이합니다.
     */
    public void useInfiniteNotifier(boolean use) {
        mUseInfiniteNotifier = use;
    }

    /**
     * default 값인 false 인 경우, view 가 노출된 상태에서 다른 화면으로 갔다가 다시와도 exposure notify 를 하지 않는다.
     * true 인 경우, view 가 노출된 상태에서 다른 화면으로 갔다가 다시오면, 다시 조건 체크해서 notify 를 한다.
     */
    public void resetAllWhenOnPause() {
        mResetAllWhenOnPause = true;
    }

    public void activateDisappearanceChecker(IDisappearListener disappearListener) {
        this.mDisappearListener = disappearListener;
    }

    protected boolean isInfiniteNotifier() {
        return mUseInfiniteNotifier;
    }

    private boolean isDestroyed() {
        return mDestroyed;
    }

    protected void onDestroy() {
        mDestroyed = true;
        mView = null;
        mData = null;
        mExposureListener = null;
        mExposureChecker = null;
    }

    protected void onPause() {
        mPaused = true;
        mHandler.removeMessages(MSG_CHECK_EXPOSURE);

        if (mResetAllWhenOnPause) {
            resetAll();
        } else {
            mExposureTimeLengthChecker.reset();
        }
    }

    protected void onResume() {
        mPaused = false;
        repeatCheckIfPossible();
    }

    protected void resetAll() {
        mFinishExposureNotify = false;
        mFinishDisappearanceNotify = null;
        mExposureTimeLengthChecker.reset();
    }

    class LifecycleObserverImple implements LifecycleObserver {
        private LifecycleOwner mLifeCycleOwner;

        LifecycleObserverImple(@NonNull LifecycleOwner owner) {
            this.mLifeCycleOwner = owner;
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        public void resume() {
            ViewExposureNotifier.this.onResume();
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        public void pause() {
            ViewExposureNotifier.this.onPause();
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        public void destroy() {
            ViewExposureNotifier.this.onDestroy();

            this.mLifeCycleOwner.getLifecycle().removeObserver(this);
            this.mLifeCycleOwner = null;
        }
    }

}
