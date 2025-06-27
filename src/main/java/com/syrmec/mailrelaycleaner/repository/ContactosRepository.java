package com.syrmec.mailrelaycleaner.repository;

import com.syrmec.mailrelaycleaner.model.Contactos;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactosRepository extends JpaRepository<Contactos, Long> {
}
