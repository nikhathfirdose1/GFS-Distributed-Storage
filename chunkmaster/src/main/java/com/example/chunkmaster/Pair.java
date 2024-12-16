package com.example.chunkmaster;

/**
 * @param first Getters
 */
public record Pair<F, S>(F first, S second) {
    // Static factory method
    public static <F, S> Pair<F, S> of(F first, S second) {
        return new Pair<>(first, second);
    }

    // ToString method
    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}
