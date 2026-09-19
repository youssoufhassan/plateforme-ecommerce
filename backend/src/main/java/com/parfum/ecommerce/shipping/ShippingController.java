package com.parfum.ecommerce.shipping;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/shipping")
public class ShippingController {

    private final JdbcTemplate jdbcTemplate;

    public ShippingController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Pays vers lesquels la livraison est possible. */
    @GetMapping("/countries")
    public List<String> getSupportedCountries() {
        return jdbcTemplate.queryForList(
                "SELECT country_code FROM shipping_zone_countries ORDER BY country_code",
                String.class);
    }
}