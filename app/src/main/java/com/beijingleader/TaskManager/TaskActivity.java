package com.beijingleader.TaskManager;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

public class TaskActivity extends Activity implements View.OnClickListener {
    private ViewPager mViewPager;//内容 viewPage
    private PagerAdapter mPageAdapter;
    private ArrayList<View> mViews = new ArrayList<View>();

    private LinearLayout mTask_linearLayout;//任务
    private LinearLayout mWork_linearLayout;//工作
    private LinearLayout mColleague_linearLayout;//同事
    private LinearLayout mSetting_linearLayout;//设置

    private ImageButton mTask_img;
    private ImageButton mWork_img;
    private ImageButton mColleague_img;
    private ImageButton mSetting_img;

    private TextView mTop_Action_Bar_TextView;
    private String mLoginName;
    private String mLoginPwd;
    private String mUserId;

    //temp textview display task detail
    private TextView mTaskStateTextView;
    private TextView mTaskIdTextView;
    private TextView mTaskTimeTextView;
    private TextView mTaskContentTextView;
    private Button mGainTaskListBtn;

    StringBuilder sTaskList;//任务列表
    private static final String mTaskUrl = "http://219.143.38.173:8080/TaskSchedule/UserTask";
    private static final String TAG = "TaskActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);//去掉标题栏
        setContentView(R.layout.activity_task);

