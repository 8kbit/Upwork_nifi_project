package com.thoughtapps.droppoint.droppoint.model;

import com.thoughtapps.droppoint.core.dto.Instruction;
import com.thoughtapps.droppoint.core.dto.InstructionType;
import com.thoughtapps.droppoint.core.helpers.CGSON;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * Created by zaskanov on 04.04.2017.
 */

/**
 * Entity to store instruction and some info about it execution
 */
@Data
@Builder
@Entity
public class Task {
    @Id
    @GeneratedValue(strategy = SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private InstructionType type;

    @Column(nullable = false, length = 2048)
    private String instructionJSON;

    @Column
    private Date lastFinished;

    @Column(nullable = false)
    private String nodeId;

    @Tolerate
    public Task() {
    }

    @Transient
    public Instruction getInstruction() {
        return CGSON.fromJson(instructionJSON, Instruction.class);
    }

    public void setInstruction(Instruction instruction) {
        this.instructionJSON = CGSON.toJson(instruction);
    }
}
