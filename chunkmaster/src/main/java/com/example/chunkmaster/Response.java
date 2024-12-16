package com.example.chunkmaster;

import jakarta.annotation.Nullable;

public interface Response<S, E> {
    boolean isSuccess();

    @Nullable
    S data();

    @Nullable
    E error();
}
