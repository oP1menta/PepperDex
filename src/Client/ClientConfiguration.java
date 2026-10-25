package Client;

import java.util.Locale;

public class ClientConfiguration {

    private Locale locale;

    public ClientConfiguration(Locale locale) {

        setLocale(locale);

    }

    //=========SETTERS============

    public void setLocale(Locale locale) {

        this.locale = locale;

    }

    //==========GETTERS===========

    public Locale getLocale() {

        return locale;

    }

}