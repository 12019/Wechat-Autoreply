package com.example.autoreply;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainSetting extends Activity {
	
	private EditText editmsg;
	private EditText editfriend;
	private Button startbtn;
	private Button aboutbtn;
	private Button setbtn;
	private CheckBox autobox;
	private CheckBox notallbox;
	private LinearLayout linearlayout3;
	//ϵͳ�汾�Ƿ�֧���Զ��ظ�����Android4.3���ϲ���ֱ��ͨ�����id����
	private boolean canAuto;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {  
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_main);
	    
	    //���ϵͳ�汾
	    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
	    	canAuto = true;
	    else
	    	canAuto = false;
	    
	    autobox = (CheckBox) this.findViewById(R.id.autocheck);
	    notallbox = (CheckBox) this.findViewById(R.id.allcheck);
	    editmsg = (EditText) this.findViewById(R.id.editmsg);
	    editfriend = (EditText) this.findViewById(R.id.editfriend);
	    startbtn = (Button) this.findViewById(R.id.startbtn);
	    aboutbtn = (Button) this.findViewById(R.id.aboutbtn);
	    setbtn = (Button) this.findViewById(R.id.setbtn);
	    linearlayout3 = (LinearLayout) this.findViewById(R.id.linearLayout3);

	    //����Ĭ����Ϣ
	    editmsg.setText(StaticData.message);
	    editfriend.setText(StaticData.friend);
	    if(!StaticData.auto)
	    	linearlayout3.setVisibility(View.GONE);
	    autobox.setChecked(StaticData.auto);
	    notallbox.setChecked(!StaticData.showall);

	    //�жϲ���ʾ���������Ƿ���
	    this.showInfo();
	    
	    autobox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton cb, boolean b) {
				if(!canAuto) {
					showTips("_�Զ��ظ���Android4.3������_");
					return;
				}
				if(b) {
					StaticData.auto = true;
					linearlayout3.setVisibility(View.VISIBLE);
					showTips("_�ѿ����Զ��ظ�_");
				} else {
					StaticData.auto = false;
					linearlayout3.setVisibility(View.GONE);
					showTips("_�ѹر��Զ��ظ�_");
				}
			}
	    	
	    });
	    
	    notallbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton cb, boolean b) {
				if(b) {
					StaticData.showall = false;
					showTips("_��������ֻ��ʾ��Ϣ������_");
				} else {
					StaticData.showall = true;
					showTips("_����������ʾ��ϸ��Ϣ����_");
				}
			}
	    	
	    });
	    
	    startbtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String msg = editmsg.getText().toString();
				if( !msg.isEmpty() ) {
					Log.i("demo", msg);
					StaticData.message = msg;
				} else {
					StaticData.message = "���ñ�����æ����������Ե�  ���Զ��ظ���";
				}
				String friend = editfriend.getText().toString();
				if( !friend.isEmpty() ) {
					StaticData.friend = friend;
					StaticData.isfriend = true;
				} else {
					StaticData.friend = "";
					StaticData.isfriend = false;
					
				}
				showTips("�ɹ�Ӧ������");
			}
			
		});
	    
	    aboutbtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainSetting.this, About.class);
				startActivity(intent);
			}
		});
	    
	    setbtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
			}
		});
	    
	}
	
	//��ʾ��ʾ��Ϣ���ڰ�ť�¼��������ڵ��õ�
	private void showTips(String info) {
		Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
	}
	
	private void showInfo() {
		if(serviceIsRunning())
	    	setbtn.setText("������������ >>");
	    else
	    	setbtn.setText("������δ���� >>");
	}
	//��ȡϵͳ�������еķ����б����жϸ��������Ƿ���
	private boolean serviceIsRunning() {
		ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> lists = am.getRunningServices(150);//��ȡ�������ʵ�����
		for (RunningServiceInfo info : lists) {
			if (info.service.getClassName().equals("com.example.autoreply.AutoReplyService")) {
				return true;
			}
		}
		return false;
		
	}
	
	@Override
	public void onResume(){
		super.onResume();  
		showInfo();
	}
	
	//�������ؼ�����ʾ�˳���ʾ��
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		 if(keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME){
	         showExitDialog();
		 }
		 return super.onKeyDown(keyCode, event);
	}
	//�˳���ʾ��
	private void showExitDialog() {  
        AlertDialog.Builder builder = new Builder(MainSetting.this);
        String message = "";
        if(serviceIsRunning())
        	message = "�����������У��رպ��Ի�ظ�\n";
        else
        	message = "�����Ѿ��ر�\n";
        builder.setMessage(message + "ȷ��Ҫ�˳���?");
        builder.setPositiveButton("ȷ��", 
        new android.content.DialogInterface.OnClickListener() {  
            @Override  
            public void onClick(DialogInterface dialog, int which) {  
            	finish();  
            }  
        });  
        builder.setNegativeButton("ȡ��", 
        new android.content.DialogInterface.OnClickListener() {  
            @Override  
            public void onClick(DialogInterface dialog, int which) {  
                dialog.dismiss();  
            }  
        });  
        builder.create().show();  
    }
}
