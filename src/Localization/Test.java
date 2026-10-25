package Localization;

import java.util.Locale;

public class Test {

    public static void main(String[] args) {

        LanguageManager languageManager =
                new LanguageManager(Locale.of("pt", "BR"));

        System.out.println(
                languageManager.get("connected")
        );

        languageManager.setLocale(
                Locale.of("en", "US")
        );

        System.out.println(
                languageManager.get("connected")
        );
    }
}