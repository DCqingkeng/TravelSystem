package com.travel.mapper;

import com.travel.entity.Destination;
import com.travel.entity.DestinationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface DestinationMapper {

    // 关键修复：添加缺失的 @Select 注解
    @Select("SELECT * FROM destination WHERE id = #{id}")
    Destination selectById(Long id);

    @Select("SELECT * FROM destination WHERE type = #{type} ORDER BY heat_score DESC LIMIT #{limit}")
    List<Destination> findTopByTypeOrderByHeat(@Param("type") DestinationType type, @Param("limit") int limit);

    @Select("SELECT * FROM destination ORDER BY heat_score DESC LIMIT #{limit}")
    List<Destination> findTopOrderByHeat(int limit);

    @Select("""
    <script>
    SELECT * FROM destination 
    WHERE 1=1 
    <if test='type != null'>
        AND type = #{type}  <!-- 这里使用了 type 参数！ -->
    </if>
    AND (name LIKE CONCAT('%', #{keyword}, '%') 
         OR keywords LIKE CONCAT('%', #{keyword}, '%'))
    ORDER BY heat_score DESC 
    LIMIT #{limit}
    </script>
    """)
    List<Destination> searchByKeyword(@Param("type") DestinationType type,
                                      @Param("keyword") String keyword,
                                      @Param("limit") int limit);

    @Select("SELECT id, heat_score as heatScore FROM destination")
    List<Map<String, Object>> selectAllHeatScores();

    @Update("UPDATE destination SET heat_score = #{heatScore} WHERE id = #{id}")
    void updateHeatScore(@Param("id") Long id, @Param("heatScore") Double heatScore);
}