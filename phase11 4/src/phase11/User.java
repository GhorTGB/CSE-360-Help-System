package phase11;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class User {
    private String username;
    private String password;
    private String email;
    private String otp;
    private Date otpExpiration;
    private List<String> roles;

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.roles = new ArrayList<>();
    }

    public void setOtp(String otp, Date expiration) {
        this.otp = otp;
        this.otpExpiration = expiration;
        System.out.println("OTP set to: " + otp + ", Expiration Time: " + expiration);
    }

    public boolean isOtpValid(String enteredOtp) {
        Date currentTime = new Date();
        System.out.println("Current Time: " + currentTime);
        System.out.println("OTP Expiration Time: " + otpExpiration);

        if (this.otp == null || currentTime.after(otpExpiration)) {
            System.out.println("OTP is either null or expired.");
            return false;
        }

        enteredOtp = enteredOtp.trim();
        return this.otp.equals(enteredOtp);
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public void addRole(String role) {
        if (!roles.contains(role)) {
            roles.add(role);
        }
    }

    public void removeRole(String role) {
        roles.remove(role);
    }

    public List<String> getRoles() {
        return roles;
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }
}
