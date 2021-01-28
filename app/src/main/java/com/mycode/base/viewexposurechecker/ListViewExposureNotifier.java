package com.mycode.base.viewexposurechecker;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

/**
 * Created by kyunghoon on 2019-07-29
 *
 * {@link ViewExposureNotifier#getData()} 에 hashCode 잘 정의되어있어야 합니다.
 */
public class ListViewExposureNotifier extends ViewExposureNotifier {
    private ListItemTagger mListItemTagger = new ListItemTagger();

    private int mPosition = NOT_SET_POSITION;
    private static final int NOT_SET_POSITION = -1;

    public ListViewExposureNotifier(@Nullable LifecycleOwner owner, @NonNull View targetView, @NonNull ExposureChecker exposureChecker, IExposureListener listener) {
        super(targetView, exposureChecker, listener);
        owner.getLifecycle().addObserver(new LifecycleObserverImple(owner));
    }

    /**
     * 리스트에서는 position 을 기억해야하니 {@link ViewExposureNotifier#setData(Object)} 말고 본 메소드를 써주세요.
     *
     * memo. {@link RecyclerView.Adapter#onBindViewHolder(RecyclerView.ViewHolder, int)} 가 {@link #onExpose()} 보다 먼저 불립니다.
     * @param data : Recycler에 바인드 될 리스트의 item(View Holder 하나에 바인드될 item). hashCode 잘 정의되어있어야 합니다.
     */
    public void bind(int position, Object data) {
        mPosition = position;
        setDataInternal(data);
        resetIfDataRefreshed();
    }

    private void resetIfDataRefreshed() {
        // memo. pull to refresh 시 tagId hashCode가 바뀜
        if (!mListItemTagger.isAlreadyTagged(mPosition, getTagId())) {
            resetAll();
        }
    }

    /**
     * {@link #bind(int, Object)} 참고
     */
    @Override
    public void setData(Object data) {
        throw new UnsupportedOperationException("use bind() instead of setData()!!");
    }

    private Integer getTagId() {
        if (getData() == null) {
            return null;
        }
        return getData().hashCode();
    }

    @Override
    protected boolean shouldCheckAgain() {
        // ViewHolder 하나에 여러개 데이터가 바인딩 될 수 있다는 점,
        // 그리고 full to refresh 등에 대응하려면 한 번만 notify 되어야하는 상황에서도 지속 체크 필요.
        return true;
    }

    @Override
    protected void onExpose() {
        if (!isInfiniteNotifier()) {
            // memo. pull to refresh 시 tagId(hashCode)가 바뀌므로 onExpose를 탈 것임
            if (mListItemTagger.isAlreadyTagged(mPosition, getTagId())) {
                return;
            }
        }
        super.onExpose();
        mListItemTagger.setTag(mPosition, getTagId());
    }
}
