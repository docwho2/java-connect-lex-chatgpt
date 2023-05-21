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

/**
 * The Bot details
 *
 * @author Mark Borner
 */
public class Bot {

    private String name;
    private String alias;
    private String version;

    public Bot(@JsonProperty("name") String name,
               @JsonProperty("aliasName") String alias,
               @JsonProperty("version") String version) {
        this.name = name;
        this.alias = alias;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public String getAlias() {
        return alias;
    }

    public String getVersion() {
        return version;
    }
}
