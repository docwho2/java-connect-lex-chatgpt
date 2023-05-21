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

/**
 * A Response Message
 *
 * @author Mark Borner
 */
public class Message {

    private String contentType;
    private String content;

    public Message(String content) {
        if (content == null) {
            throw new IllegalArgumentException("Content should not be null");
        }
        this.content = content;
        if (content.startsWith("<speak>")) {
            contentType = "SSML";
        } else {
            contentType = "PlainText";
        }
    }

    public String getContentType() {
        return contentType;
    }

    public String getContent() {
        return content;
    }
}
