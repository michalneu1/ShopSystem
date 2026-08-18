package org.example.cli;

import java.util.Arrays;
import java.util.Optional;

public enum MenuOption {
    SHOW_PRODUCTS(1, "Przeglądaj produkty"),
    ADD_PRODUCT(2, "Dodaj produkt do koszyka"),
    SHOW_CART(3, "Sprawdź koszyk"),
    PLACE_ORDER(4, "Złóż zamówienie"),
    CLEAR_CART(5, "Wyczyść koszyk"),
    END(6, "Wyjdź");

    private final int id;
    private final String label;

    MenuOption(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public static Optional<MenuOption> fromId(int id) {
        return Arrays.stream(values())
                .filter(option -> option.id == id)
                .findFirst();
    }
}
