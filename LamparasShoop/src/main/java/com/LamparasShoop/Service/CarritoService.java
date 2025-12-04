package com.LamparasShoop.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.LamparasShoop.Model.CarritoItem;
import com.LamparasShoop.Model.Producto;
import com.LamparasShoop.Model.Usuario;
import com.LamparasShoop.Repository.CarritoRepository;
import com.LamparasShoop.Repository.ProductoRepository;

@Service
public class CarritoService {

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Obtiene todos los items del carrito del usuario autenticado
     */
    public List<CarritoItem> obtenerCarritoUsuario() {
        Usuario usuario = usuarioService.getUsuarioAutenticado();
        return carritoRepository.findByUsuarioId(usuario.getId());
    }

    /**
     * Agrega un producto al carrito o incrementa la cantidad si ya existe
     */
    @Transactional
    public CarritoItem agregarProducto(Long productoId, Integer cantidad) {
        Usuario usuario = usuarioService.getUsuarioAutenticado();

        // Verificar que el producto existe
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Buscar si el producto ya está en el carrito
        Optional<CarritoItem> itemExistente = carritoRepository
                .findByUsuarioIdAndProductoId(usuario.getId(), productoId);

        if (itemExistente.isPresent()) {
            // Si ya existe, incrementar la cantidad
            CarritoItem item = itemExistente.get();
            item.setCantidad(item.getCantidad() + cantidad);
            return carritoRepository.save(item);
        } else {
            // Si no existe, crear nuevo item
            CarritoItem nuevoItem = new CarritoItem();
            nuevoItem.setUsuario(usuario);
            nuevoItem.setProducto(producto);
            nuevoItem.setCantidad(cantidad);
            return carritoRepository.save(nuevoItem);
        }
    }

    /**
     * Actualiza la cantidad de un item del carrito
     */
    @Transactional
    public CarritoItem actualizarCantidad(Long itemId, Integer nuevaCantidad) {
        Usuario usuario = usuarioService.getUsuarioAutenticado();

        CarritoItem item = carritoRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item no encontrado"));

        // Verificar que el item pertenece al usuario autenticado
        if (!item.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("No tienes permiso para modificar este item");
        }

        if (nuevaCantidad <= 0) {
            throw new RuntimeException("La cantidad debe ser mayor a 0");
        }

        item.setCantidad(nuevaCantidad);
        return carritoRepository.save(item);
    }

    /**
     * Elimina un item específico del carrito
     */
    @Transactional
    public void eliminarItem(Long itemId) {
        Usuario usuario = usuarioService.getUsuarioAutenticado();

        CarritoItem item = carritoRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item no encontrado"));

        // Verificar que el item pertenece al usuario autenticado
        if (!item.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("No tienes permiso para eliminar este item");
        }

        carritoRepository.delete(item);
    }

    /**
     * Vacía completamente el carrito del usuario autenticado
     */
    @Transactional
    public void vaciarCarrito() {
        Usuario usuario = usuarioService.getUsuarioAutenticado();
        carritoRepository.deleteByUsuarioId(usuario.getId());
    }

    /**
     * Obtiene el número total de items en el carrito
     */
    public int obtenerCantidadTotal() {
        Usuario usuario = usuarioService.getUsuarioAutenticado();
        List<CarritoItem> items = carritoRepository.findByUsuarioId(usuario.getId());
        return items.stream()
                .mapToInt(CarritoItem::getCantidad)
                .sum();
    }
}
