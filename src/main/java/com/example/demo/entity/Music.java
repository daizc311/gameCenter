package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Music extends BaseDemoEntity {

    private static final long serialVersionUID = -4075001296306615572L;

    private int id;

    private int length;

    private String name;

    public String playMusic() {

        return String.format("[%s]这是一段音乐",this.toString());
    }

}
