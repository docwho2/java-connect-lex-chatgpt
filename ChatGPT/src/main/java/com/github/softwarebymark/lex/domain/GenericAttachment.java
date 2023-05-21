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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A Generic Attachment for a Response Card
 *
 * @author Mark Borner
 */
public class GenericAttachment {

    private String title;
    private String subTitle;
    private String imageUrl;
    private String attachmentLinkUrl;
    private List<Button> buttons;

    public GenericAttachment(String title) {
        if (title == null) {
            throw new IllegalArgumentException("Title should not be null");
        }
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public List<Button> getButtons() {
        return buttons == null ? null : Collections.unmodifiableList(buttons);
    }

    public void addButton(String text, String value) {
        if (buttons == null) {
            // We are lazily instantiating this because Lex doesn't like an empty Button array
            buttons = new ArrayList<>();
        }
        buttons.add(new Button(text, value));
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAttachmentLinkUrl() {
        return attachmentLinkUrl;
    }

    public void setAttachmentLinkUrl(String attachmentLinkUrl) {
        this.attachmentLinkUrl = attachmentLinkUrl;
    }
}
