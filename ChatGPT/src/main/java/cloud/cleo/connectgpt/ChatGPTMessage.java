/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cloud.cleo.connectgpt;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

/**
 * Our representation of a ChatGPT Message to be stored in Dynamo DB
 *
 * @author sjensen
 */
@DynamoDbBean
public class ChatGPTMessage {

    private MessageRole role;
    private String content;

    public ChatGPTMessage() {
    }

    public ChatGPTMessage(MessageRole role, String content) {
        this.role = role;
        this.content = content;
    }

    public static enum MessageRole {
        user,
        system,
        assistant
    }

    /**
     * @return the role
     */
    public MessageRole getRole() {
        return role;
    }

    /**
     * @param role the role to set
     */
    public void setRole(MessageRole role) {
        this.role = role;
    }

    /**
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * @param content the content to set
     */
    public void setContent(String content) {
        this.content = content;
    }
}
