package com.fundingsocieties.jumpcloudbot.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.io.Serializable;
import java.time.OffsetDateTime;

@MappedSuperclass
@Data
public class Thing implements Serializable {
    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at", columnDefinition= "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", columnDefinition= "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "deleted_at", columnDefinition= "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime deletedAt;

    @Column(name = "deleted_by")
    private String deletedBy;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
