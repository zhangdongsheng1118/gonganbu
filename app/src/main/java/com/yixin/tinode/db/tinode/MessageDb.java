package com.yixin.tinode.db.tinode;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.util.Log;

import com.tencent.bugly.crashreport.BuglyLog;
import com.yixin.tinode.ui.adapter.SessionAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import co.tinode.tinodesdk.Topic;
import co.tinode.tinodesdk.model.Drafty;

/**
 * Storage structure for messages:
 * public String id -- not stored
 * public String topic -> as topic_id
 * public String from; -> as user_id
 * public Map head -- not stored yet
 * public Date ts;
 * public int seq;
 * public T content;
 */
public class MessageDb implements BaseColumns {
    private static final String TAG = "MessageDb";

    /**
     * Content provider authority.
     */
    private static final String CONTENT_AUTHORITY = "com.yixin.tinode";

    /**
     * Base URI. (content://co.tinode.tindroid)
     */
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * The name of the main table.
     */
    public static final String TABLE_NAME = "messages";

    /**
     * Topic ID, references topics._ID
     */
    private static final String COLUMN_NAME_TOPIC_ID = "topic_id";
    /**
     * Id of the originator of the message, references users._ID
     */
    private static final String COLUMN_NAME_USER_ID = "user_id";
    /**
     * Status of the message: unsent, delivered, deleted
     */
    private static final String COLUMN_NAME_STATUS = "status";
    /**
     * Uid as string. Deserialized here to avoid a join.
     */
    private static final String COLUMN_NAME_SENDER = "sender";
    /**
     * Message timestamp
     */
    private static final String COLUMN_NAME_TS = "ts";
    /**
     * Server-issued sequence ID, integer, indexed
     */
    private static final String COLUMN_NAME_SEQ = "seq";
    /**
     * Content MIME type
     */
    private static final String COLUMN_NAME_MIME = "mime";
    /**
     * Serialized message content
     */
    private static final String COLUMN_NAME_CONTENT = "content";
    /**
     * 检索字段
     */
    private static final String COLUMN_NAME_SEARCH = "search";


    /**
     * SQL statement to create Messages table
     */
    static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME_TOPIC_ID
                    + " REFERENCES " + TopicDb.TABLE_NAME + "(" + TopicDb._ID + ")," +
                    COLUMN_NAME_USER_ID
                    + " REFERENCES " + UserDb.TABLE_NAME + "(" + UserDb._ID + ")," +
                    COLUMN_NAME_STATUS + " INT," +
                    COLUMN_NAME_SENDER + " TEXT," +
                    COLUMN_NAME_TS + " INT," +
                    COLUMN_NAME_SEQ + " INT," +
                    COLUMN_NAME_MIME + " TEXT," +
                    COLUMN_NAME_SEARCH + " TEXT," +
                    COLUMN_NAME_CONTENT + " BLOB)";
    /**
     * SQL statement to drop Messages table.
     */
    static final String DROP_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    /**
     * The name of index: messages by topic and sequence.
     */
    private static final String INDEX_NAME = "message_topic_id_seq";
    /**
     * Drop the index too
     */
    static final String DROP_INDEX =
            "DROP INDEX IF EXISTS " + INDEX_NAME;
    /**
     * Add index on account_id-topic-seq, in descending order
     */
    static final String CREATE_INDEX =
            "CREATE INDEX " + INDEX_NAME +
                    " ON " + TABLE_NAME + " (" +
                    COLUMN_NAME_TOPIC_ID + "," +
                    COLUMN_NAME_TS + " DESC)";

    /**
     * Path component for "message"-type resources..
     */
    private static final String PATH_MESSAGES = "messages";
    /**
     * URI for "messages" resource.
     */
    public static final Uri CONTENT_URI =
            BASE_CONTENT_URI.buildUpon().appendPath(PATH_MESSAGES).build();

    static final int COLUMN_IDX_ID = 0;
    static final int COLUMN_IDX_TOPIC_ID = 1;
    static final int COLUMN_IDX_USER_ID = 2;
    static final int COLUMN_IDX_STATUS = 3;
    static final int COLUMN_IDX_SENDER = 4;
    static final int COLUMN_IDX_TS = 5;
    static final int COLUMN_IDX_SEQ = 6;
    static final int COLUMN_IDX_MIME = 7;
    static final int COLUMN_IDX_CONTENT = 9;
    static final int COLUMN_IDX_SEARCH = 8;

