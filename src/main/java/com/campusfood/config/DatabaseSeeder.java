package com.campusfood.config;

import com.campusfood.entity.Categoria;
import com.campusfood.repository.CategoriaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class DatabaseSeeder {

    @Bean
    public CommandLineRunner initCategorias(CategoriaRepository categoriaRepository) {
        return args -> {
            if (categoriaRepository.count() == 0) {
                List<String> categoriasBase = Arrays.asList(
                    "Sándwiches", "Postres", "Dulces", "Snacks", "Bebidas", "Comida preparada", "Otros"
                );
                
                for (String nombre : categoriasBase) {
                    categoriaRepository.save(new Categoria(nombre));
                }
                System.out.println("Categorías iniciales cargadas exitosamente.");
            }
        };
    }
}
