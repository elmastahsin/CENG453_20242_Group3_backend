package com.uno.dtos;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardEntryDTO {
    private Long id;
    private Long userId;
    private String username;
    private Long score;
    @JsonSerialize(using = ToStringSerializer.class)
    private LocalDateTime scoreDate;
    private Long gameId;
    private String gameType;
}