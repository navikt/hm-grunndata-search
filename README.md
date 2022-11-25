# hm-grunndata-search
Search api for Grunndata.

## Start Application:

``` 
./gradlew build

export OPEN_SEARCH_URI <url to opensearch in dev>
export OPEN_SEARCH_USERNAME <username>
export OPEN_SEARCH_PASSWORD <password>

./gradlew run

```

## Query examples

```
http://localhost:8080/product/_search
http://localhost:8080/supplier/_search
http://localhost:8080/agreement/_search

```
Documentation for how to [query](https://opensearch.org/docs/1.3/opensearch/query-dsl/index/)


