package socket;

import java.util.Date;


public class ChatSummery {
    private int friendId;
    private String friendName;
    private String lastMessage;
    private Date lastTimeStamp;
    private int unreadCount;
    private String profileImage;
    
    public ChatSummery() {}

    public ChatSummery(int friendId, String friendName, String lastMessage, Date lastTimeStamp, int unreadCount, String profileImage) {
        this.friendId = friendId;
        this.friendName = friendName;
        this.lastMessage = lastMessage;
        this.lastTimeStamp = lastTimeStamp;
        this.unreadCount = unreadCount;
        this.profileImage = profileImage;
    }

    /**
     * @return the friendId
     */
    public int getFriendId() {
        return friendId;
    }

    /**
     * @param friendId the friendId to set
     */
    public void setFriendId(int friendId) {
        this.friendId = friendId;
    }

    /**
     * @return the friendName
     */
    public String getFriendName() {
        return friendName;
    }

    /**
     * @param friendName the friendName to set
     */
    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    /**
     * @return the lastMessage
     */
    public String getLastMessage() {
        return lastMessage;
    }

    /**
     * @param lastMessage the lastMessage to set
     */
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    /**
     * @return the lastTimeStamp
     */
    public Date getLastTimeStamp() {
        return lastTimeStamp;
    }

    /**
     * @param lastTimeStamp the lastTimeStamp to set
     */
    public void setLastTimeStamp(Date lastTimeStamp) {
        this.lastTimeStamp = lastTimeStamp;
    }

    /**
     * @return the unreadCount
     */
    public int getUnreadCount() {
        return unreadCount;
    }

    /**
     * @param unreadCount the unreadCount to set
     */
    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    /**
     * @return the profileImage
     */
    public String getProfileImage() {
        return profileImage;
    }

    /**
     * @param profileImage the profileImage to set
     */
    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
    
    
}
