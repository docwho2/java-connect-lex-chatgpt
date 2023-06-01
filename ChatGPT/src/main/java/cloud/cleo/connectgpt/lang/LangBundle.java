/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cloud.cleo.connectgpt.lang;


import static cloud.cleo.connectgpt.lang.LangUtil.LanguageIds.*;
import java.util.ListResourceBundle;

/**
 * English Strings
 *
 * @author sjensen
 */
public class LangBundle extends ListResourceBundle {

    private final static String[][] contents = {
        // Sorry, I'm having a problem fulfilling your request.  Chat GPT might be down, Please try again later.
        {UNHANDLED_EXCEPTION.toString(), "Sorry, I'm having a problem fulfilling your request.  Chat GPT might be down, Please try again later."},
        // I'm sorry, I didn't catch that, if your done, simply say good bye, otherwise tell me how I can help?
        {BLANK_RESPONSE.toString(), "I'm sorry, I didn't catch that, if your done, simply say good bye, otherwise tell me how I can help?"},
        // What else can I help you with?
        {ANYTHING_ELSE.toString(), "  What else can I help you with?"},
        // The operation timed out, please ask your question again
        {OPERATION_TIMED_OUT.toString(), "The operation timed out, please ask your question again"},
        // Response Language
        {CHATGPT_RESPONSE_LANGUAGE.toString(), "Please respond to all prompts in English"},
       
    };

    @Override
    protected Object[][] getContents() {
        return contents;
    }

}
