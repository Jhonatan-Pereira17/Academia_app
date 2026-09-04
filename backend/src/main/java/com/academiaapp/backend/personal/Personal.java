package com.academiaapp.backend.personal;

import com.academiaapp.backend.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "personals")
@Getter
@Setter
@NoArgsConstructor
public class Personal {

    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    @Column(nullable = false)
    private String name;

    private String cref;

    private String phone;
}