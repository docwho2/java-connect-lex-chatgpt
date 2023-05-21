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

import com.github.softwarebymark.lex.domain.Button;
import com.github.softwarebymark.lex.domain.GenericAttachment;

/**
 * A Generic Attachment Verifier
 *
 * @author Mark Borner
 */
public class GenericAttachmentVerifier extends AbstractVerifier<GenericAttachment> {

    public static int MAX_TITLE_LENGTH = 80;
    public static  int MAX_SUBTITLE_LENGTH = 80;
    public static  int MAX_URL_LENGTH = 2048;
    public static  int MAX_BUTTONS = 5;
    public static  int MAX_FACEBOOK_BUTTONS = 3;
    private ButtonVerifier buttonVerifier = new ButtonVerifier();

    @Override
    public void verify(GenericAttachment genericAttachment) {
        if (genericAttachment.getTitle().length() > MAX_TITLE_LENGTH) {
            logger.warn("Generic Attachment Title should not be greater than {} characters: {}", MAX_TITLE_LENGTH, genericAttachment.getTitle());
        }
        if (genericAttachment.getSubTitle() != null && genericAttachment.getSubTitle().length() > MAX_SUBTITLE_LENGTH) {
            logger.warn("Generic Attachment SubTitle should not be greater than {} characters: {}", MAX_SUBTITLE_LENGTH, genericAttachment.getSubTitle());
        }
        if (genericAttachment.getImageUrl() != null && genericAttachment.getImageUrl().length() > MAX_URL_LENGTH) {
            logger.warn("Generic Attachment ImageUrl should not be greater than {} characters: {}", MAX_URL_LENGTH, genericAttachment.getImageUrl());
        }
        if (genericAttachment.getAttachmentLinkUrl() != null && genericAttachment.getAttachmentLinkUrl().length() > 2048) {
            logger.warn("Generic Attachment AttachmentLinkUrl should not be greater than {} characters: {}", MAX_URL_LENGTH, genericAttachment.getAttachmentLinkUrl());
        }

        if (genericAttachment.getButtons() != null) {
            if (genericAttachment.getButtons().size() > MAX_BUTTONS) {
                logger.warn("Number of Buttons should be less than {} for Generic Attachment with title: {}", MAX_BUTTONS, genericAttachment.getTitle());
            }
            if (genericAttachment.getButtons().size() > MAX_FACEBOOK_BUTTONS) {
                logger.warn("Facebook only allows {} buttons - Generic Attachment with title: {}", MAX_FACEBOOK_BUTTONS, genericAttachment.getTitle());
            }

            for (Button button : genericAttachment.getButtons()) {
                buttonVerifier.verify(button);
            }
        }
    }
}