    /**
     * 判断是不是 实时位置消息
     *
     * @param msg
     * @return
     */
    public static boolean isRTPMsg(StoredMessage msg) {
        Drafty content = msg.content;
        Drafty.Style[] fmt = content.getStyles();
        if (fmt != null) {
            Drafty.Entity entity;
            for (Drafty.Style style : fmt) {
                CharacterStyle span = null;
                int offset = -1, length = -1;
                String tp = style.getType();
                entity = content.getEntity(style);

                if (entity != null) {
                    tp = entity.getType();
                }

                if (tp == null) {
                    Log.d(TAG, "Null type in " + style.toString());
                    continue;
                }
                if ("RTP".equals(tp)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Save message to DB
     *
     * @return ID of the newly added message
     */
    static long insert(SQLiteDatabase db, Topic topic, StoredMessage msg) {
        if (isRTPMsg(msg)) {
            return -1;
        }
        Log.e(TAG, "MessageDb insert:" + msg.id);
        if (msg.id > 0) {
            return msg.id;
        }

        db.beginTransaction();
        try {
            if (msg.topicId <= 0) {
                msg.topicId = TopicDb.getId(db, msg.topic);
            }
            if (msg.userId <= 0) {
                msg.userId = UserDb.getId(db, msg.from);
            }

            if (msg.userId <= 0 || msg.topicId <= 0) {
                Log.d(TAG, "Failed to insert message " + msg.seq);
                return -1;
            }

            int status;
            if (msg.seq == 0) {
                msg.seq = TopicDb.getNextUnsentSeq(db, topic);
                status = msg.status == BaseDb.STATUS_UNDEFINED ? BaseDb.STATUS_QUEUED : msg.status;
            } else if (msg.seq == -1) {
                msg.seq = getNextUnsentId(db, msg.topicId);
                status = BaseDb.STATUS_PRE_QUEUED;
            } else {
                status = BaseDb.STATUS_SYNCED;
            }

            // Convert message to a map of values
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME_TOPIC_ID, msg.topicId);
            values.put(COLUMN_NAME_USER_ID, msg.userId);
            values.put(COLUMN_NAME_STATUS, status);
            values.put(COLUMN_NAME_SENDER, msg.from);
            values.put(COLUMN_NAME_TS, msg.ts.getTime());
            values.put(COLUMN_NAME_SEQ, msg.seq);
            values.put(COLUMN_NAME_MIME, (String) msg.getHeader("mime"));
            values.put(COLUMN_NAME_CONTENT, BaseDb.serialize(msg.content));
            values.put(COLUMN_NAME_SEARCH, SessionAdapter.getMsgText(msg, ""));
            BuglyLog.d(TAG,SessionAdapter.getMsgText(msg, ""));
            msg.id = db.insertOrThrow(TABLE_NAME, null, values);
            db.setTransactionSuccessful();
        } catch (Exception ex) {
            Log.w(TAG, "Insert failed", ex);
        } finally {
            db.endTransaction();
        }

        Log.e(TAG, "MessageDb after insert:" + msg.id);

        return msg.id;
    }

    static boolean updateStatusAndContent(SQLiteDatabase db, long msgId, int status, Object
            content) {
        ContentValues values = new ContentValues();
        if (status != BaseDb.STATUS_UNDEFINED) {
            values.put(COLUMN_NAME_STATUS, status);
        }
        if (content != null) {
            values.put(COLUMN_NAME_CONTENT, BaseDb.serialize(content));
        }

        if (values.size() > 0) {
            return db.update(TABLE_NAME, values, _ID + "=" + msgId, null) > 0;
        }
        return false;
    }

    static boolean delivered(SQLiteDatabase db, long msgId, Date timestamp, int seq) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_STATUS, BaseDb.STATUS_SYNCED);
        values.put(COLUMN_NAME_TS, timestamp.getTime());
        values.put(COLUMN_NAME_SEQ, seq);

        return db.update(TABLE_NAME, values, _ID + "=" + msgId, null) > 0;
    }


    static boolean update(SQLiteDatabase db, StoredMessage msg) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_CONTENT, BaseDb.serialize(msg.content));
        values.put(COLUMN_NAME_STATUS, BaseDb.STATUS_QUEUED);

