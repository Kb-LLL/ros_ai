package com.itheima.consultant.mapper;

import com.itheima.consultant.entity.Conversation;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ConversationMapper {

    @Insert("INSERT INTO conversation(user_id, title) VALUES(#{userId}, #{title})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Conversation conversation);

    @Select("SELECT * FROM conversation WHERE user_id = #{userId} ORDER BY update_time DESC")
    List<Conversation> findByUserId(Long userId);

    @Select("SELECT * FROM conversation WHERE id = #{id}")
    Conversation findById(Long id);

    @Update("UPDATE conversation SET title = #{title} WHERE id = #{id}")
    int updateTitle(@Param("id") Long id, @Param("title") String title);

    @Update("UPDATE conversation SET update_time = NOW() WHERE id = #{id}")
    int touch(Long id);

    @Delete("DELETE FROM conversation WHERE id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}