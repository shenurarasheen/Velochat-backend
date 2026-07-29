package socket;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import entity.Chat;
import entity.User;
import java.util.List;
import java.util.Map;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/chat")
public class ChatEndPoint {

    private static final Gson GSON = new Gson();
    private int userId;

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("onOpen");
        String query = session.getQueryString();
        if (query != null && query.startsWith("userId=")) {
            userId = Integer.parseInt(query.substring("userId=".length()));
            ChatService.register(userId, session);
        }
    }

    @OnClose
    public void onClose(Session session) {
        if (userId > 0) {
            ChatService.unregister(userId);
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("Web socket error for user" + userId);
        throwable.printStackTrace();
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            Map<String, Object> map = ChatEndPoint.GSON.fromJson(message, Map.class);
            String type = (String) map.get("type");
            switch (type) {
                case "get_chat_list": {
                    System.out.println("get_chat_list");
                    ChatService.sendToUser(userId, ChatService.friendListEnvelop(ChatService.getFriendChatsForUser(userId)));
                    break;
                }
                case "get_single_chat": {
                    int friendId = (int)((double)map.get("friendId"));
                    List<Chat> chats = ChatService.getChatHistory(userId, friendId);
                    Map<String, Object> envelop = ChatService.singleChatEnvelop(chats);
                    ChatService.sendToUser(userId, envelop);
                    break;
                }
                case "send_message": {
                    int friendId = (int)((double) map.get("toUserId"));
                    String chat = String.valueOf(map.get("message"));
                    ChatService.saveNewChat(userId, friendId, chat);
                    break;
                }
                case "get_all_friends": {
                    Map<String, Object> envelop = UserService.getAllFriends(userId);
                    ChatService.sendToUser(userId, envelop);
                    break;
                }
                case "save_new_contact": {
                    LinkedTreeMap userObject = (LinkedTreeMap) map.get("user");
                    User user = new User(
                            String.valueOf(userObject.get("fullname")),
                            String.valueOf(userObject.get("countryCode")),
                            String.valueOf(userObject.get("contactNo"))
                    );
                    Map<String, Object> envelop = UserService.saveNewContact(userId, user);
                    ChatService.sendToUser(userId, envelop);
                    break;
                }
                case "set_user_profile": {
                    Map<String, Object> envelop = UserService.getMyProfileData(userId);
                    ChatService.sendToUser(userId, envelop);
                    break;
                }
                case "send_profile_details": {
                    Map<String, Object> envelop = UserService.getMyProfileData(userId);
                    ChatService.sendToUser(userId, envelop);
                    break;
                }
                default: {
                    System.out.println("Ignored unknown client type:" + type);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
