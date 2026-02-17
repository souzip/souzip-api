public class City extends BaseEntity {

    @Column(nullable = false)
    private String nameEn;

    @Column(nullable = false)
    private String nameKr;

    @Column(nullable = false)
    private BigDecimal latitude;

    @Column(nullable = false)
    private BigDecimal longitude;

    @Column(nullable = true)
    private Integer priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Country country;

    public static City of(
        String nameEn,
        String nameKr,
        BigDecimal latitude,
        BigDecimal longitude,
        Country country
    ) {
        return City.builder()
            .nameEn(nameEn)
            .nameKr(nameKr)
            .latitude(latitude)
            .longitude(longitude)
            .country(country)
            .priority(null)
            .build();
    }
}
