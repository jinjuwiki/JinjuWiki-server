package com.jinju.jinjuwiki.domain.search.service;

import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

// 급상승 문서 제목 필터
@Component
public class TrendingDocumentTitleFilter {

    // 급상승 문서 제외 대상 금칙어 목록
    private static final List<String> BANNED_TITLE_KEYWORDS = List.of(
            "씨발",
            "병신",
            "개새끼",
            "좆",
            "멍청이",
            "바보",
            "사기꾼",
            "도박",
            "쓰레기"
    );

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("(01[016789]|02|0[3-9][0-9])[- )]?[0-9]{3,4}[- ]?[0-9]{4}");
    private static final Pattern RESIDENT_ID_PATTERN =
            Pattern.compile("\\d{6}[- ]?[1-4]\\d{6}");

    // 제목 개인정보 포함 여부 확인 메서드
    public boolean containsPersonalInformation(String title) {
        if (title == null || title.isBlank()) {
            return false;
        }

        return EMAIL_PATTERN.matcher(title).find()
                || PHONE_PATTERN.matcher(title).find()
                || RESIDENT_ID_PATTERN.matcher(title).find();
    }

    // 제목 금칙어 포함 여부 확인 메서드
    public boolean containsBannedExpression(String title) {
        if (title == null || title.isBlank()) {
            return false;
        }

        return BANNED_TITLE_KEYWORDS.stream().anyMatch(title::contains);
    }

    // 급상승 문서 제목 제외 여부 확인 메서드
    public boolean isExcludedTitle(String title) {
        return containsPersonalInformation(title) || containsBannedExpression(title);
    }
}
