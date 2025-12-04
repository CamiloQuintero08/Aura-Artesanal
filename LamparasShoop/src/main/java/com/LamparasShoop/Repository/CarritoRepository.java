package com.LamparasShoop.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.LamparasShoop.Model.CarritoItem;

@Repository
public interface CarritoRepository extends JpaRepository<CarritoItem, Long> {

    /**
     * Obtiene todos los items del carrito de un usuario específico
     */
    List<CarritoItem> findByUsuarioId(Long usuarioId);

    /**
     * Busca un item específico en el carrito de un usuario
     */
    Optional<CarritoItem> findByUsuarioIdAndProductoId(Long usuarioId, Long productoId);

    /**
     * Elimina todos los items del carrito de un usuario (vaciar carrito)
     */
    void deleteByUsuarioId(Long usuarioId);
}
