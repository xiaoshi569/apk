package com.vone.vmq;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import com.vone.qrcode.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogViewActivity extends Activity {
    private TextView logTextView;
    private ScrollView scrollView;
    private Button clearButton;
    private Button refreshButton;
    private Handler uiHandler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_view);
        
        uiHandler = new Handler(Looper.getMainLooper());
        
        initViews();
        loadLogs();
    }
    
    private void initViews() {
        logTextView = findViewById(R.id.log_text_view);
        scrollView = findViewById(R.id.log_scroll_view);
        clearButton = findViewById(R.id.btn_clear_log);
        refreshButton = findViewById(R.id.btn_refresh_log);
        
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearLogs();
            }
        });
        
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadLogs();
            }
        });
    }
    
    private void loadLogs() {
        SharedPreferences prefs = getSharedPreferences("vmq_logs", MODE_PRIVATE);
        String logs = prefs.getString("logs", "暂无日志记录\n\n使用说明：\n1. 返回主界面\n2. 进行支付宝收款测试\n3. 回到此页面查看日志\n4. 日志会自动记录通知监听情况");
        
        logTextView.setText(logs);
        
        // 滚动到底部显示最新日志
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }
    
    private void clearLogs() {
        SharedPreferences prefs = getSharedPreferences("vmq_logs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("logs", "日志已清除 - " + getCurrentTime() + "\n");
        editor.apply();
        
        loadLogs();
    }
    
    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
    
    // 静态方法供其他类调用，添加日志
    public static void addLog(Activity context, String message) {
        if (context == null) return;
        
        SharedPreferences prefs = context.getSharedPreferences("vmq_logs", MODE_PRIVATE);
        String existingLogs = prefs.getString("logs", "");
        
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault());
        String timestamp = sdf.format(new Date());
        
        String newLog = "[" + timestamp + "] " + message + "\n";
        String updatedLogs = existingLogs + newLog;
        
        // 限制日志长度，保留最近的日志
        if (updatedLogs.length() > 10000) {
            updatedLogs = updatedLogs.substring(updatedLogs.length() - 8000);
        }
        
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("logs", updatedLogs);
        editor.apply();
    }
}
