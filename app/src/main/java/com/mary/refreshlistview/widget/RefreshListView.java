package com.mary.refreshlistview.widget;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.mary.refreshlistview.R;

/**
 * 下拉刷新和上拉加载
 * Created by Administrator on 2016/2/4.
 */
public class RefreshListView extends RelativeLayout implements OnScrollListener {
    /**顶部最大拉伸距离*/
    static int MAX_PULL_TOP_HEIGHT;
    /**底部最大拉伸距离*/
    static int MAX_PULL_BOTTOM_HEIGHT;
    /**顶部正在刷新的距离*/
    static int REFRESHING_TOP_HEIGHT;
    /**底部正在加载的距离*/
    static int REFRESHING_BOTTOM_HEIGHT;

    // 状态
    /**到达顶部*/
    private boolean isTop;
    /**到达底部*/
    private boolean isBottom;
    /**正在刷新或加载*/
    private boolean isRefreshing;
    /**正在执行动画*/
    private boolean isAnimation;

    /**顶部布局*/
    RelativeLayout layoutHeader;
    /**底部布局*/
    RelativeLayout layoutFooter;
    /**当前的Y值*/
    private int mCurrentY = 0;
    boolean pullTag = false;
    /**滑动监听*/
    OnScrollListener mOnScrollListener;
    /**拉伸顶部或底部的监听事件*/
    OnPullHeightChangeListener mOnPullHeightChangeListener;

    public RefreshListView(Context context) {
        this(context, null);
    }

    public RefreshListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ListView getListView() {
        return this.mListView;
    }

    /**
     * 设置拉伸顶部或底部的监听事件
     * @param listener 监听
     */
    public void setOnPullHeightChangeListener(OnPullHeightChangeListener listener) {
        this.mOnPullHeightChangeListener = listener;
    }

    /**
     * 设置滑动监听
     * @param listener 监听
     */
    public void setOnScrollListener(OnScrollListener listener) {
        this.mOnScrollListener = listener;
    }

    /**是否正在刷新*/
    public boolean isRefreshing() {
        return this.isRefreshing;
    }

