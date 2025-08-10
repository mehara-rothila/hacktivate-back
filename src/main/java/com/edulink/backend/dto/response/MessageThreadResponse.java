
// ============================================================================
// dto/response/MessageThreadResponse.java
// ============================================================================
package com.edulink.backend.dto.response;

import com.edulink.backend.model.entity.Message;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MessageThreadResponse {
    private String id;
    private List<Message.MessageThread> messages;
}
