/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.github.softwarebymark.lex.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author sjensen
 */
public class SessionState {
    
    
    private Map<String,String> sessionAttributes;
    private Intent intent;

    protected SessionState() {
    }
  
    
    
    public SessionState(@JsonProperty("sessionAttributes") Map<String,String> sessionAttributes,
                  @JsonProperty("intent")Intent intent) {
        this.sessionAttributes = sessionAttributes;
        this.intent = intent;
    }
    
     public Map<String, String> getSessionAttributes() {
        return sessionAttributes == null ? Collections.unmodifiableMap(new HashMap<>()) : Collections.unmodifiableMap(sessionAttributes);
    }

    public String getSessionAttribute(String key) {
        return sessionAttributes.get(key);
    }
    
     public Intent getIntent() {
        return intent;
    }
  

    public void setSessionAttributes(Map<String, String> sessionAttributes) {
        this.sessionAttributes = sessionAttributes;
    }
}
