package com.yixin.tinode.db.tinode;


import android.database.Cursor;

import java.util.Date;

import co.tinode.tinodesdk.model.VCard;

/**
 * StoredMessage fetched from the database
 */
public class StoredMessageForSearch extends StoredMessage {

    //处理过后的viewType,搜索列表里使用
    public int viewType;
    //0联系人1聊天记录
    public int dataType;
    public VCard pub;
    public int count;
    public long msgId;

    public StoredMessageForSearch() {
    }

    //uid as topic,pub,0 as dataType,updated as ts,'' as search,1 as count
    public static StoredMessageForSearch readMessage(Cursor c) {
        StoredMessageForSearch msg = new StoredMessageForSearch();

        msg.from = c.getString(0);
        msg.pub = BaseDb.deserialize(c.getBlob(1));
        msg.dataType = c.getInt(2);
        msg.ts = new Date(c.getLong(3));
        msg.search = c.getString(4);
        msg.count = c.getInt(5);
        msg.msgId = c.getLong(6);

        return msg;
    }
}
