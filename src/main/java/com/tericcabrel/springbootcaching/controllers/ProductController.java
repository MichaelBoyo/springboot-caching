package com.tericcabrel.springbootcaching.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tericcabrel.springbootcaching.models.CacheData;
import com.tericcabrel.springbootcaching.models.Product;
import com.tericcabrel.springbootcaching.models.dtos.CreateProductDto;
import com.tericcabrel.springbootcaching.models.dtos.SearchProductDto;
import com.tericcabrel.springbootcaching.models.responses.ProductListResponse;
import com.tericcabrel.springbootcaching.models.responses.ProductResponse;
import com.tericcabrel.springbootcaching.repositories.CacheDataRepository;
import com.tericcabrel.springbootcaching.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final CacheDataRepository cacheDataRepository;
    private final ObjectMapper objectMapper= new ObjectMapper();


    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductDto createProductDto) {
        Product product = productService.create(createProductDto);
//        cacheDataRepository.deleteAll();
        return ResponseEntity.ok(new ProductResponse(product));
    }

    @GetMapping
    public ResponseEntity<ProductListResponse> getAll() throws JsonProcessingException {
        Optional<CacheData> optionalCacheData = cacheDataRepository.findById("allProducts");

        // Cache hit
        ResponseEntity<ProductListResponse> productList1 = checkCache(optionalCacheData);
        if (productList1 != null) return productList1;

        // Cache miss
        List<Product> productList = productService.findAll();
        String productsAsJsonString = objectMapper.writeValueAsString(productList);
        CacheData cacheData = new CacheData("allProducts", productsAsJsonString);

        cacheDataRepository.save(cacheData);

        return ResponseEntity.ok(new ProductListResponse(productList));
    }

    private ResponseEntity<ProductListResponse> checkCache(Optional<CacheData> optionalCacheData) throws JsonProcessingException {
        if (optionalCacheData.isPresent()) {
            String productAsString = optionalCacheData.get().getValue();
            TypeReference<List<Product>> mapType = new TypeReference<>() {};
            List<Product> productList = objectMapper.readValue(productAsString, mapType);

            return ResponseEntity.ok(new ProductListResponse(productList));
        }
        return null;
    }

    @GetMapping("/search")
    public ResponseEntity<ProductListResponse> search(@Valid  @RequestBody SearchProductDto searchProductDto) throws InterruptedException, JsonProcessingException {
        String cacheKey = searchProductDto.buildCacheKey("searchProducts");

        Optional<CacheData> optionalCacheData = cacheDataRepository.findById(cacheKey);

        // Cache hit
        ResponseEntity<ProductListResponse> productList1 = checkCache(optionalCacheData);
        if (productList1 != null) return productList1;

        List<Product> productList = productService.search(searchProductDto);

        String productsAsJsonString = objectMapper.writeValueAsString(productList);
        CacheData cacheData = new CacheData(cacheKey, productsAsJsonString);

        cacheDataRepository.save(cacheData);

        return ResponseEntity.ok(new ProductListResponse(productList));
    }
}
