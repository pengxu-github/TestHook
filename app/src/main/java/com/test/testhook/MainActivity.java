package com.test.testhook;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


import java.lang.reflect.Field;

public class MainActivity extends Activity implements View.OnClickListener {

    Button btContextStart, btActivityStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Class<?> activityThreadClass = Class.forName("android.app.Activity");
            Field mInstrumentationField = activityThreadClass.getDeclaredField("mInstrumentation");
            mInstrumentationField.setAccessible(true);
            Instrumentation mInstrumentation = (Instrumentation) mInstrumentationField.get(this);
            Instrumentation evilInstrumentation = new EvilInstrumentation(mInstrumentation);
            mInstrumentationField.set(this, evilInstrumentation);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setContentView(R.layout.activity_main);

        btContextStart = findViewById(R.id.context_start);
        btActivityStart = findViewById(R.id.activity_start);
        btContextStart.setOnClickListener(this);
        btActivityStart.setOnClickListener(this);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        try {
            // 在这里进行Hook
            HookHelper.attachContext();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.context_start:
                contextStart();
                break;
            case R.id.activity_start:
                activityStart();
                break;
        }
    }

    private void contextStart() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse("http://www.baidu.com"));

        // 注意这里使用的ApplicationContext 启动的Activity
        // 因为Activity对象的startActivity使用的并不是ContextImpl的mInstrumentation
        // 而是自己的mInstrumentation, 如果你需要这样, 可以自己Hook
        // 比较简单, 直接替换这个Activity的此字段即可.
        getApplicationContext().startActivity(intent);
    }

    private void activityStart() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse("http://www.baidu.com"));

        startActivity(intent);
    }
}
