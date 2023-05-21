/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cloud.cleo.connectgpt;

import com.github.softwarebymark.lex.lambda.LexRequestStreamHandler;

/**
 *
 * @author sjensen
 */
public class ChatGPTLambda extends LexRequestStreamHandler {
    
    public ChatGPTLambda() {
        super(new ChatGPTHandler());
    }
    
}
