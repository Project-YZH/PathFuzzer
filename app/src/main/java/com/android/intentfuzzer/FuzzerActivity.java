package com.android.intentfuzzer;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.android.intentfuzzer.bo.Data;
import com.android.intentfuzzer.util.CsvUtil;
import com.android.intentfuzzer.util.SerializableTest;
import com.android.intentfuzzer.util.Utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

public class FuzzerActivity extends Activity{

	private ArrayList<String> cmpTypes = new ArrayList<String>();
	private String currentType = null;
	private Spinner typeSpinner = null;
	private ListView cmpListView = null;
	private Button fuzzAllNullBtn = null;
	private Button fuzzAllSeBtn = null;
	private Button zrButton = null;
	private Button pathButton = null;
	private Button hqcsButton = null;
	private Button qhljzyButton = null;



	private ArrayAdapter<String> cmpAdapter = null;
	
	private ArrayList<String> cmpNames = new ArrayList<String>();
	private ArrayList<ComponentName> components = new ArrayList<ComponentName>();
	private PackageInfo pkgInfo = null;
	
	
	private static Map<Integer, String> ipcTypesToNames = new TreeMap<Integer, String>();
	private static Map<String, Integer> ipcNamesToTypes = new HashMap<String, Integer>();
	
	
	
	
	static {
		ipcTypesToNames.put(Utils.ACTIVITIES, "Activities");
		ipcTypesToNames.put(Utils.RECEIVERS, "Receivers");
		ipcTypesToNames.put(Utils.SERVICES, "Services");
		
		for (Entry<Integer, String> entry : ipcTypesToNames.entrySet()) {
			ipcNamesToTypes.put(entry.getValue(), entry.getKey());
		}
	}
	
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fuzzer);
		
		for (String name : ipcTypesToNames.values())
			cmpTypes.add(name);
		currentType = cmpTypes.get(0);
		
		initView();
		initTypeSpinner();
		
		//pkgInfo = getPkgInfo();
		pkgInfo = ((MyApp)getApplication()).packageInfo;
		if(pkgInfo == null){
			Toast.makeText(this, R.string.pkginfo_null, Toast.LENGTH_LONG).show();
			return;
		}
	}



	private PackageInfo getPkgInfo()
	{
		PackageInfo pkgInfo = null;
		
		Intent intent = getIntent();
		if (intent.hasExtra(Utils.PKGINFO_KEY)){
			pkgInfo = intent.getParcelableExtra(Utils.PKGINFO_KEY);
		}	
		return pkgInfo;
	}
	
	private void initView(){
		typeSpinner = (Spinner) findViewById(R.id.type_select);
		cmpListView = (ListView) findViewById(R.id.cmp_listview);
		fuzzAllNullBtn = (Button) findViewById(R.id.fuzz_all_null);
		fuzzAllSeBtn = (Button) findViewById(R.id.fuzz_all_se);
		zrButton = findViewById(R.id.zr);
		pathButton = findViewById(R.id.path);
		hqcsButton = findViewById(R.id.hqcs);
		qhljzyButton = findViewById(R.id.qhljzy);


		cmpListView.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					// TODO Auto-generated method stub
					ComponentName toSend = null;
					Intent intent = new Intent();
					String className =  cmpAdapter.getItem(position).toString();
					for (ComponentName cmpName : components) {
						if (cmpName.getClassName().equals(className)) {
							toSend = cmpName;
							break;
						}
					}
					
					intent.setComponent(toSend);

					if (sendIntentByType(intent, currentType)) {
						Toast.makeText(FuzzerActivity.this, "Sent Null " + intent, Toast.LENGTH_LONG).show();
					} 
					
					else {
						Toast.makeText(FuzzerActivity.this, "Send " + intent + " Failed!", Toast.LENGTH_LONG).show();
					}

					// 变色
					String[] split = className.split("\\.");
					String name = split[split.length - 1];
					View floatBallView = FloatBallView.getInstance(FuzzerActivity.this).getView();
					LinearLayout middleComponentId = floatBallView.findViewById(R.id.middleComponentId);
					int childCount = middleComponentId.getChildCount();
					TextView stopComponetId = floatBallView.findViewById(R.id.stopComponetId);
					if(stopComponetId != null && (childCount == Data.middleClickCount + 1) && stopComponetId.getText().equals(name)){
						stopComponetId.setBackgroundColor(Color.parseColor("#E6EDA8"));
						Data.middleClickCount ++;
					}
					TextView child = (TextView) middleComponentId.getChildAt(Data.middleClickCount + 1  );
					if(child != null && child.getText().equals(name)){
						child.setBackgroundColor(Color.parseColor("#E6EDA8"));
						Data.middleClickCount ++;
					}

					// 进度变更
					TextView currentProgressId = floatBallView.findViewById(R.id.currentProgressId);
					currentProgressId.setText((int)(1.0 * Data.middleClickCount / (childCount ) * 100) + "%");
					TextView totalProgressId = floatBallView.findViewById(R.id.totalProgressId);
					totalProgressId.setText((int)((1.0 * Data.index) / Integer.parseInt(Data.kyCount) * 100) + "%");
				}
	       	
	       });
	    
	    cmpListView.setOnItemLongClickListener(new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				ComponentName toSend = null;
				Intent intent = new Intent();
				String className =  cmpAdapter.getItem(position).toString();
				for (ComponentName cmpName : components) {
					if (cmpName.getClassName().equals(className)) {
						toSend = cmpName;
						break;
					}
				}
				
				intent.setComponent(toSend);
				intent.putExtra("test", new SerializableTest());

				if (sendIntentByType(intent, currentType)) {
					Toast.makeText(FuzzerActivity.this, "Sent Serializeable " + intent, Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(FuzzerActivity.this, "Send " + intent + " Failed!", Toast.LENGTH_LONG).show();
				}
				return true;
			}
       	
       });

	    
	    fuzzAllNullBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				for(ComponentName cmpName : components){
					Intent intent = new Intent();
					intent.setComponent(cmpName);
					if (sendIntentByType(intent, currentType)) {
						Toast.makeText(FuzzerActivity.this, "Sent Null " + intent, Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(FuzzerActivity.this, R.string.send_faild, Toast.LENGTH_LONG).show();
					}
				}
			}
 	
	    });
	    
	    fuzzAllSeBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				for(ComponentName cmpName : components){
					Intent intent = new Intent();
					intent.setComponent(cmpName);
					intent.putExtra("test", new SerializableTest());
					if (sendIntentByType(intent, currentType)) {
						Toast.makeText(FuzzerActivity.this, "Sent Serializeable " + intent, Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(FuzzerActivity.this, R.string.send_faild, Toast.LENGTH_LONG).show();
					}
				}
			}
	    	
	    });

		zrButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				checkJurisdiction();
				pathButton.setText(Data.rowName);
			}
		});

		hqcsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if(!isRun){
					return;
				}
				View floatBallView = FloatBallView.getInstance(FuzzerActivity.this).getView();
				LinearLayout cslxId = floatBallView.findViewById(R.id.cslxId);
				changeBack(cslxId);
				LinearLayout csnrId = floatBallView.findViewById(R.id.csnrId);
				changeBack(csnrId);
			}
		});

		qhljzyButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if(!isRun){
					return;
				}
				Data.index ++;
				Data.middleClickCount = 0;
				FloatBallView.getInstance(FuzzerActivity.this).parseCsv();
				pathButton.setText(Data.rowName);
				TextView totalProgressId = FloatBallView.getInstance(FuzzerActivity.this).getView().findViewById(R.id.totalProgressId);
				totalProgressId.setText((int)((1.0 * Data.index) / Integer.parseInt(Data.kyCount) * 100) + "%");
				TextView currentProgressId = FloatBallView.getInstance(FuzzerActivity.this).getView().findViewById(R.id.currentProgressId);
				currentProgressId.setText("0%");
				TextView stopComponetId = FloatBallView.getInstance(FuzzerActivity.this).getView().findViewById(R.id.stopComponetId);
				stopComponetId.setBackgroundColor(android.R.drawable.btn_default);

				View floatBallView = FloatBallView.getInstance(FuzzerActivity.this).getView();
				LinearLayout cslxId = floatBallView.findViewById(R.id.cslxId);
				changeBackInt(cslxId, android.R.drawable.btn_default);
				LinearLayout csnrId = floatBallView.findViewById(R.id.csnrId);
				changeBackInt(csnrId, android.R.drawable.btn_default);

			}
		});
	}

	private void changeBack(LinearLayout cslxId) {
		int childCount = cslxId.getChildCount();
		int colorNumber = Color.parseColor("#5AE485");
		for (int i = 0; i < childCount; i++) {
			TextView childAt = (TextView) cslxId.getChildAt(i);
			if(childAt.getTextColors().getDefaultColor() == colorNumber){
				childAt.setBackgroundColor(Color.parseColor("#E6EDA8"));
			}
		}
	}

	private void changeBackInt(LinearLayout cslxId, int colr) {
		int childCount = cslxId.getChildCount();
		int colorNumber = Color.parseColor("#5AE485");
		for (int i = 0; i < childCount; i++) {
			TextView childAt = (TextView) cslxId.getChildAt(i);
			if(childAt.getTextColors().getDefaultColor() == colorNumber){
				childAt.setBackgroundColor(colr);
			}
		}
	}

	private void initTypeSpinner(){
		ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, cmpTypes);
		typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		typeSpinner.setAdapter(typeAdapter);
		
		typeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				updateType();
				updateComponents(currentType);
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}
	
	private void updateType() {
		Object selector = typeSpinner.getSelectedItem();
		if (selector != null) {
			currentType = typeSpinner.getSelectedItem().toString();
		}
	}
	
	
	private void updateComponents(String currentType){
		fuzzAllNullBtn.setVisibility(View.GONE);
		fuzzAllSeBtn.setVisibility(View.GONE);
		components = getComponents(currentType);
		cmpNames.clear();
		if(!components.isEmpty())
		{
			for(ComponentName cmpName : components){
				if (!cmpNames.contains(cmpName.getClassName()))
				{
					cmpNames.add(cmpName.getClassName());
				}
			}

			fuzzAllNullBtn.setVisibility(View.GONE);
			fuzzAllSeBtn.setVisibility(View.GONE);
			
		}else{
			Toast.makeText(this, R.string.no_compt, Toast.LENGTH_LONG).show();
		}
		setListView();
		
	}
	
	
	private ArrayList<ComponentName> getComponents(String currentType){
		PackageItemInfo items[] = null;
		ArrayList<ComponentName> cmpsFound = new ArrayList<ComponentName>();
		switch(ipcNamesToTypes.get(currentType)){
		case Utils.ACTIVITIES:
			items = pkgInfo.activities;
			break;
		case Utils.RECEIVERS:
			items = pkgInfo.receivers;
			break;
		case Utils.SERVICES:
			items = pkgInfo.services;
			break;
		default:
			items = pkgInfo.activities;
			break;
		}
			
		if (items != null){
			for (PackageItemInfo pkgItemInfo : items){
					cmpsFound.add(new ComponentName(pkgInfo.packageName, pkgItemInfo.name));
				}
		}
		
		return cmpsFound;
	}
	
	private void setListView(){
		cmpAdapter = new ArrayAdapter<String>(this, R.layout.component, cmpNames );
		cmpListView.setAdapter(cmpAdapter);
	}
	
	private boolean sendIntentByType(Intent intent, String type) {

		try {
				switch (ipcNamesToTypes.get(type)) {
				case Utils.ACTIVITIES:
					startActivity(intent);
					return true;
				case Utils.RECEIVERS:
					sendBroadcast(intent);
					return true;
				case Utils.SERVICES:
					startService(intent); 
					return true;
				default:
					return true;
				}
		} catch (Exception e) {
			//e.printStackTrace();
			return false;
		}
		
	}

	private Boolean isRun = false;

	private void checkJurisdiction() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (!Settings.canDrawOverlays(FuzzerActivity.this)) {
				Toast.makeText(getApplicationContext(),
						"无权限", Toast.LENGTH_LONG).show();
			} else {
				if(isRun){
					return;
				}
				isRun = true;
				FloatBallView.getInstance(FuzzerActivity.this).createFloatView();
				Toast.makeText(getApplicationContext(),
						"弹窗已生效", Toast.LENGTH_LONG).show();


			}
		}

	}
	
}
