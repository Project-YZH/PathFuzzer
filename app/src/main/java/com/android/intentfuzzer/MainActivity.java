package com.android.intentfuzzer;

import com.android.intentfuzzer.util.Utils;


import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.provider.Settings;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
	
	private GridView gridView = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		gridView=(GridView)findViewById(R.id.gridview);
        gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));//ѡ�������͸��
        gridView.setAdapter(new MainMenuAdapter(this));//���������� ���� MainMenuAdapter.java 
        //����¼�         
        gridView.setOnItemClickListener(new OnItemClickListener(){ 
        	
        public void onItemClick(AdapterView<?> parent, View view, int position, long id){  
          	 
            if(position==0){  		
          		Intent intent=new Intent(MainActivity.this,AppInfoActivity.class);
          		intent.putExtra("type", Utils.ALL_APPS);
              	startActivity(intent);
          	}
          	
          	if(position==1){ 		
          		Intent intent=new Intent(MainActivity.this,AppInfoActivity.class);
          		intent.putExtra("type", Utils.SYSTEM_APPS);
              	startActivity(intent);
         	}
          	
          	if(position==2){
          		Intent intent=new Intent(MainActivity.this,AppInfoActivity.class);
          		intent.putExtra("type", Utils.NONSYSTEM_APPS);
              	startActivity(intent);   		
         	}
            
          	if(position==3){        	
          		Dialog dialog = new Dialog(MainActivity.this, R.style.dialog);
          		dialog.setContentView(R.layout.dialog);
          		dialog.show();
         	}    
          	
          }  
        });


		// 申请权限
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			//检查是否已经授予权限
			if (!Settings.canDrawOverlays(MainActivity.this)) {
				//若未授权则请求权限
				Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
				intent.setData(Uri.parse("package:" + getPackageName()));
				startActivityForResult(intent, 0);
			} else {
				Toast.makeText(getApplicationContext(),
						"已经获取到悬浮框权限！", Toast.LENGTH_LONG).show();
			}
		}

		verifyStoragePermissions(this);
	}

	// Storage Permissions
	private static final int REQUEST_EXTERNAL_STORAGE = 1;
	private static String[] PERMISSIONS_STORAGE = {
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE };

	/**
	 * Checks if the app has permission to write to device storage
	 *
	 * If the app does not has permission then the user will be prompted to
	 * grant permissions
	 *
	 * @param activity
	 */
	public static void verifyStoragePermissions(Activity activity) {
		// Check if we have write permission
		int permission = ActivityCompat.checkSelfPermission(activity,
				Manifest.permission.WRITE_EXTERNAL_STORAGE);

		if (permission != PackageManager.PERMISSION_GRANTED) {
			// We don't have permission so prompt the user
			ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
					REQUEST_EXTERNAL_STORAGE);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	

}
