package com.example.fourinarow;

public enum Man {
    EMPTY, BLACK, WHITE;

    public Man getRival() {
        if (this == BLACK) {
            return WHITE;
        } else {
            return BLACK;
        }
    }
}


