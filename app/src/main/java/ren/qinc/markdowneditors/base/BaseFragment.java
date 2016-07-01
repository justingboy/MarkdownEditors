package ren.qinc.markdowneditors.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.leakcanary.RefWatcher;

import butterknife.ButterKnife;
import ren.qinc.markdowneditors.BuildConfig;
import ren.qinc.markdowneditors.event.RxEvent;
import ren.qinc.markdowneditors.event.RxEventBus;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * fragment基类
 * Created by 沈钦赐 on 2016/1/25.
 */
public abstract class BaseFragment extends BaseStatedFragment implements BaseViewInterface, EventInterface {
    protected Context mContext;
    protected View rootView;
    protected BaseApplication application;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        application = (BaseApplication) mContext.getApplicationContext();
        if (rootView == null) {
            rootView = View.inflate(getActivity(), getLayoutId(), null);
            if (rootView == null)
                throw new IllegalStateException(this.getClass().getSimpleName() + ":LayoutID找不到对应的布局");

        }
        ButterKnife.bind(this, rootView);
        registerEvent();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // MobclickAgent.onPageStart(this.getClass().getSimpleName());
        if (isFirstFocused) {
            isFirstFocused = false;
            initData();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // MobclickAgent.onPageEnd(this.getClass().getSimpleName());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(rootView);
        //注销EventBus
        unregisterEvent();
        mContext = null;
        rootView = null;


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (BuildConfig.DEBUG) {//Debug的时候检查内存泄露
            RefWatcher refWatcher = BaseApplication.getRefWatcher(mContext);
            if (refWatcher != null) {
                refWatcher.watch(this);
            }
        }
    }

    @Override
    protected void onFirstLaunched() {
        super.onFirstLaunched();
        //包含菜单到所在Activity
        setHasOptionsMenu(hasMenu());
        onCreateAfter(null);
    }


    private boolean isFirstFocused = true;


    //用于接收事件
    private Subscription mSubscribe;

    @Override
    public void registerEvent() {
        //订阅
        mSubscribe = RxEventBus.getInstance().toObserverable()
                .filter(o -> o instanceof RxEvent)//只接受RxEvent
                .map(o -> (RxEvent) o)
                .filter(r -> hasNeedEvent(r.type))//只接受type = 1和type = 2的东西
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onEventMainThread);
    }

    @Override
    public void unregisterEvent() {
        if (mSubscribe != null) {
            mSubscribe.unsubscribe();
        }
    }

    /**
     * 根据需要重写，如果返回True，这代表该type的Event你是要接受的 会回调
     * Has need event boolean.
     *
     * @param type the type
     * @return the boolean
     */
    @Override
    public boolean hasNeedEvent(int type) {
        return type == RxEvent.TYPE_FINISH;
    }
    @Override
    public void onEventMainThread(RxEvent e) {
        if (e.type == RxEvent.TYPE_FINISH && e.o.length > 0) {
            //xxxx执行响应的操作
        }
    }



    public boolean hasMenu() {
        return false;
    }

//    /**
//     * 代替findViewById 自动类型转换
//     *
//     * @param id
//     * @return View
//     */
//    @SuppressWarnings("unchecked")
//    @Deprecated
//    protected final <T extends View> T findView(@IdRes int id) {
//        if (rootView == null) {
//            return null;
//        }
//        return (T) rootView.findViewById(id);
//    }
//
//    /**
//     * 设置view的点击事件
//     * Set view click listener t.
//     *
//     * @param <T>           the type parameter
//     * @param v             the v
//     * @param clickListener the click listener
//     * @return the t
//     */
//    protected final <T extends View> T setViewClickListener(T v, @NonNull View.OnClickListener clickListener) {
//        if (v == null) return null;
//        v.setOnClickListener(clickListener);
//        return v;
//    }
//
//    /**
//     * Set view click listener by id t.
//     *
//     * @param <T>           the type parameter
//     * @param id            the id
//     * @param clickListener the click listener
//     * @return the t
//     */
//    @SuppressWarnings("unchecked")
//    protected final <T extends View> T setViewClickListenerById(@IdRes int id, @NonNull View.OnClickListener clickListener) {
//        return setViewClickListener((T) findView(id), clickListener);
//    }

    /**
     * 返回键，预留给所在activity调用
     * On back pressed boolean.
     *
     * @return the boolean
     */
    public boolean onBackPressed() {
        return false;
    }

    /**
     * 需要重写hasMenu() 返回True，才会创建菜单
     *
     * @param menu     the menu
     * @param inflater the inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * 需要重写hasMenu() 返回True，才会回调
     * On options item selected boolean.
     *
     * @param item the item
     * @return the boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


    /**
     * Save Fragment's State here
     */
    @Override
    protected void onSaveState(Bundle outState) {
        super.onSaveState(outState);
        // For example:
        //outState.putString("text", tvSample.getText().toString());
    }

    /**
     * Restore Fragment's State here
     */
    @Override
    protected void onRestoreState(Bundle savedInstanceState) {
        super.onRestoreState(savedInstanceState);
        // For example:
        //tvSample.setText(savedInstanceState.getString("text"));
    }

}
