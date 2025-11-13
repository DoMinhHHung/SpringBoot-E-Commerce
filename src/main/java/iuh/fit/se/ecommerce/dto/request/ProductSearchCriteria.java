package iuh.fit.se.ecommerce.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductSearchCriteria {
    private String brand;
    private String productType;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private List<String> specTerms;
    private String text;

}

