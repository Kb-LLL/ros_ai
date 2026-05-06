package com.itheima.consultant.mapper;

import com.itheima.consultant.entity.Message;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface MessageMapper {

    @Insert("INSERT INTO message(conversation_id, role, content) VALUES(#{conversationId}, #{role}, #{content})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Message message);

    @Select("SELECT * FROM message WHERE conversation_id = #{conversationId} ORDER BY create_time ASC")
    List<Message> findByConversationId(Long conversationId);

    @Select("SELECT * FROM message WHERE conversation_id = #{conversationId} AND role = 'assistant' ORDER BY id DESC LIMIT 1")
    Message findLastAssistantMessage(Long conversationId);

    @Delete("DELETE FROM message WHERE id = #{id}")
    int deleteById(Long id);

    @Delete("DELETE FROM message WHERE conversation_id = #{conversationId}")
    int deleteByConversationId(Long conversationId);
}
