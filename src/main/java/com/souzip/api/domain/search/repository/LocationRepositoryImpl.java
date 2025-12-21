package com.souzip.api.domain.search.repository;

import co.elastic.clients.elasticsearch._types.query_dsl.*;
import com.souzip.api.domain.search.document.LocationDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Repository
public class LocationRepositoryImpl implements LocationRepositoryCustom {

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public List<SearchHit<LocationDocument>> searchByKeywordWithFuzzy(String keyword) {
        Query query = buildFunctionScoreQuery(keyword, false);
        return executeSearchWithHighlight(query, 100.0f);
    }

    @Override
    public List<SearchHit<LocationDocument>> searchWithMultipleKeywords(String[] keywords) {
        Query query = buildMultiKeywordQuery(keywords);
        return executeSearchWithHighlight(query, 100.0f);
    }

    private List<SearchHit<LocationDocument>> executeSearchWithHighlight(Query query, float minScore) {
        NativeQuery searchQuery = NativeQuery.builder()
            .withQuery(query)
            .withMinScore(minScore)
            .build();

        SearchHits<LocationDocument> searchHits = elasticsearchOperations.search(
            searchQuery,
            LocationDocument.class
        );

        return searchHits.stream().toList();
    }

    private Query buildMultiKeywordQuery(String[] keywords) {
        BoolQuery.Builder mustBuilder = new BoolQuery.Builder();

        for (String keyword : keywords) {
            mustBuilder.must(buildSingleKeywordQuery(keyword));
        }

        return FunctionScoreQuery.of(fs -> fs
            .query(mustBuilder.build()._toQuery())
            .functions(
                FunctionScore.of(f -> f
                    .filter(buildMultiKeywordExactFilter(keywords))
                    .weight(200.0)
                ),
                FunctionScore.of(f -> f
                    .filter(buildMultiKeywordPrefixFilter(keywords))
                    .weight(50.0)
                ),
                FunctionScore.of(f -> f
                    .filter(buildMultiKeywordWildcardFilter(keywords))
                    .weight(30.0)
                )
            )
            .scoreMode(FunctionScoreMode.Multiply)
            .boostMode(FunctionBoostMode.Multiply)
        )._toQuery();
    }

    private Query buildSingleKeywordQuery(String keyword) {
        return BoolQuery.of(b -> b
            .should(buildMatch("nameEn", keyword, 5.0f))
            .should(buildMatch("nameKr", keyword, 5.0f))
            .should(buildMatch("countryNameEn", keyword, 10.0f))
            .should(buildMatch("countryNameKr", keyword, 10.0f))
            .should(buildConditionalFuzzy("nameEn", keyword, 1.0f))
            .should(buildConditionalFuzzy("nameKr", keyword, 1.0f))
            .should(buildConditionalFuzzy("countryNameEn", keyword, 2.0f))
            .should(buildConditionalFuzzy("countryNameKr", keyword, 2.0f))
        )._toQuery();
    }

    private Query buildConditionalFuzzy(String field, String keyword, float boost) {
        if (keyword.length() <= 2) {
            return MatchQuery.of(m -> m
                .field(field)
                .query(keyword)
                .boost(0.1f)
            )._toQuery();
        }

        return MatchQuery.of(m -> m
            .field(field)
            .query(keyword)
            .fuzziness("1")
            .prefixLength(1)
            .boost(boost)
        )._toQuery();
    }

    private Query buildMultiKeywordExactFilter(String[] keywords) {
        BoolQuery.Builder mustBuilder = new BoolQuery.Builder();

        for (String keyword : keywords) {
            mustBuilder.must(
                BoolQuery.of(b -> b
                    .should(TermQuery.of(t -> t.field("nameEn.keyword").value(keyword))._toQuery())
                    .should(TermQuery.of(t -> t.field("nameKr.keyword").value(keyword))._toQuery())
                    .should(TermQuery.of(t -> t.field("countryNameEn.keyword").value(keyword))._toQuery())
                    .should(TermQuery.of(t -> t.field("countryNameKr.keyword").value(keyword))._toQuery())
                )._toQuery()
            );
        }

        return mustBuilder.build()._toQuery();
    }

    private Query buildMultiKeywordPrefixFilter(String[] keywords) {
        BoolQuery.Builder mustBuilder = new BoolQuery.Builder();

        for (String keyword : keywords) {
            mustBuilder.must(
                BoolQuery.of(b -> b
                    .should(PrefixQuery.of(p -> p.field("nameEn").value(keyword.toLowerCase()))._toQuery())
                    .should(PrefixQuery.of(p -> p.field("nameKr").value(keyword))._toQuery())
                    .should(PrefixQuery.of(p -> p.field("nameEn.keyword").value(keyword))._toQuery())
                    .should(PrefixQuery.of(p -> p.field("nameKr.keyword").value(keyword))._toQuery())
                    .should(PrefixQuery.of(p -> p.field("countryNameEn").value(keyword.toLowerCase()))._toQuery())
                    .should(PrefixQuery.of(p -> p.field("countryNameKr").value(keyword))._toQuery())
                    .should(PrefixQuery.of(p -> p.field("countryNameEn.keyword").value(keyword))._toQuery())
                    .should(PrefixQuery.of(p -> p.field("countryNameKr.keyword").value(keyword))._toQuery())
                )._toQuery()
            );
        }

        return mustBuilder.build()._toQuery();
    }

