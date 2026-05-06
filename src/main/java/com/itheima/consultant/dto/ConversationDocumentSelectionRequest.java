package com.itheima.consultant.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ConversationDocumentSelectionRequest {
    private List<Long> documentIds = new ArrayList<>();
}
