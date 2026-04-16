package com.android.intentfuzzer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.intentfuzzer.bo.Data;
import com.android.intentfuzzer.util.CsvUtil;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FloatBallView {


    private Context context;

    private int height = 0;

    private int width = 0;

    public static FloatBallView floatView2;


    private TextView condstext = null;

    public View getView() {
        return view;
    }

    public static FloatBallView getInstance(Context context) {
        if (floatView2 == null) {
            floatView2 = new FloatBallView(context);
        }
        return floatView2;
    }

    public FloatBallView(Context c) {

        this.context = c;

    }

    private WindowManager wm;

    private View view;// 浮动按钮

    WindowManager.LayoutParams params;

    /**
     * 添加悬浮View
     *
     * @Cony
     */

    public void createFloatView() {

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.home_floatview, null);
        }


        wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        height = wm.getDefaultDisplay().getHeight();
        width = wm.getDefaultDisplay().getWidth();

        params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_BASE_APPLICATION;// 所有程序窗口的“基地”窗口，其他应用程序窗口都显示在它上面。
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.format = PixelFormat.TRANSLUCENT;// 不设置这个弹出框的透明遮罩显示为黑色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }

        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.TOP | Gravity.LEFT;
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        params.y = screenHeight - (4 * height) / 5;//设置距离底部高度为屏幕五分之四
        params.x = screenWidth;
        //view.setBackgroundColor(Color.TRANSPARENT);
        view.setVisibility(View.VISIBLE);
        view.setOnTouchListener(new View.OnTouchListener() {
            // 触屏监听
            float lastX, lastY;
            int oldOffsetX, oldOffsetY;
            int tag = 0;// 悬浮球 所需成员变量

            @Override

            public boolean onTouch(View v, MotionEvent event) {
                final int action = event.getAction();
                float x = event.getX();
                float y = event.getY();
                if (tag == 0) {
                    oldOffsetX = params.x; // 偏移量
                    oldOffsetY = params.y; // 偏移量
                }

                if (action == MotionEvent.ACTION_DOWN) {
                    lastX = x;
                    lastY = y;
                } else if (action == MotionEvent.ACTION_MOVE) {
                    params.x += (int) (x - lastX) / 3; // 减小偏移量,防止过度抖动
                    params.y += (int) (y - lastY) / 3; // 减小偏移量,防止过度抖动
                    tag = 1;
                    wm.updateViewLayout(view, params);
                } else if (action == MotionEvent.ACTION_UP) {
                    int newOffsetX = params.x;
                    int newOffsetY = params.y;
                    // 只要按钮一动位置不是很大,就认为是点击事件
                    if (Math.abs(oldOffsetX - newOffsetX) <= 20
                            && Math.abs(oldOffsetY - newOffsetY) <= 20) {
                        if (l != null) {
                            l.onClick(view);
                        }
                    } else {
                        if (params.x < width / 2) {
                            params.x = 0;
                        } else {
                            params.x = width;
                        }
                        wm.updateViewLayout(view, params);
                        tag = 0;
                    }
                }
                return true;
            }
        });
        try {
            wm.addView(view, params);
        } catch (Exception e) {

        }

        /*floatView2.onFloatViewClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            //这边是点击悬浮按钮的响应事件
                Toast.makeText(context, "点击了悬浮球", Toast.LENGTH_LONG);
            }
        });*/

        settingsData();
    }

    private void settingsData() {
        TextView view = this.view.findViewById(R.id.appName);
        view.setText(Data.appName);

        parseCsv();
    }


    public void parseCsv() {
        String absolutePath2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        try {
            List<String[]> dataList = CsvUtil.read(new FileInputStream(absolutePath2 + "/readFile.csv"), "utf-8");
            String[] rowDataList = dataList.get(Data.index);

            // 当前路径
            String currentPath = rowDataList[0];
            // 可疑路径条数
            String kyCount = rowDataList[1];
            // 起始组件
            String startComponent = rowDataList[2];
            // 类型参数
            String typeParam = rowDataList[3];
            // 参数内容
            String paramContent = rowDataList[4];
            // 中间组件名
            List<String> middleComponentList =  new ArrayList<String>(Arrays.asList(rowDataList)).subList(5, 20);
            // 中止组件名
            String terminateComponentName = rowDataList[22];

            //数据填充
            if(kyCount != null && kyCount.trim().length() > 0){
                Data.kyCount = kyCount;
            }
            TextView kyCountId = view.findViewById(R.id.kyCountId);
            kyCountId.setText(Data.kyCount);
            TextView currentPathId = view.findViewById(R.id.currentPathId);
            currentPathId.setText(currentPath);
            TextView startComponentId = view.findViewById(R.id.startComponentId);
            startComponentId.setText(startComponent);
            TextView parentTypeId = view.findViewById(R.id.parentTypeId);
            parentTypeId.setText(typeParam);
            TextView parentContentId = view.findViewById(R.id.parentContentId);
            parentContentId.setText(paramContent);
            LinearLayout middleComponentId = view.findViewById(R.id.middleComponentId);
            middleComponentId.removeAllViews();
            TextView view = new TextView(context);
            view.setText("中间组件: ");
            view.setTextColor(Color.parseColor("#FF3F3F"));
            view.setTextSize(10);
            middleComponentId.addView(view);
            for (String name : middleComponentList) {
                if(name == null || name.trim().length() == 0){
                    continue;
                }
                TextView textView = new TextView(context);
                textView.setTextColor(Color.parseColor("#5AE485"));
                textView.setText(name);
                textView.setTextSize(10);
                middleComponentId.addView(textView);
            }
            TextView stopComponetId = this.view.findViewById(R.id.stopComponetId);
            stopComponetId.setText(terminateComponentName);

            Data.rowName = currentPath;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 点击浮动按钮触发事件，需要override该方法
     */

    private View.OnClickListener l;

    public void onFloatViewClick(View.OnClickListener l) {
        this.l = l;
    }

    /**
     * 将悬浮View从WindowManager中移除，需要与createFloatView()成对出现
     */

    public void removeFloatView() {
        if (wm != null && view != null) {
            wm.removeViewImmediate(view);
//          wm.removeView(view);//不要调用这个，WindowLeaked
            view = null;
            wm = null;
        }
    }

    /**
     * 隐藏悬浮View
     */

    public void hideFloatView() {
        if (wm != null && view != null && view.isShown()) {
            view.setVisibility(View.GONE);
        }
    }

    /**
     * 显示悬浮View
     */

    public void showFloatView() {
        if (wm != null && view != null && !view.isShown()) {
            view.setVisibility(View.VISIBLE);
        }
    }

    //数据更新测试方法
    public void updatatest(String ss) {
        if (wm != null && view != null && view.isShown()) {
            condstext = (TextView) view.findViewById(R.id.condstext);
            condstext.setText(ss);
        }
    }

    public void updateViewLayout() {
        if (wm != null) {
            int screenWidth = (int) 480;
            int screenHeight = (int) 720;
            if (screenWidth == 0) {
                screenWidth = context.getResources().getDisplayMetrics().widthPixels;
            }

            if (screenHeight == 0) {
                screenHeight = context.getResources().getDisplayMetrics().heightPixels;
                params.y = screenHeight - height / 3;//设置距离底部高度为屏幕三分之一
            } else {
                params.y = screenHeight;
            }
            params.x = screenWidth;
            wm.updateViewLayout(view, params);
        }

    }

}

