package com.yixin.tinode.model.message;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeleteContactMessage  {
    private String contact_id;
    public static final Parcelable.Creator<DeleteContactMessage> CREATOR = new Parcelable.Creator() {
        public DeleteContactMessage createFromParcel(Parcel var1) {
            return new DeleteContactMessage(var1);
        }

        public DeleteContactMessage[] newArray(int var1) {
            return new DeleteContactMessage[var1];
        }
    };

    public byte[] encode() {
        JSONObject var1 = new JSONObject();

        try {
            if (!TextUtils.isEmpty(this.getContact_id())) {
                var1.put("contact_id", this.contact_id);
            }

        } catch (JSONException var4) {
            var4.printStackTrace();
        }

        try {
            return var1.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException var3) {
            var3.printStackTrace();
            return null;
        }
    }

    private String getEmotion(String var1) {
        Pattern var2 = Pattern.compile("\\[/u([0-9A-Fa-f]+)\\]");
        Matcher var3 = var2.matcher(var1);
        StringBuffer var4 = new StringBuffer();

        while (var3.find()) {
            int var5 = Integer.parseInt(var3.group(1), 16);
            var3.appendReplacement(var4, String.valueOf(Character.toChars(var5)));
        }

        var3.appendTail(var4);
        return var4.toString();
    }

    protected DeleteContactMessage() {
    }

    public static DeleteContactMessage obtain(String contact_id) {
        DeleteContactMessage var4 = new DeleteContactMessage();
        var4.setContact_id(contact_id);
        return var4;
    }

    public DeleteContactMessage(byte[] var1) {
        String var2 = null;

        try {
            var2 = new String(var1, "UTF-8");
        } catch (UnsupportedEncodingException var5) {
            ;
        }

        try {
            JSONObject var3 = new JSONObject(var2);

            if (var3.has("contact_id")) {
                this.setContact_id(var3.optString("contact_id"));
            }

        } catch (JSONException var4) {
            ;
        }

    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel var1, int var2) {
    }

    public DeleteContactMessage(Parcel var1) {
    }

    public String getContact_id() {
        return contact_id;
    }

    public void setContact_id(String contact_id) {
        this.contact_id = contact_id;
    }

}