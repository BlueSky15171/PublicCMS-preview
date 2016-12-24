package com.publiccms.entities.sys;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;

// Generated 2016-1-19 11:28:06 by Hibernate Tools 4.3.1

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.sanluan.common.source.entity.MyColumn;

/**
 * SystemRoleMoudle generated by hbm2java
 */
@Entity
@Table(name = "sys_role_moudle")
public class SysRoleMoudle implements java.io.Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    @MyColumn(title = "ID")
    private SysRoleMoudleId id;

    public SysRoleMoudle() {
    }

    public SysRoleMoudle(SysRoleMoudleId id) {
        this.id = id;
    }

    @EmbeddedId
    @AttributeOverrides({ @AttributeOverride(name = "roleId", column = @Column(name = "role_id", nullable = false)),
            @AttributeOverride(name = "moudleId", column = @Column(name = "moudle_id", nullable = false)) })
    public SysRoleMoudleId getId() {
        return this.id;
    }

    public void setId(SysRoleMoudleId id) {
        this.id = id;
    }

}
