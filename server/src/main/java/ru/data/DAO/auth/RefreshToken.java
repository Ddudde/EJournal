package ru.data.DAO.auth;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
@Entity
public class RefreshToken {
    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "valuee")
    private String value;

    @ManyToOne
    private User usr;

    private Long timeOfExpired;
}
