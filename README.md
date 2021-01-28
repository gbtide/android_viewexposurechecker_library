# android_viewexposurechecker_library
View 노출을 판정하는 라이브러리 입니다.

### 용도
View의 노출을 판정합니다.
- case 1 : 화면에 있는 특정 View가 노출되면 로그를 쏴주세요. (View의 위치는 ScrollView든, RecyclerView든, 일반 ViewGroup이든 상관없음)
- case 2 : 화면에 있는 특정 View가 50%만 노출되었을 때 노출 로그를 쏴주세요
- case 3 : 화면에 있는 특정 View가 1초간 노출되었을 때 노출 로그를 쏴주세요
- case 4 : 광고 View가 노출되면 PV로 쏴주세요 (PV라서 광고를 다시 불러올 때까지는 딱 한번 쏴야합니다.)

### 코드 예시
- 일반 뷰
<pre>
<code>
private void checkPV(@NonNull LifecycleOwner owner, View targetView, String pvCallUrl) {
     
    // LifecycleOwner를 넣어주면, 생명주기에 맞추어서 필요한 사항들을 관리해줍니다.
    // Activity, Fragment 넣어주면 되니 꼭 넣어주세요
    // 1) 폴링 체크 stop(onPause) or restart(onResume)
    // 2) 내부에서 물고 있는 View 참조 처리(onDestroy)
    ViewExposureNotifier viewExposureNotifier = new ViewExposureNotifier(owner, targetView, new ExposureChecker() {
        @Override
        public float getViewExposureRate() {
            // View의 몇 %를 노출시켜야 노출로 판정할 것인가? (Of ~ 1.0f)
            return 0.5f;
        }
 
        @Override
        public boolean ableToAutoExposureCheck() {
            // View가 노출 중인지를 확인하는 폴링 체크를 내부에서 알아서 하게 할 것인가? false로 하면 scroll event 등을 바깥에서 넣어주면 됩니다
            return true;
        }
 
        @Override
        public int getAutoExposureCheckPeriodMillis() {
            // {@link #ableToAutoExposureCheck()} 가 true 일 때, 얼마나 자주 폴링 체크를 할 것인가?
            return 100;
        }
 
        @Override
        public int getExposureTimeMillisForNotify() {
            // View가 얼마동안 오래 노출되어야 노출되었다고 판정할 것인가?
            return 1000;
        }
    }, new IExposureListener() {
        @Override
        public void onExpose(Bundle bundle) {
            logger.d("### exposure!!");
            SimpleHttpRequest.sendGet(pvCallUrl);
        }
    });
 
    viewExposureNotifier.useInfiniteNotifier(false);   // PV check 를 위해 딱 1번만 notify 합니다. default는 true라서 일반 노출 로그에서는 호출안해도 됩니다.
    viewExposureNotifier.start();
}
</code>
</pre>

- 리스트 뷰 아이템
<pre>
<code>
public class MyRecyclerViewAdapter extends Adapter {
 
    // LifecycleOwner가 Destroy 되면, LifecycleOwnerContainer 내부의 LifecycleOwner 참조도 끊어지게 됩니다.
    private LifecycleOwnerContainer mLifecycleOwnerContainer;
 
    // Adpater 생성자에서 owner를 받습니다.
    public SectionHomeRecyclerViewAdapter(LifecycleOwner owner) {
        mLifecycleOwnerContainer = new LifecycleOwnerContainer(owner);
    }
 
    @NonNull
    @Override
    public BaseVMViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (ItemType.from(viewType)) {
            case MY_ITEM:
                ItemBinding itemBinding = ItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new ItemViewHolder(itemBinding, ViewExposureNotifierFactory.forAAABanner(mLifecycleOwnerContainer.get(), itemBinding.getRoot()));
        }
        return null;
    }
}
 
 
public class ItemViewHolder extends ViewHolder {
    private ItemBinding mBinding;
 
    private ViewExposureNotifier mExposureNotifier;
 
    public ItemViewHolder(ItemBinding binding, ListViewExposureNotifier notifier) {
        super(binding.getRoot());
        mBinding = binding;
        mExposureNotifier = notifier;
        mExposureNotifier.start();
    }
 
    @Override
    public void bind(int position, BaseListElementVM vm) {
        this.mBinding.setViewModel((MyViewModel) vm);
        this.mBinding.executePendingBindings();
         
        // bind 시 아래 코드 추가
        // bind 할 데이터는 Object 이니 ViewModel 말고 어떤 것도 들어갈 수 있습니다
        // ViewHolder 재사용 특성 때문에 라이브러리 내부에 position 정보를 넣어줘야합니다.
        this.mExposureNotifier.bind(position, viewModel);
    }
 
}
 
 
// 새롭게 작성이 필요한 클래스입니다.
class ViewExposureNotifierFactory {
 
    static void ListViewExposureNotifier forAAABanner(LifecycleOwner owner, View view) {
        return new ListViewExposureNotifier(owner, view, new ExposureChecker() {
            @Override
                public float getViewExposureRate() {
                return 0.5f;
            }
 
            @Override
            public int getAutoExposureCheckPeriodMillis() {
                return 100;
            }
 
            @Override
            public int getExposureTimeMillisForNotify() {
                return 1000;
            }
        }, new IExposureListener() {
            @Override
            public void onExpose(Object data) {
                if (data instanceof MyViewModel) {
                    MyViewModel vm = (MyViewModel) data;
                    vm.onExposeBanner();
                }
            }
 
        });    
    }
}
</code>
</pre>
