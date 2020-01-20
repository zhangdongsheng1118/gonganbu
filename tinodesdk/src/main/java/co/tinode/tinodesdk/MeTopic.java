package co.tinode.tinodesdk;

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import co.tinode.tinodesdk.model.Acs;
import co.tinode.tinodesdk.model.Description;
import co.tinode.tinodesdk.model.Drafty;
import co.tinode.tinodesdk.model.MsgServerMeta;
import co.tinode.tinodesdk.model.MsgServerPres;
import co.tinode.tinodesdk.model.PrivateType;
import co.tinode.tinodesdk.model.ServerMessage;
import co.tinode.tinodesdk.model.Subscription;

/**
 * MeTopic manages contact list. MeTopic::Private is unused.
 */
public class MeTopic<DP> extends Topic<DP, PrivateType, DP, PrivateType> {
    private static final String TAG = "MeTopic";

    public MeTopic(Tinode tinode, Listener<DP, PrivateType, DP, PrivateType> l) {
        super(tinode, Tinode.TOPIC_ME, l);
    }

    protected MeTopic(Tinode tinode, Description<DP, PrivateType> desc) {
        super(tinode, Tinode.TOPIC_ME, desc);
    }

    public void setTypes(JavaType typeOfPu) {
        mTinode.setMeTypeOfMetaPacket(typeOfPu);
    }

    @Override
    protected void addSubToCache(Subscription<DP, PrivateType> sub) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void removeSubFromCache(Subscription sub) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PromisedReply<ServerMessage> publish(Drafty content) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PromisedReply<ServerMessage> publish(String content) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Subscription getSubscription(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Subscription<DP, PrivateType>> getSubscriptions() {
        throw new UnsupportedOperationException();
    }

    public PrivateType getPriv() {
        return null;
    }

    public void setPriv(PrivateType priv) { /* do nothing */ }

    @Override
    protected void routeMetaSub(MsgServerMeta<DP, PrivateType, DP, PrivateType> meta) {

        for (Subscription<DP, PrivateType> sub : meta.sub) {
            processOneSub(sub);
        }

        if (mListener != null) {
            mListener.onSubsUpdated();
        }
    }

    @SuppressWarnings("unchecked")
    void processOneSub(Subscription<DP, PrivateType> sub) {
//        try {
//            Log.d(TAG, "Sub " + sub.topic + " is " + new ObjectMapper().writeValueAsString(sub.pub));
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }
        Topic topic = mTinode.getTopic(sub.topic);
        if (topic != null) {
            // This is an existing topic.
            if (sub.deleted != null) {
                // Expunge deleted topic
                mTinode.stopTrackingTopic(sub.topic);
                topic.persist(false);
            } else {
                // Update its record in memory and in the database.
                if(topic.state != Topic.STATE_NORMAL && topic.getRecv() < sub.seq) {
                    topic.state =  Topic.STATE_NORMAL;
                    mStore.topicUpdateState(topic);
                }

                topic.update(sub);

                // Notify topic to update self.
                if (topic.mListener != null) {
                    topic.mListener.onContUpdate(sub);
                }

                if (topic.getTopicType() == TopicType.P2P && mStore != null) {
                    // Use P2P description to generate and update user
                    User user = mTinode.getUser(topic.getName());
                    if (user == null) {
                        user = mTinode.addUser(topic.getName());
                    }
                    if (user.merge(topic.mDesc)) {
                        mStore.userUpdate(user);
                    }
                }
            }
        } else if (sub.deleted == null) {
            // This is a new topic. Register it and write to DB.
            topic = mTinode.newTopic(sub);
            topic.persist(true);

            //addUser
            if (topic.getTopicType() == TopicType.P2P && mStore != null) {
                // Use P2P description to generate and update user
                User user = mTinode.getUser(topic.getName());
                if (user == null) {
                    user = mTinode.addUser(topic.getName());
                }
                if (user.merge(topic.mDesc)) {
                    mStore.userUpdate(user);
                }
            }

            if(sub.recv >= sub.seq){
                topic.state = Topic.STATE_DEL;
                topic.lastAct = null;
                topic.lastMsg = null;

                if(mStore != null)
                    mStore.topicUpdateState(topic);
            }
        }

        if (mListener != null) {
            mListener.onMetaSub(sub);
        }
    }

