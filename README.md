# word-analyzer

Index .pdf|.docx documents using Elasticsearch and an ingestion data pipeline and the *ingest-attachment* bash plugin.

Do some simple analysis, expose example api and web page.

## Example usage

Run elastic stack locally
```bash
podman-compose up -d
```

Run application
```bash
mvn quarkus:dev
```

Create ingest pipeline
```json
DELETE _ingest/pipeline/attachment
PUT _ingest/pipeline/attachment
{
  "description" : "What did you hide in this file?",
  "processors" : [
    {
      "attachment" : {
        "field" : "data",
        "properties": [ "content", "title" ]
      },
      "remove":{
        "field":"data"
      }
    }
  ]
}
```

Create index
```bash
DELETE /engagements
PUT /engagements
{
  "mappings": {
    "properties": {
      "attachment.content": {
        "type": "text",
        "analyzer": "english"
      }
    }
  }
}
```

Upload documents
```bash
cd ~/Downloads
(echo -n '{"filename":"Week 1 Report - Vodafone Lab External.docx", "url": "https://docs.google.com/document/d/1kE61g4BEkZnjvoPyb5LZd690Fy-oALMZQ5FMGI8ONPY/edit", "data": "'; base64 ./'Week 1 Report - Vodafone Lab External.docx'; echo '"}') | curl -H "Content-Type: application/json" -d @-  http://localhost:9200/engagements/_doc/1?pipeline=attachment
(echo -n '{"filename":"Week 2 Report - Vodafone Lab External.docx", "url": "https://docs.google.com/document/d/1KkbHqO3F02c7uUh68ptfaUbE_RlcmBoazpbBeVJKdMA/edit", "data": "'; base64 ./'Week 2 Report - Vodafone Lab External.docx'; echo '"}') | curl -H "Content-Type: application/json" -d @-  http://localhost:9200/engagements/_doc/2?pipeline=attachment
(echo -n '{"filename":"Week 3 Report - Vodafone Lab External.docx", "url": "https://docs.google.com/document/d/14vLX9ptnqEy5-7Vf9q4S_Zseyz69ROFKS1VCiGmE9lE/edit", "data": "'; base64 ./'Week 3 Report - Vodafone Lab External.docx'; echo '"}') | curl -H "Content-Type: application/json" -d @-  http://localhost:9200/engagements/_doc/3?pipeline=attachment
(echo -n '{"filename":"Week 4 Report - Vodafone Lab External.docx", "url": "https://docs.google.com/document/d/1HREqetlM-JMTZlbbZKCGCr4G35L086bTl8ix_D7hZjs/edit", "data": "'; base64 ./'Week 4 Report - Vodafone Lab External.docx'; echo '"}') | curl -H "Content-Type: application/json" -d @-  http://localhost:9200/engagements/_doc/4?pipeline=attachment
(echo -n '{"filename":"Week 5 Report - Vodafone Lab External.docx", "url": "https://docs.google.com/document/d/1_dfOTBxXTDGmVDxOh-K9XhsirZVLILBtvVrFYMPJmmI/edit", "data": "'; base64 ./'Week 5 Report - Vodafone Lab External.docx'; echo '"}') | curl -H "Content-Type: application/json" -d @-  http://localhost:9200/engagements/_doc/5?pipeline=attachment
(echo -n '{"filename":"Week 6 Report - Vodafone Lab External.docx", "url": "https://docs.google.com/document/d/1rd9H23Ou2g9GwJiN9QSSMS4kIPcpdM6fPWHnEZ6yedE/edit", "data": "'; base64 ./'Week 6 Report - Vodafone Lab External.docx'; echo '"}') | curl -H "Content-Type: application/json" -d @-  http://localhost:9200/engagements/_doc/6?pipeline=attachment
(echo -n '{"filename":"Week 7 Report - Vodafone Lab External.docx", "url": "https://docs.google.com/document/d/1UQI6WVhZYDySHo8_qCzSK3FiaP_ONYr9tA3UHxp8kW4/edit", "data": "'; base64 ./'Week 7 Report - Vodafone Lab External.docx'; echo '"}') | curl -H "Content-Type: application/json" -d @-  http://localhost:9200/engagements/_doc/7?pipeline=attachment
(echo -n '{"filename":"Week 8 Report - Vodafone Lab External.docx", "url": "https://docs.google.com/document/d/19_ShKLPtJhos9SZFbO3rFOTjWRZhBWaXcsKUD1S8sKY/edit", "data": "'; base64 ./'Week 8 Report - Vodafone Lab External.docx'; echo '"}') | curl -H "Content-Type: application/json" -d @-  http://localhost:9200/engagements/_doc/8?pipeline=attachment
```

Match all
```bash
GET engagements/_search
{
  "query": {
    "match_all": {}
  }
}
```

Search for reports containing a phrase

- https://www.elastic.co/guide/en/elasticsearch/reference/master/search-aggregations-bucket-significanttext-aggregation.html

```json
GET engagements/_search
{
  "_source": false,
  "fields": ["filename", "url"],
  "query": {
    "match_phrase": { "attachment.content": "event storming" }
  },
  "aggregations": {
    "event_storming": {
      "sampler": {
        "shard_size": 100
      },
      "aggregations": {
        "keywords": {
          "significant_text": { "field": "attachment.content" }
        }
      }
    }
  }
}
```

Reindex reports containing a phrase

- https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-reindex.html#docs-reindex-select-query

```json
POST _reindex
{
  "source": {
    "index": "engagements",
    "fields": ["filename", "url"],
    "query": {
      "match_phrase": { "attachment.content": "event storming" }
    },
    "aggregations": {
      "event_storming": {
        "sampler": {
          "shard_size": 100
        },
        "aggregations": {
          "keywords": {
            "significant_text": { "field": "attachment.content" }
          }
        }
      }
    }
  },
  "dest": {
    "index": "event-storming"
  }
}
```
