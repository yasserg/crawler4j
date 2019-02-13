/*
 * Copyright 2018 Federico Tolomei <mail@s17t.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uci.ics.crawler4j.examples.spring.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public enum CrawlerStatus {
    @ApiModelProperty(value = "The request has been accepted and the crawler has started.")
    ACCEPTED,
    @ApiModelProperty(value = "The crawler is still working.")
    WORKING,
    @ApiModelProperty(value = "The crawler died for unexpected error.")
    ERROR,
    @ApiModelProperty(hidden = true)
    NONE,
    @ApiModelProperty(value = "The crawler process finished. This does not make assumption over a successful state.")
    DONE
}