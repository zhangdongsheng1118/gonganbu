package com.yixin.tinode.db.tinode;


import android.database.Cursor;

import java.util.Date;
import java.util.Map;

import co.tinode.tinodesdk.Storage;
import co.tinode.tinodesdk.model.Drafty;
import co.tinode.tinodesdk.model.MsgServerData;

/**
 * StoredMessage fetched from the database
 */
public class StoredMessage extends MsgServerData implements Storage.Message {
    public long id;
    public long topicId;
    public long userId;
    public int status;
    //检索关键字
    public String search;

    public StoredMessage() {
    }

    public StoredMessage(MsgServerData m) {
        topic = m.topic;
        head = m.head;
        from = m.from;
        ts = m.ts;
        seq = m.seq;
        content = m.content;
    }

    public StoredMessage(MsgServerData m, int status) {
        this(m);
        this.status = status;
    }

    public static StoredMessage readMessage(Cursor c) {
        StoredMessage msg = new StoredMessage();

        msg.id = c.getLong(MessageDb.COLUMN_IDX_ID);
        msg.topicId = c.getLong(MessageDb.COLUMN_IDX_TOPIC_ID);
        msg.userId = c.getLong(MessageDb.COLUMN_IDX_USER_ID);
        msg.status = c.getInt(MessageDb.COLUMN_IDX_STATUS);
        msg.from = c.getString(MessageDb.COLUMN_IDX_SENDER);
        msg.seq = c.getInt(MessageDb.COLUMN_IDX_SEQ);
        msg.ts = new Date(c.getLong(MessageDb.COLUMN_IDX_TS));
        msg.content = BaseDb.deserialize(c.getBlob(MessageDb.COLUMN_IDX_CONTENT));
        msg.search = c.getString(MessageDb.COLUMN_IDX_SEARCH);

        return msg;
    }

    public static int readSeqId(Cursor c) {
        return c.getInt(0);
    }

    public boolean isMine() {
        return BaseDb.isMe(from);
    }

    @Override
    public Drafty getContent() {
        return content;
    }

    @Override
    public long getId() {
        return id;
    }

    public Map<String, Object> getHeader() {
        return head;
    }

    @Override
    public int getSeqId() {
        return seq;
    }

    @Override
    public boolean isDraft() {
        return status == BaseDb.STATUS_DRAFT;
    }

    @Override
    public boolean isReady() {
        return status == BaseDb.STATUS_QUEUED;
    }

    @Override
    public boolean isFailed() {
        return status == BaseDb.STATUS_ERROR;
    }

    @Override
    public boolean isDeleted() {
        return status == BaseDb.STATUS_DELETED_SOFT || status == BaseDb.STATUS_DELETED_HARD;
    }

    @Override
    public boolean isDeleted(boolean hard) {
        return hard ? status == BaseDb.STATUS_DELETED_HARD : status == BaseDb.STATUS_DELETED_SOFT;
    }

    @Override
    public boolean isSynced() {
        return status == BaseDb.STATUS_SYNCED;
    }
}
