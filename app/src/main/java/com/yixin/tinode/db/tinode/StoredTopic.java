package com.yixin.tinode.db.tinode;

import android.database.Cursor;
import android.util.Log;

import java.util.Date;

import co.tinode.tinodesdk.LocalData;
import co.tinode.tinodesdk.Topic;
import co.tinode.tinodesdk.model.VCard;

/**
 * Representation of a topic stored in a database;
 */
public class StoredTopic implements LocalData.Payload {
    private static final String TAG = "StoredTopic";

    public long id;
    public Date lastUsed;
    public int minLocalSeq;
    public int maxLocalSeq;
    public int status;
    public int nextUnsentId;

    public StoredTopic() {
    }

    @SuppressWarnings("unchecked")
    protected static void deserialize(Topic topic, Cursor c) {
        StoredTopic st = new StoredTopic();

        st.id = c.getLong(TopicDb.COLUMN_IDX_ID);
        st.status = c.getInt(TopicDb.COLUMN_IDX_STATUS);
        st.lastUsed = new Date(c.getLong(TopicDb.COLUMN_IDX_LASTUSED));
        st.minLocalSeq = c.getInt(TopicDb.COLUMN_IDX_MIN_LOCAL_SEQ);
        st.maxLocalSeq = c.getInt(TopicDb.COLUMN_IDX_MAX_LOCAL_SEQ);
        st.nextUnsentId = c.getInt(TopicDb.COLUMN_IDX_NEXT_UNSENT_SEQ);

        topic.setUpdated(new Date(c.getLong(TopicDb.COLUMN_IDX_UPDATED)));
        topic.setTouched(st.lastUsed);

        topic.setRead(c.getInt(TopicDb.COLUMN_IDX_READ));
        topic.setRecv(c.getInt(TopicDb.COLUMN_IDX_RECV));
        topic.setSeq(c.getInt(TopicDb.COLUMN_IDX_SEQ));
        topic.setClear(c.getInt(TopicDb.COLUMN_IDX_CLEAR));
        topic.setMaxDel(c.getInt(TopicDb.COLUMN_IDX_MAX_DEL));
        topic.setMsgBeginId(topic.getRecv());

        topic.setTags(BaseDb.deserializeTags(c.getString(TopicDb.COLUMN_IDX_TAGS)));
        topic.setPub(BaseDb.deserialize(c.getBlob(TopicDb.COLUMN_IDX_PUBLIC)));
        topic.setPriv(BaseDb.deserialize(c.getBlob(TopicDb.COLUMN_IDX_PRIVATE)));

        topic.setAccessMode(BaseDb.deserializeMode(c.getString(TopicDb.COLUMN_IDX_ACCESSMODE)));
        topic.setDefacs(BaseDb.deserializeDefacs(c.getString(TopicDb.COLUMN_IDX_DEFACS)));
        topic.setIsTop(c.getInt(TopicDb.COLUMN_IDX_TOP));
        topic.setState(c.getInt(TopicDb.COLUMN_IDX_STATE));
        topic.setLastAct(c.getString(TopicDb.COLUMN_IDX_LASTACT));
        topic.setLastMsg(c.getString(TopicDb.COLUMN_IDX_LASTMSG));

        try {
            if (c.getColumnCount() > TopicDb.COLUMN_IDX_SEARCH) {
                topic.setLastMsg(c.getString(TopicDb.COLUMN_IDX_SEARCH));
            }
            if (c.getColumnCount() > TopicDb.COLUMN_IDX_TS) {
                Long time = c.getLong(TopicDb.COLUMN_IDX_TS);
                if (time != null) {
                    topic.setLastTs(new Date(time));
                }
            }
            if (c.getColumnCount() > TopicDb.COLUMN_IDX_PUB) {
                byte[] bytes = c.getBlob(TopicDb.COLUMN_IDX_PUB);
                if (bytes != null) {
                    VCard vCard = (VCard) BaseDb.deserialize(bytes);
                    if (vCard != null) {
                        topic.setUserName(vCard.fn);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("", e.getMessage());
        }

        topic.setLocal(st);
    }

    public static long getId(Topic topic) {
        StoredTopic st = (StoredTopic) topic.getLocal();
        return st != null ? st.id : -1;
    }

    public static boolean isAllDataLoaded(Topic topic) {
        StoredTopic st = (StoredTopic) topic.getLocal();
        Log.d(TAG, "Is all data loaded? " + (st == null ? "st=null" : "st.min=" + st.minLocalSeq) + ", topic.seq=" + topic.getSeq());
        return topic.getSeq() == 0 || (st != null && st.minLocalSeq == 1);
    }

    public static boolean isAllDataLoaded(Topic topic, int min) {
        StoredTopic st = (StoredTopic) topic.getLocal();
        Log.d(TAG, "Is all data loaded? " + (st == null ? "st=null" : "st.min=" + st.minLocalSeq) + ", topic.seq=" + topic.getSeq());
        return topic.getSeq() == 0 || (st != null && st.minLocalSeq <= min);
    }
}
