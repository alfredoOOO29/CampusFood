package com.campusfood.controller;

import com.campusfood.entity.EstadoReclamo;
import com.campusfood.entity.Pedido;
import com.campusfood.entity.Reclamo;
import com.campusfood.entity.Usuario;
import com.campusfood.repository.PedidoRepository;
import com.campusfood.repository.ReclamoRepository;
import com.campusfood.repository.UsuarioRepository;
import com.campusfood.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ReclamoController {

    private final ReclamoRepository reclamoRepository;
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;

    public ReclamoController(ReclamoRepository reclamoRepository, PedidoRepository pedidoRepository, UsuarioRepository usuarioRepository) {
        this.reclamoRepository = reclamoRepository;
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Crear un reclamo (Vendedor reporta Comprador)
    @PostMapping("/panel/pedidos/{id}/reportar")
    public String reportarComprador(@PathVariable Long id,
                                    @RequestParam String motivo,
                                    @RequestParam(required = false) String evidenciaUrl,
                                    @AuthenticationPrincipal CustomUserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {
        
        Pedido pedido = pedidoRepository.findById(id).orElseThrow();
        
        // Validar que el que reporta es el vendedor
        if (!pedido.getVendedor().getId().equals(userDetails.getId())) {
            redirectAttributes.addFlashAttribute("error", "No tienes permiso para reportar este pedido.");
            return "redirect:/panel/ventas";
        }

        Reclamo reclamo = new Reclamo();
        reclamo.setPedido(pedido);
        reclamo.setReportante(pedido.getVendedor());
        reclamo.setReportado(pedido.getComprador());
        reclamo.setMotivo(motivo);
        reclamo.setEvidenciaUrl(evidenciaUrl);
        reclamo.setEstado(EstadoReclamo.PENDIENTE);
        
        reclamoRepository.save(reclamo);
        
        redirectAttributes.addFlashAttribute("mensajeExito", "Reclamo enviado. Un administrador lo revisará pronto.");
        return "redirect:/panel/ventas";
    }

    // Pantalla pública para apelación (el usuario suspendido entra aquí)
    @GetMapping("/apelar")
    public String verApelacion(Model model) {
        return "apelar";
    }

    // Procesar apelación pública
    @PostMapping("/apelar")
    public String enviarApelacion(@RequestParam String email, 
                                  @RequestParam String motivoApelacion, 
                                  RedirectAttributes redirectAttributes) {
        
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("error", "Correo no encontrado en el sistema.");
            return "redirect:/apelar";
        }
        
        // Buscar si tiene algún reclamo aprobado o pendiente para apelar
        Reclamo reclamo = reclamoRepository.findByReportadoIdOrderByFechaCreacionDesc(usuario.getId())
                .stream().findFirst().orElse(null);
                
        if (reclamo == null) {
            redirectAttributes.addFlashAttribute("error", "No tienes strikes recientes para apelar.");
            return "redirect:/apelar";
        }
        
        reclamo.setMotivoApelacion(motivoApelacion);
        reclamo.setEstado(EstadoReclamo.APELADO);
        reclamoRepository.save(reclamo);
        
        redirectAttributes.addFlashAttribute("mensajeExito", "Tu apelación ha sido enviada al equipo de administración.");
        return "redirect:/apelar";
    }
}
