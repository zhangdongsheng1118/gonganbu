package com.yixin.tinode.tinode;

import android.text.TextUtils;

import com.yixin.tinode.util.PinyinUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import co.tinode.tinodesdk.Tinode;
import co.tinode.tinodesdk.Topic;
import co.tinode.tinodesdk.model.VCard;

/**
 * Created by Administrator on 2018/6/20.
 */

public class TinodeUtil {
    /**
     * Return a collection of topics which satisfy the filters.
     *
     * @param type    type of topics to return.
     * @param updated return topics with update timestamp after this
     */
    @SuppressWarnings("unchecked")
    public static <T extends Topic> List<T> getFilteredTopics(Topic.TopicType type, Date updated, String key) {
        Tinode tinode = Cache.getTinode();
        List<T> topics = (List<T>) tinode.getTopics();
        if (type == Topic.TopicType.ANY && updated == null) {
            return topics;
        }
        if (type == Topic.TopicType.UNKNOWN) {
            return null;
        }
        ArrayList<T> result = new ArrayList<>();
        for (T t : topics) {
            if (t.getTopicType().compare(type) &&
                    (updated == null || updated.before(t.getUpdated()))) {
                VCard info = (VCard) t.getPub();
                if (TextUtils.isEmpty(key) || info.fn.contains(key) || PinyinUtils.getPinyin(info.fn).toLowerCase().contains(key.toLowerCase())) {
                    result.add(t);
                }
            }
        }
        return result;
    }
}
