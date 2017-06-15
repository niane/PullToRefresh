package com.yzg.refreshdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.yzg.pulltorefresh.PullToRefreshLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yzg on 2017/4/12.
 */

public class MainActivity extends AppCompatActivity {
    ListView indexList;
    PullToRefreshLayout pullRefresh;

    private SimpleAdapter mAdapter;
    private ArrayList<Map<String, Object>> mList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        indexList = (ListView) findViewById(R.id.index_list);
        pullRefresh = (PullToRefreshLayout) findViewById(R.id.pull_refresh);

        initActivityies();
        mAdapter = new SimpleAdapter(this, mList, android.R.layout.simple_list_item_1, new String[]{"name"}, new int[]{android.R.id.text1});
        indexList.setAdapter(mAdapter);
        indexList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((Activities) mList.get(position).get("activity")).launch(MainActivity.this);
            }
        });
        pullRefresh.setOnRefreshListener(new PullToRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pullRefresh.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pullRefresh.finish();
                    }
                }, 5000);
            }
        });

        pullRefresh.postDelayed(new Runnable() {
            @Override
            public void run() {
                pullRefresh.start();
            }
        }, 50);
    }

    private void initActivityies() {
        Activities[] activities = Activities.values();
        for (int i = 0; i < activities.length; i++) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("name", activities[i].name);
            map.put("activity", activities[i]);
            mList.add(map);
        }
    }

    enum Activities {
        PULL_REFRESH(NestScrollTest.class, "NestScrollTest");

        private Class cls;
        private String name;

        Activities(Class<?> cls, String name) {
            this.cls = cls;
            this.name = name;
        }

        public void launch(Context context) {
            Intent intent = new Intent(context, cls);
            context.startActivity(intent);
        }
    }
}
