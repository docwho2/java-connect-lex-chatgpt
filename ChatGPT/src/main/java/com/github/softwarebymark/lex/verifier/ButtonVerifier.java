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

/**
 * A Button Verifier
 *
 * @author Mark Borner
 */
public class ButtonVerifier extends AbstractVerifier<Button> {

    public static int MAX_TEXT_LENGTH = 15;
    public static int MAX_VALUE_LENGTH = 1000;

    @Override
    public void verify(Button button) {
        if (button.getText().length() > MAX_TEXT_LENGTH) {
            logger.warn("Button Text length should be less than {} characters: {}", MAX_TEXT_LENGTH, button.getText());
        }
        if (button.getValue().length() > MAX_VALUE_LENGTH) {
            logger.warn("Button Value length should be less than {} characters: {}", MAX_VALUE_LENGTH, button.getValue());
        }
    }
}
