# Crawler4j Spring boot integration example

On popular demand here and example of integration with Spring Boot. It has been designed to be the most idiomatic as possible
(`@Service`, `@Async`, hibernate-validator etc).

Run it:

```bash
$ mvn spring-boot:run -Dspring.profiles.active=dev
```

Submit a crawler request:

```bash
 curl -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{
  "url": "http://example.com",
  "callback": ""
}' "http://localhost:8080/api/v1/crawl"
```

- callback: you can specify URL to callback when the crawling is done. The call will POST the response (see later) to the 
specified callback URL.

The response is similar to:

```json5
{
    "id":1,
    "url":"http://example.com",
    "callback":"",
    "started":"2018-12-15T08:09:40.436Z",
    "status":"ACCEPTED",
}
```

Monitor the status of a crawl request:

```bash
curl -X GET -v "http://localhost:8080/api/v1/crawl/1"
``` 

```json5
{
    "id":1,
    "url":"http://example.com",
    "callback":"",
    "started":"2018-12-14T19:59:22.665Z",
    "status":"DONE",
}
```
