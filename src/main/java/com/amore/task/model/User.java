package com.amore.task.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class User {
    private long no;
    private String name;

    public User(long no, String name) {
        this.no = no;
        this.name = name;
    }
}
