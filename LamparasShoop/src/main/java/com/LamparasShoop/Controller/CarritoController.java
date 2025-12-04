package com.LamparasShoop.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.LamparasShoop.Model.CarritoItem;
import com.LamparasShoop.Service.CarritoService;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    /**
     * DTO para la respuesta del carrito
     */
    public static class CarritoItemDTO {
        public Long id;
        public Long productoId;
        public String nombre;
        public byte[] imagen;
        public Double precio;
        public Integer cantidad;
        public String categoria;

        public CarritoItemDTO(CarritoItem item) {
            this.id = item.getId();
            this.productoId = item.getProducto().getId();
            this.nombre = item.getProducto().getNombre();
            this.imagen = item.getProducto().getImagen();
            this.precio = item.getProducto().getPrecio();
            this.cantidad = item.getCantidad();
            this.categoria = item.getProducto().getCategoria().toString();
        }
    }

    /**
     * DTO para agregar producto
     */
    public static class AgregarProductoRequest {
        public Long productoId;
        public Integer cantidad;
    }

    /**
     * DTO para actualizar cantidad
     */
    public static class ActualizarCantidadRequest {
        public Integer cantidad;
    }

    /**
     * GET /api/carrito - Obtiene todos los items del carrito del usuario
     * autenticado
     */
    @GetMapping
    public ResponseEntity<List<CarritoItemDTO>> obtenerCarrito() {
        try {
            List<CarritoItem> items = carritoService.obtenerCarritoUsuario();
            List<CarritoItemDTO> itemsDTO = items.stream()
                    .map(CarritoItemDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(itemsDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/carrito/cantidad - Obtiene el número total de items en el carrito
     */
    @GetMapping("/cantidad")
    public ResponseEntity<Map<String, Integer>> obtenerCantidadTotal() {
        try {
            int cantidad = carritoService.obtenerCantidadTotal();
            Map<String, Integer> response = new HashMap<>();
            response.put("cantidad", cantidad);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/carrito/agregar - Agrega un producto al carrito
     */
    @PostMapping("/agregar")
    public ResponseEntity<Map<String, String>> agregarProducto(@RequestBody AgregarProductoRequest request) {
        try {
            System.out.println(
                    "[DEBUG] Agregando producto ID: " + request.productoId + ", cantidad: " + request.cantidad);
            carritoService.agregarProducto(request.productoId, request.cantidad);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Producto agregado al carrito");
            System.out.println("[DEBUG] Producto agregado exitosamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("[ERROR] Error al agregar producto: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * PUT /api/carrito/actualizar/{itemId} - Actualiza la cantidad de un item
     */
    @PutMapping("/actualizar/{itemId}")
    public ResponseEntity<Map<String, String>> actualizarCantidad(
            @PathVariable Long itemId,
            @RequestBody ActualizarCantidadRequest request) {
        try {
            carritoService.actualizarCantidad(itemId, request.cantidad);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Cantidad actualizada");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * DELETE /api/carrito/eliminar/{itemId} - Elimina un item del carrito
     */
    @DeleteMapping("/eliminar/{itemId}")
    public ResponseEntity<Map<String, String>> eliminarItem(@PathVariable Long itemId) {
        try {
            carritoService.eliminarItem(itemId);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Item eliminado del carrito");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * DELETE /api/carrito/vaciar - Vacía completamente el carrito
     */
    @DeleteMapping("/vaciar")
    public ResponseEntity<Map<String, String>> vaciarCarrito() {
        try {
            carritoService.vaciarCarrito();
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Carrito vaciado");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
