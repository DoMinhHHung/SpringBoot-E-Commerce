package iuh.fit.se.ecommerce.repository;

import iuh.fit.se.ecommerce.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {}