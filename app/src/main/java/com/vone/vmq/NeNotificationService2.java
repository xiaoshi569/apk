package com.vone.vmq;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NeNotificationService2  extends NotificationListenerService {
    private String TAG = "NeNotificationService2";
    private String host = "";
    private String key = "";
    private Thread newThread = null;
    private PowerManager.WakeLock mWakeLock = null;

    // 添加通知去重机制
    private java.util.Set<String> processedNotifications = new java.util.HashSet<>();
    private long lastProcessTime = 0;
    private static final long MIN_PROCESS_INTERVAL = 2000; // 2秒内不重复处理相同通知


    //申请设备电源锁
    @SuppressLint("InvalidWakeLockTag")
    public void acquireWakeLock(Context context) {
        if (null == mWakeLock)
        {
            PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE, "WakeLock");
            if (null != mWakeLock)
            {
                mWakeLock.acquire();
            }
        }
    }
    //释放设备电源锁
    public void releaseWakeLock() {
        if (null != mWakeLock)
        {
            mWakeLock.release();
            mWakeLock = null;
        }
    }
    //心跳进程
    public void initAppHeart(){
        Log.d(TAG, "开始启动心跳线程");
        if (newThread!=null){
            return;
        }
        acquireWakeLock(this);
        newThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "心跳线程启动！");
                while (true){

                    SharedPreferences read = getSharedPreferences("vone", MODE_PRIVATE);
                    host = read.getString("host", "");
                    key = read.getString("key", "");

                    //这里写入子线程需要做的工作
                    String t = String.valueOf(new Date().getTime());
                    String sign = md5(t+key);


                    OkHttpClient okHttpClient = new OkHttpClient();
                    Request request = new Request.Builder().url("http://"+host+"/appHeart?t="+t+"&sign="+sign).method("GET",null).build();
                    Call call = okHttpClient.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            final String error = e.getMessage();
                            Handler handlerThree=new Handler(Looper.getMainLooper());
                            handlerThree.post(new Runnable(){
                                public void run(){
                                    Toast.makeText(getApplicationContext() ,"心跳状态错误，请检查配置是否正确!"+error,Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                        //请求成功执行的方法
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            Log.d(TAG, "onResponse heard: "+response.body().string());
                        }
                    });
                    try {
                        Thread.sleep(30*1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        newThread.start(); //启动线程
    }




    //当收到一条消息的时候回调，sbn是收到的消息
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.d(TAG, "接受到通知消息");

        // 获取通知的基本信息
        Notification notification = sbn.getNotification();
        String pkg = sbn.getPackageName();
        long currentTime = System.currentTimeMillis();

        if (notification != null) {
            Bundle extras = notification.extras;
            if (extras != null) {
                String title = extras.getString(NotificationCompat.EXTRA_TITLE, "");
                String content = extras.getString(NotificationCompat.EXTRA_TEXT, "");

                // 创建通知唯一标识符
                String notificationId = pkg + "_" + title + "_" + content;
                String notificationHash = String.valueOf(notificationId.hashCode());

                // 检查是否为重复通知
                if (processedNotifications.contains(notificationHash)) {
                    Log.d(TAG, "跳过重复通知: " + notificationHash);
                    addAppLog("⚠️ 跳过重复通知");
                    return;
                }

                // 检查时间间隔（防止短时间内重复处理）
                if (currentTime - lastProcessTime < MIN_PROCESS_INTERVAL) {
                    Log.d(TAG, "处理间隔过短，跳过通知");
                    addAppLog("⚠️ 处理间隔过短，跳过");
                    return;
                }

                // 添加到已处理列表
                processedNotifications.add(notificationHash);
                lastProcessTime = currentTime;

                // 限制已处理通知列表大小（避免内存泄漏）
                if (processedNotifications.size() > 100) {
                    processedNotifications.clear();
                    addAppLog("🔄 清理通知缓存");
                }

                addAppLog("✅ 处理新通知 ID: " + notificationHash.substring(0, 8));

                SharedPreferences read = getSharedPreferences("vone", MODE_PRIVATE);
                host = read.getString("host", "");
                key = read.getString("key", "");
                Log.d(TAG, "**********************");
                Log.d(TAG, "包名:" + pkg);
                Log.d(TAG, "标题:" + title);
                Log.d(TAG, "内容:" + content);
                Log.d(TAG, "通知时间:" + new java.util.Date(sbn.getPostTime()));
                Log.d(TAG, "**********************");

                // 记录通知到应用日志（包含时间戳）
                java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault());
                String notificationTime = timeFormat.format(new java.util.Date(sbn.getPostTime()));
                addAppLog("📱 [" + notificationTime + "] 通知详情 - 包名: " + pkg);
                addAppLog("📝 标题: " + title);
                addAppLog("📄 内容: " + content);

                // 专门记录支付宝相关的所有通知，便于调试
                if (pkg.contains("alipay") || pkg.contains("Alipay") || title.contains("支付宝") || content.contains("支付宝")) {
                    Log.d(TAG, "=== 支付宝相关通知 ===");
                    Log.d(TAG, "完整包名: " + pkg);
                    Log.d(TAG, "完整标题: " + title);
                    Log.d(TAG, "完整内容: " + content);
                    Log.d(TAG, "==================");
                    addAppLog("🔍 发现支付宝相关通知！包名: " + pkg);
                }


                // 支持多个支付宝包名
                if (pkg.equals("com.eg.android.AlipayGphone") || pkg.equals("com.alipay.android.app")){
                    addAppLog("✅ 检测到支付宝通知 - 包名: " + pkg);
                    if (content!=null && !content.equals("")) {
                        Log.d(TAG, "支付宝通知 - 标题: " + title + ", 内容: " + content);
                        addAppLog("支付宝通知内容: " + content);

                        // 扩展支付宝收款关键词匹配
                        if (content.indexOf("通过扫码向你付款")!=-1 ||
                            content.indexOf("成功收款")!=-1 ||
                            content.indexOf("收钱码收款")!=-1 ||
                            content.indexOf("向你付款")!=-1 ||
                            content.indexOf("转账")!=-1 ||
                            content.indexOf("收款成功")!=-1 ||
                            content.indexOf("到账")!=-1 ||
                            (title!=null && (title.indexOf("收款")!=-1 || title.indexOf("到账")!=-1))){

                            addAppLog("🎯 匹配到支付宝收款关键词！");

                            // 先尝试从内容中提取金额
                            String money = getMoney(content);

                            // 如果内容中没有金额，尝试从标题中提取
                            if (money == null && title != null) {
                                money = getMoney(title);
                                addAppLog("从标题中尝试提取金额: " + title);
                            }

                            if (money!=null){
                                Log.d(TAG, "onAccessibilityEvent: 匹配成功： 支付宝 到账 " + money);
                                addAppLog("💰 成功提取金额: " + money + "元，正在回调服务端...");
                                appPush(2, Double.valueOf(money));
                            }else {
                                final String finalContent = content; // 声明为final变量
                                final String finalTitle = title; // 声明为final变量
                                addAppLog("❌ 匹配到收款通知但无法提取金额！");
                                addAppLog("标题: " + title);
                                addAppLog("内容: " + content);
                                Handler handlerThree=new Handler(Looper.getMainLooper());
                                handlerThree.post(new Runnable(){
                                    public void run(){
                                        Toast.makeText(getApplicationContext() ,"监听到支付宝消息但未匹配到金额！标题：" + finalTitle + " 内容：" + finalContent,Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        } else {
                            // 记录所有支付宝通知，便于调试
                            Log.d(TAG, "支付宝通知未匹配收款关键词: " + content);
                            addAppLog("⚠️ 支付宝通知未匹配收款关键词: " + content);
                        }

                    }

                }else if(pkg.equals("com.tencent.mm")){
                    addAppLog("✅ 检测到微信通知");
                    if (content!=null && !content.equals("")){
                        addAppLog("微信通知内容: " + content);
                        if (title.equals("微信支付") || title.equals("微信收款助手") || title.equals("微信收款商业版")){
                            addAppLog("🎯 匹配到微信收款通知！");

                            // 先尝试从内容中提取金额
                            String money = getMoney(content);

                            // 如果内容中没有金额，尝试从标题中提取
                            if (money == null && title != null) {
                                money = getMoney(title);
                                addAppLog("从微信标题中尝试提取金额: " + title);
                            }

                            if (money!=null){
                                Log.d(TAG, "onAccessibilityEvent: 匹配成功： 微信到账 "+ money);
                                addAppLog("💰 成功提取金额: " + money + "元，正在回调服务端...");
                                appPush(1,Double.valueOf(money));
                            }else{
                                addAppLog("❌ 匹配到微信收款通知但无法提取金额！");
                                addAppLog("微信标题: " + title);
                                addAppLog("微信内容: " + content);
                                Handler handlerThree=new Handler(Looper.getMainLooper());
                                handlerThree.post(new Runnable(){
                                    public void run(){
                                        Toast.makeText(getApplicationContext() ,"监听到微信消息但未匹配到金额！",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } else {
                            addAppLog("⚠️ 微信通知标题不匹配: " + title);
                        }
                    }

                }else if(pkg.equals("com.vone.qrcode")){

                    if (content.equals("这是一条测试推送信息，如果程序正常，则会提示监听权限正常")){
                        Handler handlerThree=new Handler(Looper.getMainLooper());
                        handlerThree.post(new Runnable(){
                            public void run(){
                                Toast.makeText(getApplicationContext() ,"监听正常，如无法正常回调请联系作者反馈！",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        }
    }
    //当移除一条消息的时候回调，sbn是被移除的消息
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

    }
    //当连接成功时调用，一般在开启监听后会回调一次该方法
    @Override
    public void onListenerConnected() {
        //开启心跳线程
        initAppHeart();

        Handler handlerThree = new Handler(Looper.getMainLooper());
        handlerThree.post(new Runnable(){
            public void run(){
                Toast.makeText(getApplicationContext() ,"监听服务开启成功！",Toast.LENGTH_SHORT).show();
            }
        });


    }





    public void appPush(int type,double price){
        SharedPreferences read = getSharedPreferences("vone", MODE_PRIVATE);
        host = read.getString("host", "");
        key = read.getString("key", "");

        Log.d(TAG, "onResponse  push: 开始:"+type+"  "+price);

        String t = String.valueOf(new Date().getTime());
        String sign = md5(type+""+ price + t + key);
        String url = "http://"+host+"/appPush?t="+t+"&type="+type+"&price="+price+"&sign="+sign;
        Log.d(TAG, "onResponse  push: 开始:"+url);

        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(url).method("GET",null).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onResponse  push: 请求失败");
                addAppLog("❌ 回调服务端失败: " + e.getMessage());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d(TAG, "onResponse  push: "+responseBody);
                addAppLog("✅ 回调服务端成功: " + responseBody);
            }
        });
    }

    public static String getMoney(String content){

        List<String> ss = new ArrayList<String>();
        for(String sss:content.replaceAll("[^0-9.]", ",").split(",")){
            if (sss.length()>0)
                ss.add(sss);
        }
        if (ss.size()<1){
            return null;
        }else {
            return ss.get(ss.size()-1);
        }

    }
    public static String md5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    // 添加日志到应用内日志系统
    private void addAppLog(String message) {
        try {
            SharedPreferences prefs = getSharedPreferences("vmq_logs", MODE_PRIVATE);
            String existingLogs = prefs.getString("logs", "");

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM-dd HH:mm:ss", java.util.Locale.getDefault());
            String timestamp = sdf.format(new java.util.Date());

            String newLog = "[" + timestamp + "] " + message + "\n";
            String updatedLogs = existingLogs + newLog;

            // 限制日志长度，保留最近的日志
            if (updatedLogs.length() > 10000) {
                updatedLogs = updatedLogs.substring(updatedLogs.length() - 8000);
            }

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("logs", updatedLogs);
            editor.apply();
        } catch (Exception e) {
            Log.e(TAG, "添加应用日志失败: " + e.getMessage());
        }
    }
}
