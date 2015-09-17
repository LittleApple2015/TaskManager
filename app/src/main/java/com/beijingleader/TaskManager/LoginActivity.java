package com.beijingleader.TaskManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends Activity implements OnClickListener,
        OnItemClickListener, OnDismissListener {
    protected static final String TAG = "LoginActivity";
    private LinearLayout mLoginLinearLayout; // 登录内容的容器
    private LinearLayout mUserNameLinearLayout; // 将下拉弹出窗口在此容器下方显示
    private Animation mTranslate; // 位移动画
    private Dialog mLoginDlg; // 显示正在登录的Dialog
    private EditText mNameEditText; // 登录Name编辑框
    private EditText mPwdEditText; // 登录密码编辑框
    private ImageView mMoreUser; // 下拉图标
    private Button mLoginButton; // 登录按钮
    private ImageView mLoginMoreUserView; // 弹出下拉弹出窗的按钮
    private String mNameString;
    private String mPwdString;
    private ArrayList<User> mUsers; // 用户列表
    private ListView mUserNameListView; // 下拉弹出窗显示的ListView对象
    private UserAdapter mAdapter; // ListView的监听器
    private PopupWindow mPop; // 下拉弹出窗
    private BroadCastUtils networkBroadcast = new BroadCastUtils();
    private String mUserId;//服务器返回的用户ID
    private String mLoginResult;//服务器返回的登陆结果
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        registerNetworkReceiver();//注册网络状态改变的广播
        initView();
        setListener();

        mLoginLinearLayout.startAnimation(mTranslate); // Y轴水平移动

		/* 获取已经保存好的用户密码 */
        mUsers = Utils.getUserList(LoginActivity.this);

        if (mUsers.size() > 0) {
            /* 将列表中的第一个user显示在编辑框 */
            mNameEditText.setText(mUsers.get(0).getName());
            mPwdEditText.setText(mUsers.get(0).getPwd());
            Log.d(TAG, "mUsers size is > 0 ");
        } else {
            Log.d(TAG, "mUsers size is < 0 ");
        }

        LinearLayout parent = (LinearLayout) getLayoutInflater().inflate(
                R.layout.userifo_listview, null);
        mUserNameListView = (ListView) parent.findViewById(android.R.id.list);
        parent.removeView(mUserNameListView); // 必须脱离父子关系,不然会报错
        mUserNameListView.setOnItemClickListener(this); // 设置点击事
        mAdapter = new UserAdapter(mUsers);
        mUserNameListView.setAdapter(mAdapter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterNetworkReceiver();//卸载广播  网络状态改变
    }


    //注册广播  网络状态改变
    private void registerNetworkReceiver() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        this.registerReceiver(networkBroadcast, filter);
    }

    //卸载广播  网络状态改变
    private void unRegisterNetworkReceiver() {
        this.unregisterReceiver(networkBroadcast);
    }

    /* ListView的适配器 */
    class UserAdapter extends ArrayAdapter<User> {

        public UserAdapter(ArrayList<User> users) {
            super(LoginActivity.this, 0, users);
        }

        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(
                        R.layout.listview_item, null);
            }

            TextView userNameText = (TextView) convertView
                    .findViewById(R.id.listview_username);
            userNameText.setText(getItem(position).getName());

            ImageView deleteUser = (ImageView) convertView
                    .findViewById(R.id.login_delete_user);
            deleteUser.setOnClickListener(new OnClickListener() {
                // 点击删除deleteUser时,在mUsers中删除选中的元素
                @Override
                public void onClick(View v) {

                    if (mNameString.equals(getItem(position).getName())) {
                        // 如果要删除的用户name和name编辑框当前值相等，则清空
                        mNameString = "";
                        mPwdString = "";
                        mNameEditText.setText(mNameString);
                        mPwdEditText.setText(mPwdString);
                    }
                    mUsers.remove(getItem(position));
                    mAdapter.notifyDataSetChanged(); // 更新ListView
                }
            });
            return convertView;
        }

    }

    private void setListener() {
        mNameEditText.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                mNameString = s.toString();
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });
        mPwdEditText.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                mPwdString = s.toString();
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });
        mLoginButton.setOnClickListener(this);
        mLoginMoreUserView.setOnClickListener(this);
    }

    private void initView() {
        mNameEditText = (EditText) findViewById(R.id.login_edtName);
        mPwdEditText = (EditText) findViewById(R.id.login_edtPwd);
        mMoreUser = (ImageView) findViewById(R.id.login_more_user);
        mLoginButton = (Button) findViewById(R.id.login_btnLogin);
        mLoginMoreUserView = (ImageView) findViewById(R.id.login_more_user);
        mLoginLinearLayout = (LinearLayout) findViewById(R.id.login_linearLayout);
        mUserNameLinearLayout = (LinearLayout) findViewById(R.id.userName_LinearLayout);
        mTranslate = AnimationUtils.loadAnimation(this, R.anim.my_translate); // 初始化动画对象
        initLoginingDlg();
    }

    public void initPop() {
        int width = mUserNameLinearLayout.getWidth() - 4;
        int height = LayoutParams.WRAP_CONTENT;
        mPop = new PopupWindow(mUserNameListView, width, height, true);
        mPop.setOnDismissListener(this);// 设置弹出窗口消失时监听器

        // 注意要加这句代码，点击弹出窗口其它区域才会让窗口消失
        mPop.setBackgroundDrawable(new ColorDrawable(0xffffffff));

    }

    /* 初始化正在登录对话框 */
    private void initLoginingDlg() {

        mLoginDlg = new Dialog(this, R.style.loginingDlg);
        mLoginDlg.setContentView(R.layout.logining_dlg);

        Window window = mLoginDlg.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        // 获取和mLoginingDlg关联的当前窗口的属性，从而设置它在屏幕中显示的位置

        // 获取屏幕的高宽
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int cxScreen = dm.widthPixels;
        int cyScreen = dm.heightPixels;

        int height = (int) getResources().getDimension(
                R.dimen.loginingdlg_height);// 高42dp
        int lrMargin = (int) getResources().getDimension(
                R.dimen.loginingdlg_lr_margin); // 左右边沿10dp
        int topMargin = (int) getResources().getDimension(
                R.dimen.loginingdlg_top_margin); // 上沿20dp

        params.y = (-(cyScreen - height) / 2) + topMargin; // -199
        /* 对话框默认位置在屏幕中心,所以x,y表示此控件到"屏幕中心"的偏移量 */

        params.width = cxScreen;
        params.height = height;
        // width,height表示mLoginingDlg的实际大小

        mLoginDlg.setCanceledOnTouchOutside(true); // 设置点击Dialog外部任意区域关闭Dialog
    }

    /* 显示正在登录对话框 */
    private void showLoginingDlg() {
        if (mLoginDlg != null)
            mLoginDlg.show();
    }

    /* 关闭正在登录对话框 */
    private void closeLoginingDlg() {
        if (mLoginDlg != null && mLoginDlg.isShowing())
            mLoginDlg.dismiss();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_btnLogin:
                // 启动登录
                showLoginingDlg(); // 显示"正在登录"对话框
                Log.i(TAG, mNameString + "  " + mPwdString);
                if (mNameString == null || mNameString.equals("")) { // 账号为空时
                    Toast.makeText(LoginActivity.this, "请输入账号", Toast.LENGTH_SHORT)
                            .show();
                } else if (mPwdString == null || mPwdString.equals("")) {// 密码为空时
                    Toast.makeText(LoginActivity.this, "请输入密码", Toast.LENGTH_SHORT)
                            .show();
                } else {// 账号和密码都不为空时
                    LoginAsyncTask loginAsyncTask = new LoginAsyncTask();
                    String loginUrl = "http://219.143.38.173:8080/TaskSchedule/UserLogin";
                    loginAsyncTask.execute(loginUrl);//
                }
                break;
            case R.id.login_more_user: // 当点击下拉栏
                if (mPop == null) {
                    initPop();
                }
                if (!mPop.isShowing() && mUsers.size() > 0) {
                    // Log.i(TAG, "切换为角向上图标");
                    mMoreUser.setImageResource(R.drawable.login_more_down); // 切换图标
                    mPop.showAsDropDown(mUserNameLinearLayout, 2, 1); // 显示弹出窗口
                }
                break;
            default:
                break;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        mNameEditText.setText(mUsers.get(position).getName());
        mPwdEditText.setText(mUsers.get(position).getPwd());
        mPop.dismiss();
    }

    /* PopupWindow对象dismiss时的事件 */
    @Override
    public void onDismiss() {
        // Log.i(TAG, "切换为角向下图标");
        mMoreUser.setImageResource(R.drawable.login_more_up);
    }

    /* 退出此Activity时保存users */
    @Override
    public void onPause() {
        super.onPause();
        try {
            Utils.saveUserList(LoginActivity.this, mUsers);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class LoginAsyncTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            Log.d(TAG, "doInBackground");
            JSONObject UserData = new JSONObject();
            try {
                UserData.put("user_name", mNameString);
                UserData.put("user_pwd", mPwdString);
                UserData.put("user_action", "login");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String sUserData = String.valueOf(UserData);//将userjson转成字符串
            Log.d(TAG + "longin user json", sUserData);//打印登录用户的JSON内容

            StringBuilder sLoginReturnResult = null;
            try {
                URL loginUrl = new URL(params[0]);//用execute的参数构造URL
                HttpURLConnection connection = (HttpURLConnection) loginUrl.openConnection();
                connection.setConnectTimeout(5000);
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("POST");
                //  设置contentType
                connection.setRequestProperty("Content-Type", "application/json");
                // 设置User-Agent: Fiddler
                connection.setRequestProperty("ser-Agent", "Fiddler");
                connection.setRequestProperty("Charset", "utf-8");//设置字符集
                connection.connect();

                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(sUserData.getBytes());//将sUserData内容发送给服务器
                outputStream.close();
                int code = connection.getResponseCode();//获取http返回码
                connection.getConnectTimeout();
                if (code == 200) {
                    Log.d(TAG + " code:", String.valueOf(code));
                } else {
                    Log.d(TAG + " http failed code:", String.valueOf(code));
                    //Toast.makeText(LoginAsyncTask.this, "连接服务器超时，请重新连接", Toast.LENGTH_SHORT).show();
                }

                sLoginReturnResult = new StringBuilder();
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    sLoginReturnResult.append(line);//获取服务器返回的字符串
                }
                Log.d(TAG + "asyn return:", sLoginReturnResult.toString());//

                JSONObject loginReturnResult = new JSONObject(sLoginReturnResult.toString());//将字符串转成json
                mUserId = loginReturnResult.getString("user_id");//返回user_name对应的user_id
                mLoginResult = loginReturnResult.getString("result");//返回登录结果
                Log.d(TAG + " Login userid:", mUserId);
                if (mLoginResult.equals("欢迎登录")) {
                    Log.d(TAG + " Login result:", "登陆成功");

                    boolean mIsSave = true;
                    try {
                        Log.i(TAG, "保存用户列表");
                        for (User user : mUsers) { // 判断本地文档是否有此ID用户
                            if (user.getName().equals(mNameString)) {
                                mIsSave = false;
                                break;
                            }
                        }
                        if (mIsSave) { // 将新用户加入users
                            User user = new User(mNameString, mPwdString);
                            mUsers.add(user);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Intent mStartTaskActivity = new Intent(LoginActivity.this, TaskActivity.class);
                    mStartTaskActivity.putExtra("Name",mNameString);
                    mStartTaskActivity.putExtra("Pwd",mPwdString);
                    mStartTaskActivity.putExtra("UserId",mUserId);//启动TaskActivity 并将用户名、密码、用户ID传递过去
                    startActivity(mStartTaskActivity);
                    closeLoginingDlg();// 关闭对话框
                    //   Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                    finish();
                    //Toast.makeText(LoginAsyncTask.this, result, Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG + " Login result:", mLoginResult);
                    closeLoginingDlg();// 关闭对话框
                    finish();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                //  Toast.makeText(LoginAsyncTask.this, "连接服务器超时，请重试", Toast.LENGTH_SHORT).show();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // publishProgress();
            //在doInBackground中调用publishProgress可以更新进度,将更新的进度值传递给onProgressUpdate
            return sLoginReturnResult.toString();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, "onPreExecute");
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "onPostExecute");
            Log.d(TAG + " asyn return:", s);
            JSONObject loginReturnResult = null;//将字符串转成json
            try {
                loginReturnResult = new JSONObject(s);
                String result = loginReturnResult.getString("result");//返回登录结果
                if ("欢迎登录".equals(result)) {
                    Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, result, Toast.LENGTH_SHORT).show();//显示登录错误信息
                    closeLoginingDlg();// 关闭对话框
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
