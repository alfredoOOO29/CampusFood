package com.campusfood.service;

import com.campusfood.dto.CrearProductoDTO;
import com.campusfood.entity.Categoria;
import com.campusfood.entity.DisponibilidadProducto;
import com.campusfood.entity.Producto;
import com.campusfood.entity.Usuario;
import com.campusfood.repository.CategoriaRepository;
import com.campusfood.repository.DisponibilidadProductoRepository;
import com.campusfood.repository.ProductoRepository;
import com.campusfood.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final DisponibilidadProductoRepository disponibilidadProductoRepository;
    private final UsuarioRepository usuarioRepository;

    public ProductoService(ProductoRepository productoRepository,
                           CategoriaRepository categoriaRepository,
                           DisponibilidadProductoRepository disponibilidadProductoRepository,
                           UsuarioRepository usuarioRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.disponibilidadProductoRepository = disponibilidadProductoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Producto crearProducto(CrearProductoDTO dto, Long vendedorId) {
        Usuario vendedor = usuarioRepository.findById(vendedorId)
                .orElseThrow(() -> new IllegalArgumentException("Vendedor no encontrado"));

        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        // Validar el horario
        if (dto.getHoraInicio().isAfter(dto.getHoraFin())) {
            throw new IllegalArgumentException("La hora de inicio no puede ser posterior a la hora de fin");
        }

        Producto producto = new Producto();
        producto.setVendedor(vendedor);
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setStock(dto.getStock());
        producto.setCategoria(categoria);
        producto.setActivo(dto.getStock() > 0);

        // Guardar primero para obtener el ID si es necesario o por la cascada se guardará
        
        DisponibilidadProducto disponibilidad = new DisponibilidadProducto();
        disponibilidad.setProducto(producto);
        disponibilidad.setUbicacion(dto.getUbicacion());
        disponibilidad.setFechaDisponible(dto.getFechaDisponible());
        disponibilidad.setHoraInicio(dto.getHoraInicio());
        disponibilidad.setHoraFin(dto.getHoraFin());

        producto.setDisponibilidad(disponibilidad);

        return productoRepository.save(producto);
    }
}
