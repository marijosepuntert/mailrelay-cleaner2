package com.syrmec.mailrelaycleaner.model;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import com.syrmec.mailrelaycleaner.csv.BooleanConverter;
import com.syrmec.mailrelaycleaner.csv.LocalDateTimeConverter;
import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
@Table(name = "contacto")
public class Contactos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* === Campos que vienen del CSV === */
    @CsvBindByName(column = "email")
    private String email;

    @Enumerated(EnumType.STRING)
    @CsvBindByName(column = "estado")
    private EstadoContacto estado;

    // Conversor personalizado para LocalDateTime
    @CsvCustomBindByName(column = "ultimaApertura", converter = LocalDateTimeConverter.class)
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(name = "ultima_apertura")
    private LocalDateTime ultimaApertura;

    @CsvCustomBindByName(column = "eliminado", converter = BooleanConverter.class)
    private boolean eliminado;


    /* ================================= */

    //-- getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public EstadoContacto getEstado() { return estado; }
    public void setEstado(EstadoContacto estado) { this.estado = estado; }

    public LocalDateTime getUltimaApertura() { return ultimaApertura; }
    public void setUltimaApertura(LocalDateTime ultimaApertura) { this.ultimaApertura = ultimaApertura; }

    public boolean isEliminado() { return eliminado; }
    public void setEliminado(boolean eliminado) { this.eliminado = eliminado; }
}
