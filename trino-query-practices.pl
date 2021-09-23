#!/usr/bin/env perl
use strict;
use Convert::Base32;

#
# Generate a query we can run in trino / superset for all practies
# Need to base32 adhoc queries. Using attachment search aggregator.
#

my @practices;
my $file = 'src/main/resources/trino-practice-list';
open my $handle, "<$file" || die "cant open $file $!";
chomp(@practices = <$handle>);
close $handle;

sub getQueryString() {
    my ($p) = @_;
    my $query_string = qq[{
       "_source": [
       "filename",
       "url"
       ],
       "query": {
         "match_phrase": {
           "attachment.content": "$p"
         }
       },
       "aggregations": {
         "practices": {
           "sampler": {
                  "shard_size": 100
           },
           "aggregations": {
             "keywords": {
               "significant_text": {
                 "field": "attachment.content"
                }
              }
            }
          }
        }
    }];
    return $query_string;
}

my $top = q[select count(*) as cnt_total,];
my $prefix = q[select json_extract(result, '$.hits.total.value') from elasticsearch.default."engagements-read$query:];
my $bottom = q[from elasticsearch.default."engagements-read" e];

my $counter;
print $top;
foreach my $p (@practices) {
    #print $prefix, &getQueryString($p), "\n";
    my $query_string_b32 = encode_base32(&getQueryString($p));
    (my $name = $p) =~ s/\s/_/g;
    my $doc = qq[/* "attachment.content": $p */];
    if (++$counter == scalar(@practices)) {
        # last element no comma
        print $doc, "\n(", $prefix, $query_string_b32, '"', ") as cnt_$name", "\n";        
    } else {
        print $doc, "\n(", $prefix, $query_string_b32, '"', ") as cnt_$name,", "\n";
    }
}
print $bottom, "\n";
