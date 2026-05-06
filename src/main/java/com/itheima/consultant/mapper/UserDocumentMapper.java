package com.itheima.consultant.mapper;

import com.itheima.consultant.entity.UserDocument;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface UserDocumentMapper {

    @Insert("INSERT INTO user_document(user_id,file_name,file_type,file_size) " +
            "VALUES(#{userId},#{fileName},#{fileType},#{fileSize})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserDocument doc);

    @Select("SELECT * FROM user_document WHERE id=#{id} AND user_id=#{userId} LIMIT 1")
    UserDocument findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Select("SELECT * FROM user_document WHERE user_id=#{userId} AND status=1 ORDER BY create_time DESC")
    List<UserDocument> findByUserId(Long userId);

    @Update("UPDATE user_document SET status=0 WHERE id=#{id} AND user_id=#{userId}")
    int softDelete(@Param("id") Long id, @Param("userId") Long userId);

    @Delete("DELETE FROM user_document WHERE id=#{id} AND user_id=#{userId}")
    int hardDeleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
