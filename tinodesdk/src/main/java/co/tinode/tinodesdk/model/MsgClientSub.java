package co.tinode.tinodesdk.model;

/**
 * Subscribe to topic packet.
 *
 */
public class MsgClientSub<Pu,Pr,T> {
    public String id;
    public String topic;
    public boolean bkg;
    public MsgSetMeta<Pu,Pr> set;
    public MsgGetMeta get;

    public MsgClientSub() {}

    public MsgClientSub(String id, String topic, MsgSetMeta<Pu,Pr> set, MsgGetMeta get,boolean background) {
        this.id = id;
        this.topic = topic;
        this.bkg = background;
        this.set = set;
        this.get = get;
    }
}
