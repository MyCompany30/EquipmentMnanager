package com.example.uhfxintong;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.example.uhfxintong.adapter.PhotoGridViewAdapter;
import com.example.uhfxintong.db.Uhf;
import com.example.uhfxintong.eventbus.DefectListener;
import com.example.uhfxintong.util.OpenCameraUtils;

import de.greenrobot.event.EventBus;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;

public class QueXianInputActivity extends Activity {
	private EditText qxContent;
	private GridView gridView;
	private List<String> photoPaths = new ArrayList<String>();
	private int addPic = R.drawable.add_icon;
	private PhotoGridViewAdapter adapter;
	public static final int GET_IMAGE_VIA_CAMERA = 5000;
	private String localTempImgDir = "localTempImgDir";
//	private String rfidDir = "";
	private Uhf uhf;
	private int position = 0;
	private String localTempImgFileName = "test.jpg";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.acctivity_quexian_inpput);
//		rfidDir = this.getIntent().getStringExtra("rifd");
		uhf = (Uhf) this.getIntent().getSerializableExtra("uhf");
		position = this.getIntent().getIntExtra("position", 0);
		qxContent = (EditText) this.findViewById(R.id.quexianContent);
		gridView = (GridView) this.findViewById(R.id.gridView);
		photoPaths.add("" + R.drawable.add_icon);
		qxContent.setText(uhf.getDefect());
		if(!"".equals(uhf.getPhotos())) {
			String[] photos = uhf.getPhotos().split(",");
			if(photos.length > 0) {
				for(String s : photos) {
					photoPaths.add(s);
				}
			}
		}
		adapter = new PhotoGridViewAdapter(this, photoPaths);
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if(arg2 == 0) {
//					OpenCameraUtils.openCameraImage(QueXianInputActivity.this);
					File dir=new File(Environment.getExternalStorageDirectory() + "/"+ localTempImgDir + "/" + uhf.getUhfId()); 
					if(!dir.exists())dir.mkdirs(); 
					SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
					long time = System.currentTimeMillis();
					Intent intent=new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE); 
					localTempImgFileName = time+".jpg";
					File f=new File(dir, localTempImgFileName);//localTempImgDir和localTempImageFileName是自己定义的名字 
					Uri u=Uri.fromFile(f); 
					intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0); 
					intent.putExtra(MediaStore.EXTRA_OUTPUT, u); 
					startActivityForResult(intent, GET_IMAGE_VIA_CAMERA); 
				}
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case GET_IMAGE_VIA_CAMERA:
			File f=new File(Environment.getExternalStorageDirectory() 
					+"/"+localTempImgDir + "/" + uhf.getUhfId() + "/"+localTempImgFileName); 
			Log.i("a", "f path = " + f.getAbsolutePath());
			photoPaths.add(f.getAbsolutePath());
			adapter.notifyDataSetChanged();
			break;
		}
	}
	
	public void btnClick(View view) {
		switch (view.getId()) {
		case R.id.back:
			this.finish();
			break;
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		StringBuffer sb = new StringBuffer();
		if(photoPaths.size() > 1) {
			for(int i=1; i<photoPaths.size(); i++) {
				sb.append(photoPaths.get(i)).append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
		}
		EventBus.getDefault().post(new DefectListener(qxContent.getText().toString(), sb.toString(), position));
		
	}
	
}
