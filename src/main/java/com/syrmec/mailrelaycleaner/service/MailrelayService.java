package com.syrmec.mailrelaycleaner.service;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class MailrelayService {

    private static final String[] ESTADOS_SIMULADOS = {
            "ACTIVO", "INACTIVO", "REBOTADO", "NO_ABIERTO", "CANCELADO"
    };

    private final Random random = new Random();

    public String obtenerEstadoContacto(String email) {
        // Lógica simulada: devuelve un estado aleatorio para cualquier email
        int indice = random.nextInt(ESTADOS_SIMULADOS.length);
        return ESTADOS_SIMULADOS[indice];
    }
}

