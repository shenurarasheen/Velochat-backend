package model;

import java.io.File;
import java.io.IOException;

public class AdbExecutor {
    
    private static final String PATH_TO_PLATFORM_TOOLS = "D:\\Android\\Sdk\\platform-tools";
    private static final String SENDER_NUMBER = "VeloChat";

    public static boolean sendSms(String platformToolsPath, String sender, String message) {

        String adbCommand = platformToolsPath + File.separator + "adb";

        ProcessBuilder processBuilder = new ProcessBuilder(
            adbCommand,
            "emu",
            "sms",
            "send",
            sender,
            message
        );

        System.out.println("Running command: " + String.join(" ", processBuilder.command()));

        try {

            Process process = processBuilder.start();

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("Command executed successfully.");
                return true;
            } else {
                System.err.println("Command failed with exit code: " + exitCode);
                return false;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean sendOtpToUser(String verificationMessage) {
        String pathToPlatformTools = AdbExecutor.PATH_TO_PLATFORM_TOOLS;
        String senderNumber = AdbExecutor.SENDER_NUMBER;

        boolean success = sendSms(pathToPlatformTools, senderNumber, verificationMessage);

        return success;
    }
}
