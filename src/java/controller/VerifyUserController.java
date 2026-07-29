package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.AdbExecutor;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import util.HibernateUtil;

/**
 *
 * @author DELL
 */
@WebServlet(name = "VerifyUserController", urlPatterns = {"/VerifyUserController"})
public class VerifyUserController extends HttpServlet {

    Map<String, String> otpMap = new HashMap();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject contactData = gson.fromJson(request.getReader(), JsonObject.class);

        String countryCode = contactData.get("code").getAsString().trim();
        String contactNo = contactData.get("number").getAsString().trim();

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", Boolean.FALSE);

        if (countryCode.isEmpty()) {
            responseObject.addProperty("message", "Country Code can not be empty");
        } else if (contactNo.isEmpty()) {
            responseObject.addProperty("message", "Contact No can not be empty");
        } else {
            String otp = VerifyUserController.getOtpCode();
            otpMap.put(countryCode + contactNo, otp);
            System.out.println("OTP is : " + otp);

            boolean status = AdbExecutor.sendOtpToUser("Your VeloChat verification code is " + otp);

            if (status) {
                //code sending success.
                responseObject.addProperty("status", Boolean.TRUE);
            } else {
                //code sending failed.
                responseObject.addProperty("status", Boolean.FALSE);
            }
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        Session s = HibernateUtil.getSessionFactory().openSession();

        String countryCode = request.getParameter("countryCode").trim();
        String contactNo = request.getParameter("contactNo").trim();
        String otp = request.getParameter("otp").trim();
        String mode = request.getParameter("mode");

        JsonObject responseobject = new JsonObject();
        responseobject.addProperty("status", Boolean.FALSE);

        String sendedOtp = otpMap.get("+" + countryCode + contactNo);
        if (sendedOtp == null) {
            responseobject.addProperty("message", "Verification failed. Please resend the OTP and try again.");
            responseobject.addProperty("status", Boolean.FALSE);
        } else {
            if (otp.equals(sendedOtp)) {
                
                System.out.println(mode);

                if (mode.equals("signIn")) {
                    Criteria c1 = s.createCriteria(User.class);
                    c1.add(Restrictions.and(
                            Restrictions.eq("countryCode", "+" + countryCode),
                            Restrictions.eq("contactNo", contactNo)
                    ));
                    User user = (User) c1.uniqueResult();
                    if (user != null) {
                        responseobject.addProperty("userId", user.getId());
                        responseobject.addProperty("status", Boolean.TRUE);
                    } else {
                        responseobject.addProperty("message", "User not found!");
                    }
                } else {
                    responseobject.addProperty("status", Boolean.TRUE);
                }

            } else {
                responseobject.addProperty("message", "OTP is invalid");
                responseobject.addProperty("status", Boolean.FALSE);
            }
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseobject));
    }

    public static String getOtpCode() {
        SecureRandom random = new SecureRandom();
        int fiveDigitCode = 10000 + random.nextInt(90000);

        return String.valueOf(fiveDigitCode);
    }

}
