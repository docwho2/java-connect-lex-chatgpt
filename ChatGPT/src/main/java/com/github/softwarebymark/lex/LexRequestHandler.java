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

import com.github.softwarebymark.lex.domain.LexRequest;
import com.github.softwarebymark.lex.domain.LexResponse;

import java.util.Map;

/**
 * An interface for a handler which processes a Lex request
 *
 * @author Mark Borner
 */
public interface LexRequestHandler {

    /**
     * Handles the given request from Lex and returns a response
     *
     * @param lexRequest The Lex Request
     * @param sessionAttributes A modifiable list of session attributes that will be populated into the Lex Response
     * @return The Lex Response
     */
    LexResponse handleRequest(LexRequest lexRequest, Map<String,String> sessionAttributes);

}
