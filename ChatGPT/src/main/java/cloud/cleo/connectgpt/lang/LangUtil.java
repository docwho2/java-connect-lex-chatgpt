/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cloud.cleo.connectgpt.lang;



import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Class to get Language Strings
 *
 * @author sjensen
 */
public class LangUtil {

    // Cache Bundles
    private final static Map<Locale, ResourceBundle> map = new HashMap<>();

    // Reference the Bundle class Name in this package
    private final static String BUNDLE_NAME = LangUtil.class.getPackageName() + ".LangBundle";

    /**
     * ID's for all available language Strings
     */
    public enum LanguageIds {
        /**
         * Sorry, I'm having a problem fulfilling your request.  Chat GPT might be down, Please try again later.
         */
        UNHANDLED_EXCEPTION,
        /**
         * I'm sorry, I didn't catch that, if your done, simply say good bye, otherwise tell me how I can help?
         */
        BLANK_RESPONSE,
        /**
         * What else can I help you with?
         */
        ANYTHING_ELSE,
        /**
         * The operation timed out, please ask your question again
         */
        OPERATION_TIMED_OUT,
        /**
         * Specify the Chat language response
         */
        CHATGPT_RESPONSE_LANGUAGE,
       
    }

    private final Locale locale;

    public LangUtil(String locale) {
        final var splits = locale.split("_");
        this.locale =  new Locale(splits[0], splits[1]);
    }


    public Locale getLocale() {
        return locale;
    }

    public String getString(LanguageIds id) {
        return getString(id, locale);
    }

    public static String getString(LanguageIds id, Locale locale) {
        var bundle = map.get(locale);
        if (bundle == null) {
            bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
            map.put(locale, bundle);
        }
        return bundle.getString(id.toString());
    }
    
   
}
