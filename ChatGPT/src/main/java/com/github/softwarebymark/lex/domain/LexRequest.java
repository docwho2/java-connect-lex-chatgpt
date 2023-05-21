/*
 *    Copyright 2017 Mark Borner
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.github.softwarebymark.lex.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * A Lex Request
 *
 * @author Mark Borner
 */
public class LexRequest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
    }

    //private Intent intent;
    private Bot bot;
    //private String userId;
    private String inputTranscript;
    private InvocationSource invocationSource;
    //private OutputDialogMode outputDialogMode;
    private String messageVersion;
    private SessionState sessionState;

    public LexRequest(
                      @JsonProperty("bot") Bot bot,
                      @JsonProperty("inputTranscript") String inputTranscript,
                      @JsonProperty("invocationSource") InvocationSource invocationSource,
                      @JsonProperty("messageVersion") String messageVersion,
                      @JsonProperty("sessionState") SessionState sessionState) {
        this.bot = bot;
        this.inputTranscript = inputTranscript;
        this.invocationSource = invocationSource;
        this.messageVersion = messageVersion;
        this.sessionState = sessionState;
        
    }

    public static LexRequest fromJson(byte[] requestJson) throws Exception {
        return OBJECT_MAPPER.readValue(requestJson, LexRequest.class);
    }

   

    public Bot getBot() {
        return bot;
    }


    public String getInputTranscript() {
        return inputTranscript;
    }

    public InvocationSource getInvocationSource() {
        return invocationSource;
    }


    public String getMessageVersion() {
        return messageVersion;
    }

    public SessionState getSessionState() {
        return sessionState;
    }

}
