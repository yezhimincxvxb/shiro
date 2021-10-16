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
 * 权限表
 * </p>
 *
 * @author Yzm
 * @since 2021-10-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("permissions")
public class Permissions implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "p_id", type = IdType.AUTO)
    private Integer pId;

    /**
     * 权限名称
     */
    @TableField("p_name")
    private String pName;

    /**
     * 权限描述
     */
    @TableField("p_desc")
    private String pDesc;


}
