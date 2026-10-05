package com.campusfood.config;

import com.campusfood.entity.Categoria;
import com.campusfood.entity.Rol;
import com.campusfood.entity.Usuario;
import com.campusfood.repository.CategoriaRepository;
import com.campusfood.repository.RolRepository;
import com.campusfood.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
public class DatabaseSeeder {

    @Bean
    public CommandLineRunner initData(CategoriaRepository categoriaRepository,
            RolRepository rolRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            // 1. Inicializar Categorías
            if (categoriaRepository.count() == 0) {
                List<String> categoriasBase = Arrays.asList(
                        "Sándwiches", "Postres", "Dulces", "Snacks", "Bebidas", "Comida preparada", "Otros");
                for (String nombre : categoriasBase) {
                    categoriaRepository.save(new Categoria(nombre));
                }
                System.out.println("Categorías iniciales cargadas exitosamente.");
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
        };
    }
}
