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

import com.github.softwarebymark.lex.domain.GenericAttachment;
import com.github.softwarebymark.lex.domain.ResponseCard;

/**
 * A Response Card Verifier
 *
 * @author Mark Borner
 */
public class ResponseCardVerifier extends AbstractVerifier<ResponseCard> {

    public static final int MAX_GENERIC_ATTACHMENTS = 10;
    private GenericAttachmentVerifier genericAttachmentVerifier = new GenericAttachmentVerifier();

    @Override
    public void verify(ResponseCard responseCard) {
        if (responseCard.getGenericAttachments().size() > MAX_GENERIC_ATTACHMENTS) {
            logger.warn("Generic Attachments should be less than {}", MAX_GENERIC_ATTACHMENTS);
        }

        for (GenericAttachment genericAttachment : responseCard.getGenericAttachments()) {
            genericAttachmentVerifier.verify(genericAttachment);
        }
    }
}
