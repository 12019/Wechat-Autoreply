package com.example.autoreply;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class LockScreenActivity extends Activity {

	private Button closebtn;
	private Button detailbtn;
	private TextView text;
	private TextView caltext;
	private TextView timetext;
	private showReceiver receiver;
	private ViewFlipper viewFlipper;
	private ListView listView;
	private Button Levelbtn;
	
	//�������
	private int batteryLevel;
    private int batteryScale;
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //��ȡ��ǰ��������δ��ȡ������ֵ����Ĭ��Ϊ0
            batteryLevel=intent.getIntExtra(BatteryManager.EXTRA_LEVEL,0);
            //��ȡ����������δ��ȡ��������ֵ����Ĭ��Ϊ100
            batteryScale=intent.getIntExtra(BatteryManager.EXTRA_SCALE,100);
            //��ʾ����
            Levelbtn.setText((batteryLevel*100/batteryScale)+"%");
        }
    };
	//30�����һ������ʱ����Ϣ
	private final Handler handler = new Handler();
    private final Runnable runnable = new Runnable() {
         public void run () {
        	 showInfo();
        	 handler.postDelayed(this, 30000); 
         }
    };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {  
	    super.onCreate(savedInstanceState);
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		setContentView(R.layout.activity_screen);
		
		Log.i("demo", "Create...");
		closebtn = (Button) this.findViewById(R.id.closebtn);
		text = (TextView) this.findViewById(R.id.screentext);
		caltext = (TextView)findViewById(R.id.caltext);
		timetext = (TextView)findViewById(R.id.timetext);
		viewFlipper = (ViewFlipper) this.findViewById(R.id.viewFlipper);
		detailbtn = (Button) this.findViewById(R.id.showDetails);
		listView = (ListView) this.findViewById(R.id.listview);
		Levelbtn = (Button)findViewById(R.id.progbLevel);
		
		//�����л�����
		viewFlipper.setInAnimation(this, android.R.anim.slide_in_left);
		viewFlipper.setOutAnimation(this, android.R.anim.slide_out_right);
		
		//��ʱ���½���
		handler.removeCallbacks(runnable);
		handler.postDelayed(runnable,30000); 

		//������ť����¼�
		closebtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				StaticData.total = 0;
				StaticData.replaied = 0;
				StaticData.data.clear();
				finish();
			}
		});
		
		detailbtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				viewFlipper.showNext();
			}
		});
		
		//ע��㲥������
	    receiver = new showReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.autoreply.SHOW_ACTION");//�Զ���㲥������ʵʱ����������ʾ��Ϣ
        registerReceiver(receiver, filter);
        
        IntentFilter intentFilter=new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        //ע��������Ի�ȡ������Ϣ
        registerReceiver(broadcastReceiver, intentFilter);
		
        //��ʾ��Ϣ
		showInfo();
		showDetails();
      		
		Log.i("demo", "Create");
	}
	
	public void showInfo() {
		Time time = new Time(); 
		time.setToNow(); 
		int year = time.year; 
		int month = time.month; 
		int day = time.monthDay; 
		int minute = time.minute; 
		int hour = time.hour;
		caltext.setText(year + "-" + (month + 1) + "-" + day);
		timetext.setText((hour < 10 ? "0" + hour : hour) + ":" + (minute < 10 ? "0" + minute : minute));
		text.setText("���յ� " + StaticData.total + " ��΢����Ϣ\n\n" + "�Զ��ظ� " + StaticData.replaied + " ��\n");
		if(StaticData.total == 0) {
			detailbtn.setEnabled(false);
			detailbtn.setText("������Ϣ");
		} else {
			detailbtn.setEnabled(true);
			detailbtn.setText("��Ϣ����");
		}
		Log.i("demo", "showInfo");
	}
	
	public void showDetails() {
		ArrayAdapter<String> adapter = new ArrayAdapter<String> (LockScreenActivity.this, android.R.layout.simple_list_item_1, StaticData.data);
		listView.setAdapter(adapter);
		Log.i("demo", "showDetails");
	}
	
	//���η����¼�
	@Override
	public void onBackPressed() {
		
	}
	
	@Override
	public void finish() {
		super.finish();
		//activity����ʧ����Ч��
		//overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);//ϵͳ�Դ�����
		overridePendingTransition(R.drawable.activity_close, R.drawable.activity_start);//�Զ��嶯��
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		unregisterReceiver(receiver);
		unregisterReceiver(broadcastReceiver);
		
		Log.i("demo", "Destroy");
	}

	//�Զ���㲥��������ʵʱ����������Ϣ
	class showReceiver extends BroadcastReceiver {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    	    String action = intent.getAction();
    	    if (action.equals("com.example.autoreply.SHOW_ACTION")) {
    	    	showInfo();
    	    	showDetails();
    	    }
    	}
    }
}
