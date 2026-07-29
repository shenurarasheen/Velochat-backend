package socket;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

/**
 *
 * @author DELL
 */
public class ProfileService {
    
    public boolean saveProfileImage(int userId, HttpServletRequest request) throws IOException, ServletException {

        Part profileImage = request.getPart("profileImage");

        String appPath = request.getServletContext().getRealPath("");
        String newPath = appPath.replace("build\\web", "web\\profile-images");

        File profileFolder = new File(newPath, String.valueOf(userId));
        if (!profileFolder.exists()) {
            profileFolder.mkdirs();
        }

        File file1 = new File(profileFolder, "profile1.jpg");
        Files.copy(profileImage.getInputStream(), file1.toPath(), StandardCopyOption.REPLACE_EXISTING);

        return true;
    }

    public static String getProfileUrl(int userId) {
        try {
            URL url = new URI("http://localhost:8080/NexChat/profile-images/" + userId + "/profile1.jpg").toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            int responseCode = conn.getResponseCode();
            conn.connect();
            System.out.println(responseCode);
            String profile;
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println(ChatService.URL);
                profile = ChatService.URL + "/NexChat/profile-images/" + userId + "/profile1.jpg";
            } else {
                profile = "";
            }
            return profile;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
