package com.example.shop.specification;

import com.example.shop.model.Product;
import com.example.shop.model.Specification;
import com.example.shop.model.ProductType;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

public class ProductSpecs {

    public static Specification<Product> hasProductType(String type) {
        return (root, query, cb) -> {
            if (type == null || type.isEmpty()) return null;
            return cb.equal(root.get("productType"), ProductType.valueOf(type));
        };
    }

    public static Specification<Product> hasSpec(String specName, String specValue) {
        return (root, query, cb) -> {

            if (specName == null || specValue == null || specValue.isEmpty())
                return null;

            Join<Product, Specification> specJoin = root.join("specifications", JoinType.INNER);

            return cb.and(
                    cb.equal(specJoin.get("specName"), specName),
                    cb.equal(specJoin.get("specValue"), specValue)
            );
        };
    }
}
