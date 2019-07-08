package com.esch.eschfindr.registration;

import android.media.MediaPlayer;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;

/**
 * Helper Class to faciliate easier registration
 */

public class ProfileBuilder {

    private String email = null;
    private String password = null;
    private String nickname = null;

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Pass only sanitized entries here
     * @param listener
     */

    public void build(OnCompleteListener<AuthResult> listener) {
        if(nickname == null || password == null || email == null) try {
            throw new RegistrationException("Err");
        } catch (RegistrationException e) {
            e.printStackTrace();
            System.exit(100);
        }


    }

    private class RegistrationException extends Throwable {
        public RegistrationException(String err) {
            super(err);
        }
    }
}
