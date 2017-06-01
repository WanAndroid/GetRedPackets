package com.android.lseven.wxredplugin;


import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;


public class WXRedPluginService extends AccessibilityService {

    private String currentActivityName;
    private AccessibilityNodeInfo rootNodeInfo;
    private AccessibilityNodeInfo unOpenNode;
    /**
     * 已领取红包标识
     */
    private StringBuilder openedIdSet = new StringBuilder();
    /**
     * 当前需领取红包标识
     */
    private String curUnOpenId;


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        setCurrentActivityName(event);
        monitorNotifications(event);
        monitorChat(event);
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        openedIdSet = null;
    }

    private void setCurrentActivityName(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;
        try {
            ComponentName componentName = new ComponentName(event.getPackageName().toString(), event.getClassName().toString());
            getPackageManager().getActivityInfo(componentName, 0);
            currentActivityName = componentName.flattenToShortString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * notification监控
     */
    private void monitorNotifications(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) return;
        if (event.getText() == null ||
                (event.getText().toString() != null && !event.getText().toString().contains(SelfConst.WX_RED_NOTIFICATION_TEXT)))
            return;
        //模拟点击Notification
        if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
            Notification notification = (Notification) event.getParcelableData();
            try {
                notification.contentIntent.send();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * chat聊天基面监控
     *
     * @param event
     */
    private void monitorChat(AccessibilityEvent event) {

        rootNodeInfo = getRootInActiveWindow();
        if (null == rootNodeInfo) return;
        unOpenNode = null;
        //领取红包
        getLastNodeInfo(SelfConst.WX_RED_WILL_OPEN_TEXT);
        //点击“领取红包”
        Log.e("LSeven", "已经领取红包标识集合==>" + openedIdSet);
        if (null != unOpenNode) {
            curUnOpenId = SelfUtil.getRedSenderInfo(unOpenNode);
            unOpenNode.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
        //开红包
        AccessibilityNodeInfo openButton = getOpenButton(rootNodeInfo);
        if (null != openButton) {
            Log.e("LSeven", "开红包===>");
            openButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
        //红包详情
        if (currentActivityName.contains("LuckyMoneyDetailUI")) {
            boolean detail = getLastNodeInfo(SelfConst.WX_RED_WILL_DETAIL_TEXT);
            if (detail) {
                performGlobalAction(GLOBAL_ACTION_BACK);//返回
                Log.e("LSeven", "红包详情 curUnOpenId =>" + curUnOpenId);
                if (null != curUnOpenId && !openedIdSet.toString().contains(curUnOpenId))
                    openedIdSet.append(curUnOpenId).append("||");
                Log.e("LSeven", "红包详情 已经领取红包标识集合=>" + openedIdSet);
            }
        }
    }

    private boolean getLastNodeInfo(String... texts) {
        for (String text : texts) {
            List<AccessibilityNodeInfo> nodes = rootNodeInfo.findAccessibilityNodeInfosByText(text);
            if (null != nodes || !nodes.isEmpty()) {
                Log.e("LSeven", "nodes==>" + nodes.size());
                AccessibilityNodeInfo lastNode = null;
                for (AccessibilityNodeInfo node : nodes) {
                    //模拟点击领取
                    if (text.equals(SelfConst.WX_RED_WILL_DETAIL_TEXT)) {
                        Log.e("LSeven", "红包详情==>");
                        return true;
                    } else {
                        Log.e("LSeven", "领取红包==>");
                        if (!openedIdSet.toString().contains(SelfUtil.getRedSenderInfo(node)))
                            lastNode = node;
                    }
                }
                unOpenNode = lastNode;
                if (null != unOpenNode) return true;
            }
        }
        return false;
    }

    private AccessibilityNodeInfo getOpenButton(AccessibilityNodeInfo node) {
        if (null == node) return null;
        if (node.getChildCount() == 0) {
            if ("android.widget.Button".equals(node.getClassName()))
                return node;
            else
                return null;
        }
        AccessibilityNodeInfo button;
        for (int i = 0; i < node.getChildCount(); i++) {
            button = getOpenButton(node.getChild(i));
            if (button != null)
                return button;
        }
        return null;
    }


}
