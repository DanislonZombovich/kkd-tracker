package ru.sber.kkd.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KkdIssueId implements Serializable {
    private String runId;
    private String id;
}