    @Override
    protected void routePres(MsgServerPres pres) {
        // FIXME(gene): pres.src may contain UID
        Topic topic = mTinode.getTopic(pres.src);
        MsgServerPres.What what = MsgServerPres.parseWhat(pres.what);
        if (topic != null) {
            switch (what) {
                case ON: // topic came online
                    topic.setOnline(true);
                    break;

                case OFF: // topic went offline
                    topic.setOnline(false);
                    topic.setLastSeen(new Date());
                    break;

                case MSG: // new message received
                    if(topic.state != Topic.STATE_NORMAL) {
                        topic.state =  Topic.STATE_NORMAL;
                        mStore.topicUpdateState(topic);
                    }

                    topic.setSeq(pres.seq);
                    topic.setLastAct(pres.act);
                    topic.setLastMsg(pres.ct);
                    if(pres.ts!=null){
                        topic.setTouched(pres.ts);
                    }

                    //topic.setTouched(new Date());
                    break;

                case UPD: // pub/priv updated
                    this.getMeta(getMetaGetBuilder().withGetSub(pres.src).build());
                    break;

                case ACS: // access mode changed
                    if (topic.updateAccessMode(pres.dacs) && mStore != null) {
                        mStore.topicUpdate(topic);
                    }
                    break;

                case UA: // user agent changed
                    topic.setLastSeen(new Date(), pres.ua);
                    break;

                case RECV: // user's other session marked some messages as received
                    if (topic.getRecv() < pres.seq) {
                        topic.setRecv(pres.seq);
                        if (mStore != null) {
                            mStore.setRecv(topic, pres.seq);
                        }
                    }
                    break;

                case READ: // user's other session marked some messages as read
                    if (topic.getRead() < pres.seq) {
                        topic.setRead(pres.seq);
                        if (mStore != null) {
                            mStore.setRead(topic, pres.seq);
                        }
                        if (topic.getRecv() < topic.getRead()) {
                            topic.setRecv(topic.getRead());
                            if (mStore != null) {
                                mStore.setRecv(topic, topic.getRead());
                            }
                        }
                    }
                    break;

                case DEL: // messages deleted
                    // TODO(gene): add handling for del
                    break;

                case GONE:
                    // If topic is unknown (==null), then we don't care to unregister it.
                    mTinode.stopTrackingTopic(pres.src);
                    topic.persist(false);
                    break;
            }
        } else {
            switch (what) {
                case ACS:
                    Acs acs = new Acs();
                    acs.update(pres.dacs);
                    if (acs.isModeDefined()) {
                        getMeta(getMetaGetBuilder().withGetSub(pres.src).build());
                    } else {
                        Log.d(TAG, "Unexpected access mode in presence: '" + pres.dacs.want + "'/'" + pres.dacs.given + "'");
                    }
                    break;
                default:
                    Log.d(TAG, "Topic not found in me.routePres: " + pres.what + " in " + pres.src);
                    break;
            }
        }

        if (mListener != null) {
            if (what == MsgServerPres.What.GONE) {
                mListener.onSubsUpdated();
            }
            Log.d(TAG, "line234 onPres: " + pres.what);
            mListener.onPres(pres);
        }
    }

    @Override
    protected void topicLeft(boolean unsub, int code, String reason) {
        super.topicLeft(unsub, code, reason);

        List<Topic> topics = mTinode.getTopics();
        if (topics != null) {
            for (Topic t : topics) {
                t.setOnline(false);
            }
        }
    }

    public static class MeListener<DP> extends Listener<DP, PrivateType, DP, PrivateType> {
        /**
         * {meta} message received
         */
        public void onMeta(MsgServerMeta<DP, PrivateType, DP, PrivateType> meta) {
        }

        /**
         * {meta what="sub"} message received, and this is one of the subs
         */
        public void onMetaSub(Subscription<DP, PrivateType> sub) {
        }

        /**
         * {meta what="desc"} message received
         */
        public void onMetaDesc(Description<DP, PrivateType> desc) {
        }

        /**
         * Called by MeTopic when topic descriptor as contact is updated
         */
        public void onContUpdate(Subscription<DP, PrivateType> sub) {
        }
    }

}
