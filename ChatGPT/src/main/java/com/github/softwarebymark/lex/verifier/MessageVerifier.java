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

package com.github.softwarebymark.lex.verifier;

import com.github.softwarebymark.lex.domain.Message;

/**
 * A Message Verifier
 *
 * @author Mark Borner
 */
public class MessageVerifier extends AbstractVerifier<Message> {

    public static int MAX_CONTENT_LENGTH = 1000;

    @Override
    public void verify(Message message) {
        if (message.getContent().length() > MAX_CONTENT_LENGTH) {
            logger.warn("Content length should be less than {}: {}", MAX_CONTENT_LENGTH, message.getContent());
        }
    }
}
