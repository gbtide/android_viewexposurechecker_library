package com.mycode.base.viewexposurechecker;

import androidx.annotation.NonNull;
import androidx.core.app.ComponentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

/**
 * Created by kyunghoon on 2020-01-20
 */
public class ActivityReferenceContainer {
    private ComponentActivity mActivity;

    public ActivityReferenceContainer(@NonNull ComponentActivity activity) {
        this.mActivity = activity;
        LifecycleOwner owner = activity;
        owner.getLifecycle().addObserver(new ActivityReferenceContainer.LifecycleObserverImple(owner));
    }

    public ComponentActivity get() {
        return mActivity;
    }

    void destory() {
        mActivity = null;

    }

    class LifecycleObserverImple implements LifecycleObserver {
        private LifecycleOwner mLifeCycleOwner;

        LifecycleObserverImple(@NonNull LifecycleOwner owner) {
            this.mLifeCycleOwner = owner;
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        public void destroy() {
            ActivityReferenceContainer.this.destory();
            mLifeCycleOwner.getLifecycle().removeObserver(this);
            mLifeCycleOwner = null;
        }
    }
}
