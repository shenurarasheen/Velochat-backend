package socket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import entity.Chat;
import entity.FriendList;
import entity.Status;
import entity.User;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.websocket.Session;
import org.hibernate.Criteria;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import util.HibernateUtil;

public class ChatService {

    private static final ConcurrentHashMap<Integer, Session> SESSIONS = new ConcurrentHashMap<>();
    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
    public static final String URL = "https://9fb33dd17fe2.ngrok-free.app";

    public static void register(int userId, Session session) {
        SESSIONS.put(userId, session);
    }

    public static void unregister(int userId) {
        SESSIONS.remove(userId);
    }

    public static void sendToUser(int userId, Object payload) {
        Session ws = SESSIONS.get(userId);
        if (ws != null && ws.isOpen()) {
            try {
                System.out.println(GSON.toJson(payload));
                ws.getBasicRemote().sendText(GSON.toJson(payload));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static List<ChatSummery> getFriendChatsForUser(int userId) {
        org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tr = session.beginTransaction();
        try {
            Criteria c1 = session.createCriteria(FriendList.class);
            c1.add(Restrictions.eq("user.id", userId));
            c1.add(Restrictions.eq("status", Status.ACTIVE));
            List<FriendList> friendList = c1.list();

            Map<Integer, ChatSummery> map = new LinkedHashMap<>();
            for (FriendList fl : friendList) {
                User myFriend = fl.getFriend();
                Criteria c2 = session.createCriteria(Chat.class);
                Criterion rest1 = Restrictions.and(Restrictions.eq("from.id", userId), Restrictions.eq("to.id", myFriend.getId()));
                Criterion rest2 = Restrictions.and(Restrictions.eq("from.id", myFriend.getId()), Restrictions.eq("to.id", userId));
                Criterion rest3 = Restrictions.or(rest1, rest2);
                c2.add(rest3);
                c2.addOrder(Order.desc("updatedAt"));

                List<Chat> chats = c2.list();

                for (Chat c : chats) {
                    if (c.getFrom().getId() != userId && c.getStatus().equals(Status.SENT)) {
                        c.setStatus(Status.DELIVERED);
                        session.update(c);
                    }
                }               

                int unread = 0;
                for (Chat c : chats) {
                    if (c.getFrom().getId() == myFriend.getId() && c.getTo().getId() == userId && c.getStatus().equals(Status.DELIVERED)) {
                        unread += 1;
                    }
                }

                System.out.println(unread);

                if (chats.size() != 0 && !map.containsKey(myFriend.getId())) {
                    String profileImage = ProfileService.getProfileUrl(myFriend.getId());
                    map.put(myFriend.getId(), new ChatSummery(
                            myFriend.getId(),
                            myFriend.getName(),
                            chats.get(0).getMessage(),
                            chats.get(0).getUpdatedAt(),
                            unread,
                            profileImage
                    )
                    );
                }

            }
            tr.commit();
            return new ArrayList(map.values());

        } finally {
            session.close();
        }
    }

    public static Map<String, Object> friendListEnvelop(List<ChatSummery> list) {
        Map<String, Object> envelop = new HashMap();
        envelop.put("type", "friend_list");
        envelop.put("payload", list);
        return envelop;
    }

    public static List<Chat> getChatHistory(int userId, int friendId) {
        org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            Criteria c = session.createCriteria(Chat.class);
            c.add(Restrictions.or(
                    Restrictions.and(
                            Restrictions.eq("from.id", userId),
                            Restrictions.eq("to.id", friendId)
                    ),
                    Restrictions.and(
                            Restrictions.eq("from.id", friendId),
                            Restrictions.eq("to.id", userId)
                    )
            ));
            c.addOrder(Order.asc("createdAt"));
            List<Chat> list = c.list();

            Transaction tr = session.beginTransaction();
            for (Chat chat : list) {
                if (chat.getFrom().getId() == friendId && chat.getTo().getId() == userId
                        && chat.getStatus().equals(Status.DELIVERED)) {
                    chat.setStatus(Status.READ);
                    session.update(chat);
                }
            }
            tr.commit();
            return list;
        } finally {
            session.close();
        }
    }

    public static Map<String, Object> singleChatEnvelop(List<Chat> chats) {
        Map<String, Object> envelop = new HashMap();
        envelop.put("type", "single_chat");
        envelop.put("payload", chats);
        return envelop;
    }

    public static void saveNewChat(int userId, int friendId, String message) {
        org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tr = session.beginTransaction();
        User me = (User) session.get(User.class, userId);
        User friend = (User) session.get(User.class, friendId);

        Criteria c1 = session.createCriteria(FriendList.class);
        c1.add(Restrictions.and(
                Restrictions.eq("user", me),
                Restrictions.eq("friend", friend)
        ));
        FriendList fl1 = (FriendList) c1.uniqueResult();

        if (fl1 == null) {
            FriendList friend1 = new FriendList();
            friend1.setFriend(friend);
            friend1.setUser(me);
            friend1.setStatus(Status.ACTIVE);
            session.save(friend1);
        }

        Criteria c2 = session.createCriteria(FriendList.class);
        c2.add(Restrictions.and(
                Restrictions.eq("user", friend),
                Restrictions.eq("friend", me)
        ));
        FriendList fl2 = (FriendList) c2.uniqueResult();

        if (fl2 == null) {
            FriendList friend1 = new FriendList();
            friend1.setFriend(me);
            friend1.setUser(friend);
            friend1.setStatus(Status.ACTIVE);
            session.save(friend1);
        }

        Chat chat = new Chat();
        chat.setFrom(me);
        chat.setTo(friend);
        chat.setMessage(message);
        chat.setCreatedAt(new Date());
        chat.setUpdatedAt(new Date());
        chat.setFiles("File:");
        session.save(chat);
        tr.commit();
        session.close();

        Map<String, Object> envelop = new HashMap<>();
        envelop.put("type", "new_message");
        envelop.put("payload", chat);

        ChatService.sendToUser(userId, envelop);
        ChatService.sendToUser(friendId, envelop);

        List<ChatSummery> fromList = ChatService.getFriendChatsForUser(chat.getFrom().getId()); //from List
        List<ChatSummery> toList = ChatService.getFriendChatsForUser(chat.getTo().getId()); //to List
        Map<String, Object> fromMap = ChatService.friendListEnvelop(fromList); ///from
        Map<String, Object> toMap = ChatService.friendListEnvelop(toList); //to

        ChatService.sendToUser(chat.getFrom().getId(), fromMap); //update from home chat
        ChatService.sendToUser(chat.getTo().getId(), toMap);
    }

}
