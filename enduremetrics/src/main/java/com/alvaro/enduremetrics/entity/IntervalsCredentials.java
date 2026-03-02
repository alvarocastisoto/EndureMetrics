package com.alvaro.enduremetrics.entity;


import com.alvaro.enduremetrics.util.StringCryptoConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "intervals_credentials")
@Getter
@Setter
@NoArgsConstructor
public class IntervalsCredentials {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(nullable = false)
    private String intervalsId;

    @Convert(converter = StringCryptoConverter.class)
    @Column(nullable = false)
    private String intervalsApiKey;



}
