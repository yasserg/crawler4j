package edu.uci.ics.crawler4j.examples.spring.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.URL;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CrawlerRequestModel {

    private Long id;

    @URL
    private String url;
    @URL
    private String callback;

    private DateTime started;

    private DateTime finished;

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
