package com.parfum.ecommerce.supplier;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class SupplierAdapterRegistry {

    private final Map<String, SupplierAdapter> adapters;

    public SupplierAdapterRegistry(List<SupplierAdapter> adapterList) {
        this.adapters = adapterList.stream()
                .collect(Collectors.toMap(SupplierAdapter::getSupplierKey, Function.identity()));
    }

    public SupplierAdapter get(String supplierKey) {
        SupplierAdapter adapter = adapters.get(supplierKey);
        if (adapter == null) {
            throw new IllegalArgumentException("Fournisseur inconnu : " + supplierKey);
        }
        if (!adapter.isAvailable()) {
            throw new IllegalStateException(
                "Le fournisseur " + supplierKey + " n'est pas configuré (clé API manquante ?)");
        }
        return adapter;
    }

    public List<Map<String, Object>> listAvailable() {
        return adapters.values().stream()
                .map(a -> Map.<String, Object>of(
                        "key", a.getSupplierKey(),
                        "name", a.getSupplierName(),
                        "available", a.isAvailable()
                ))
                .toList();
    }
}