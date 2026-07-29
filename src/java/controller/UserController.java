package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.User;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import util.HibernateUtil;

/**
 *
 * @author DELL
 */
@MultipartConfig
@WebServlet(name = "UserController", urlPatterns = {"/UserController"})
public class UserController extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        Session ses = HibernateUtil.getSessionFactory().openSession();

        String fullname = request.getParameter("fullname");
        String email = request.getParameter("email");
        String countryCode = request.getParameter("countryCode");
        String contactNo = request.getParameter("contactNo");
        Part profileImage = request.getPart("profileImage");

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        if (fullname.isEmpty()) {
            responseObject.addProperty("message", "Fullname can not be empty!");
        } else if (email.isEmpty()) {
            responseObject.addProperty("message", "Email can not be empty!");
        } else if (countryCode.isEmpty()) {
            responseObject.addProperty("message", "Country Code can not be empty!");
        } else if (contactNo.isEmpty()) {
            responseObject.addProperty("message", "Contact No can not be empty!");
        } else {

            Criteria c1 = ses.createCriteria(User.class);
            c1.add(Restrictions.eq("countryCode", countryCode));
            c1.add(Restrictions.eq("contactNo", contactNo));

            User user1 = (User) c1.uniqueResult();
            if (user1 != null) { //user with same contact no already exists
                responseObject.addProperty("message", "User with same contact already exists");
            } else { //no use with the contact
                Criteria c2 = ses.createCriteria(User.class);
                c2.add(Restrictions.eq("email", email));

                User user2 = (User) c2.uniqueResult();
                if (user2 != null) { //user with same email already exists
                    responseObject.addProperty("message", "User with same email already exists");
                } else {
                    //save new user to db
                    user1 = new User(fullname, email, countryCode, contactNo, new Date(), new Date());
                    int id = (int) ses.save(user1);
                    ses.beginTransaction().commit();

                    responseObject.add("user", gson.toJsonTree(user1));

                    //store profile image
                    if (profileImage.getSubmittedFileName() != null) {
                        String appPath = getServletContext().getRealPath("");
                        String newPath = appPath.replace("build\\web", "web\\profile-images");

                        File profileFolder = new File(newPath, String.valueOf(id));
                        if (!profileFolder.exists()) {
                            profileFolder.mkdirs();
                        }

                        File file1 = new File(profileFolder, "profile1.jpg");
                        Files.copy(profileImage.getInputStream(), file1.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    }
                    responseObject.addProperty("status", true);
                    responseObject.addProperty("userId", id);
                }
            }
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));
    }

}
