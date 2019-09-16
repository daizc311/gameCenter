package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Video extends BaseDemoEntity {

    private int id;

    private int length;

    private String name;

    public String playVideo() {

        return String.format("[%s]这是一段视频",this.toString());
    }

}
