package com.docker.atsea.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.docker.atsea.model.Product;
import com.docker.atsea.repositories.ProductRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Seeds product data on startup if the database is empty.
 * This mirrors the Docker init SQL used for local deployments.
 */
@Component
public class ProductSeedDataLoader implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(ProductSeedDataLoader.class);
    private static final String SEED_RESOURCE = "seed/products.json";

    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    public ProductSeedDataLoader(ProductRepository productRepository, ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (productRepository.count() > 0) {
            logger.info("Product seed skipped: existing products found.");
            return;
        }

        ClassPathResource resource = new ClassPathResource(SEED_RESOURCE);
        if (!resource.exists()) {
            logger.warn("Product seed resource not found: {}", SEED_RESOURCE);
            return;
        }

        try (InputStream inputStream = resource.getInputStream()) {
            List<ProductSeed> seeds = objectMapper.readValue(
                    inputStream,
                    new TypeReference<List<ProductSeed>>() {});

            List<Product> products = seeds.stream()
                    .map(seed -> {
                        Product product = new Product();
                        product.setName(seed.getName());
                        product.setDescription(seed.getDescription());
                        product.setImage(seed.getImage());
                        product.setPrice(seed.getPrice());
                        return product;
                    })
                    .collect(Collectors.toList());

            productRepository.saveAll(products);
            logger.info("Seeded {} products into the database.", products.size());
        } catch (IOException e) {
            logger.error("Failed to seed products from {}.", SEED_RESOURCE, e);
        }
    }

    private static class ProductSeed {
        private String name;
        private String description;
        private String image;
        private double price;

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getImage() {
            return image;
        }

        public double getPrice() {
            return price;
        }
    }
}
