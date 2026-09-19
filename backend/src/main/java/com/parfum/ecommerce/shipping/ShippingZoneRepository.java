package com.parfum.ecommerce.shipping;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ShippingZoneRepository extends JpaRepository<ShippingZone, UUID> {

    @Query(value = """
        SELECT z.* FROM shipping_zones z
        JOIN shipping_zone_countries c ON c.zone_id = z.id
        WHERE c.country_code = :countryCode
        """, nativeQuery = true)
    Optional<ShippingZone> findByCountryCode(@Param("countryCode") String countryCode);
}