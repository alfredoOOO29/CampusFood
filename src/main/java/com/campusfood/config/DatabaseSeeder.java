package com.campusfood.config;

import com.campusfood.entity.*;
import com.campusfood.repository.CategoriaRepository;
import com.campusfood.repository.ProductoRepository;
import com.campusfood.repository.RolRepository;
import com.campusfood.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
public class DatabaseSeeder {

    @Bean
    public CommandLineRunner initData(CategoriaRepository categoriaRepository,
                                      RolRepository rolRepository,
                                      UsuarioRepository usuarioRepository,
                                      ProductoRepository productoRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            // 1. Inicializar Categorías
            List<String> categoriasBase = Arrays.asList(
                    "Comida rápida", "Bebidas", "Postres", "Snacks", "Sándwiches", "Saludable", "Otros");
            for (String nombre : categoriasBase) {
                if (categoriaRepository.findByNombreIgnoreCase(nombre).isEmpty()) {
                    categoriaRepository.save(new Categoria(nombre));
                }
            }

            // 2. Inicializar Roles
            Rol rolAdmin = rolRepository.findByNombre("ADMIN").orElseGet(() -> {
                return rolRepository.save(new Rol("ADMIN"));
            });

            Rol rolEstudiante = rolRepository.findByNombre("ESTUDIANTE").orElseGet(() -> {
                return rolRepository.save(new Rol("ESTUDIANTE"));
            });

            // 3. Inicializar Administrador
            if (usuarioRepository.findByEmail("jefe@utp.edu.pe").isEmpty()) {
                Usuario admin = new Usuario();
                admin.setNombre("Administrador");
                admin.setApellidos("Jefe");
                admin.setEmail("jefe@utp.edu.pe");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRoles(Collections.singleton(rolAdmin));
                admin.setHabilitado(true);
                usuarioRepository.save(admin);
                System.out.println("Usuario Administrador (jefe@utp.edu.pe) creado por defecto.");
            }

            // 4. Inicializar Estudiante Vendedor de prueba
            Usuario estudiante = usuarioRepository.findByEmail("estudiante@utp.edu.pe").orElseGet(() -> {
                Usuario est = new Usuario();
                est.setNombre("Alfredo");
                est.setApellidos("Salas");
                est.setEmail("estudiante@utp.edu.pe");
                est.setPassword(passwordEncoder.encode("estudiante123"));
                est.setRoles(Collections.singleton(rolEstudiante));
                est.setHabilitado(true);
                return usuarioRepository.save(est);
            });

            // 5. Cargar productos representativos de la maqueta si aún no existen
            if (!productoRepository.existsByNombre("Hamburguesa Clásica")) {
                Categoria comidaRapida = categoriaRepository.findByNombreIgnoreCase("Comida rápida")
                        .orElseGet(() -> categoriaRepository.save(new Categoria("Comida rápida")));
                Categoria bebidas = categoriaRepository.findByNombreIgnoreCase("Bebidas")
                        .orElseGet(() -> categoriaRepository.save(new Categoria("Bebidas")));
                Categoria snacks = categoriaRepository.findByNombreIgnoreCase("Snacks")
                        .orElseGet(() -> categoriaRepository.save(new Categoria("Snacks")));
                Categoria postres = categoriaRepository.findByNombreIgnoreCase("Postres")
                        .orElseGet(() -> categoriaRepository.save(new Categoria("Postres")));
                Categoria sandiwches = categoriaRepository.findByNombreIgnoreCase("Sándwiches")
                        .orElseGet(() -> categoriaRepository.save(new Categoria("Sándwiches")));
                Categoria saludable = categoriaRepository.findByNombreIgnoreCase("Saludable")
                        .orElseGet(() -> categoriaRepository.save(new Categoria("Saludable")));

                crearProductoDemo(productoRepository, estudiante, "Hamburguesa Clásica",
                        "Deliciosa hamburguesa con carne de res, queso cheddar, lechuga fresca, tomate y salsa especial.",
                        new BigDecimal("8.00"), 15, comidaRapida,
                        "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=600&q=80",
                        "Puesto 12 - Cafetería Central");

                crearProductoDemo(productoRepository, estudiante, "Refresco Natural",
                        "Refrescante jugo helado de maracuyá y frutas de estación.",
                        new BigDecimal("4.00"), 20, bebidas,
                        "https://images.unsplash.com/photo-1621263764928-df1444c5e859?auto=format&fit=crop&w=600&q=80",
                        "Patio de Comidas - Pabellón A");

                crearProductoDemo(productoRepository, estudiante, "Papas Fritas",
                        "Porción generosa de papas fritas crujientes con cremas a elección.",
                        new BigDecimal("5.00"), 12, snacks,
                        "https://images.unsplash.com/photo-1576107232684-1279f3908594?auto=format&fit=crop&w=600&q=80",
                        "Pabellón B - Planta Baja");

                crearProductoDemo(productoRepository, estudiante, "Brownie Casero",
                        "Brownie artesanal de puro chocolate con nueces y centro fudge suave.",
                        new BigDecimal("6.00"), 8, postres,
                        "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?auto=format&fit=crop&w=600&q=80",
                        "Entrada de Biblioteca Central");

                crearProductoDemo(productoRepository, estudiante, "Sándwich de Pollo",
                        "Pechuga de pollo deshilachada con mayonesa casera, apio y papitas al hilo.",
                        new BigDecimal("7.00"), 10, sandiwches != null ? sandiwches : comidaRapida,
                        "https://images.unsplash.com/photo-1550547660-d9450f859349?auto=format&fit=crop&w=600&q=80",
                        "Pabellón C - Mesas Exteriores");

                crearProductoDemo(productoRepository, estudiante, "Jugo de Naranja",
                        "100% natural, recién exprimido, sin preservantes ni azúcar añadida.",
                        new BigDecimal("4.00"), 14, bebidas,
                        "https://images.unsplash.com/photo-1613478223719-2ab802602423?auto=format&fit=crop&w=600&q=80",
                        "Patio Central");

                crearProductoDemo(productoRepository, estudiante, "Hot Dog Especial",
                        "Pan suave con salchicha frankfurter, papitas crocantes y todas las salsas.",
                        new BigDecimal("7.00"), 9, comidaRapida,
                        "https://images.unsplash.com/photo-1619740455993-9e612b1af08a?auto=format&fit=crop&w=600&q=80",
                        "Pabellón B - Cafetería");

                crearProductoDemo(productoRepository, estudiante, "Yogurt con Frutas",
                        "Copa de yogurt natural con granola crocante, fresas, arándanos y miel.",
                        new BigDecimal("5.00"), 11, saludable != null ? saludable : postres,
                        "https://images.unsplash.com/photo-1488477181946-6428a0291777?auto=format&fit=crop&w=600&q=80",
                        "Pabellón A - Piso 2");

                System.out.println("Productos representativos de la maqueta cargados exitosamente.");
            }
        };
    }

    private void crearProductoDemo(ProductoRepository repo, Usuario vendedor, String nombre,
                                   String descripcion, BigDecimal precio, int stock,
                                   Categoria categoria, String imagenUrl, String ubicacion) {
        if (categoria == null) return;
        Producto p = new Producto();
        p.setVendedor(vendedor);
        p.setNombre(nombre);
        p.setDescripcion(descripcion);
        p.setPrecio(precio);
        p.setStock(stock);
        p.setCategoria(categoria);
        p.setImagenUrl(imagenUrl);
        p.setActivo(true);

        DisponibilidadProducto disp = new DisponibilidadProducto();
        disp.setProducto(p);
        disp.setUbicacion(ubicacion);
        disp.setFechaDisponible(LocalDate.now());
        disp.setHoraInicio(LocalTime.of(8, 0));
        disp.setHoraFin(LocalTime.of(18, 0));
        p.setDisponibilidad(disp);

        repo.save(p);
    }
}
