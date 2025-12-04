package iuh.fit.se.ecommerce.repository;

import iuh.fit.se.ecommerce.dto.request.ProductSearchCriteria;
import iuh.fit.se.ecommerce.entity.Product;
import iuh.fit.se.ecommerce.entity.Specification;
import iuh.fit.se.ecommerce.entity.enums.ProductType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional(readOnly = true)
    public Page<Product> search(ProductSearchCriteria criteria, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Product> cq = cb.createQuery(Product.class);
        Root<Product> root = cq.from(Product.class);
        Join<Product, Specification> specJoin = root.join("specifications", JoinType.LEFT);
        cq.select(root).distinct(true);

        List<Predicate> predicates = new ArrayList<>();

        if (criteria.getBrand() != null && !criteria.getBrand().isBlank()) {
            predicates.add(cb.like(cb.lower(root.get("brand")), "%" + criteria.getBrand().toLowerCase() + "%"));
        }

        if (criteria.getProductType() != null && !criteria.getProductType().isBlank()) {
            try {
                ProductType pt = ProductType.valueOf(criteria.getProductType().toUpperCase());
                predicates.add(cb.equal(root.get("productType"), pt));
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (criteria.getMinPrice() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("price"), criteria.getMinPrice()));
        }
        if (criteria.getMaxPrice() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("price"), criteria.getMaxPrice()));
        }

        if (criteria.getSpecTerms() != null && !criteria.getSpecTerms().isEmpty()) {
            List<Predicate> specPreds = new ArrayList<>();
            for (String t : criteria.getSpecTerms()) {
                String like = "%" + t.toLowerCase() + "%";
                specPreds.add(cb.or(
                        cb.like(cb.lower(specJoin.get("specName")), like),
                        cb.like(cb.lower(specJoin.get("specValue")), like)
                ));
            }
            predicates.add(cb.or(specPreds.toArray(new Predicate[0])));
        }

        if (criteria.getText() != null && !criteria.getText().isBlank()) {
            String like = "%" + criteria.getText().toLowerCase() + "%";
            Join<Product, Specification> specJoin2 = root.join("specifications", JoinType.LEFT);
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("description")), like),
                    cb.like(cb.lower(root.get("brand")), like),
                    cb.like(cb.lower(specJoin2.get("specName")), like),
                    cb.like(cb.lower(specJoin2.get("specValue")), like)
            ));
        }

        if (!predicates.isEmpty()) {
            cq.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        cq.orderBy(cb.desc(root.get("id")));

        TypedQuery<Product> query = em.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<Product> content = query.getResultList();

        CriteriaQuery<Long> countCq = cb.createQuery(Long.class);
        Root<Product> countRoot = countCq.from(Product.class);
        countRoot.join("specifications", JoinType.LEFT);
        countCq.select(cb.countDistinct(countRoot));

        if (!predicates.isEmpty()) {
            List<Predicate> countPreds = new ArrayList<>();
            if (criteria.getBrand() != null && !criteria.getBrand().isBlank()) {
                countPreds.add(cb.like(cb.lower(countRoot.get("brand")), "%" + criteria.getBrand().toLowerCase() + "%"));
            }
            if (criteria.getProductType() != null && !criteria.getProductType().isBlank()) {
                try {
                    ProductType pt = ProductType.valueOf(criteria.getProductType().toUpperCase());
                    countPreds.add(cb.equal(countRoot.get("productType"), pt));
                } catch (IllegalArgumentException ignored) {
                }
            }
            if (criteria.getMinPrice() != null) {
                countPreds.add(cb.greaterThanOrEqualTo(countRoot.get("price"), criteria.getMinPrice()));
            }
            if (criteria.getMaxPrice() != null) {
                countPreds.add(cb.lessThanOrEqualTo(countRoot.get("price"), criteria.getMaxPrice()));
            }

            if (criteria.getSpecTerms() != null && !criteria.getSpecTerms().isEmpty()) {
                Join<Product, Specification> specJoinCount = countRoot.join("specifications", JoinType.LEFT);
                List<Predicate> specPreds = new ArrayList<>();
                for (String t : criteria.getSpecTerms()) {
                    String like = "%" + t.toLowerCase() + "%";
                    specPreds.add(cb.or(
                            cb.like(cb.lower(specJoinCount.get("specName")), like),
                            cb.like(cb.lower(specJoinCount.get("specValue")), like)
                    ));
                }
                countPreds.add(cb.or(specPreds.toArray(new Predicate[0])));
            }

            if (criteria.getText() != null && !criteria.getText().isBlank()) {
                String like = "%" + criteria.getText().toLowerCase() + "%";
                Join<Product, Specification> specJoinCount2 = countRoot.join("specifications", JoinType.LEFT);
                countPreds.add(cb.or(
                        cb.like(cb.lower(countRoot.get("name")), like),
                        cb.like(cb.lower(countRoot.get("description")), like),
                        cb.like(cb.lower(countRoot.get("brand")), like),
                        cb.like(cb.lower(specJoinCount2.get("specName")), like),
                        cb.like(cb.lower(specJoinCount2.get("specValue")), like)
                ));
            }

            countCq.where(cb.and(countPreds.toArray(new Predicate[0])));
        }

        Long total = em.createQuery(countCq).getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> search(ProductSearchCriteria criteria, Pageable pageable, String sort) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Product> cq = cb.createQuery(Product.class);
        Root<Product> root = cq.from(Product.class);
        Join<Product, Specification> specJoin = root.join("specifications", JoinType.LEFT);
        cq.select(root).distinct(true);

        List<Predicate> predicates = buildPredicates(criteria, cb, root, specJoin);

        if (!predicates.isEmpty()) {
            cq.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        // Apply sorting
        applySorting(cq, cb, root, sort);

        TypedQuery<Product> query = em.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<Product> content = query.getResultList();

        // Count query
        CriteriaQuery<Long> countCq = cb.createQuery(Long.class);
        Root<Product> countRoot = countCq.from(Product.class);
        countRoot.join("specifications", JoinType.LEFT);
        countCq.select(cb.countDistinct(countRoot));

        if (!predicates.isEmpty()) {
            List<Predicate> countPreds = buildCountPredicates(criteria, cb, countRoot);
            countCq.where(cb.and(countPreds.toArray(new Predicate[0])));
        }

        Long total = em.createQuery(countCq).getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }

    private List<Predicate> buildPredicates(ProductSearchCriteria criteria, CriteriaBuilder cb, 
                                           Root<Product> root, Join<Product, Specification> specJoin) {
        List<Predicate> predicates = new ArrayList<>();

        if (criteria.getBrand() != null && !criteria.getBrand().isBlank()) {
            predicates.add(cb.like(cb.lower(root.get("brand")), "%" + criteria.getBrand().toLowerCase() + "%"));
        }

        if (criteria.getProductType() != null && !criteria.getProductType().isBlank()) {
            try {
                ProductType pt = ProductType.valueOf(criteria.getProductType().toUpperCase());
                predicates.add(cb.equal(root.get("productType"), pt));
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (criteria.getMinPrice() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("price"), criteria.getMinPrice()));
        }
        if (criteria.getMaxPrice() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("price"), criteria.getMaxPrice()));
        }

        if (criteria.getSpecTerms() != null && !criteria.getSpecTerms().isEmpty()) {
            List<Predicate> specPreds = new ArrayList<>();
            for (String t : criteria.getSpecTerms()) {
                String like = "%" + t.toLowerCase() + "%";
                specPreds.add(cb.or(
                        cb.like(cb.lower(specJoin.get("specName")), like),
                        cb.like(cb.lower(specJoin.get("specValue")), like)
                ));
            }
            predicates.add(cb.or(specPreds.toArray(new Predicate[0])));
        }

        if (criteria.getText() != null && !criteria.getText().isBlank()) {
            String like = "%" + criteria.getText().toLowerCase() + "%";
            Join<Product, Specification> specJoin2 = root.join("specifications", JoinType.LEFT);
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("description")), like),
                    cb.like(cb.lower(root.get("brand")), like),
                    cb.like(cb.lower(specJoin2.get("specName")), like),
                    cb.like(cb.lower(specJoin2.get("specValue")), like)
            ));
        }

        return predicates;
    }

    private List<Predicate> buildCountPredicates(ProductSearchCriteria criteria, CriteriaBuilder cb, 
                                                 Root<Product> countRoot) {
        List<Predicate> countPreds = new ArrayList<>();
        
        if (criteria.getBrand() != null && !criteria.getBrand().isBlank()) {
            countPreds.add(cb.like(cb.lower(countRoot.get("brand")), "%" + criteria.getBrand().toLowerCase() + "%"));
        }
        if (criteria.getProductType() != null && !criteria.getProductType().isBlank()) {
            try {
                ProductType pt = ProductType.valueOf(criteria.getProductType().toUpperCase());
                countPreds.add(cb.equal(countRoot.get("productType"), pt));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (criteria.getMinPrice() != null) {
            countPreds.add(cb.greaterThanOrEqualTo(countRoot.get("price"), criteria.getMinPrice()));
        }
        if (criteria.getMaxPrice() != null) {
            countPreds.add(cb.lessThanOrEqualTo(countRoot.get("price"), criteria.getMaxPrice()));
        }

        if (criteria.getSpecTerms() != null && !criteria.getSpecTerms().isEmpty()) {
            Join<Product, Specification> specJoinCount = countRoot.join("specifications", JoinType.LEFT);
            List<Predicate> specPreds = new ArrayList<>();
            for (String t : criteria.getSpecTerms()) {
                String like = "%" + t.toLowerCase() + "%";
                specPreds.add(cb.or(
                        cb.like(cb.lower(specJoinCount.get("specName")), like),
                        cb.like(cb.lower(specJoinCount.get("specValue")), like)
                ));
            }
            countPreds.add(cb.or(specPreds.toArray(new Predicate[0])));
        }

        if (criteria.getText() != null && !criteria.getText().isBlank()) {
            String like = "%" + criteria.getText().toLowerCase() + "%";
            Join<Product, Specification> specJoinCount2 = countRoot.join("specifications", JoinType.LEFT);
            countPreds.add(cb.or(
                    cb.like(cb.lower(countRoot.get("name")), like),
                    cb.like(cb.lower(countRoot.get("description")), like),
                    cb.like(cb.lower(countRoot.get("brand")), like),
                    cb.like(cb.lower(specJoinCount2.get("specName")), like),
                    cb.like(cb.lower(specJoinCount2.get("specValue")), like)
            ));
        }

        return countPreds;
    }

    private void applySorting(CriteriaQuery<Product> cq, CriteriaBuilder cb, Root<Product> root, String sort) {
        switch (sort) {
            case "price-asc":
                cq.orderBy(cb.asc(root.get("price")));
                break;
            case "price-desc":
                cq.orderBy(cb.desc(root.get("price")));
                break;
            case "name-asc":
                cq.orderBy(cb.asc(root.get("name")));
                break;
            case "name-desc":
                cq.orderBy(cb.desc(root.get("name")));
                break;
            default:
                cq.orderBy(cb.desc(root.get("id")));
                break;
        }
    }
}
