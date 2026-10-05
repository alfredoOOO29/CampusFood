package com.campusfood.entity;

public enum EstadoReclamo {
    PENDIENTE,
    APROBADO,   // El strike fue aplicado
    RECHAZADO,  // El strike fue desestimado
    APELADO     // El usuario apeló el strike
}
