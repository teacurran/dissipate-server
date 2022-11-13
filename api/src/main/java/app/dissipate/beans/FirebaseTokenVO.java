package app.dissipate.beans;

import com.google.firebase.auth.FirebaseToken;

public class FirebaseTokenVO {
    String uid;
    String tenantId;
    String issuer;
    String picture;
    boolean isEmailVerified;
    String email;

    public FirebaseTokenVO() {
    }

    public FirebaseTokenVO(FirebaseToken fbToken) {
        this.uid = fbToken.getUid();
        this.tenantId = fbToken.getTenantId();
        this.issuer = fbToken.getIssuer();
        this.picture = fbToken.getPicture();
        this.isEmailVerified = fbToken.isEmailVerified();
        this.email = fbToken.getEmail();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public boolean isEmailVerified() {
        return isEmailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        isEmailVerified = emailVerified;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
