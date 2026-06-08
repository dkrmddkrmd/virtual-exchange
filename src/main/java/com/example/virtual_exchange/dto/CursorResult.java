package com.example.virtual_exchange.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class CursorResult<T> {
    private List<T> content;    // 실제 주문 내역 데이터 리스트
    private Long nextCursor;    // 다음 요청 시 넘겨주어야 할 마지막 ID (Cursor)
    private boolean hasNext;    // 다음 페이지가 존재하는지 여부 (프론트 버튼 활성화용)
}