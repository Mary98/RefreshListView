package com.mary.refreshlistview;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mary.refreshlistview.widget.RefreshListView;

public class MainActivity extends Activity {

    final String TAG = "MainActivity";
    static String[] adapterData = new String[] { "A", "B", "C", "D", "E", "F",
            "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
            "S", "T", "U", "V", "W", "X", "Y", "Z","A", "B", "C", "D", "E", "F",
            "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
            "S", "T", "U", "V", "W", "X", "Y", "Z" };

    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        initViews();
    }

    private void initViews() {
        final RefreshListView refreshListView = (RefreshListView) this.findViewById(R.id.id_rlv_Item);
        final ProgressBar progressView = (ProgressBar) this
                .findViewById(R.id.progressView);
        final ProgressBar eyeView = (ProgressBar) this.findViewById(R.id.eyeView);
        refreshListView.getListView().setAdapter(mAdapter);
        refreshListView.setOnPullHeightChangeListener(new RefreshListView.OnPullHeightChangeListener() {
            @Override
            public void onTopHeightChange(int headerHeight, int pullHeight) {
                float progress = (float) pullHeight
                        / (float) headerHeight;

                if(progress<0.5){
                    progress = 0.0f;
                }else{
                    progress = (progress-0.5f)/0.5f;
                }


                if (progress > 1.0f) {
                    progress = 1.0f;
                }

                if (!refreshListView.isRefreshing()) {
                    eyeView.setProgress((int) progress);
                }
            }

            @Override
            public void onBottomHeightChange(int footerHeight, int pullHeight) {
                float progress = (float) pullHeight
                        / (float) footerHeight;

                if(progress<0.5){
                    progress = 0.0f;
                }else{
                    progress = (progress-0.5f)/0.5f;
                }

                if (progress > 1.0f) {
                    progress = 1.0f;
                }

                if (!refreshListView.isRefreshing()) {
                    progressView.setProgress((int) progress);
                }
            }

            @Override
            public void onRefreshing(boolean isTop) {
                /*if (isTop) {
                    eyeView.startAnimate();
                } else {
                    progressView.startAnimate();
                }*/

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        refreshListView.pullUp();
                       /* if (isTop) {
                            eyeView.stopAnimate();
                        } else {
                            progressView.stopAnimate();
                        }*/
                    }

                }, 10 * 1000);
            }
        });
    }

    private BaseAdapter mAdapter = new BaseAdapter(){

        @Override
        public int getCount() {
            return adapterData.length;
        }

        @Override
        public Object getItem(int position) {
            return adapterData[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView = new TextView(mContext);
            textView.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT,dp2px(mContext,50)));
            textView.setText(adapterData[position]);
            textView.setTextSize(20);
            textView.setTextColor(0xff000000);
            textView.setGravity(Gravity.LEFT| Gravity.CENTER_VERTICAL);
            textView.setPadding(50, 0, 0, 0);

            return textView;
        }

    };

    public static int dp2px(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

}
