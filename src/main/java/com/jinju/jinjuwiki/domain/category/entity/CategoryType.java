package com.jinju.jinjuwiki.domain.category.entity;

import lombok.Getter;

// 기본 카테고리 정의 Enum
@Getter
public enum CategoryType {
    SCHOOL("학교"),
    STUDENT("학생"),
    INCIDENT("사건사고"),
    TEACHER("선생님"),
    ETC("기타");

    private final String displayName;

    CategoryType(String displayName) {
        this.displayName = displayName;
    }
}
