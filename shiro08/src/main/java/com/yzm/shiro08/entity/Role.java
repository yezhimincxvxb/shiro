package com.yzm.shiro08.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 角色表
 * </p>
 *
 * @author Yzm
 * @since 2021-10-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("role")
public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "r_id", type = IdType.AUTO)
    private Integer rId;

    /**
     * 角色名称
     */
    @TableField("r_name")
    private String rName;

    /**
     * 角色描述
     */
    @TableField("r_desc")
    private String rDesc;

    @TableField("p_ids")
    private String pIds;


}