    private ListView mListView = new ListView(getContext()) {
        int lastY = 0;

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            // 正在执行动画或刷新中
            if (isAnimation || isRefreshing) {
                return super.onTouchEvent(ev);
            }

            RelativeLayout parent = (RelativeLayout) mListView.getParent();

            /**
             * RawX,RawY 相对于屏幕位置坐标
             * X,Y 相对于容器的位置坐标
             */
            int currentY = (int) ev.getRawY();// 当前的Y值
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastY = (int) ev.getRawY();// 按下时的Y值
                    break;
                case MotionEvent.ACTION_MOVE:
                    boolean isToBottom = (currentY - lastY >= 0);
                    int step = Math.abs(currentY - lastY);// 滑动的距离
                    lastY = currentY;

                    // 在顶部
                    if (isTop && mListView.getTop() >= 0) {
                        if (isToBottom && mListView.getTop() <= MAX_PULL_TOP_HEIGHT) {
                            MotionEvent event = MotionEvent.obtain(ev);
                            ev.setAction(MotionEvent.ACTION_UP);
                            super.onTouchEvent(ev);
                            pullTag = true;

                            if (mListView.getTop() > layoutHeader.getHeight()) {
                                step = step / 2;
                            }
                            if ((mListView.getTop() + step) > MAX_PULL_TOP_HEIGHT) {
                                mCurrentY = MAX_PULL_TOP_HEIGHT;
                                scrollTopTo(mCurrentY);
                            } else {
                                mCurrentY += step;
                                scrollTopTo(mCurrentY);
                            }
                        } else if (!isToBottom && mListView.getTop() > 0) {
                            MotionEvent event = MotionEvent.obtain(ev);
                            ev.setAction(MotionEvent.ACTION_UP);
                            super.onTouchEvent(ev);
                            if ((mListView.getTop() - step) < 0) {
                                mCurrentY = 0;
                                scrollTopTo(mCurrentY);
                            } else {
                                mCurrentY -= step;
                                scrollTopTo(mCurrentY);
                            }
                        } else if (!isToBottom && mListView.getTop() == 0){
                            if (!pullTag) {
                                return super.onTouchEvent(ev);
                            }
                        }
                        return true;
                    } else if (isBottom && mListView.getBottom() <= parent.getHeight()) {
                        if (!isToBottom && (parent.getHeight() - mListView.getBottom()) <= MAX_PULL_BOTTOM_HEIGHT) {
                            MotionEvent event = MotionEvent.obtain(ev);
                            ev.setAction(MotionEvent.ACTION_UP);
                            super.onTouchEvent(ev);
                            pullTag = true;
                            if(parent.getHeight()-mListView.getBottom()>layoutFooter.getHeight()){
                                step = step/2;
                            }

                            if ((mListView.getBottom() - step) < (parent.getHeight()-MAX_PULL_BOTTOM_HEIGHT)) {
                                mCurrentY = -MAX_PULL_BOTTOM_HEIGHT;
                                scrollBottomTo(mCurrentY);
                            } else {
                                mCurrentY -= step;
                                scrollBottomTo(mCurrentY);
                            }
                        } else if (isToBottom&&(mListView.getBottom()<parent.getHeight())) {
                            if ((mListView.getBottom() + step) > parent.getHeight()) {
                                mCurrentY = 0;
                                scrollBottomTo(mCurrentY);
                            } else {
                                mCurrentY += step;
                                scrollBottomTo(mCurrentY);
                            }
                        } else if (isToBottom&&mListView.getBottom()==parent.getHeight()) {
                            if (!pullTag) {
                                return super.onTouchEvent(ev);
                            }
                        }
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    pullTag = false;
                    if (mListView.getTop() > 0) {
                        if (mListView.getTop() > REFRESHING_TOP_HEIGHT) {
                            animateTopTo(layoutHeader.getMeasuredHeight());
                            isRefreshing = true;
                            if (null != mOnPullHeightChangeListener) {
                                mOnPullHeightChangeListener.onRefreshing(true);
                            }
                        } else {
                            animateTopTo(0);
                        }

                    } else if (mListView.getBottom() < parent.getHeight()) {
                        if ((parent.getHeight()-mListView.getBottom()) > REFRESHING_BOTTOM_HEIGHT) {
                            animateBottomTo(-layoutFooter.getMeasuredHeight());
                            isRefreshing = true;
                            if (null != mOnPullHeightChangeListener) {
                                mOnPullHeightChangeListener.onRefreshing(false);
                            }
                        } else {
                            animateBottomTo(0);
                        }
                    }
                    break;
            }
            return super.onTouchEvent(ev);
        }
    };

    /**
     * 滑动到指定位置
     * @param value 数值
     */
    public void scrollTopTo(int value) {
        mListView.layout(mListView.getLeft(), value, mListView.getRight(), this.getMeasuredHeight() + value);
        if (null != mOnPullHeightChangeListener) {
            mOnPullHeightChangeListener.onTopHeightChange(layoutHeader.getHeight(), value);
        }
    }

    public void scrollBottomTo(int value) {
        mListView.layout(mListView.getLeft(), value, mListView.getRight(),
                this.getMeasuredHeight() + value);
        if (null != mOnPullHeightChangeListener) {
            mOnPullHeightChangeListener.onBottomHeightChange(
                    layoutHeader.getHeight(), -value);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void animateTopTo(final int value) {
        ValueAnimator animator = ValueAnimator.ofInt(mListView.getTop(), value);
        animator.setDuration(300);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int frameValue = (int) animation.getAnimatedValue();
                mCurrentY = frameValue;
                scrollTopTo(frameValue);
                if (frameValue == value) {
                    isAnimation = false;
                }
            }
        });
        isAnimation = true;
        animator.start();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void animateBottomTo(final int value) {
        ValueAnimator animator = ValueAnimator.ofInt(mListView.getBottom() - this.getMeasuredHeight(), value);
        animator.setDuration(300);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int frameValue = (Integer) animation.getAnimatedValue();
                mCurrentY = frameValue;
                scrollBottomTo(frameValue);
                if (frameValue == value) {
                    isAnimation = false;
                }
            }
        });
        isAnimation = true;
        animator.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        REFRESHING_TOP_HEIGHT = layoutHeader.getMeasuredHeight();
        REFRESHING_BOTTOM_HEIGHT = layoutFooter.getMeasuredHeight();

        MAX_PULL_TOP_HEIGHT = this.getMeasuredHeight();
        MAX_PULL_BOTTOM_HEIGHT = this.getMeasuredHeight();
    }

    @Override
    protected void onFinishInflate() {
        mListView.setBackgroundColor(0xffffffff);
        mListView.setCacheColorHint(Color.TRANSPARENT);
        mListView.setVerticalScrollBarEnabled(false);
        mListView.setLayoutParams(new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mListView.setOnScrollListener(this);
        this.addView(mListView);

        layoutHeader = (RelativeLayout) this.findViewById(R.id.layoutHeader);
        layoutFooter = (RelativeLayout) this.findViewById(R.id.layoutFooter);
        super.onFinishInflate();
    }

    /**
     * 还原
     */
    public void pullUp() {
        isRefreshing = false;
        if (mListView.getTop() > 0) {
            animateTopTo(0);
        } else if (mListView.getBottom() < this.getHeight()) {
            animateBottomTo(0);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if(null != mOnScrollListener) {
            mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
        if (mListView.getCount() > 0) {
            if ((firstVisibleItem + visibleItemCount) == totalItemCount) {
                View lastItem = (View) mListView.getChildAt(visibleItemCount - 1);
                if (null != lastItem) {
                    /**
                     * top:直接计算
                     * left:直接计算
                     * right = left + width;
                     * bottom = top + height;
                     */
                    if (lastItem.getBottom() == mListView.getHeight()) {
                        Log.e("my", lastItem.getBottom() + "");
                        isBottom = true;
                    } else {
                        isBottom = false;
                    }
                }
            } else {
                isBottom = false;
            }
        } else {
            isBottom = false;
        }

        if (mListView.getCount() > 0) {
            if (firstVisibleItem == 0) {
                View firstItem = mListView.getChildAt(0);
                if (null != firstItem) {
                    if (firstItem.getTop() == 0) {
                        isTop = true;
                    } else {
                        isTop = false;
                    }
                }
            } else {
                isTop = false;
            }
        } else {
            isTop = true;
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if(null != mOnScrollListener){
            mOnScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    /**
     * 拉伸顶部或底部的监听事件
     */
    public interface OnPullHeightChangeListener {
        /**监听顶部被拉伸的状态*/
        public void onTopHeightChange(int headerHeight, int pullHeight);
        /**监听底部被拉伸的状态*/
        public void onBottomHeightChange(int footerHeight, int pullHeight);
        /**刷新或加载：true -> 刷新     false -> 加载*/
        public void onRefreshing(boolean isTop);
    }
}
