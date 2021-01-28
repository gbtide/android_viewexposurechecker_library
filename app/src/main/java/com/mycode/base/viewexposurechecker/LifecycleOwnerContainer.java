package com.mycode.base.viewexposurechecker;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

/**
 * Created by kyunghoon on 2019-07-25
 *
 * LifecycleOwner 참조를 들고 있다가, Owner(Activity or Fragment ..)가 destroy 되면 참조를 알아서 제거함.
 */
public class LifecycleOwnerContainer {
    private LifecycleOwner mOwner;

    public LifecycleOwnerContainer(@NonNull LifecycleOwner owner) {
        this.mOwner = owner;
        owner.getLifecycle().addObserver(new LifecycleObserverImple(owner));
    }

    public LifecycleOwner get() {
        return mOwner;
    }

    void destory() {
        mOwner = null;

    }

    class LifecycleObserverImple implements LifecycleObserver {
        private LifecycleOwner mLifeCycleOwner;

        LifecycleObserverImple(@NonNull LifecycleOwner owner) {
            this.mLifeCycleOwner = owner;
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        public void destroy() {
            LifecycleOwnerContainer.this.destory();
            mLifeCycleOwner.getLifecycle().removeObserver(this);
            mLifeCycleOwner = null;
        }
    }

}
