package com.itheima.consultant.mapper;

import com.itheima.consultant.entity.UserDocument;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ConversationDocumentBindingMapper {

    @Select("""
            SELECT d.*
            FROM conversation_document_binding b
            JOIN user_document d ON d.id = b.document_id
            WHERE b.conversation_id = #{conversationId}
              AND d.status = 1
            ORDER BY b.id ASC
            """)
    List<UserDocument> findDocumentsByConversationId(Long conversationId);

    @Select("""
            SELECT document_id
            FROM conversation_document_binding
            WHERE conversation_id = #{conversationId}
            ORDER BY id ASC
            """)
    List<Long> findDocumentIdsByConversationId(Long conversationId);

    @Insert("""
            INSERT INTO conversation_document_binding(conversation_id, document_id)
            VALUES(#{conversationId}, #{documentId})
            """)
    int insert(@Param("conversationId") Long conversationId, @Param("documentId") Long documentId);

    @Delete("DELETE FROM conversation_document_binding WHERE conversation_id = #{conversationId}")
    int deleteByConversationId(Long conversationId);

    @Delete("DELETE FROM conversation_document_binding WHERE document_id = #{documentId}")
    int deleteByDocumentId(Long documentId);
}
