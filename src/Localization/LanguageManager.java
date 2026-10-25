package Localization;

import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageManager {

    private Locale locale;
    private ResourceBundle messages;

    public LanguageManager(Locale locale) {
        setLocale(locale);
    }

    private void loadMessages() {
        messages = ResourceBundle.getBundle(
            "i18n.messages",
            locale
        );
    }

    public String get(String key) {
        return messages.getString(key);
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
        loadMessages();
    }
}