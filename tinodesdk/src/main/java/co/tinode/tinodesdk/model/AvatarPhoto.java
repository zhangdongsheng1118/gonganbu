package co.tinode.tinodesdk.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

/**
 * Utility class: constructs a square 128x128 Bitmap from bytes/serializes to jpeg bytes
 */
public class AvatarPhoto implements Serializable {
    public byte[] data;
    public String type;
    public String uri;

    @JsonIgnore
    protected transient Bitmap mImage = null;

    public AvatarPhoto() {
    }

    public AvatarPhoto(byte[] bits) {
        data = bits;
        constructBitmap();
    }

    public AvatarPhoto(Bitmap bmp) {
        this.mImage = bmp;
        serializeBitmap();
    }

    public boolean constructBitmap() {
        if (data != null) {
            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
            if (bmp != null) {
                mImage = Bitmap.createScaledBitmap(bmp, 128, 128, false);
                // createScaledBitmap may return the same object if scaling is not required.
                if (bmp != mImage) {
                    bmp.recycle();
                }
            }
        }
        return mImage != null;
    }

    @JsonIgnore
    public Bitmap getBitmap() {
        if (mImage == null) {
            constructBitmap();
        }
        return mImage;
    }

    protected void serializeBitmap() {
        if (mImage != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            mImage.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
            this.data = byteArrayOutputStream.toByteArray();
            this.type = "jpg";
        }
    }


    public AvatarPhoto copy() {
        AvatarPhoto ret = new AvatarPhoto();
        ret.data = data;
        ret.type = type;
        ret.uri = uri;
        return ret;
    }
}

