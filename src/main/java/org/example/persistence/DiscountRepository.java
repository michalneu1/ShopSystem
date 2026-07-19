package org.example.persistence;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class DiscountRepository {
    private final Path path;

    public DiscountRepository(Path path) {
        this.path = path;
    }

    public BigDecimal getDiscount(String code) {
        try {
            List<String> lines = Files.readAllLines(path);
            return lines.stream().map(line -> line.split(";"))
                    .filter(line -> line[0].trim().equals(code))
                    .map(line -> new BigDecimal(line[1].trim()))
                    .findFirst()
                    .orElse(BigDecimal.ONE);
        } catch (Exception e) {
            System.out.println("Rabat nie zostal naliczony: " + e.getMessage());
            return BigDecimal.ONE;
        }
    }
}
