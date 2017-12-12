package com.thoughtapps.droppoint.droppoint.model;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * Created by zaskanov on 04.04.2017.
 */

/**
 * Configurations stored in DB
 */
@Data
@Builder
@Entity
public class Configuration {
    @Id
    @GeneratedValue(strategy = SEQUENCE)
    private Long id;

    @Column(nullable = false, unique = true)
    private String key;

    @Column(nullable = false)
    private String value;

    @Tolerate
    public Configuration() {
    }
}