        return db.update(TABLE_NAME, values, _ID + "=" + msg.getId(), null) > 0;
    }

    /**
     * Query messages. To select all messages set <b>from</b> and <b>to</b> equal to -1.
     *
     * @param db database to select from;
     * @return cursor with the messages
     */
    public static StoredMessage readOne(SQLiteDatabase db, long id) {
        String sql = "SELECT * FROM " + TABLE_NAME +
                " WHERE " +
                _ID + "=" + id;

        // Log.d(TAG, "Sql=[" + sql + "]");

        Cursor c = db.rawQuery(sql, null);
        c.moveToNext();
        StoredMessage message = StoredMessage.readMessage(c);
        c.close();
        return message;
    }


    /**
     * Query messages. To select all messages set <b>from</b> and <b>to</b> equal to -1.
     *
     * @param db      database to select from;
     * @param topicId Tinode topic ID (topics._id) to select from
     * @param from    minimum seq value to select, exclusive
     * @param to      maximum seq value to select, inclusive
     * @return cursor with the messages
     */
    public static Cursor query(SQLiteDatabase db, long topicId, int from, int to, int limit) {
        String sql = "SELECT * FROM " + TABLE_NAME +
                " WHERE " +
                COLUMN_NAME_TOPIC_ID + "=" + topicId +
                (from > 0 ? " AND " + COLUMN_NAME_SEQ + ">" + from : "") +
                (to > 0 ? " AND " + COLUMN_NAME_SEQ + "<=" + to : "") +
                " AND " + COLUMN_NAME_STATUS + "<=" + BaseDb.STATUS_VISIBLE +
                " ORDER BY " + COLUMN_NAME_TS +
                (limit > 0 ? " LIMIT " + limit : "");

        // Log.d(TAG, "Sql=[" + sql + "]");
        return db.rawQuery(sql, null);
    }

    /**
     * 根据msgid获取msg所在页号
     **/
    public static int getPageFromMsgId(SQLiteDatabase db, long topicId, long msgId, int pageSize) {
        int page = 1;
        String sql = "SELECT count(*) as page FROM " + TABLE_NAME +
                " WHERE " +
                COLUMN_NAME_TOPIC_ID + "=" + topicId +
                " AND " + COLUMN_NAME_STATUS + "<=" + BaseDb.STATUS_VISIBLE +
                " ORDER BY " + COLUMN_NAME_TS + " DESC ";

        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToNext()) {
            int count = cursor.getInt(0);
            if (count > pageSize) {
                if (count % pageSize == 0) {
                    page = count / pageSize;
                } else {
                    page = count / pageSize + 1;
                }
            }
        }

        cursor.close();
        return page;
    }

    /**
     * Query messages. To select all messages set <b>from</b> and <b>to</b> equal to -1.
     *
     * @param db        database to select from;
     * @param topicId   Tinode topic ID (topics._id) to select from
     * @param pageCount number of pages to return
     * @param pageSize  number of messages per page
     * @return cursor with the messages
     */
    public static Cursor query(SQLiteDatabase db, long topicId, int pageCount, int pageSize) {
//        String sql = "SELECT * FROM " + TABLE_NAME +
//                " WHERE " +
//                COLUMN_NAME_TOPIC_ID + "=" + topicId +
//                " AND " + COLUMN_NAME_STATUS + "<=" + BaseDb.STATUS_VISIBLE +
//                " ORDER BY " + COLUMN_NAME_TS + " DESC LIMIT " + (pageCount * pageSize);
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " +
                COLUMN_NAME_TOPIC_ID + "=" + topicId + " AND " + COLUMN_NAME_STATUS + "<=" + BaseDb.STATUS_VISIBLE +
                " ORDER BY " + _ID + " DESC LIMIT " + (pageCount * pageSize);

        // Log.d(TAG, "Sql=[" + sql + "]");
        return db.rawQuery(sql, null);
    }

    /**
     * Query messages. To select all messages set <b>from</b> and <b>to</b> equal to -1.
     *
     * @param db    database to select from;
     * @param msgId _id of the message to retrieve.
     * @return cursor with the message.
     */
    static Cursor getMessageById(SQLiteDatabase db, long msgId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE _id=" + msgId;

        return db.rawQuery(sql, null);
    }

    /**
     * Query messages. To select all messages set <b>from</b> and <b>to</b> equal to -1.
     *
     * @param db        database to select from;
     * @param topicId   Tinode topic ID (topics._id) to select from
     * @param pageCount number of pages to return
     * @param pageSize  number of messages per page
     * @return cursor with the messages
     */
    public static Cursor query(SQLiteDatabase db, long topicId, int pageCount,
                               int pageSize, String key) {
        String sql = "SELECT * FROM " + TABLE_NAME +
                " WHERE " +
                COLUMN_NAME_TOPIC_ID + "=" + topicId +
                " AND " + COLUMN_NAME_STATUS + "<=" + BaseDb.STATUS_VISIBLE +
                " AND " + COLUMN_NAME_SEARCH + " like '%" + key +
                "%' ORDER BY " + COLUMN_NAME_TS + " DESC LIMIT " + (pageCount * pageSize);

        // Log.d(TAG, "Sql=[" + sql + "]");

        return db.rawQuery(sql, null);
    }

    /****列表数据，类型0联系人1聊天记录。首个显示标题。两个类型之间显示分割线。超过三个显示更多选项。*/
    public static List<StoredMessageForSearch> searchList(SQLiteDatabase db, String key) {
        long accountId = BaseDb.getInstance().getAccountId();
        List<StoredMessageForSearch> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("select name as topic,public,0 as dataType,updated as ts,'' as sear,1 as count,0 as msgId  from topics where account_id=");
        sql.append(accountId);
        sql.append(" and sear like'%");
        sql.append(key);
        sql.append("%'  union ");
        sql.append("select topic.name as topic,topic.public,1 as dataType,msg.ts,msg.search,msg.count, msg._id as msgId from ");
        sql.append("(select * from topics where account_id=");
        sql.append(accountId);
        sql.append(") topic ");
        sql.append(" inner join ");
        sql.append("(select search,content,ts,user_id,count(topic_id) as count,topic_id,_id from messages where status<=");
        sql.append(BaseDb.STATUS_VISIBLE);
        sql.append(" and search like '%");
        sql.append(key);
        sql.append("%' group by topic_id having max(ts)) msg");
        sql.append(" on topic._id=msg.topic_id  ");
        sql.append(" inner join  ");
        sql.append("(select _id,pub from users where account_id=");
        sql.append(accountId);
        sql.append(") users     on msg.user_id=users._id  order by dataType asc, msg.ts desc limit 100");
        Cursor cursor = db.rawQuery(sql.toString(), null);
        while (cursor.moveToNext()) {
            list.add(StoredMessageForSearch.readMessage(cursor));
        }
        cursor.close();
        return list;
    }

    public static List<StoredMessage> queryList(SQLiteDatabase db, long topicId, int pageCount,
                                                int pageSize, String key) {
        List<StoredMessage> list = new ArrayList<>();
        Cursor cursor = query(db, topicId, pageCount, pageSize, key);
        while (cursor.moveToNext()) {
            list.add(StoredMessage.readMessage(cursor));
        }
        cursor.close();
        return list;
    }

    /**
     * Query messages which has not been sent yet.
     *
     * @param db      database to select from;
     * @param topicId Tinode topic ID (topics._id) to select from
     * @return cursor with the messages
     */
    public static Cursor queryUnsent(SQLiteDatabase db, long topicId) {
        String sql = "SELECT * FROM " + TABLE_NAME +
                " WHERE " +
                COLUMN_NAME_TOPIC_ID + "=" + topicId + " AND " + COLUMN_NAME_SEQ + "<=0 "
                + " AND " + COLUMN_NAME_STATUS + "!=" + BaseDb.STATUS_PRE_QUEUED +
                " ORDER BY " + COLUMN_NAME_TS;
        // Log.d(TAG, "Sql=[" + sql + "]");

        return db.rawQuery(sql, null);
    }

    /**
     * Query messages marked for deletion but not deleted yet.
     *
     * @param db      database to select from;
     * @param topicId Tinode topic ID (topics._id) to select from;
     * @param hard    if true to return hard-deleted messages, soft-deleted otherwise.
     * @return cursor with the message seqIDs
     */
    static Cursor queryDeleted(SQLiteDatabase db, long topicId, boolean hard) {
        int status = hard ? BaseDb.STATUS_DELETED_HARD : BaseDb.STATUS_DELETED_SOFT;

        String sql = "SELECT " + COLUMN_NAME_SEQ + " FROM " + TABLE_NAME +
                " WHERE " + COLUMN_NAME_TOPIC_ID + "=" + topicId +
                " AND " + COLUMN_NAME_STATUS + "=" + status +
                " ORDER BY " + COLUMN_NAME_TS;

        return db.rawQuery(sql, null);
    }

    /**
     * Mark sent messages as deleted without actually deleting them. Delete unsent messages.
     *
     * @param db         Database to use.
     * @param doDelete   delete messages instead of marking them deleted.
     * @param topicId    Tinode topic ID to delete messages from.
     * @param fromId     minimum seq value to delete, inclusive (closed).
     * @param toId       maximum seq value to delete, exclusive (open).
     * @param list       list of message IDs to delete.
     * @param markAsHard mark messages as hard-deleted.
     * @return true if some messages were updated or deleted, false otherwise
     */
    private static boolean deleteOrMarkDeleted(SQLiteDatabase db, boolean doDelete,
                                               long topicId,
                                               int fromId, int toId, List<Integer> list, boolean markAsHard) {
        int affected = 0;
        db.beginTransaction();
        String messageSelector;
        if (list != null) {
            StringBuilder sb = new StringBuilder();
            for (int i : list) {
                sb.append(",");
                sb.append(i);
            }
            sb.deleteCharAt(0);
            messageSelector = COLUMN_NAME_SEQ + " IN (" + sb.toString() + ")";
        } else {
            ArrayList<String> parts = new ArrayList<>();
            if (fromId > 0) {
                parts.add(COLUMN_NAME_SEQ + ">=" + fromId);
            }
            if (toId != -1) {
                parts.add(COLUMN_NAME_SEQ + "<" + toId);
            }
            messageSelector = TextUtils.join(" AND ", parts);
        }

        if (!TextUtils.isEmpty(messageSelector)) {
            messageSelector = " AND " + messageSelector;
        }

        try {
            if (!doDelete) {
                // Mark sent messages as deleted
                ContentValues values = new ContentValues();
                values.put(COLUMN_NAME_STATUS, markAsHard ? BaseDb.STATUS_DELETED_HARD : BaseDb.STATUS_DELETED_SOFT);
                affected = db.update(TABLE_NAME, values, COLUMN_NAME_TOPIC_ID + "=" + topicId +
                        messageSelector +
                        " AND " + COLUMN_NAME_STATUS + "=" + BaseDb.STATUS_SYNCED, null);
            }
            // Unsent messages are deleted.
            affected += db.delete(TABLE_NAME, COLUMN_NAME_TOPIC_ID + "=" + topicId +
                    messageSelector +
                    // Either delete all messages or just unsent+draft messages.
                    (doDelete ? "" : " AND " + COLUMN_NAME_STATUS + "<=" + BaseDb.STATUS_QUEUED), null);
            db.setTransactionSuccessful();
        } catch (SQLException ex) {
            Log.w(TAG, "Delete failed", ex);
        } finally {
            db.endTransaction();
        }
        return affected > 0;
    }

    /**
     * Mark sent messages as deleted without actually deleting them. Delete unsent messages.
     *
     * @param db         Database to use.
     * @param topicId    Tinode topic ID to delete messages from.
     * @param list       list of message IDs to delete.
     * @param markAsHard mark messages as hard-deleted.
     * @return true if some messages were updated or deleted, false otherwise
     */
    static boolean markDeleted(SQLiteDatabase db, long topicId, List<Integer> list,
                               boolean markAsHard) {
        return deleteOrMarkDeleted(db, false, topicId, Integer.MAX_VALUE, 0, list, markAsHard);
    }

    /**
     * Mark sent messages as deleted without actually deleting them. Delete unsent messages.
     *
     * @param db         Database to use.
     * @param topicId    Tinode topic ID to delete messages from.
     * @param fromId     minimum seq value to delete, inclusive (closed).
     * @param toId       maximum seq value to delete, exclusive (open).
     * @param markAsHard mark messages as hard-deleted.
     * @return true if some messages were updated or deleted, false otherwise
     */
    static boolean markDeleted(SQLiteDatabase db, long topicId, int fromId, int toId,
                               boolean markAsHard) {
        return deleteOrMarkDeleted(db, false, topicId, fromId, toId, null, markAsHard);
    }

    /**
     * Delete messages between 'from' and 'to'. To delete all messages make before equal to -1.
     *
     * @param db      Database to use.
     * @param topicId Tinode topic ID to delete messages from.
     * @param fromId  minimum seq value to delete, inclusive (closed).
     * @param toId    maximum seq value to delete, exclusive (open)
     * @param soft    mark messages as deleted but do not actually delete them
     * @return number of deleted messages
     */
    public static boolean delete(SQLiteDatabase db, long topicId, int delId, int fromId,
                                 int toId, boolean soft) {
        return db.delete(TABLE_NAME, COLUMN_NAME_TOPIC_ID + "=" + topicId +
                (fromId > 0 ? " AND " + COLUMN_NAME_SEQ + ">=" + fromId : "") +
                (toId != -1 ? " AND " + COLUMN_NAME_SEQ + "<" + toId : ""), null) > 0;
    }

    /**
     * Delete messages between 'from' and 'to'. To delete all messages make from and to equal to -1.
     *
     * @param db      Database to use.
     * @param topicId Tinode topic ID to delete messages from.
     * @param list    maximum seq value to delete, inclusive.
     * @param soft    ignored. teher is no value in keeping soft-deleted messages locally
     * @return number of deleted messages
     */
    public static boolean delete(SQLiteDatabase db, long topicId, int delId, int[] list,
                                 boolean soft) {
        StringBuilder sb = new StringBuilder();
        for (int i : list) {
            sb.append(",");
            sb.append(i);
        }
        sb.deleteCharAt(0);
        String ids = sb.toString();

        return db.delete(TABLE_NAME, COLUMN_NAME_TOPIC_ID + "=" + topicId +
                " AND " + COLUMN_NAME_SEQ + " IN (" + ids + ")", null) > 0;
    }

    /**
     * Delete messages between 'from' and 'to'. To delete all messages make from and to equal to -1.
     *
     * @param db      Database to use.
     * @param topicId Tinode topic ID to delete messages from.
     * @param list    maximum seq value to delete, inclusive.
     * @return number of deleted messages
     */
    public static boolean delete(SQLiteDatabase db, long topicId, List<Integer> list) {
        return deleteOrMarkDeleted(db, true, topicId, Integer.MAX_VALUE, 0, list, false);
    }

    /**
     * Delete messages by database ID.
     *
     * @param db    Database to use.
     * @param msgId Database ID of the message (_id).
     * @return true on success, false on failure
     */
    static boolean delete(SQLiteDatabase db, long msgId) {
        return db.delete(TABLE_NAME, _ID + "=" + msgId, null) > 0;
    }


    /**
     * Get locally-unique ID of the message (content of _ID field).
     *
     * @param cursor Cursor to query
     * @return _id of the message at the current position.
     */
    public static long getLocalId(Cursor cursor) {
        return cursor.getLong(0);
    }

    /**
     * Get negative ID to be used as seq for unsent messages.
     */
    public static int getNextUnsentId(SQLiteDatabase db, long topicId) {
        return -1;
    }

    public static class Loader extends AsyncTaskLoader<Cursor> {
        SQLiteDatabase mDb;
        private Cursor mCursor;

        private long topicId;
        private int pageCount;
        private int pageSize;

        public Loader(Context context, String topic, int pageCount, int pageSize) {
            super(context);

            mDb = BaseDb.getInstance().getReadableDatabase();
            this.topicId = TopicDb.getId(mDb, topic);
            this.pageCount = pageCount;
            this.pageSize = pageSize;
        }

        @Override
        public Cursor loadInBackground() {
            return query(mDb, topicId, pageCount, pageSize);
        }

        /* Runs on the UI thread */
        @Override
        public void deliverResult(Cursor cursor) {
            if (isReset()) {
                // An async query came in while the loader is stopped
                if (cursor != null) {
                    cursor.close();
                }
                return;
            }
            Cursor oldCursor = mCursor;
            mCursor = cursor;

            if (isStarted()) {
                super.deliverResult(cursor);
            }

            if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
                oldCursor.close();
            }
        }

        /**
         * Starts an asynchronous load of the contacts list data. When the result is ready the callbacks
         * will be called on the UI thread. If a previous load has been completed and is still valid
         * the result may be passed to the callbacks immediately.
         * <p/>
         * Must be called from the UI thread
         */
        @Override
        protected void onStartLoading() {
            if (mCursor != null) {
                deliverResult(mCursor);
            }
            if (takeContentChanged() || mCursor == null) {
                forceLoad();
            }
        }

        /**
         * Must be called from the UI thread
         */
        @Override
        protected void onStopLoading() {
            // Attempt to cancel the current load task if possible.
            cancelLoad();
        }

        @Override
        public void onCanceled(Cursor cursor) {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        @Override
        protected void onReset() {
            super.onReset();

            // Ensure the loader is stopped
            onStopLoading();

            if (mCursor != null && !mCursor.isClosed()) {
                mCursor.close();
            }
            mCursor = null;
        }
    }
}
