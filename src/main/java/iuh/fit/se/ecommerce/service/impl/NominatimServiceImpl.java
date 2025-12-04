package iuh.fit.se.ecommerce.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.fit.se.ecommerce.dto.response.AddressGeocodeResponse;
import iuh.fit.se.ecommerce.exception.AppException;
import iuh.fit.se.ecommerce.exception.ErrorCode;
import iuh.fit.se.ecommerce.service.interfaces.NominatimService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;

@Slf4j
@Service
@RequiredArgsConstructor
public class NominatimServiceImpl implements NominatimService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${nominatim.api-url:https://nominatim.openstreetmap.org}")
    private String nominatimUrl;

    @Value("${nominatim.email:baon6777@gmail.com}")
    private String email;

    @Value("${nominatim.user-agent:SpringBoot-ECommerce/1.0}")
    private String userAgent;

    // Rate limiting: max 1 request/second
    private long lastRequestTime = 0;
    private static final long MIN_REQUEST_INTERVAL = 1000; // 1 second

    @Override
    @Cacheable(value = "nominatim", keyGenerator = "nominatimKeyGenerator")
    public AddressGeocodeResponse reverseGeocode(BigDecimal lat, BigDecimal lng) {
        // Rate limiting
        long now = System.currentTimeMillis();
        long timeSinceLastRequest = now - lastRequestTime;
        if (timeSinceLastRequest < MIN_REQUEST_INTERVAL) {
            try {
                Thread.sleep(MIN_REQUEST_INTERVAL - timeSinceLastRequest);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        lastRequestTime = System.currentTimeMillis();

        // Round coordinates for cache key (4 decimals ≈ 11m accuracy)
        BigDecimal roundedLat = lat.setScale(4, RoundingMode.HALF_UP);
        BigDecimal roundedLng = lng.setScale(4, RoundingMode.HALF_UP);

        try {
            // Build URL
            String url = String.format(
                "%s/reverse?format=jsonv2&lat=%s&lon=%s&addressdetails=1&accept-language=vi&email=%s",
                nominatimUrl,
                roundedLat,
                roundedLng,
                email
            );

            // Headers (BẮT BUỘC theo policy)
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", userAgent);
            headers.set("Referer", "http://localhost:8080");

            RequestEntity<Void> request = new RequestEntity<>(
                headers,
                HttpMethod.GET,
                URI.create(url)
            );

            log.info("Calling Nominatim API: lat={}, lng={}", roundedLat, roundedLng);

            // Call API
            ResponseEntity<String> response = restTemplate.exchange(
                request,
                String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new AppException(ErrorCode.INTERNAL_ERROR,
                    "Nominatim API error: " + response.getStatusCode());
            }

            // Parse response
            JsonNode json = objectMapper.readTree(response.getBody());
            JsonNode addressNode = json.get("address");

            if (addressNode == null) {
                throw new AppException(ErrorCode.BAD_REQUEST, "Không tìm thấy địa chỉ cho tọa độ này");
            }

            // Map to response
            return mapToResponse(json, addressNode);

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error calling Nominatim API: ", e);
            throw new AppException(ErrorCode.INTERNAL_ERROR,
                "Lỗi khi gọi Nominatim API: " + e.getMessage());
        }
    }

    private AddressGeocodeResponse mapToResponse(JsonNode json, JsonNode addressNode) {
        // Extract address components với fallback
        String houseNumber = getText(addressNode, "house_number");
        String road = getText(addressNode, "road");
        String ward = getText(addressNode, "suburb", "neighbourhood", "village");
        String province = getText(addressNode, "city", "town", "state", "region");
        String country = getText(addressNode, "country", "Việt Nam");
        String countryCode = getText(addressNode, "country_code", "vn");
        String postcode = getText(addressNode, "postcode");
        String displayName = getText(json, "display_name");

        // Build full address
        StringBuilder fullAddress = new StringBuilder();
        if (houseNumber != null && !houseNumber.isEmpty()) {
            fullAddress.append(houseNumber).append(" ");
        }
        if (road != null && !road.isEmpty()) {
            fullAddress.append(road);
        }
        if (ward != null && !ward.isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(ward);
        }
        if (province != null && !province.isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(province);
        }
        if (country != null && !country.isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(country);
        }

        return AddressGeocodeResponse.builder()
                .houseNumber(houseNumber)
                .road(road)
                .ward(ward)
                .district(null) // Không còn dùng district
                .province(province)
                .country(country)
                .countryCode(countryCode)
                .postcode(postcode)
                .displayName(displayName)
                .fullAddress(fullAddress.toString().trim())
                .build();
    }

    private String getText(JsonNode node, String... keys) {
        for (String key : keys) {
            JsonNode value = node.get(key);
            if (value != null && !value.isNull() && !value.asText().isEmpty()) {
                return value.asText();
            }
        }
        // Last key là fallback value
        if (keys.length > 0 && (keys[keys.length - 1].equals("Việt Nam") || keys[keys.length - 1].equals("vn"))) {
            return keys[keys.length - 1];
        }
        return null;
    }
}

