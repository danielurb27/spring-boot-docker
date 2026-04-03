package com.easy.offers.shared.exception;

/**
 * InvalidReferenceException — Una FK referencia un registro que no existe.
 *
 * Se lanza cuando:
 * - offer_type_id enviado no existe en la tabla offer_types
 * - sector_id enviado no existe en la tabla sectors
 *
 * Esto puede ocurrir si el cliente envía un ID inventado o si los datos
 * de referencia no están cargados correctamente en la BD.
 *
 * ¿Por qué HTTP 400 y no 404?
 * - 404 Not Found: el RECURSO PRINCIPAL que se busca no existe
 *   (ej: la oferta que se quiere editar)
 * - 400 Bad Request: los DATOS DE ENTRADA son inválidos
 *   (ej: el offer_type_id enviado no es válido)
 *
 * Requerimiento: 3.6 — HTTP 400 cuando offer_type_id o sector_id no existen.
 */
public class InvalidReferenceException extends DomainException {

    /**
     * @param fieldName Nombre del campo con referencia inválida (ej: "offer_type_id")
     * @param id        Valor del ID que no existe
     */
    public InvalidReferenceException(String fieldName, Object id) {
        super("El valor '" + id + "' para el campo '" + fieldName + "' no es válido o no existe.");
    }
}
