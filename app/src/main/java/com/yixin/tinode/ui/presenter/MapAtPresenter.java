package com.yixin.tinode.ui.presenter;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.ToastUtils;
import com.scoopit.weedfs.client.outer.UploadAndGetUrl;
import com.yixin.tinode.db.tinode.StoredMessage;
import com.yixin.tinode.model.data.LocationData;
import com.yixin.tinode.tinode.Cache;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.base.BasePresenter;
import com.yixin.tinode.ui.view.IMapAtView;
import com.yixin.tinode.util.LogUtils;
import com.yixin.tinode.util.StringUtils;

import co.tinode.tinodesdk.NotConnectedException;
import co.tinode.tinodesdk.PromisedReply;
import co.tinode.tinodesdk.Topic;
import co.tinode.tinodesdk.model.Drafty;
import co.tinode.tinodesdk.model.MetaSetSub;
import co.tinode.tinodesdk.model.MsgSetMeta;
import co.tinode.tinodesdk.model.ServerMessage;

public class MapAtPresenter extends BasePresenter<IMapAtView> {
    private String TAG = "MapAtPresenter";
    public MapAtPresenter(BaseActivity context) {
        super(context);
    }
    /**
     * 发送实时位置消息
     * @param locationData
     */
    public void sendRealTimePositionMessage(LocationData locationData,Topic topic){
        Drafty content = Drafty.parse(" ");
        content.insertRealTimePosition(locationData.getLat(), locationData.getLng());
        content.txt = "{\"isRTPMsg\":true,\"lat\":"+locationData.getLat()+",\"lng\":"+locationData.getLng()+"}";
        sendMessage(topic,content);
    }

    /**
     * 离开实时会话消息
     * @param locationData
     * @param topic
     */
    public void sendLeaveMessage(LocationData locationData,Topic topic){
        Drafty content = Drafty.parse(" ");
        content.insertRealTimePosition(locationData.getLat(), locationData.getLng());
        content.txt = "{\"isRTPMsg\":true,\"isLeave\":true}";
        sendMessage(topic,content);
    }
    private boolean sendMessage(Topic topic ,Drafty content) {
        if (topic != null) {
            try {
                PromisedReply<ServerMessage> reply = topic.publish(content);
                reply.thenApply(new PromisedReply.SuccessListener<ServerMessage>() {
                    @Override
                    public PromisedReply<ServerMessage> onSuccess(ServerMessage result) throws Exception {
                        // Updates message list with "delivered" icon.
//                        System.out.println("成功");
                        //群聊需要获取Delete权限，单聊暂时不支持delete。单聊的撤回方案：发送撤回消息，收到撤回消息后，删除对应的消息。
                        // 群聊撤回方案：硬删除消息，发送撤回消息
                        return null;
                    }
                }, new PromisedReply.FailureListener<ServerMessage>() {
                    @Override
                    public PromisedReply<ServerMessage> onFailure(Exception err) throws Exception {
//                        System.out.println("失败");
                        return null;
                    }
                });
            } catch (NotConnectedException ignored) {
                Log.d(TAG, "sendMessage -- NotConnectedException", ignored);
            } catch (Exception ignored) {
                Log.d(TAG, "sendMessage -- Exception", ignored);
//                Toast.makeText(mContext, R.string.failed_to_send_message, Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }
        return false;
    }


}
