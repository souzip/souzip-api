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
        return executeSearch(query, 1.0f);
    }

    @Override
    public List<SearchHit<LocationDocument>> searchWithMultipleKeywords(String[] keywords) {
        Query query = buildMultiKeywordQuery(keywords);
        return executeSearch(query, 1.0f);
    }

    private List<SearchHit<LocationDocument>> executeSearch(Query query, float minScore) {
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

    private Query buildFunctionScoreQuery(String keyword, boolean includeCountry) {
        return FunctionScoreQuery.of(fs -> fs
            .query(buildBaseQuery(keyword, includeCountry))
            .functions(
                // 1. 완전 일치 - 최고 점수
                FunctionScore.of(f -> f
                    .filter(buildExactMatchFilter(keyword, includeCountry))
                    .weight(100000.0)
                ),
                // 2. Prefix 매칭
                FunctionScore.of(f -> f
                    .filter(buildPrefixFilter(keyword, includeCountry))
                    .weight(50000.0)
                ),
                // 3. Fuzzy 매칭 (편집 거리 1-2)
                FunctionScore.of(f -> f
                    .filter(buildFuzzyFilter(keyword, includeCountry))
                    .weight(30000.0)
                ),
                // 4. 자소 매칭 - 낮춤 (부분 매칭 허용)
                FunctionScore.of(f -> f
                    .filter(buildJasoFilter(keyword, includeCountry))
                    .weight(5000.0)
                ),
                // 5. Autocomplete
                FunctionScore.of(f -> f
                    .filter(buildAutocompleteFilter(keyword, includeCountry))
                    .weight(1000.0)
                ),
                // 6. Ngram
                FunctionScore.of(f -> f
                    .filter(buildNgramFilter(keyword, includeCountry))
                    .weight(500.0)
                ),
                // 7. Wildcard
                FunctionScore.of(f -> f
                    .filter(buildWildcardFilter(keyword, includeCountry))
                    .weight(100.0)
                )
            )
            .scoreMode(FunctionScoreMode.Sum)
            .boostMode(FunctionBoostMode.Replace)
        )._toQuery();
    }

    private Query buildBaseQuery(String keyword, boolean includeCountry) {
        BoolQuery.Builder builder = new BoolQuery.Builder()
            // 일반 매칭
            .should(buildMatch("nameEn", keyword, 50.0f))
            .should(buildMatch("nameKr", keyword, 50.0f))
            // Fuzzy 매칭 - 가중치 높임
            .should(buildConditionalFuzzy("nameEn", keyword, 200.0f))
            .should(buildConditionalFuzzy("nameKr", keyword, 200.0f))
            // 자소 매칭 - 가중치 낮춤
            .should(buildMatch("nameEn.jaso", keyword, 100.0f))
            .should(buildMatch("nameKr.jaso", keyword, 100.0f))
            // Autocomplete
            .should(buildMatch("nameEn.autocomplete", keyword, 30.0f))
            .should(buildMatch("nameKr.autocomplete", keyword, 30.0f))
            // Ngram
            .should(buildMatch("nameEn.ngram", keyword, 80.0f))
            .should(buildMatch("nameKr.ngram", keyword, 80.0f))
            // Wildcard
            .should(buildWildcard("nameKr", keyword, 20.0f))
            .should(buildWildcard("nameEn", keyword, 20.0f));

        if (includeCountry) {
            builder
                .should(buildMatch("countryNameEn", keyword, 25.0f))
                .should(buildMatch("countryNameKr", keyword, 25.0f))
                .should(buildMatch("countryNameEn.jaso", keyword, 50.0f))
                .should(buildMatch("countryNameKr.jaso", keyword, 50.0f))
                .should(buildMatch("countryNameEn.ngram", keyword, 40.0f))
                .should(buildMatch("countryNameKr.ngram", keyword, 40.0f))
                .should(buildConditionalFuzzy("countryNameEn", keyword, 50.0f))
                .should(buildConditionalFuzzy("countryNameKr", keyword, 50.0f));
        }

        return builder.build()._toQuery();
    }

    private Query buildExactMatchFilter(String keyword, boolean includeCountry) {
        BoolQuery.Builder builder = new BoolQuery.Builder()
            .should(buildTerm("nameEn.keyword", keyword))
            .should(buildTerm("nameKr.keyword", keyword));

        if (includeCountry) {
            builder
                .should(buildTerm("countryNameEn.keyword", keyword))
                .should(buildTerm("countryNameKr.keyword", keyword));
        }

        return builder.build()._toQuery();
    }

    private Query buildPrefixFilter(String keyword, boolean includeCountry) {
        BoolQuery.Builder builder = new BoolQuery.Builder()
            .should(buildPrefix("nameEn", keyword))
            .should(buildPrefix("nameKr", keyword))
            .should(buildPrefix("nameEn.keyword", keyword))
            .should(buildPrefix("nameKr.keyword", keyword));

        if (includeCountry) {
            builder
                .should(buildPrefix("countryNameEn", keyword))
                .should(buildPrefix("countryNameKr", keyword))
                .should(buildPrefix("countryNameEn.keyword", keyword))
                .should(buildPrefix("countryNameKr.keyword", keyword));
        }

        return builder.build()._toQuery();
    }

    private Query buildJasoFilter(String keyword, boolean includeCountry) {
        BoolQuery.Builder builder = new BoolQuery.Builder()
            .should(buildMatch("nameEn.jaso", keyword, 5.0f))
            .should(buildMatch("nameKr.jaso", keyword, 5.0f));

        if (includeCountry) {
            builder
                .should(buildMatch("countryNameEn.jaso", keyword, 2.0f))
                .should(buildMatch("countryNameKr.jaso", keyword, 2.0f));
        }

        return builder.build()._toQuery();
    }

    private Query buildFuzzyFilter(String keyword, boolean includeCountry) {
        if (keyword.length() <= 1) {
            return buildTerm("_id", "never_match");
        }

        BoolQuery.Builder builder = new BoolQuery.Builder()
            .should(buildFuzzyQuery("nameEn", keyword, 10.0f))
            .should(buildFuzzyQuery("nameKr", keyword, 10.0f));

        if (includeCountry) {
            builder
                .should(buildFuzzyQuery("countryNameEn", keyword, 5.0f))
                .should(buildFuzzyQuery("countryNameKr", keyword, 5.0f));
        }

        return builder.build()._toQuery();
    }

    private Query buildAutocompleteFilter(String keyword, boolean includeCountry) {
        BoolQuery.Builder builder = new BoolQuery.Builder()
            .should(buildMatch("nameEn.autocomplete", keyword, 1.0f))
            .should(buildMatch("nameKr.autocomplete", keyword, 1.0f));

        if (includeCountry) {
            builder
                .should(buildMatch("countryNameEn.autocomplete", keyword, 1.0f))
                .should(buildMatch("countryNameKr.autocomplete", keyword, 1.0f));
        }

        return builder.build()._toQuery();
    }

    private Query buildWildcardFilter(String keyword, boolean includeCountry) {
        BoolQuery.Builder builder = new BoolQuery.Builder()
            .should(buildWildcardQuery("nameKr.keyword", keyword + "*"))
            .should(buildWildcardQuery("nameEn.keyword", keyword + "*"));

        if (includeCountry) {
            builder
                .should(buildWildcardQuery("countryNameKr.keyword", keyword + "*"))
                .should(buildWildcardQuery("countryNameEn.keyword", keyword + "*"));
        }

        return builder.build()._toQuery();
    }

    private Query buildNgramFilter(String keyword, boolean includeCountry) {
        BoolQuery.Builder builder = new BoolQuery.Builder()
            .should(buildNgramMatch("nameEn.ngram", keyword))
            .should(buildNgramMatch("nameKr.ngram", keyword));

        if (includeCountry) {
            builder
                .should(buildNgramMatch("countryNameEn.ngram", keyword))
                .should(buildNgramMatch("countryNameKr.ngram", keyword));
        }

        return builder.build()._toQuery();
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
                    .weight(1000.0)
                ),
                FunctionScore.of(f -> f
                    .filter(buildMultiKeywordJasoFilter(keywords))
                    .weight(500.0)
                ),
                FunctionScore.of(f -> f
                    .filter(buildMultiKeywordPrefixFilter(keywords))
                    .weight(300.0)
                ),
                FunctionScore.of(f -> f
                    .filter(buildMultiKeywordWildcardFilter(keywords))
                    .weight(200.0)
                )
            )
            .scoreMode(FunctionScoreMode.Sum)
            .boostMode(FunctionBoostMode.Replace)
        )._toQuery();
    }

    private Query buildSingleKeywordQuery(String keyword) {
        return BoolQuery.of(b -> b
            .should(buildMatch("nameEn", keyword, 5.0f))
            .should(buildMatch("nameKr", keyword, 5.0f))
            .should(buildMatch("countryNameEn", keyword, 10.0f))
            .should(buildMatch("countryNameKr", keyword, 10.0f))
            .should(buildMatch("nameEn.jaso", keyword, 50.0f))
            .should(buildMatch("nameKr.jaso", keyword, 50.0f))
            .should(buildMatch("countryNameEn.jaso", keyword, 70.0f))
            .should(buildMatch("countryNameKr.jaso", keyword, 70.0f))
            .should(buildConditionalFuzzy("nameEn", keyword, 20.0f))
            .should(buildConditionalFuzzy("nameKr", keyword, 20.0f))
        )._toQuery();
    }

    private Query buildMultiKeywordExactFilter(String[] keywords) {
        BoolQuery.Builder mustBuilder = new BoolQuery.Builder();

        for (String keyword : keywords) {
            mustBuilder.must(buildMultiFieldOr(keyword, "keyword"));
        }

        return mustBuilder.build()._toQuery();
    }

    private Query buildMultiKeywordJasoFilter(String[] keywords) {
        BoolQuery.Builder mustBuilder = new BoolQuery.Builder();

        for (String keyword : keywords) {
            BoolQuery.Builder shouldBuilder = new BoolQuery.Builder()
                .should(buildMatch("nameEn.jaso", keyword, 1.0f))
                .should(buildMatch("nameKr.jaso", keyword, 1.0f))
                .should(buildMatch("countryNameEn.jaso", keyword, 1.0f))
                .should(buildMatch("countryNameKr.jaso", keyword, 1.0f));

            mustBuilder.must(shouldBuilder.build()._toQuery());
        }

        return mustBuilder.build()._toQuery();
    }

    private Query buildMultiKeywordPrefixFilter(String[] keywords) {
        BoolQuery.Builder mustBuilder = new BoolQuery.Builder();

        for (String keyword : keywords) {
            BoolQuery.Builder shouldBuilder = new BoolQuery.Builder()
                .should(buildPrefix("nameEn", keyword))
                .should(buildPrefix("nameKr", keyword))
                .should(buildPrefix("nameEn.keyword", keyword))
                .should(buildPrefix("nameKr.keyword", keyword))
                .should(buildPrefix("countryNameEn", keyword))
                .should(buildPrefix("countryNameKr", keyword))
                .should(buildPrefix("countryNameEn.keyword", keyword))
                .should(buildPrefix("countryNameKr.keyword", keyword));

            mustBuilder.must(shouldBuilder.build()._toQuery());
        }

        return mustBuilder.build()._toQuery();
    }

    private Query buildMultiKeywordWildcardFilter(String[] keywords) {
        BoolQuery.Builder mustBuilder = new BoolQuery.Builder();

        for (String keyword : keywords) {
            BoolQuery.Builder shouldBuilder = new BoolQuery.Builder()
                .should(buildWildcardQuery("nameKr.keyword", keyword + "*"))
                .should(buildWildcardQuery("nameEn.keyword", keyword + "*"))
                .should(buildWildcardQuery("countryNameKr.keyword", keyword + "*"))
                .should(buildWildcardQuery("countryNameEn.keyword", keyword + "*"));

            mustBuilder.must(shouldBuilder.build()._toQuery());
        }

        return mustBuilder.build()._toQuery();
    }

    private Query buildMultiFieldOr(String keyword, String suffix) {
        return BoolQuery.of(b -> b
            .should(buildTerm("nameEn." + suffix, keyword))
            .should(buildTerm("nameKr." + suffix, keyword))
            .should(buildTerm("countryNameEn." + suffix, keyword))
            .should(buildTerm("countryNameKr." + suffix, keyword))
        )._toQuery();
    }

    private Query buildMatch(String field, String keyword, float boost) {
        return MatchQuery.of(m -> m
            .field(field)
            .query(keyword)
            .boost(boost)
        )._toQuery();
    }

    private Query buildTerm(String field, String keyword) {
        return TermQuery.of(t -> t
            .field(field)
            .value(keyword)
        )._toQuery();
    }

    private Query buildPrefix(String field, String keyword) {
        String value = field.contains("En") ? keyword.toLowerCase() : keyword;
        return PrefixQuery.of(p -> p
            .field(field)
            .value(value)
        )._toQuery();
    }

    private Query buildWildcard(String field, String keyword, float boost) {
        String pattern = "*" + (field.contains("En") ? keyword.toLowerCase() : keyword) + "*";
        return WildcardQuery.of(w -> w
            .field(field)
            .value(pattern)
            .boost(boost)
            .caseInsensitive(true)
        )._toQuery();
    }

    private Query buildWildcardQuery(String field, String pattern) {
        return WildcardQuery.of(w -> w
            .field(field)
            .value(pattern)
            .caseInsensitive(true)
        )._toQuery();
    }

    private Query buildNgramMatch(String field, String keyword) {
        return MatchQuery.of(m -> m
            .field(field)
            .query(keyword)
            .minimumShouldMatch("70%")
        )._toQuery();
    }

    private Query buildFuzzyQuery(String field, String keyword, float boost) {
        return FuzzyQuery.of(fq -> fq
            .field(field)
            .value(keyword)
            .fuzziness("AUTO")
            .boost(boost)
        )._toQuery();
    }

    private Query buildConditionalFuzzy(String field, String keyword, float boost) {
        if (keyword.length() <= 1) {
            return buildMatch(field, keyword, 0.1f);
        }

        String fuzziness = keyword.length() <= 2 ? "1" : "AUTO";

        return MatchQuery.of(m -> m
            .field(field)
            .query(keyword)
            .fuzziness(fuzziness)
            .prefixLength(0)
            .maxExpansions(50)
            .boost(boost)
            .fuzzyTranspositions(true)
        )._toQuery();
    }
}
