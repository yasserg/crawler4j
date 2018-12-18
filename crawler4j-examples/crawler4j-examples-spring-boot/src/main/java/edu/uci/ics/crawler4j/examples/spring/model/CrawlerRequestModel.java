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

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.URL;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(value="CrawlerRequestModel", description="A simple crawling request.")
public class CrawlerRequestModel {

    @ApiModelProperty(value = "Id of the crawling request.")
    private Long id;

    @ApiModelProperty(value = "Url to crawl.")
    @URL
    private String url;

    @ApiModelProperty(value = "Callback URL to call at end of the crawling.")
    @URL
    private String callback;

    private DateTime started;

    private DateTime finished;

    @ApiModelProperty(value = "Status of the crawling request.")
    private CrawlerStatus status;

    public static final CrawlerRequestModel EMPTY =
        new CrawlerRequestModel(
            -1L
            , StringUtils.EMPTY
            , StringUtils.EMPTY
            , DateTime.parse("1970-01-01 00:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm"))
            , DateTime.parse("1970-01-01 00:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm"))
            , CrawlerStatus.NONE
        );
}
