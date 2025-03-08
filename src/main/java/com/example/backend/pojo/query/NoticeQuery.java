package com.example.backend.pojo.query;

import lombok.Data;

import java.time.LocalDate;

@Data
public class NoticeQuery extends PageQuery{
    private LocalDate time;
    private String title;
}
