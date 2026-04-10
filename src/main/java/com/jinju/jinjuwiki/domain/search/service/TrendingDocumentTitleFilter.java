package com.jinju.jinjuwiki.domain.search.service;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

// 급상승 문서 제목 필터
@Component
public class TrendingDocumentTitleFilter {

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
}
