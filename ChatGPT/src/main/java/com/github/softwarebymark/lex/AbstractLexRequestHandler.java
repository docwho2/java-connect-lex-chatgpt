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

package com.github.softwarebymark.lex;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.softwarebymark.lex.domain.*;
import com.github.softwarebymark.lex.domain.action.*;

import java.util.Map;

/**
 * An abstract Lex Request Handler with helper methods for creating Lex Response objects
 * and serialising/de-serialising objects from the session attributes.
 *
 * @author Mark Borner
 */
public abstract class AbstractLexRequestHandler implements LexRequestHandler {

    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Creates a Lex Request with a Delegate Dialog Action
     *
     * @param slots A map of slots
     * @return A Lex Response
     */
//    protected LexResponse createDelegateDialogActionResponse(Map<String,String> slots) {
//        return new LexResponse(new DelegateDialogAction(slots));
//    }

    /**
     * Creates a Lex Request with a Close Dialog Action
     *
     * @param fulfillmentState The fulfillment state for the response
     * @return A Lex Response
     */
//    protected LexResponse createCloseDialogActionResponse(FulfillmentState fulfillmentState) {
//        return new LexResponse(new CloseDialogAction(fulfillmentState));
//    }

    /**
     * Creates a Lex Request with a Close Dialog Action
     *
     * @param fulfillmentState The fulfillment state for the response
     * @param message The message for the response
     * @return A Lex Response
     */
    protected LexResponse createCloseDialogActionResponse(FulfillmentState fulfillmentState, String message, Intent intent) {
        return new LexResponse(new CloseDialogAction(fulfillmentState), message, intent);
    }

    /**
     * Creates a Lex Request with a Close Dialog Action
     *
     * @param fulfillmentState The fulfillment state for the response
     * @param message The message for the response
     * @param responseCard The response card for the response
     * @return A Lex Response
     */
//    protected LexResponse createCloseDialogActionResponse(FulfillmentState fulfillmentState, String message, ResponseCard responseCard) {
//        return new LexResponse(new CloseDialogAction(fulfillmentState, new Message(message), responseCard));
//    }

    /**
     * Creates a Lex Request with a Close Dialog Action
     *
     * @param fulfillmentState The fulfillment state for the response
     * @param responseCard The response card for the response
     * @return A Lex Response
     */
//    protected LexResponse createCloseDialogActionResponse(FulfillmentState fulfillmentState, ResponseCard responseCard) {
//        return new LexResponse(new CloseDialogAction(fulfillmentState, responseCard));
//    }

   

    
    /**
     * Serialises and saves an object into the Session.  This is a handy way of storing a Java POJO
     * into the Lex Session.
     *
     * @param sessionAttributes The session attributes
     * @param key The key for the attribute
     * @param value The Object to serialise as the value
     * @param typeReference A Type Reference for the Object to serialise
     */
    protected void saveObjectIntoSession(Map<String,String> sessionAttributes, String key, Object value, TypeReference<?> typeReference) {
        String serializedObject;
        try {
            // Note: we have to use the @depreciated method because the Lambda runtime uses an older Jackson library
            serializedObject = objectMapper.writerWithType(typeReference).writeValueAsString(value);
        } catch (Exception e) {
            throw new RuntimeException("Unable to serialize object to session", e);
        }
        sessionAttributes.put(key, serializedObject);
    }

    /**
     * Gets and de-serialises an object from the Session.  This is a handy way of retrieving a Java
     * POJO from the Lex session.
     *
     * @param sessionAttributes The session attributes
     * @param key The key of the attribute
     * @param typeReference A Type Reference for the Object to deserialise
     * @param <TYPE> the class of the Java POJO
     * @return the Java POJO
     */
    protected <TYPE> TYPE getObjectFromSession(Map<String,String> sessionAttributes, String key, TypeReference<TYPE> typeReference) {
        String serializedObject = sessionAttributes.get(key);
        if (serializedObject == null) {
            return null;
        }
        TYPE object;
        try {
            // Note: we have to use the @depreciated method because the Lambda runtime uses an older Jackson library
            object = objectMapper.reader(typeReference).readValue(serializedObject);
        } catch (Exception e) {
            throw new RuntimeException("Unable to deserialize object from session", e);
        }
        return object;
    }

}
