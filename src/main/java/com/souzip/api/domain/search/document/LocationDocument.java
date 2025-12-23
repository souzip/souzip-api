package com.souzip.api.domain.search.document;

import com.souzip.api.domain.city.entity.City;
import com.souzip.api.domain.country.entity.Country;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;
import org.springframework.data.elasticsearch.annotations.Setting;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Document(indexName = "locations")
@Setting(settingPath = "elasticsearch/location-settings.json")
public class LocationDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String type;

    @Field(type = FieldType.Long)
    private Long entityId;

    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "korean_analyzer"),
        otherFields = {
            @InnerField(suffix = "keyword", type = FieldType.Keyword),
            @InnerField(suffix = "jaso", type = FieldType.Text, analyzer = "jaso_index_analyzer", searchAnalyzer = "jaso_search_analyzer"),
            @InnerField(suffix = "chosung", type = FieldType.Text, analyzer = "chosung_index_analyzer", searchAnalyzer = "chosung_search_analyzer"),
            @InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "ngram_analyzer"),
            @InnerField(suffix = "autocomplete", type = FieldType.Text, analyzer = "autocomplete_analyzer", searchAnalyzer = "autocomplete_search_analyzer")
        }
    )
    private String nameEn;

    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "korean_analyzer"),
        otherFields = {
            @InnerField(suffix = "keyword", type = FieldType.Keyword),
            @InnerField(suffix = "jaso", type = FieldType.Text, analyzer = "jaso_index_analyzer", searchAnalyzer = "jaso_search_analyzer"),
            @InnerField(suffix = "chosung", type = FieldType.Text, analyzer = "chosung_index_analyzer", searchAnalyzer = "chosung_search_analyzer"),
            @InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "ngram_analyzer"),
            @InnerField(suffix = "autocomplete", type = FieldType.Text, analyzer = "autocomplete_analyzer", searchAnalyzer = "autocomplete_search_analyzer")
        }
    )
    private String nameKr;

    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "korean_analyzer"),
        otherFields = {
            @InnerField(suffix = "keyword", type = FieldType.Keyword),
            @InnerField(suffix = "jaso", type = FieldType.Text, analyzer = "jaso_index_analyzer", searchAnalyzer = "jaso_search_analyzer"),
            @InnerField(suffix = "chosung", type = FieldType.Text, analyzer = "chosung_index_analyzer", searchAnalyzer = "chosung_search_analyzer"),
            @InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "ngram_analyzer"),
            @InnerField(suffix = "autocomplete", type = FieldType.Text, analyzer = "autocomplete_analyzer", searchAnalyzer = "autocomplete_search_analyzer")
        }
    )
    private String countryNameEn;

    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "korean_analyzer"),
        otherFields = {
            @InnerField(suffix = "keyword", type = FieldType.Keyword),
            @InnerField(suffix = "jaso", type = FieldType.Text, analyzer = "jaso_index_analyzer", searchAnalyzer = "jaso_search_analyzer"),
            @InnerField(suffix = "chosung", type = FieldType.Text, analyzer = "chosung_index_analyzer", searchAnalyzer = "chosung_search_analyzer"),
            @InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "ngram_analyzer"),
            @InnerField(suffix = "autocomplete", type = FieldType.Text, analyzer = "autocomplete_analyzer", searchAnalyzer = "autocomplete_search_analyzer")
        }
    )
    private String countryNameKr;

    public static LocationDocument from(Country country) {
        return LocationDocument.builder()
            .id("country_" + country.getId())
            .entityId(country.getId())
            .type("country")
            .nameEn(country.getNameEn())
            .nameKr(country.getNameKr())
            .countryNameEn(null)
            .countryNameKr(null)
            .build();
    }

    public static LocationDocument from(City city) {
        return LocationDocument.builder()
            .id("city_" + city.getId())
            .entityId(city.getId())
            .type("city")
            .nameEn(city.getNameEn())
            .nameKr(city.getNameKr())
            .countryNameEn(city.getCountry().getNameEn())
            .countryNameKr(city.getCountry().getNameKr())
            .build();
    }
}
