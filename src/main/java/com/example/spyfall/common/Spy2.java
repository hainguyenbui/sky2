package com.example.spyfall.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Spy2 {
    int id;
    String ipConfig;
    String userName;
    String keyword;
    @Builder.Default
    boolean isRemove = false;
    @Builder.Default
    String role = "Dân Thường";
}
