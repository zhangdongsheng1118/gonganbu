package com.yixin.tinode.db.model;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

import co.tinode.tinodesdk.model.AvatarPhoto;

/**
 * @创建者 CSDN_LQR
 * @描述 群成员
 */
public class GroupMember extends DataSupport implements Serializable {

    private String userId;
    private String name;
    private String portraitUri;
    private String groupId;
    private String displayName;
    private String nameSpelling;
    private String displayNameSpelling;
    private String groupName;
    private String groupNameSpelling;
    private String groupPortrait;


    // Avatar photo
    private AvatarPhoto photo;

    public GroupMember(String userId, String name, String portraitUri) {
        this.userId = userId;
        this.name = name;
        this.portraitUri = portraitUri;
    }

    public GroupMember(String userId, String name, AvatarPhoto photo) {
        this.userId = userId;
        this.name = name;
        this.photo = photo;
    }


    public GroupMember(String groupId, String userId, String name, String portraitUri, String displayName, String nameSpelling, String displayNameSpelling, String groupName, String groupNameSpelling, String groupPortrait) {
        this(userId, name, portraitUri);
        this.groupId = groupId;
        this.displayName = displayName;
        this.nameSpelling = nameSpelling;
        this.displayNameSpelling = displayNameSpelling;
        this.groupName = groupName;
        this.groupNameSpelling = groupNameSpelling;
        this.groupPortrait = groupPortrait;
    }

    public GroupMember(String groupId, String userId, String name, AvatarPhoto photo, String displayName, String nameSpelling, String displayNameSpelling, String groupName, String groupNameSpelling, String groupPortrait) {
        this(userId, name, photo);
        this.groupId = groupId;
        this.displayName = displayName;
        this.nameSpelling = nameSpelling;
        this.displayNameSpelling = displayNameSpelling;
        this.groupName = groupName;
        this.groupNameSpelling = groupNameSpelling;
        this.groupPortrait = groupPortrait;
    }

    public GroupMember(String groupId, String userId, String name, String portraitUri, String displayName, String nameSpelling, String displayNameSpelling, String groupName, String groupNameSpelling) {
        this(userId, name, portraitUri);
        this.groupId = groupId;
        this.displayName = displayName;
        this.nameSpelling = nameSpelling;
        this.displayNameSpelling = displayNameSpelling;
        this.groupName = groupName;
        this.groupNameSpelling = groupNameSpelling;
    }

    public GroupMember(String groupId, String userId, String name, String portraitUri, String displayName) {
        this(userId, name, portraitUri);
        this.groupId = groupId;
        this.displayName = displayName;
    }

    public AvatarPhoto getPhoto() {
        return photo;
    }

    public void setPhoto(AvatarPhoto photo) {
        this.photo = photo;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPortraitUri() {
        return portraitUri;
    }

    public void setPortraitUri(String portraitUri) {
        this.portraitUri = portraitUri;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getNameSpelling() {
        return nameSpelling;
    }

    public void setNameSpelling(String nameSpelling) {
        this.nameSpelling = nameSpelling;
    }

    public String getDisplayNameSpelling() {
        return displayNameSpelling;
    }

    public void setDisplayNameSpelling(String displayNameSpelling) {
        this.displayNameSpelling = displayNameSpelling;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupNameSpelling() {
        return groupNameSpelling;
    }

    public void setGroupNameSpelling(String groupNameSpelling) {
        this.groupNameSpelling = groupNameSpelling;
    }

    public String getGroupPortrait() {
        return groupPortrait;
    }

    public void setGroupPortrait(String groupPortrait) {
        this.groupPortrait = groupPortrait;
    }
}
