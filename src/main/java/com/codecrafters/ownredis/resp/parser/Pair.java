package com.codecrafters.ownredis.resp.parser;

import lombok.Getter;

@Getter
public class Pair<F,S> {
    private F first;
    private S second;
    private Pair(){}
    private Pair(F first, S second){
        this.first= first;
        this.second=second;
    }

    public static <F1,S1> Pair<F1,S1> of(F1 f1,S1 s1){
        return new Pair<>(f1, s1);
    }
}
