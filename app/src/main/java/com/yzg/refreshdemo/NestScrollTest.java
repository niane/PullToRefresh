package com.yzg.refreshdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.yzg.pulltorefresh.PullToRefreshLayout;

/**
 * Created by yzg on 2017/2/13.
 */

public class NestScrollTest extends AppCompatActivity {
    PullToRefreshLayout pullRefresh;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_pull_refresh);

        pullRefresh = (PullToRefreshLayout) findViewById(R.id.pull_refresh);

        pullRefresh.setOnRefreshListener(new PullToRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pullRefresh.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pullRefresh.finish();
                    }
                }, 3000);
            }
        });

//        pullRefresh.start();
    }
}
