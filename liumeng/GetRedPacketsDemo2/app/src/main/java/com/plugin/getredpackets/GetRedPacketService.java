package com.plugin.getredpackets;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

/**
 * Created by liumeng on 2017/5/16.
 */

public class GetRedPacketService extends AccessibilityService{

    private static final String TAG = "GetRedPacketService";
    private static final String WX_PACKAGE_NAME = "com.tencent.mm";
    private static final String WX_GET_RED_PACKET_PAGE_CLASS_NAME = "com.tencent.mm.ui.LauncherUI";
    private static final String WX_OPEN_RED_PACKET_PAGE_CLASS_NAME = "com.tencent.mm.plugin.luckymoney.ui.En_fba4b94f";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        switch(event.getEventType()){

            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                List<CharSequence> textList = event.getText();
                for(CharSequence c : textList){
                    if(((String)c).contains("[微信红包]")){
                        if(event.getParcelableData() != null && event.getParcelableData() instanceof Notification){
                            Notification notification = (Notification)event.getParcelableData();
                            PendingIntent pendingIntent = notification.contentIntent;
                            try {
                                pendingIntent.send();
                                Log.i(TAG,"模拟点击通知");
                            } catch (PendingIntent.CanceledException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                String className = event.getClassName().toString();
                Log.i(TAG,"class name = "+event.getClassName()+" ,text="+event.getText());
                if(className.equals(WX_GET_RED_PACKET_PAGE_CLASS_NAME)){
                    AccessibilityNodeInfo windowNodeInfo = getRootInActiveWindow();
                    parseNodeInfoAndOpenGetPacketDialog(windowNodeInfo);

                }else if(className.equals(WX_OPEN_RED_PACKET_PAGE_CLASS_NAME)){
                    AccessibilityNodeInfo windowNodeInfo = getRootInActiveWindow();
                    performGetRedPacket(windowNodeInfo);
                }

                break;
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                Log.i(TAG,"class name = "+event.getClassName().toString());

                break;
        }



    }


    private void parseNodeInfoAndOpenGetPacketDialog(AccessibilityNodeInfo accessibilityNodeInfo){
        if(accessibilityNodeInfo != null && accessibilityNodeInfo.getChildCount() >0){
            for(int i = 0;i<accessibilityNodeInfo.getChildCount();i++){
                AccessibilityNodeInfo child = accessibilityNodeInfo.getChild(i);
                parseNodeInfoAndOpenGetPacketDialog(child);
            }
        }else{
            if(accessibilityNodeInfo != null && !TextUtils.isEmpty(accessibilityNodeInfo.getText())&&accessibilityNodeInfo.getText().toString().contains("领取红包")){
                Log.i(TAG,"领取红包");
                if(accessibilityNodeInfo.isClickable()){
                    accessibilityNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }else{
                    AccessibilityNodeInfo parent  = accessibilityNodeInfo.getParent();
                    while(parent != null ){
                        if(parent.isClickable()){
                            parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            break;
                        }

                        parent = parent.getParent();
                        Log.i(TAG," 找到一个可以点击的节点");
                    }



                }

            }
        }

    }

    private void performGetRedPacket(AccessibilityNodeInfo accessibilityNodeInfo){
        if(accessibilityNodeInfo != null && accessibilityNodeInfo.getChildCount()>0){
            for(int i=0;i<accessibilityNodeInfo.getChildCount();i++){
                if(accessibilityNodeInfo.getChild(i).isClickable()){
                    accessibilityNodeInfo.getChild(i).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    return ;
                }else if(accessibilityNodeInfo.getChild(i).getChildCount()>0){
                    performGetRedPacket(accessibilityNodeInfo.getChild(i));
                }
            }
        }
    }



    @Override
    public void onInterrupt() {
        Log.i(TAG,"onInterrupt");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.i(TAG,"onServiceConnected");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG,"onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"onDestroy");
    }
}
