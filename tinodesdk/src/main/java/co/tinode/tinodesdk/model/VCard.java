package co.tinode.tinodesdk.model;

import android.graphics.Bitmap;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;

public class VCard implements Serializable {

    public final static String TYPE_HOME = "HOME";
    public final static String TYPE_WORK = "WORK";
    public final static String TYPE_MOBILE = "MOBILE";
    public final static String TYPE_PERSONAL = "PERSONAL";
    public final static String TYPE_BUSINESS = "BUSINESS";
    public final static String TYPE_OTHER = "OTHER";
    // Full name
    public String fn;
    public Name n;
    public String org;
    public String title;
    // List of phone numbers associated with the contact
    public Contact[] tel;
    // List of contact's email addresses
    public Contact[] email;
    public Contact[] impp;
    // Avatar photo
    public AvatarPhoto photo;
    public String phone;
    public String mail;
    public String dep;

    public VCard() {
    }

    public VCard(String fullName, byte[] avatar) {
        this.fn = fullName;
        this.photo = new AvatarPhoto(avatar);
    }

    public VCard(String fullName, Bitmap avatar) {
        this.fn = fullName;
        this.photo = new AvatarPhoto(avatar);
    }

    public VCard(String fullName, Bitmap avatar, String phone, String mail, String dep) {
        this.fn = fullName;
        this.photo = new AvatarPhoto(avatar);
        this.phone = phone;
        this.mail = mail;
        this.dep = dep;
    }

    protected static String typeToString(ContactType tp) {
        String str = null;
        switch (tp) {
            case HOME:
                str = TYPE_HOME;
                break;
            case WORK:
                str = TYPE_WORK;
                break;
            case MOBILE:
                str = TYPE_MOBILE;
                break;
            case PERSONAL:
                str = TYPE_PERSONAL;
                break;
            case BUSINESS:
                str = TYPE_BUSINESS;
                break;
            case OTHER:
                str = TYPE_OTHER;
                break;
        }

        return str;
    }

    private static ContactType stringToType(String str) {
        if (str == null) {
            return null;
        }

        switch (str) {
            case TYPE_HOME:
                return ContactType.HOME;
            case TYPE_WORK:
                return ContactType.WORK;
            case TYPE_MOBILE:
                return ContactType.MOBILE;
            case TYPE_PERSONAL:
                return ContactType.PERSONAL;
            case TYPE_BUSINESS:
                return ContactType.BUSINESS;
            default:
                return ContactType.OTHER;
        }
    }

    @JsonIgnore
    public byte[] getPhotoBits() {
        return photo == null ? null : photo.data;
    }
    @JsonIgnore
    public void setPhotoBits(byte[] bits) {
        photo = new AvatarPhoto(bits);
    }

    @JsonIgnore
    public Bitmap getBitmap() {
        return (photo != null) ? photo.getBitmap() : null;
    }

    @JsonIgnore
    public void setBitmap(Bitmap bmp) {
        photo = new AvatarPhoto(bmp);
    }

    public boolean constructBitmap() {
        return photo != null && photo.constructBitmap();
    }

    public void addPhone(String phone, ContactType type) {
        addPhone(phone, typeToString(type));
    }

    public void addPhone(String phone, String type) {
        tel = Contact.append(tel, new Contact(type, phone));
    }

    public void addEmail(String addr, String type) {
        email = Contact.append(email, new Contact(type, addr));
    }

    @JsonIgnore
    public String getPhoneByType(String type) {
        String phone = null;
        if (tel != null) {
            for (Contact tt : tel) {
                if (tt.type != null && tt.type.equals(type)) {
                    phone = tt.uri;
                    break;
                }
            }
        }
        return phone;
    }

    @JsonIgnore
    public String getPhoneByType(ContactType type) {
        return getPhoneByType(typeToString(type));
    }

    public enum ContactType {HOME, WORK, MOBILE, PERSONAL, BUSINESS, OTHER}

    public static <T extends VCard> T copy(T dst, VCard src) {
        dst.fn = src.fn;
        dst.n = src.n != null ? src.n.copy() : null;
        dst.org = src.org;
        dst.title = src.title;
        dst.tel = Contact.copyArray(src.tel);
        dst.email = Contact.copyArray(src.email);
        dst.impp = Contact.copyArray(src.impp);
        // Shallow copy of the photo
        dst.photo = src.photo != null ? src.photo.copy() : null;
dst.phone = src.phone;
        dst.mail = src.mail;
        dst.dep = src.dep;

        return dst;
    }

    public VCard copy() {
        return copy(new VCard(), this);
    }

    public static class Name implements Serializable {
        public String surname;
        public String given;
        public String additional;
        public String prefix;
        public String suffix;

        public Name copy() {
            Name dst = new Name();
            dst.surname = surname;
            dst.given = given;
            dst.additional = additional;
            dst.prefix = prefix;
            dst.suffix = suffix;
            return dst;
        }
    }

    public static class Contact implements Serializable, Comparable<Contact> {
        public String type;
        public String uri;

        private ContactType tp;

        public Contact(String type, String uri) {
            this.type = type;
            this.uri = uri;
            this.tp = stringToType(type);
        }

        @JsonIgnore
        public ContactType getType() {
            if (tp != null) {
                return tp;
            }
            return stringToType(type);
        }

        public Contact copy() {
            return new Contact(type, uri);
        }

        static Contact[] copyArray(Contact[] src){
            Contact[] dst = null;
            if (src != null) {
                dst = Arrays.copyOf(src, src.length);
                for (int i=0; i<src.length;i++) {
                    dst[i] = src[i].copy();
                }
            }
            return dst;
        }

        public static Contact[] append(Contact[] arr, Contact val) {
            int insertAt;
            if (arr == null) {
                arr = new Contact[1];
                arr[0] = val;
            } else if ((insertAt = Arrays.binarySearch(arr, val)) >=0) {
                if (!TYPE_OTHER.equals(val.type)) {
                    arr[insertAt].type = val.type;
                    arr[insertAt].tp = stringToType(val.type);
                }
            } else {
                arr = Arrays.copyOf(arr, arr.length + 1);
                arr[arr.length - 1] = val;
            }

            Arrays.sort(arr);

            return arr;
        }

        @Override
        public int compareTo(Contact c) {
            return uri.compareTo(c.uri);
        }

        @Override
        public String toString() {
            return type + ":" + uri;
        }
    }

    public static class Photo implements Serializable {
        public byte[] data;
        public String type;

        public Photo() {}

        public Photo(byte[] bits) {
            data = bits;
        }

        public Photo copy() {
            Photo ret = new Photo();
            ret.data = data;
            ret.type = type;
            return ret;
        }
    }
}

