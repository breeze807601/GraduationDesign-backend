package com.example.backend.pojo.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageDTO<T> {
    protected Long total;
    protected Long pages;
    protected List<T> list;
}
