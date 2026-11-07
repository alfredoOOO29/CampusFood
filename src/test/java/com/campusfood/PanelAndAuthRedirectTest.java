package com.campusfood;

import com.campusfood.config.CustomAuthenticationSuccessHandler;
import com.campusfood.controller.PanelController;
import com.campusfood.entity.*;
import com.campusfood.repository.PedidoRepository;
import com.campusfood.repository.ProductoRepository;
import com.campusfood.repository.ResenaRepository;
import com.campusfood.repository.UsuarioRepository;
import com.campusfood.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PanelAndAuthRedirectTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ResenaRepository resenaRepository;

    private PanelController panelController;
    private CustomAuthenticationSuccessHandler successHandler;

    @BeforeEach
    void setUp() {
        panelController = new PanelController(productoRepository, pedidoRepository, usuarioRepository, resenaRepository);
        successHandler = new CustomAuthenticationSuccessHandler();
    }

    @Test
    void testAuthRedirect_AdminUser_RedirectsToAdmin() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "jefe@utp.edu.pe",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        successHandler.onAuthenticationSuccess(request, response, authentication);

        assertEquals("/admin", response.getRedirectedUrl());
    }

    @Test
    void testAuthRedirect_StudentUser_RedirectsToCatalogo() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "u1234567@utp.edu.pe",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_ESTUDIANTE"))
        );

        successHandler.onAuthenticationSuccess(request, response, authentication);

        assertEquals("/catalogo", response.getRedirectedUrl());
    }

    @Test
    void testPanelController_PopulatesRealBuyerAndSellerMetrics() {
        Usuario usuario = new Usuario();
        usuario.setId(10L);
        usuario.setNombre("Carlos");
        usuario.setApellidos("García");
        usuario.setEmail("u20210001@utp.edu.pe");
        usuario.setHabilitado(true);

        CustomUserDetails userDetails = new CustomUserDetails(usuario);
        Model model = new ConcurrentModel();

        // Simulamos que el usuario tiene 1 compra entregada y 1 compra en curso (LISTO)
        Pedido compra1 = new Pedido();
        compra1.setId(101L);
        compra1.setEstado(EstadoPedido.ENTREGADO);
        compra1.setTotal(new BigDecimal("15.50"));

        Pedido compra2 = new Pedido();
        compra2.setId(102L);
        compra2.setEstado(EstadoPedido.LISTO);
        compra2.setTotal(new BigDecimal("8.00"));

        List<Pedido> misCompras = List.of(compra1, compra2);

        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(pedidoRepository.findByCompradorIdOrderByFechaPedidoDesc(10L)).thenReturn(misCompras);
        when(productoRepository.findByVendedorIdOrderByFechaCreacionDesc(10L)).thenReturn(Collections.emptyList());
        when(pedidoRepository.findByVendedorIdOrderByFechaPedidoDesc(10L)).thenReturn(Collections.emptyList());
        when(resenaRepository.findAverageRatingByVendedorId(10L)).thenReturn(null);
        when(resenaRepository.countByVendedorId(10L)).thenReturn(0L);

        String view = panelController.verPanel(userDetails, model);

        assertEquals("panel/dashboard", view);
        // Verificación de métricas reales de comprador:
        assertEquals(2L, model.getAttribute("totalCompras"));
        assertEquals(1L, model.getAttribute("pedidosActivos"));
        assertEquals(1L, model.getAttribute("pedidosListos"));
        assertEquals(new BigDecimal("15.50"), model.getAttribute("totalGastado"));

        // Verificación de métricas reales de vendedor (0 sin inventos):
        assertEquals(0L, model.getAttribute("totalProductos"));
        assertEquals(0L, model.getAttribute("totalPedidos"));
        assertEquals(BigDecimal.ZERO, model.getAttribute("ganancias"));
        assertEquals(0.0, model.getAttribute("calificacion"));
        assertEquals(0L, model.getAttribute("totalResenas"));
        assertFalse((Boolean) model.getAttribute("esVendedor"));
    }
}

