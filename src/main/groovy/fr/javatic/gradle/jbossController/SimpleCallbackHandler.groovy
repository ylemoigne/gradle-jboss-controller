package fr.javatic.gradle.jbossController

import javax.security.auth.callback.*
import javax.security.sasl.RealmCallback

class SimpleCallbackHandler implements CallbackHandler {
    private String username
    private String password

    SimpleCallbackHandler(String username, String password) {
        this.username = username
        this.password = password
    }

    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (Callback current : callbacks) {
            if (current instanceof NameCallback) {
                NameCallback ncb = (NameCallback) current;
                ncb.setName(username);
            } else if (current instanceof PasswordCallback) {
                PasswordCallback pcb = (PasswordCallback) current;
                pcb.setPassword(password.toCharArray());
            } else if (current instanceof RealmCallback) {
                RealmCallback rcb = (RealmCallback) current;
                rcb.setText(rcb.getDefaultText());
            } else {
                throw new UnsupportedCallbackException(current);
            }
        }
    }
}