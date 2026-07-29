package socket;

import com.google.gson.JsonObject;
import dto.UserDTO;
import entity.FriendList;
import entity.Status;
import entity.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import util.HibernateUtil;


public class UserService {
    
    public static Map<String, Object> getAllFriends(int userId) {
        try {
            Session ses = HibernateUtil.getSessionFactory().openSession();
            Map<String, Object> map = new HashMap();
            
            Criteria c1 = ses.createCriteria(FriendList.class);
            c1.add(Restrictions.eq("user.id", userId));
            c1.add(Restrictions.eq("status", Status.ACTIVE));
            List<FriendList> myFriends = c1.list();
            
            List<UserDTO> friendDTOs = new ArrayList();
            
            for (FriendList myFriend : myFriends) { 
                User user = myFriend.getFriend(); //user object
                UserDTO dto = new UserDTO();
                dto.setId(user.getId());
                dto.setFullname(myFriend.getFriend().getName());
                dto.setDisplayName(myFriend.getDisplayName()); //From FriendList Table
                dto.setCountryCode(user.getCountryCode());
                dto.setContactNo(user.getContactNo());
                dto.setProfileImage(ProfileService.getProfileUrl(user.getId()));
                dto.setCreatedAt(user.getCreatedAt());
                dto.setUpdatedAt(user.getUpdatedAt());
                dto.setStatus(user.getStatus());
                friendDTOs.add(dto);
            }
            
            ses.close();
            map.put("type", "all_friends");
            map.put("payload", friendDTOs);
            return map;
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
    
    public static Map<String, Object> saveNewContact(int myId, User user) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("responseStatus", Boolean.FALSE);
        
        Criteria c1 = s.createCriteria(User.class);
        c1.add(Restrictions.and(
                Restrictions.eq("countryCode", user.getCountryCode()),
                Restrictions.eq("contactNo", user.getContactNo())
        ));
        
        User u1 = (User) c1.uniqueResult();
        
        if (u1 == null){
            responseObject.addProperty("message", "This user not in VeloChat app.");
        } else{
            User me = (User) s.get(User.class, myId);
            
            Criteria c2 = s.createCriteria(FriendList.class);
            c2.add(Restrictions.and(
                    Restrictions.eq("user", me),
                    Restrictions.eq("friend", u1)
            ));
            FriendList friendList = (FriendList) c2.uniqueResult();
            responseObject.addProperty("responseStatus", Boolean.TRUE);
            
            if(friendList == null) {
                FriendList fl = new FriendList(u1, me, u1.getName());
                s.save(fl);
                responseObject.addProperty("message", "This user added to the friend list");
            } else {
                friendList.setDisplayName(user.getName());
                s.update(friendList);
                responseObject.addProperty("message", "This user already in friend list!");
            }
        }
        
        s.beginTransaction().commit();
        s.close();
        Map<String, Object> map = new HashMap();
        map.put("type", "new_contact_response_text");
        map.put("payload", responseObject);
        return map;
    }
    
    public static Map<String, Object> getMyProfileData(int userId) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        User user = (User) s.get(User.class, userId);
        UserDTO dto = new UserDTO();
        dto.setFullname(user.getName());
        dto.setEmail(user.getEmail());
        dto.setCountryCode(user.getCountryCode());
        dto.setContactNo(user.getContactNo());
        dto.setProfileImage(ProfileService.getProfileUrl(userId));
        
        s.close();
        Map<String, Object> map = new HashMap<>();
        map.put("type", "user_profile_details");
        map.put("payload",  dto);
        return map;
    }
    
}
