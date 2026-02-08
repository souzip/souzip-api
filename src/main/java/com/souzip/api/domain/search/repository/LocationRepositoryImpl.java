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

    // 🔧 수정 2,3: 최소 점수 임계값을 높여서 관련 없는 결과 필터링
    private static final float MIN_SCORE_THRESHOLD = 300.0f; // 10.0 -> 300.0으로 대폭 상향

    @Override
    public List<SearchHit<LocationDocument>> searchByKeywordWithFuzzy(String keyword) {
        Query query = buildFunctionScoreQuery(keyword, false);
        return executeSearch(query, MIN_SCORE_THRESHOLD);
    }

    @Override
    public List<SearchHit<LocationDocument>> searchWithMultipleKeywords(String[] keywords) {
        Query query = buildMultiKeywordQuery(keywords);
        return executeSearch(query, MIN_SCORE_THRESHOLD);
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
                    .weight(1000.0)
                ),
                // 2. Prefix 매칭
                FunctionScore.of(f -> f
                    .filter(buildPrefixFilter(keyword, includeCountry))
                    .weight(500.0)
                ),
                // 🔧 수정 3: 자소 기반 Fuzzy 가중치를 조정하여 과도한 매칭 방지
                FunctionScore.of(f -> f
                    .filter(buildJasoBasedFuzzyFilter(keyword, includeCountry))
                    .weight(250.0)  // 300 -> 250으로 감소
                ),
                // 4. 일반 Fuzzy (영문)
                FunctionScore.of(f -> f
                    .filter(buildFuzzyFilter(keyword, includeCountry))
                    .weight(200.0)
                ),
                // 🔧 수정 3: 자소 부분 매칭 가중치 대폭 감소
                FunctionScore.of(f -> f
                    .filter(buildJasoFilter(keyword, includeCountry))
                    .weight(50.0)  // 100 -> 50으로 감소
                ),
                // 🔧 수정 2: Ngram 가중치 감소
                FunctionScore.of(f -> f
                    .filter(buildNgramFilter(keyword, includeCountry))
                    .weight(30.0)  // 50 -> 30으로 감소
                ),
                // 7. Autocomplete
                FunctionScore.of(f -> f
                    .filter(buildAutocompleteFilter(keyword, includeCountry))
                    .weight(30.0)
                ),
                // 8. Wildcard - 가장 낮은 우선순위
                FunctionScore.of(f -> f
                    .filter(buildWildcardFilter(keyword, includeCountry))
                    .weight(10.0)
                )
            )
            .scoreMode(FunctionScoreMode.Sum)
            .boostMode(FunctionBoostMode.Replace)
        )._toQuery();
    }

    private Query buildBaseQuery(String keyword, boolean includeCountry) {
        BoolQuery.Builder builder = new BoolQuery.Builder()
            .should(buildMatch("nameEn", keyword, 5.0f))
            .should(buildMatch("nameKr", keyword, 5.0f))
            .should(buildConditionalFuzzy("nameEn", keyword, 3.0f))
            .should(buildConditionalFuzzy("nameKr", keyword, 3.0f))
            // 🔧 수정 3: jaso 매칭 부스트 감소
            .should(buildMatch("nameEn.jaso", keyword, 1.0f))  // 2.0 -> 1.0
            .should(buildMatch("nameKr.jaso", keyword, 1.0f))  // 2.0 -> 1.0
            .should(buildMatch("nameEn.autocomplete", keyword, 1.0f))
            .should(buildMatch("nameKr.autocomplete", keyword, 1.0f))
            .should(buildMatch("nameEn.ngram", keyword, 1.0f))  // 1.5 -> 1.0
            .should(buildMatch("nameKr.ngram", keyword, 1.0f)); // 1.5 -> 1.0

        if (includeCountry) {
            builder
                .should(buildMatch("countryNameEn", keyword, 2.0f))
                .should(buildMatch("countryNameKr", keyword, 2.0f))
                .should(buildMatch("countryNameEn.jaso", keyword, 0.5f))  // 1.0 -> 0.5
                .should(buildMatch("countryNameKr.jaso", keyword, 0.5f))  // 1.0 -> 0.5
                .should(buildMatch("countryNameEn.ngram", keyword, 0.3f))  // 0.5 -> 0.3
                .should(buildMatch("countryNameKr.ngram", keyword, 0.3f))  // 0.5 -> 0.3
                .should(buildConditionalFuzzy("countryNameEn", keyword, 1.0f))
                .should(buildConditionalFuzzy("countryNameKr", keyword, 1.0f));
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

    private Query buildJasoBasedFuzzyFilter(String keyword, boolean includeCountry) {
        // 🔧 수정 3: jaso fuzzy를 더 엄격하게 제한
        if (keyword.length() <= 2) {  // 1 -> 2로 변경
            return buildTerm("_id", "never_match");
        }

        // 🔧 수정 3: fuzziness를 더 보수적으로 설정
        String fuzziness = keyword.length() <= 4 ? "1" : "2";  // 3 -> 4로 변경

        BoolQuery.Builder builder = new BoolQuery.Builder()
            .should(MatchQuery.of(m -> m
                .field("nameKr.jaso")
                .query(keyword)
                .fuzziness(fuzziness)
                .prefixLength(1)  // 🔧 0 -> 1로 변경: 첫 글자는 정확히 일치해야 함
                .maxExpansions(30)  // 🔧 50 -> 30으로 감소
                .boost(2.0f)
            )._toQuery())
            .should(MatchQuery.of(m -> m
                .field("nameEn.jaso")
                .query(keyword)
                .fuzziness(fuzziness)
                .prefixLength(1)  // 🔧 0 -> 1로 변경
                .maxExpansions(30)  // 🔧 50 -> 30으로 감소
                .boost(1.5f)
            )._toQuery());

        if (includeCountry) {
            builder
                .should(MatchQuery.of(m -> m
                    .field("countryNameKr.jaso")
                    .query(keyword)
                    .fuzziness(fuzziness)
                    .prefixLength(1)  // 🔧 0 -> 1로 변경
                    .maxExpansions(30)  // 🔧 50 -> 30으로 감소
                    .boost(1.0f)
                )._toQuery())
                .should(MatchQuery.of(m -> m
                    .field("countryNameEn.jaso")
                    .query(keyword)
                    .fuzziness(fuzziness)
                    .prefixLength(1)  // 🔧 0 -> 1로 변경
                    .maxExpansions(30)  // 🔧 50 -> 30으로 감소
                    .boost(0.5f)
                )._toQuery());
        }

        return builder.build()._toQuery();
    }

    private Query buildJasoFilter(String keyword, boolean includeCountry) {
        BoolQuery.Builder builder = new BoolQuery.Builder()
            .should(buildMatch("nameEn.jaso", keyword, 1.0f))
            .should(buildMatch("nameKr.jaso", keyword, 1.0f));

        if (includeCountry) {
            builder
                .should(buildMatch("countryNameEn.jaso", keyword, 0.5f))
                .should(buildMatch("countryNameKr.jaso", keyword, 0.5f));
        }

        return builder.build()._toQuery();
    }

    private Query buildFuzzyFilter(String keyword, boolean includeCountry) {
        if (keyword.length() <= 1) {
            return buildTerm("_id", "never_match");
        }

        BoolQuery.Builder builder = new BoolQuery.Builder()
            .should(buildFuzzyQuery("nameEn", keyword, 2.0f))
            .should(buildFuzzyQuery("nameKr", keyword, 2.0f));

        if (includeCountry) {
            builder
                .should(buildFuzzyQuery("countryNameEn", keyword, 1.0f))
                .should(buildFuzzyQuery("countryNameKr", keyword, 1.0f));
        }

        return builder.build()._toQuery();
    }

    private Query buildAutocompleteFilter(String keyword, boolean includeCountry) {
        BoolQuery.Builder builder = new BoolQuery.Builder()
            .should(buildMatch("nameEn.autocomplete", keyword, 1.0f))
            .should(buildMatch("nameKr.autocomplete", keyword, 1.0f));

        if (includeCountry) {
            builder
                .should(buildMatch("countryNameEn.autocomplete", keyword, 0.5f))
                .should(buildMatch("countryNameKr.autocomplete", keyword, 0.5f));
        }

        return builder.build()._toQuery();
    }

    private Query buildWildcardFilter(String keyword, boolean includeCountry) {
        // Wildcard는 가장 낮은 우선순위, 정확한 매칭이 없을 때만 사용
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
                    .weight(100.0)
                ),
                FunctionScore.of(f -> f
                    .filter(buildMultiKeywordPrefixFilter(keywords))
                    .weight(50.0)
                ),
                FunctionScore.of(f -> f
                    .filter(buildMultiKeywordJasoFilter(keywords))
                    .weight(30.0)
                ),
                FunctionScore.of(f -> f
                    .filter(buildMultiKeywordWildcardFilter(keywords))
                    .weight(10.0)
                )
            )
            .scoreMode(FunctionScoreMode.Sum)
            .boostMode(FunctionBoostMode.Replace)
        )._toQuery();
    }

    private Query buildSingleKeywordQuery(String keyword) {
        return BoolQuery.of(b -> b
            .should(buildMatch("nameEn", keyword, 1.0f))
            .should(buildMatch("nameKr", keyword, 1.0f))
            .should(buildMatch("countryNameEn", keyword, 2.0f))
            .should(buildMatch("countryNameKr", keyword, 2.0f))
            .should(buildMatch("nameEn.jaso", keyword, 1.0f))  // 1.5 -> 1.0
            .should(buildMatch("nameKr.jaso", keyword, 1.0f))  // 1.5 -> 1.0
            .should(buildMatch("countryNameEn.jaso", keyword, 2.0f))  // 2.5 -> 2.0
            .should(buildMatch("countryNameKr.jaso", keyword, 2.0f))  // 2.5 -> 2.0
            .should(buildConditionalFuzzy("nameEn", keyword, 0.5f))
            .should(buildConditionalFuzzy("nameKr", keyword, 0.5f))
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
            // 🔧 수정 2: minimum_should_match를 90%로 더 엄격하게 설정
            .minimumShouldMatch("90%")  // 80% -> 90%로 증가
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