    private Query buildMultiKeywordWildcardFilter(String[] keywords) {
        BoolQuery.Builder mustBuilder = new BoolQuery.Builder();

        for (String keyword : keywords) {
            mustBuilder.must(
                BoolQuery.of(b -> b
                    .should(WildcardQuery.of(w -> w.field("nameKr.keyword").value(keyword + "*").caseInsensitive(true))._toQuery())
                    .should(WildcardQuery.of(w -> w.field("nameEn.keyword").value(keyword + "*").caseInsensitive(true))._toQuery())
                    .should(WildcardQuery.of(w -> w.field("countryNameKr.keyword").value(keyword + "*").caseInsensitive(true))._toQuery())
                    .should(WildcardQuery.of(w -> w.field("countryNameEn.keyword").value(keyword + "*").caseInsensitive(true))._toQuery())
                )._toQuery()
            );
        }

        return mustBuilder.build()._toQuery();
    }

    private Query buildFunctionScoreQuery(String keyword, boolean includeCountry) {
        return FunctionScoreQuery.of(fs -> fs
            .query(buildBaseQuery(keyword, includeCountry))
            .functions(
                FunctionScore.of(f -> f
                    .filter(buildExactMatchFilter(keyword, includeCountry))
                    .weight(100.0)
                ),
                FunctionScore.of(f -> f
                    .filter(buildPrefixFilter(keyword, includeCountry))
                    .weight(50.0)
                ),
                FunctionScore.of(f -> f
                    .filter(buildWildcardFilter(keyword, includeCountry))
                    .weight(20.0)
                )
            )
            .scoreMode(FunctionScoreMode.Multiply)
            .boostMode(FunctionBoostMode.Multiply)
        )._toQuery();
    }

    private Query buildBaseQuery(String keyword, boolean includeCountry) {
        BoolQuery.Builder builder = new BoolQuery.Builder()
            .should(buildMatch("nameEn", keyword, 5.0f))
            .should(buildMatch("nameKr", keyword, 5.0f))

            .should(WildcardQuery.of(w -> w
                .field("nameKr")
                .value("*" + keyword + "*")
                .boost(8.0f)
                .caseInsensitive(true)
            )._toQuery())
            .should(WildcardQuery.of(w -> w
                .field("nameEn")
                .value("*" + keyword.toLowerCase() + "*")
                .boost(8.0f)
                .caseInsensitive(true)
            )._toQuery())

            .should(buildConditionalFuzzy("nameEn", keyword, 2.0f))
            .should(buildConditionalFuzzy("nameKr", keyword, 2.0f));

        if (includeCountry) {
            builder
                .should(buildMatch("countryNameEn", keyword, 3.0f))
                .should(buildMatch("countryNameKr", keyword, 3.0f))
                .should(buildConditionalFuzzy("countryNameEn", keyword, 1.0f))
                .should(buildConditionalFuzzy("countryNameKr", keyword, 1.0f));
        }

        return builder.build()._toQuery();
    }

    private Query buildExactMatchFilter(String keyword, boolean includeCountry) {
        BoolQuery.Builder builder = new BoolQuery.Builder()
            .should(TermQuery.of(t -> t.field("nameEn.keyword").value(keyword))._toQuery())
            .should(TermQuery.of(t -> t.field("nameKr.keyword").value(keyword))._toQuery());

        if (includeCountry) {
            builder
                .should(TermQuery.of(t -> t.field("countryNameEn.keyword").value(keyword))._toQuery())
                .should(TermQuery.of(t -> t.field("countryNameKr.keyword").value(keyword))._toQuery());
        }

        return builder.build()._toQuery();
    }

    private Query buildPrefixFilter(String keyword, boolean includeCountry) {
        BoolQuery.Builder builder = new BoolQuery.Builder()
            .should(PrefixQuery.of(p -> p.field("nameEn").value(keyword.toLowerCase()))._toQuery())
            .should(PrefixQuery.of(p -> p.field("nameKr").value(keyword))._toQuery())
            .should(PrefixQuery.of(p -> p.field("nameEn.keyword").value(keyword))._toQuery())
            .should(PrefixQuery.of(p -> p.field("nameKr.keyword").value(keyword))._toQuery());

        if (includeCountry) {
            builder
                .should(PrefixQuery.of(p -> p.field("countryNameEn").value(keyword.toLowerCase()))._toQuery())
                .should(PrefixQuery.of(p -> p.field("countryNameKr").value(keyword))._toQuery())
                .should(PrefixQuery.of(p -> p.field("countryNameEn.keyword").value(keyword))._toQuery())
                .should(PrefixQuery.of(p -> p.field("countryNameKr.keyword").value(keyword))._toQuery());
        }

        return builder.build()._toQuery();
    }

    private Query buildWildcardFilter(String keyword, boolean includeCountry) {
        BoolQuery.Builder builder = new BoolQuery.Builder()
            .should(WildcardQuery.of(w -> w
                .field("nameKr.keyword")
                .value(keyword + "*")
                .caseInsensitive(true)
            )._toQuery())
            .should(WildcardQuery.of(w -> w
                .field("nameEn.keyword")
                .value(keyword.toLowerCase() + "*")
                .caseInsensitive(true)
            )._toQuery());

        if (includeCountry) {
            builder
                .should(WildcardQuery.of(w -> w
                    .field("countryNameKr.keyword")
                    .value(keyword + "*")
                    .caseInsensitive(true)
                )._toQuery())
                .should(WildcardQuery.of(w -> w
                    .field("countryNameEn.keyword")
                    .value(keyword.toLowerCase() + "*")
                    .caseInsensitive(true)
                )._toQuery());
        }

        return builder.build()._toQuery();
    }

    private Query buildMatch(String field, String keyword, float boost) {
        return MatchQuery.of(m -> m
            .field(field)
            .query(keyword)
            .boost(boost)
        )._toQuery();
    }
}
