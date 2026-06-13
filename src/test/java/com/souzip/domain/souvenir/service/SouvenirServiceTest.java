package com.souzip.domain.souvenir.service;

import com.souzip.adapter.storage.file.NcpStorage;
import com.souzip.application.file.FileModifyService;
import com.souzip.application.file.FileQueryService;
import com.souzip.application.file.required.FileStorage;
import com.souzip.auth.adapter.security.jwt.JwtTokenProvider;
import com.souzip.domain.category.entity.Category;
import com.souzip.domain.exchangerate.service.ExchangeRateService;
import com.souzip.domain.souvenir.dto.*;
import com.souzip.domain.souvenir.entity.Purpose;
import com.souzip.domain.souvenir.entity.Souvenir;
import com.souzip.domain.souvenir.repository.SouvenirRepository;
import com.souzip.domain.souvenir.vo.PriceInfo;
import com.souzip.domain.user.entity.User;
import com.souzip.domain.user.repository.UserRepository;
import com.souzip.domain.wishlist.repository.WishlistRepository;
import com.souzip.shared.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SouvenirServiceTest {

    @Mock
    private SouvenirRepository souvenirRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private NcpStorage ncpStorage;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private FileQueryService fileQueryService;

    @Mock
    private FileStorage fileStorage;

    @Mock
    private FileModifyService fileModifyService;

    @Mock
    private ExchangeRateService exchangeRateService;

    @InjectMocks
    private SouvenirService souvenirService;

    private static final Long SOUVENIR_ID = 1L;
    private static final Long USER_PK = 1L;
    private static final String USER_UUID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String BEARER_TOKEN = "Bearer valid.jwt.token";
    private static final String JWT_TOKEN = "valid.jwt.token";

    private Object[] buildRow(long id) {
        return new Object[]{
                id,
                "기념품",
                "SOUVENIR_BASIC",
                "GIFT",
                10000,
                120000,
                "$",
                null,
                new BigDecimal("37.123456"),
                new BigDecimal("127.123456"),
                "테스트 주소",
                5L
        };
    }

    @DisplayName("비로그인 시 isWishlisted는 null이다")
    @Test
    void getNearbySouvenirs_isWishlisted_null_when_not_logged_in() {
        // given
        List<Object[]> rows = Collections.singletonList(buildRow(1L));
        given(souvenirRepository.findNearbySouvenirs(anyDouble(), anyDouble(), anyDouble()))
                .willReturn(rows);

        // when
        SouvenirNearbyListResponse result = souvenirService.getNearbySouvenirs(37.0, 127.0, 5000, null);

        // then
        assertThat(result.souvenirs().get(0).isWishlisted()).isNull();
    }

    @DisplayName("로그인 유저가 찜 목록이 비어있으면 isWishlisted는 false이다")
    @Test
    void getNearbySouvenirs_isWishlisted_false_when_logged_in_with_empty_wishlist() {
        // given
        User user = mock(User.class);
        given(user.getUserId()).willReturn(USER_UUID);
        given(jwtTokenProvider.getUserIdFromToken(JWT_TOKEN)).willReturn(USER_PK);
        given(userRepository.findById(USER_PK)).willReturn(Optional.of(user));
        given(wishlistRepository.findSouvenirIdsByUserUserId(USER_UUID)).willReturn(Collections.emptySet());
        List<Object[]> rows = Collections.singletonList(buildRow(1L));
        given(souvenirRepository.findNearbySouvenirs(anyDouble(), anyDouble(), anyDouble()))
                .willReturn(rows);

        // when
        SouvenirNearbyListResponse result = souvenirService.getNearbySouvenirs(37.0, 127.0, 5000, BEARER_TOKEN);

        // then
        assertThat(result.souvenirs().get(0).isWishlisted()).isFalse();
    }

    @DisplayName("로그인 유저가 해당 기념품을 찜했으면 isWishlisted는 true이다")
    @Test
    void getNearbySouvenirs_isWishlisted_true_when_souvenir_wishlisted() {
        // given
        User user = mock(User.class);
        given(user.getUserId()).willReturn(USER_UUID);
        given(jwtTokenProvider.getUserIdFromToken(JWT_TOKEN)).willReturn(USER_PK);
        given(userRepository.findById(USER_PK)).willReturn(Optional.of(user));
        given(wishlistRepository.findSouvenirIdsByUserUserId(USER_UUID)).willReturn(Set.of(1L));
        List<Object[]> rows = Collections.singletonList(buildRow(1L));
        given(souvenirRepository.findNearbySouvenirs(anyDouble(), anyDouble(), anyDouble()))
                .willReturn(rows);

        // when
        SouvenirNearbyListResponse result = souvenirService.getNearbySouvenirs(37.0, 127.0, 5000, BEARER_TOKEN);

        // then
        assertThat(result.souvenirs().get(0).isWishlisted()).isTrue();
    }

    @DisplayName("비로그인으로 기념품 상세를 조회하면 isOwned와 isWishlisted는 false이다")
    @Test
    void getSouvenir_not_logged_in_success() {
        // given
        Souvenir souvenir = mock(Souvenir.class);
        User owner = mock(User.class);
        given(souvenirRepository.findByIdWithUser(SOUVENIR_ID)).willReturn(Optional.of(souvenir));
        given(fileQueryService.findByEntity(any(), eq(SOUVENIR_ID))).willReturn(Collections.emptyList());
        given(souvenir.getUser()).willReturn(owner);
        given(wishlistRepository.countBySouvenirId(SOUVENIR_ID)).willReturn(0L);
        given(exchangeRateService.createPriceResponse(any(), any())).willReturn(null);

        // when
        SouvenirDetailResponse result = souvenirService.getSouvenir(SOUVENIR_ID, null);

        // then
        assertThat(result.isOwned()).isFalse();
        assertThat(result.isWishlisted()).isFalse();
    }

    @DisplayName("로그인 유저가 자신의 기념품을 조회하면 isOwned는 true이다")
    @Test
    void getSouvenir_owner_can_see_isOwned_true() {
        // given
        Souvenir souvenir = mock(Souvenir.class);
        User owner = mock(User.class);
        given(jwtTokenProvider.getUserIdFromToken(JWT_TOKEN)).willReturn(USER_PK);
        given(userRepository.findById(USER_PK)).willReturn(Optional.of(owner));
        given(owner.getUserId()).willReturn(USER_UUID);
        given(souvenirRepository.findByIdWithUser(SOUVENIR_ID)).willReturn(Optional.of(souvenir));
        given(fileQueryService.findByEntity(any(), eq(SOUVENIR_ID))).willReturn(Collections.emptyList());
        given(souvenir.getUser()).willReturn(owner);
        given(souvenir.isOwnedBy(USER_UUID)).willReturn(true);
        given(wishlistRepository.existsByUserUserIdAndSouvenirId(USER_UUID, SOUVENIR_ID)).willReturn(false);
        given(wishlistRepository.countBySouvenirId(SOUVENIR_ID)).willReturn(0L);
        given(exchangeRateService.createPriceResponse(any(), any())).willReturn(null);

        // when
        SouvenirDetailResponse result = souvenirService.getSouvenir(SOUVENIR_ID, BEARER_TOKEN);

        // then
        assertThat(result.isOwned()).isTrue();
    }

    @DisplayName("존재하지 않는 기념품을 조회하면 예외가 발생한다")
    @Test
    void getSouvenir_not_found_throws_exception() {
        // given
        given(souvenirRepository.findByIdWithUser(SOUVENIR_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> souvenirService.getSouvenir(SOUVENIR_ID, null))
                .isInstanceOf(BusinessException.class);
    }

    @DisplayName("기념품 삭제에 성공한다")
    @Test
    void deleteSouvenir_success() {
        // given
        Souvenir souvenir = mock(Souvenir.class);
        User owner = mock(User.class);
        given(souvenirRepository.findByIdWithUser(SOUVENIR_ID)).willReturn(Optional.of(souvenir));
        given(souvenir.getUser()).willReturn(owner);
        given(owner.getId()).willReturn(USER_PK);

        // when
        souvenirService.deleteSouvenir(SOUVENIR_ID, USER_PK);

        // then
        verify(souvenir).delete();
        verify(fileModifyService).deleteByEntity(any(), eq(SOUVENIR_ID));
    }

    @DisplayName("인증 없이 기념품을 삭제하면 예외가 발생한다")
    @Test
    void deleteSouvenir_unauthorized_when_user_id_is_null() {
        // when & then
        assertThatThrownBy(() -> souvenirService.deleteSouvenir(SOUVENIR_ID, null))
                .isInstanceOf(BusinessException.class);

        verify(souvenirRepository, never()).findByIdWithUser(any());
    }

    @DisplayName("존재하지 않는 기념품을 삭제하면 예외가 발생한다")
    @Test
    void deleteSouvenir_not_found_throws_exception() {
        // given
        given(souvenirRepository.findByIdWithUser(SOUVENIR_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> souvenirService.deleteSouvenir(SOUVENIR_ID, USER_PK))
                .isInstanceOf(BusinessException.class);
    }

    @DisplayName("소유자가 아닌 유저가 기념품을 삭제하면 예외가 발생한다")
    @Test
    void deleteSouvenir_forbidden_when_not_owner() {
        // given
        Long otherUserId = 999L;
        Souvenir souvenir = mock(Souvenir.class);
        User owner = mock(User.class);
        given(souvenirRepository.findByIdWithUser(SOUVENIR_ID)).willReturn(Optional.of(souvenir));
        given(souvenir.getUser()).willReturn(owner);
        given(owner.getId()).willReturn(otherUserId);

        // when & then
        assertThatThrownBy(() -> souvenirService.deleteSouvenir(SOUVENIR_ID, USER_PK))
                .isInstanceOf(BusinessException.class);

        verify(souvenir, never()).delete();
    }

    @DisplayName("기념품 등록에 성공한다")
    @Test
    void createSouvenirV2_success() {
        // given
        User user = mock(User.class);
        SouvenirRequest request = new SouvenirRequest(
                "기념품", 10000, "JPY", "설명", "주소", null,
                new BigDecimal("35.0"), new BigDecimal("139.0"),
                Category.SOUVENIR_BASIC, Purpose.GIFT, "JP"
        );
        PriceData priceData = new PriceData(
                PriceInfo.of(10000, "JPY"), 90000, "¥", PriceInfo.of(90000, "KRW")
        );
        given(userRepository.findById(USER_PK)).willReturn(Optional.of(user));
        given(exchangeRateService.calculatePriceData(any(), any(), any())).willReturn(priceData);
        given(exchangeRateService.createPriceResponse(any(), any())).willReturn(null);

        // when
        SouvenirResponse result = souvenirService.createSouvenirV2(request, USER_PK, Collections.emptyList());

        // then
        assertThat(result.name()).isEqualTo("기념품");
        verify(souvenirRepository).save(any(Souvenir.class));
    }

    @DisplayName("인증 없이 기념품을 등록하면 예외가 발생한다")
    @Test
    void createSouvenirV2_unauthorized_when_user_id_is_null() {
        // given
        SouvenirRequest request = new SouvenirRequest(
                "기념품", 10000, "JPY", "설명", "주소", null,
                new BigDecimal("35.0"), new BigDecimal("139.0"),
                Category.SOUVENIR_BASIC, Purpose.GIFT, "JP"
        );

        // when & then
        assertThatThrownBy(() -> souvenirService.createSouvenirV2(request, null, Collections.emptyList()))
                .isInstanceOf(BusinessException.class);

        verify(souvenirRepository, never()).save(any());
    }

    @DisplayName("기념품 수정에 성공한다")
    @Test
    void updateSouvenirV2_success() {
        // given
        Souvenir souvenir = mock(Souvenir.class);
        User owner = mock(User.class);
        SouvenirRequest request = new SouvenirRequest(
                "수정 기념품", 20000, "USD", "수정 설명", "수정 주소", null,
                new BigDecimal("35.0"), new BigDecimal("139.0"),
                Category.FOOD_SNACK, Purpose.PERSONAL, "US"
        );
        PriceData priceData = new PriceData(
                PriceInfo.of(20000, "USD"), 28000000, "$", PriceInfo.of(28000000, "KRW")
        );
        given(souvenirRepository.findByIdWithUser(SOUVENIR_ID)).willReturn(Optional.of(souvenir));
        given(souvenir.getUser()).willReturn(owner);
        given(owner.getId()).willReturn(USER_PK);
        given(exchangeRateService.calculatePriceData(any(), any(), any())).willReturn(priceData);
        given(exchangeRateService.createPriceResponse(any(), any())).willReturn(null);

        // when
        souvenirService.updateSouvenirV2(SOUVENIR_ID, request, USER_PK);

        // then
        verify(souvenir).updateV2(eq(request), any(), any(), any(), any());
    }

    @DisplayName("소유자가 아닌 유저가 기념품을 수정하면 예외가 발생한다")
    @Test
    void updateSouvenirV2_forbidden_when_not_owner() {
        // given
        Long otherUserId = 999L;
        Souvenir souvenir = mock(Souvenir.class);
        User owner = mock(User.class);
        SouvenirRequest request = new SouvenirRequest(
                "수정 기념품", 20000, "USD", "수정 설명", "수정 주소", null,
                new BigDecimal("35.0"), new BigDecimal("139.0"),
                Category.FOOD_SNACK, Purpose.PERSONAL, "US"
        );
        given(souvenirRepository.findByIdWithUser(SOUVENIR_ID)).willReturn(Optional.of(souvenir));
        given(souvenir.getUser()).willReturn(owner);
        given(owner.getId()).willReturn(otherUserId);

        // when & then
        assertThatThrownBy(() -> souvenirService.updateSouvenirV2(SOUVENIR_ID, request, USER_PK))
                .isInstanceOf(BusinessException.class);

        verify(souvenir, never()).updateV2(any(), any(), any(), any(), any());
    }
}
