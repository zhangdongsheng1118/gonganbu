package com.starrtc.demo.listener;

import com.starrtc.demo.demo.MLOC;
import com.starrtc.demo.database.CoreDB;
import com.starrtc.demo.database.HistoryBean;
import com.starrtc.demo.utils.AEvent;
import com.starrtc.starrtcsdk.apiInterface.IXHVoipManagerListener;
import com.starrtc.starrtcsdk.socket.StarErrorCode;

import java.text.SimpleDateFormat;

public class XHVoipManagerListener implements IXHVoipManagerListener {
    @Override
    public void onCalling(String fromID) {
        HistoryBean historyBean = new HistoryBean();
        historyBean.setType(CoreDB.HISTORY_TYPE_VOIP);
        historyBean.setLastTime(new SimpleDateFormat("MM-dd HH:mm").format(new java.util.Date()));
        historyBean.setConversationId(fromID);
        historyBean.setNewMsgCount(1);
        MLOC.addHistory(historyBean,false);
        AEvent.notifyListener(AEvent.AEVENT_VOIP_REV_CALLING,true,fromID);
    }

    @Override
    public void onAudioCalling(String fromID) {
        HistoryBean historyBean = new HistoryBean();
        historyBean.setType(CoreDB.HISTORY_TYPE_VOIP);
        historyBean.setLastTime(new SimpleDateFormat("MM-dd HH:mm").format(new java.util.Date()));
        historyBean.setConversationId(fromID);
        historyBean.setNewMsgCount(1);
        MLOC.addHistory(historyBean,false);
        AEvent.notifyListener(AEvent.AEVENT_VOIP_REV_CALLING_AUDIO,true,fromID);
    }

    @Override
    public void onCancled(String fromID) {

    }

    @Override
    public void onRefused(String fromID) {
        AEvent.notifyListener(AEvent.AEVENT_VOIP_REV_REFUSED,true,fromID);
    }

    @Override
    public void onBusy(String fromID) {
        AEvent.notifyListener(AEvent.AEVENT_VOIP_REV_BUSY,true,fromID);
    }

    @Override
    public void onMiss(String fromID) {
        HistoryBean historyBean = new HistoryBean();
        historyBean.setType(CoreDB.HISTORY_TYPE_VOIP);
        historyBean.setLastTime(new SimpleDateFormat("MM-dd HH:mm").format(new java.util.Date()));
        historyBean.setConversationId(fromID);
        historyBean.setNewMsgCount(1);
        MLOC.addHistory(historyBean,false);

        AEvent.notifyListener(AEvent.AEVENT_VOIP_REV_MISS,true,fromID);
    }

    @Override
    public void onConnected(String fromID) {
        AEvent.notifyListener(AEvent.AEVENT_VOIP_REV_CONNECT,true,fromID);
    }

    @Override
    public void onHangup(String fromID) {
        AEvent.notifyListener(AEvent.AEVENT_VOIP_REV_HANGUP,true,fromID);
    }

    @Override
    public void onError(String errorCode) {
        AEvent.notifyListener(AEvent.AEVENT_VOIP_REV_ERROR,true, StarErrorCode.getErrorCode(errorCode));
    }

    @Override
    public void onReceiveRealtimeData(byte[] data) {

    }

    @Override
    public void onTransStateChanged(int state) {
        AEvent.notifyListener(AEvent.AEVENT_VOIP_TRANS_STATE_CHANGED,true, state);
    }
}
