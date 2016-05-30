package com.example.autoreply;
import java.util.List;
import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class AutoReplyService extends AccessibilityService {  
	
	private boolean canGet = false;//�ܷ�ظ�
	private boolean enableKeyguard = true;//Ĭ������Ļ��
	private int mode = 1;//΢��֪ͨģʽ��1.��ϸ֪ͨ2.����ϸ֪ͨ
	private AccessibilityNodeInfo editText = null;
	//private AccessibilityNodeInfo qtextView = null;//Ⱥ�ı���
	//�������������
	private KeyguardManager km;
	private KeyguardLock kl;
	private PowerManager pm;
	private PowerManager.WakeLock wl = null;
	private ScreenOffReceiver sreceiver;
	private PhoneReceiver preceiver;
	
    /** ���Ѻͽ������*/
	private void wakeAndUnlock(boolean unLock)
	{
	    if(unLock)
	    {
	    	if(!pm.isScreenOn()) {
	    		//��ȡ��Դ����������
	    		wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "bright");
		   		//������Ļ
		    	wl.acquire();
		    	Log.i("demo", "����");
	    	}
	    	if(km.inKeyguardRestrictedInputMode()) {
			    //����
	    		enableKeyguard = false;
	    		//kl.reenableKeyguard();
		        kl.disableKeyguard();
		        Log.i("demo", "����");
	    	}
	    } else {
	    	if(!enableKeyguard) {
	    		//����
	    		kl.reenableKeyguard();
	    		Log.i("demo", "����");
	    	}
	    	if(wl != null) {
	    	    //�ͷ�wakeLock���ص�
	      	    wl.release();
	      	    wl = null;
	      	    Log.i("demo", "�ص�");
	        }
	    }
	}

	/** ͨ���ı�����*/
	public  AccessibilityNodeInfo findNodeInfosByText(AccessibilityNodeInfo nodeInfo, String text) {
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(text);
        if(list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }
	
    private void findNodeInfosByName(AccessibilityNodeInfo nodeInfo, String name) {
    	if(name.equals(nodeInfo.getClassName())) {
    		//if(name.equals("android.widget.TextView") && (!nodeInfo.isClickable()) && (!nodeInfo.getText().toString().matches("[012]\\d:[0-6]\\d")) && (!nodeInfo.getText().toString().matches("΢��.*")) && (!nodeInfo.getText().toString().equals("��"))) {
    		//	qtextView = nodeInfo;
    		//	return;
    		//} else if(name.equals("android.widget.EditText")) {
    			editText = nodeInfo;
    			return;
    		//}
    	}
		for(int i = 0; i < nodeInfo.getChildCount(); i++) {
	        findNodeInfosByName(nodeInfo.getChild(i), name);
	    }
    }
    
	/** ����¼�*/
    public void performClick(AccessibilityNodeInfo nodeInfo) {
        if(nodeInfo == null) {
            return;
        }
        if(nodeInfo.isClickable()) {
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        } else {
            performClick(nodeInfo.getParent());
        }
    }

    /** �����¼�*/
    public  void performBack(AccessibilityService service) {
        if(service == null) {
            return;
        }
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }
    
    /** ��������¼�*/
    @Override  
    public void onAccessibilityEvent(AccessibilityEvent event) {  
        int eventType = event.getEventType();
        //Log.i("demo", Integer.toString(eventType));
        switch (eventType) {  
        //��һ��������֪ͨ����Ϣ  
        case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
        	
            List<CharSequence> texts = event.getText();
            if (!texts.isEmpty()) {
            	String message = texts.get(0).toString();
            	StaticData.total++;
            	setData(message);
            	
            	Log.i("demo", "�յ�֪ͨ����Ϣ:" + message);
            	
            	Intent i = new Intent("com.example.autoreply.SHOW_ACTION");
            	sendBroadcast(i);
            	
            	if(!StaticData.auto)
            		return;
            	
            	if(message.equals("΢�ţ����յ���һ����Ϣ��"))
            		mode = 2;
            	else
            		mode = 1;
            	
            	//�ж��Ƿ�������
            	if(StaticData.isfriend && (mode == 1) && ( !message.contains(StaticData.friend) )) {
        			return;
            	}
                //ģ���֪ͨ����Ϣ  
                if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
                	Log.i("demo", "������Get=true");
                	canGet = true;
                	wakeAndUnlock(true);
                	try {
                		Notification notification = (Notification) event.getParcelableData();  
                		PendingIntent pendingIntent = notification.contentIntent;  
                        pendingIntent.send();
                    } catch (CanceledException e) {  
                        e.printStackTrace();  
                    }
                }
                break;
            }

        //�ڶ����������Ƿ����΢���������    
        case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
	    	if(canGet) {
	        	canGet = false;
	        	reply();
	        	performBack(this);
	        }
        	break;
        	
        case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
        	String className = event.getClassName().toString();
        	
        	Log.i("demo", "���ڸı�" + className);
        }
    }   
 

    @SuppressWarnings("static-access")
	private void setData(String data) {
    	Time time = new Time(); 
		time.setToNow(); 
		int hour = time.hour;
		int minute = time.minute;
		if(StaticData.showall) {
			//if(data.length() > 23)
			//	data = data.substring(0, 23) + "...";
		} else {
			if(!data.equals("΢�ţ����յ���һ����Ϣ��")) {
				data = data.split(":")[0];
				//Log.i("demo", "showall=" + StaticData.showall+" data=" + data);
				data += " ����һ����Ϣ��";
			}
		}
		data = data.format("%s     %02d:%02d", data, hour, minute);
		//data = data + "     " + hour + ":" + minute;
		StaticData.data.add(data);
    }
    
    /** �Զ��ظ�*/
    @SuppressLint("NewApi")
    private void reply() {
    	AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if(nodeInfo == null) {
            return;
        }
        
        AccessibilityNodeInfo targetNode = null;
        
        //�ж��Ƿ�Ⱥ���Լ�mode=2ʱ�Ƿ�ƥ�����
        //findNodeInfosByName(nodeInfo, "android.widget.TextView");
        //targetNode = qtextView;
        //if(targetNode != null) {
        //	Log.i("demo", "name=" + targetNode.getText().toString());
        //}
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId(StaticData.qunId);
    	if( !list.isEmpty() ) {
    		targetNode = list.get(0);
    		
			String temp = targetNode.getText().toString();
			if( temp.matches(".*\\(([3-9]|[1-9]\\d+)\\)") || (mode == 2 && StaticData.isfriend && ( !temp.equals(StaticData.friend) ) ) ) {
				performBack(this);
				wakeAndUnlock(false);
				return;
    		}
    	}
        
        //�����ı��༭��
        if(editText == null) {
        	Log.i("demo", "���ڲ��ұ༭��...");
        	//��һ�ֲ��ҷ���
        	List<AccessibilityNodeInfo> list1 = nodeInfo.findAccessibilityNodeInfosByViewId(StaticData.editId);
        	if( !list1.isEmpty() )
        		editText = list1.get(0);
        	//�ڶ��ֲ��ҷ���
        	if(editText == null)
        		findNodeInfosByName(nodeInfo, "android.widget.EditText");
        }
        targetNode = editText;
        
        //ճ���ظ���Ϣ
        if(targetNode != null) {
        	//android > 21= 5.0ʱ������ACTION_SET_TEXT 
	        //android > 18=4.3ʱ����ͨ������ճ���ķ���,��ȷ�����㣬��ճ��ACTION_PASTE 
	        //ʹ�ü��а�
	        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE); 
	        ClipData clip = ClipData.newPlainText("message", StaticData.message);
	        clipboard.setPrimaryClip(clip);
	        //Log.i("demo", "����ճ����");
	        //���� ��n��AccessibilityNodeInfo���� 
	        targetNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
	        //Log.i("demo", "��ȡ����");
	        //ճ���������� 
	        targetNode.performAction(AccessibilityNodeInfo.ACTION_PASTE);
	        //Log.i("demo", "ճ������");
        }
        
        if(targetNode != null) { //ͨ���������
        	Log.i("demo", "���ҷ��Ͱ�ť...");
        	targetNode = null;
        	List<AccessibilityNodeInfo> list2 = nodeInfo.findAccessibilityNodeInfosByViewId(StaticData.sendId);
        	if( !list2.isEmpty() )
        		targetNode = list2.get(0);
        	//�ڶ��ֲ��ҷ���
        	if(targetNode == null)
        		targetNode = findNodeInfosByText(nodeInfo, "����");
        }

        if(targetNode != null) {
        	Log.i("demo", "������Ͱ�ť��...");
            final AccessibilityNodeInfo n = targetNode;
            performClick(n);
            StaticData.replaied++;
            
        }
        wakeAndUnlock(false);
    } 
    
    @Override
    public void onInterrupt() {
        Toast.makeText(this, "΢�����ַ����ж���~", Toast.LENGTH_LONG).show();
    }

    @Override

    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.i("demo", "����");
        //��ȡ��Դ����������
    	pm=(PowerManager)getSystemService(Context.POWER_SERVICE);
    	//�õ�����������������
	    km= (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
	    //�õ�����������������
	    kl = km.newKeyguardLock("unLock");
	    
	    editText = null;
	    //qtextView = null;
	    
	    //ע��㲥������
	    sreceiver = new ScreenOffReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(sreceiver, filter);
        
        preceiver = new PhoneReceiver();
        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
        registerReceiver(preceiver, filter);
        
        tm = (TelephonyManager)getSystemService(Service.TELEPHONY_SERVICE);  
   	 	//����һ��������
   	 	tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
	    
        Toast.makeText(this, "_�ѿ���΢�����ַ���_", Toast.LENGTH_LONG).show();
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	Log.i("demo", "�ر�");
        wakeAndUnlock(false);
        
        editText = null;
        //qtextView = null;
        
        //ע���㲥������
        unregisterReceiver(sreceiver);
        unregisterReceiver(preceiver);
        
        StaticData.total = 0;
        StaticData.replaied = 0;
        Toast.makeText(this, "_�ѹر�΢�����ַ���_", Toast.LENGTH_LONG).show();
    }
    
    //��Ļ״̬�仯�㲥������
    class ScreenOffReceiver extends BroadcastReceiver {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		if(StaticData.iscalling)
    			return;
    	    String action = intent.getAction();
    	    if (action.equals(Intent.ACTION_SCREEN_OFF)) {
    	    	Log.i("demo", "screen off");
    	        Intent lockscreen = new Intent(AutoReplyService.this, LockScreenActivity.class);
	        	lockscreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        	startActivity(lockscreen);
    	    } else if (action.equals(Intent.ACTION_SCREEN_ON)) {
    	    	Log.i("demo", "screen on");
    	    	if(canGet)
    	    		return;
    	        Intent lockscreen = new Intent(AutoReplyService.this, LockScreenActivity.class);
	        	lockscreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        	startActivity(lockscreen);
    	    }
    	}
    }
    
    //ͨ��״̬�仯�㲥��������ͨ���ڼ䲻���������
    public class PhoneReceiver extends BroadcastReceiver {
    	 @Override
    	 public void onReceive(Context context, Intent intent) {
    		 if(intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
    			 //ȥ�磨������
    			 StaticData.iscalling = true;
    			 Log.i("demo", "ȥ��");
    		 } else {
    			 StaticData.iscalling = true;
    			 Log.i("demo", "����");
    		 }
    	 }
	}
    //ͨ��״̬�仯�㲥��������ͨ���ڼ䲻���������
	private PhoneStateListener listener = new PhoneStateListener() {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
		   	//state ��ǰ״̬ incomingNumber,ò��û��ȥ���API
		 	super.onCallStateChanged(state, incomingNumber);
		 	switch(state) {
		 	case TelephonyManager.CALL_STATE_IDLE:
		 		//System.out.println("�Ҷ�");
		 		StaticData.iscalling = false;
		 		Log.i("demo", "�Ҷ�");
		 		break;
		 	case TelephonyManager.CALL_STATE_OFFHOOK:
		 		//System.out.println("����");
		 		StaticData.iscalling = true;
		 		Log.i("demo", "����");
		 		break;
		 	case TelephonyManager.CALL_STATE_RINGING:
		 		StaticData.iscalling = true;
		 		Log.i("demo", "����");
		 		//System.out.println("����:�������"+incomingNumber);
		 		break;
		   }
		}
	};
	private TelephonyManager tm;
}