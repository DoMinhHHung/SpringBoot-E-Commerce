package iuh.fit.se.ecommerce.config;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component("nominatimKeyGenerator")
public class NominatimCacheKeyGenerator implements KeyGenerator {
    @Override
    public Object generate(Object target, Method method, Object... params) {
        BigDecimal lat = (BigDecimal) params[0];
        BigDecimal lng = (BigDecimal) params[1];
        
        // Round to 4 decimals (≈11m accuracy) for cache key
        BigDecimal roundedLat = lat.setScale(4, RoundingMode.HALF_UP);
        BigDecimal roundedLng = lng.setScale(4, RoundingMode.HALF_UP);
        
        return roundedLat.toString() + ":" + roundedLng.toString();
    }
}

