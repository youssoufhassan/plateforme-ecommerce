package com.parfum.ecommerce.catalog;

import com.parfum.ecommerce.catalog.dto.ProductImageResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/products/admin/{productId}/images")
public class ProductImageAdminController {

    private final ProductImageService imageService;

    public ProductImageAdminController(ProductImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping
    public List<ProductImageResponse> list(@PathVariable UUID productId) {
        return imageService.list(productId);
    }

    @PostMapping(consumes = "multipart/form-data")
    public List<ProductImageResponse> upload(@PathVariable UUID productId,
                                              @RequestParam("files") MultipartFile[] files) {
        return imageService.upload(productId, files);
    }

    @PutMapping("/{imageId}/main")
    public List<ProductImageResponse> setMain(@PathVariable UUID productId, @PathVariable UUID imageId) {
        return imageService.setMain(productId, imageId);
    }

    @PutMapping("/order")
    public List<ProductImageResponse> reorder(@PathVariable UUID productId,
                                               @RequestBody Map<String, List<UUID>> body) {
        return imageService.reorder(productId, body.get("imageIds"));
    }

    @DeleteMapping("/{imageId}")
    public List<ProductImageResponse> delete(@PathVariable UUID productId, @PathVariable UUID imageId) {
        return imageService.delete(productId, imageId);
    }
}