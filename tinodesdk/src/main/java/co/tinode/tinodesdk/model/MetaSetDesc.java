package co.tinode.tinodesdk.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Topic intiation parameters
 */
public class MetaSetDesc<P,R> {
    public Defacs defacs;
    @JsonProperty("public")
    public P pub;
    @JsonProperty("private")
    public R priv;

    public MetaSetDesc() {}

    public MetaSetDesc(P pub, R priv, Defacs da) {
        this.defacs = da;
        this.pub = pub;
        this.priv = priv;
    }

    public MetaSetDesc(P pub, R priv) {
        this(pub, priv, null);

        this.defacs = new Defacs("JRWPASD", "N");
    }

    public MetaSetDesc(Defacs da) {
        this(null, null, da);
    }
}
