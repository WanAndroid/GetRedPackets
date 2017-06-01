package com.android.lseven.wxredplugin;


import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;


public class SelfUtil {

    private static String sender;
    private static String content;
    private static String sendTime;

    public static String getRedSenderInfo(AccessibilityNodeInfo node) {
        AccessibilityNodeInfo nodeParent = node.getParent();
        if ("android.widget.ListView".equals(nodeParent)) return null;
        if (!TextUtils.isEmpty(nodeParent.getChild(0).getText()))
            content = nodeParent.getChild(0).getText().toString();//get content
        nodeParent = nodeParent.getParent();
        if (null==nodeParent)return null;
        if (!TextUtils.isEmpty(nodeParent.getChild(0).getText()))
            sendTime = nodeParent.getChild(0).getText().toString();//get Time
        for (int i = 0; i < nodeParent.getChildCount(); i++) {
            AccessibilityNodeInfo nodeChild = nodeParent.getChild(i);
            if ("android.widget.ImageView".equals(nodeChild.getClassName())) {
                CharSequence contentDescription = nodeChild.getContentDescription();
                if (null != contentDescription)
                    sender = contentDescription.toString().replaceAll("头像$", ""); //get sender
            }
        }
        Log.e("LSeven", " red info===>" + sender + " " + content + " " + sendTime);
        return sender+"|"+content+"|"+sendTime;
    }

}