        Intent intent = getIntent();
        mLoginName = intent.getStringExtra("Name");
        mLoginPwd = intent.getStringExtra("Pwd");//获取到登录用户名和登录密码
        mUserId = intent.getStringExtra("UserId");//获取到登录用户名、登录密码、用户ID
        Log.d(TAG, mLoginName);
        Log.d(TAG, mLoginPwd);
        Log.d(TAG, mUserId);
        initView();
        initEvents();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initView() {
        mViewPager = (ViewPager) findViewById(R.id.content_view_page);
        //Tabs
        mTask_linearLayout = (LinearLayout) findViewById(R.id.task_linearLayout);
        mColleague_linearLayout = (LinearLayout) findViewById(R.id.colleague_linearLayout);
        mWork_linearLayout = (LinearLayout) findViewById(R.id.work_linearLayout);
        mSetting_linearLayout = (LinearLayout) findViewById(R.id.setting_linearLayout);
        //ImageButton
        mTask_img = (ImageButton) findViewById(R.id.task_img);
        mWork_img = (ImageButton) findViewById(R.id.work_img);
        mColleague_img = (ImageButton) findViewById(R.id.colleague_img);
        mSetting_img = (ImageButton) findViewById(R.id.setting_img);

        mTop_Action_Bar_TextView = (TextView) findViewById(R.id.top_action_bar_textview);
        LayoutInflater mInflater = LayoutInflater.from(this);
        View mTaskTab = mInflater.inflate(R.layout.task_tab, null);//将布局转化成View
        View mWorkTab = mInflater.inflate(R.layout.work_tab, null);//将布局转化成View
        View mColleagueTab = mInflater.inflate(R.layout.colleague_tab, null);//将布局转化成View
        View mSettingTab = mInflater.inflate(R.layout.setting_tab, null);//将布局转化成View
        mViews.add(mTaskTab);
        mViews.add(mWorkTab);
        mViews.add(mColleagueTab);
        mViews.add(mSettingTab);//添加到mViews中

        //temp display tasklist infomation
        mTaskIdTextView = (TextView) mTaskTab.findViewById(R.id.task_id_textview);
        mTaskStateTextView = (TextView) mTaskTab.findViewById(R.id.task_state_textview);
        mTaskContentTextView = (TextView) mTaskTab.findViewById(R.id.task_text_textview);
        mTaskTimeTextView = (TextView) mTaskTab.findViewById(R.id.task_time_textview);
        mGainTaskListBtn = (Button) mTaskTab.findViewById(R.id.gain_tasklist_btn);
        mPageAdapter = new PagerAdapter() {
                @Override
                public int getCount() {
                    return mViews.size();
                }

                @Override
                public boolean isViewFromObject(View view, Object o) {
                    return view == o;
                }

                @Override
                public void destroyItem(ViewGroup container, int position, Object object) {
                    //销毁view
                    container.removeView(mViews.get(position));

                }

                @Override
                public Object instantiateItem(ViewGroup container, int position) {
                    //实例化view
                    View view = mViews.get(position);
                    container.addView(view);
                    return view;
                }
            };
        mViewPager.setAdapter(mPageAdapter);
    }
    private void initEvents() {
        mTask_img.setOnClickListener(this);
        mWork_img.setOnClickListener(this);
        mColleague_img.setOnClickListener(this);
        mSetting_img.setOnClickListener(this);
        mGainTaskListBtn.setOnClickListener(this);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int i) {
                int currentItem = mViewPager.getCurrentItem();
                resetImage();//将所有图片变暗
                switch (currentItem) {
                    case 0:
                        mTask_img.setImageResource(R.drawable.tab_task_pressed);
                        mTop_Action_Bar_TextView.setText("任务");
                        break;
                    case 1:
                        mWork_img.setImageResource(R.drawable.tab_work_pressed);
                        mTop_Action_Bar_TextView.setText("工作");
                        break;
                    case 2:
                        mColleague_img.setImageResource(R.drawable.tab_colleague_pressed);
                        mTop_Action_Bar_TextView.setText("同事");
                        break;
                    case 3:
                        mSetting_img.setImageResource(R.drawable.tab_settings_pressed);
                        mTop_Action_Bar_TextView.setText("设置");
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        resetImage();//将所有图片变暗
        switch (v.getId()) {
            case R.id.task_img:
                mViewPager.setCurrentItem(0);//任务
                mTask_img.setImageResource(R.drawable.tab_task_pressed);
                mTop_Action_Bar_TextView.setText("任务");
                break;
            case R.id.work_img:
                mViewPager.setCurrentItem(1);//工作
                mWork_img.setImageResource(R.drawable.tab_work_pressed);
                mTop_Action_Bar_TextView.setText("工作");
                break;
            case R.id.colleague_img:
                mViewPager.setCurrentItem(2);//同事
                mColleague_img.setImageResource(R.drawable.tab_colleague_pressed);
                mTop_Action_Bar_TextView.setText("同事");
                break;
            case R.id.setting_img:
                mViewPager.setCurrentItem(3);//设置
                mSetting_img.setImageResource(R.drawable.tab_settings_pressed);
                mTop_Action_Bar_TextView.setText("设置");
                break;
            case R.id.gain_tasklist_btn:
                TaskAsync mTaskAsync = new TaskAsync();
                mTaskAsync.execute(mTaskUrl);
                break;
        }
    }

    //将所有图片变暗
    private void resetImage() {
        mTask_img.setImageResource(R.drawable.tab_task_normal);
        mWork_img.setImageResource(R.drawable.tab_work_normal);
        mColleague_img.setImageResource(R.drawable.tab_colleague_normal);
        mSetting_img.setImageResource(R.drawable.tab_settings_normal);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_task, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class TaskAsync extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("user_id", mUserId);
                String user_data = String.valueOf(jsonObject);

                URL url = new URL(params[0]);

                HttpURLConnection connection2 = (HttpURLConnection) url.openConnection();
                connection2.setConnectTimeout(5000);
                connection2.setDoOutput(true);
                connection2.setDoInput(true);
                connection2.setRequestMethod("POST");
                //  设置contentType
                connection2.setRequestProperty("Content-Type", "application/json");
                // 设置User-Agent: Fiddler
                connection2.setRequestProperty("ser-Agent", "Fiddler");
                connection2.setRequestProperty("Charset", "utf-8");//设置字符集
                connection2.connect();

                OutputStream outputStream = connection2.getOutputStream();
                outputStream.write(user_data.getBytes());
                outputStream.close();
                int code = connection2.getResponseCode();
                if (code == 200) {
                    Log.d(TAG + " code:", String.valueOf(code));
                }
                sTaskList = new StringBuilder();
                BufferedReader in = new BufferedReader(new InputStreamReader(connection2.getInputStream()));
                String line2;

                while ((line2 = in.readLine()) != null) {
                    sTaskList.append(line2);
                }
                Log.d(TAG, sTaskList.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return sTaskList.toString();
        }

        @Override
        protected void onPostExecute(final String s) {
            super.onPostExecute(s);
            JSONArray mTaskJsonArray = null;
            try {
                mTaskJsonArray = new JSONArray(s);
                for (int i = 0; i < mTaskJsonArray.length(); i++) {
                    JSONObject mTaskJson = (JSONObject) mTaskJsonArray.get(i);
                    String mTaskState = String.valueOf(mTaskJson.get("task_state"));
                    String mTaskId = String.valueOf(mTaskJson.get("task_id"));
                    String mTaskTime = String.valueOf(mTaskJson.get("task_time"));
                    String mTaskContent = String.valueOf(mTaskJson.get("task_text"));
                    Log.d(TAG, mTaskState);
                    Log.d(TAG, mTaskId);
                    Log.d(TAG, mTaskTime);
                    Log.d(TAG, mTaskContent);
                    mTaskStateTextView.setText("任务状态：" + mTaskState);
                    mTaskIdTextView.setText("任务编号：" +mTaskId);
                    mTaskTimeTextView.setText("任务创建时间："+mTaskTime);
                    mTaskContentTextView.setText("任务内容："+mTaskContent);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
