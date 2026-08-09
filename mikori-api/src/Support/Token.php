<?php

declare(strict_types=1);

namespace Mikori\Support;

/**
 * Generación de tokens opacos y códigos de vinculación.
 *
 * Los tokens se entregan al cliente en claro una sola vez; en la BD solo se
 * guarda su hash SHA-256 (patrón tipo Sanctum). Así, una fuga de la tabla
 * sessions no expone tokens usables.
 */
final class Token
{
    /** Token de acceso/refresh: 40 bytes aleatorios en hex. */
    public static function random(): string
    {
        return bin2hex(random_bytes(40));
    }

    /** Hash determinista para almacenar/buscar el token. */
    public static function hash(string $token): string
    {
        return hash('sha256', $token);
    }

    /**
     * Código de vinculación con formato MIKORI-XXXXXX (6 dígitos).
     * Evita caracteres ambiguos usando solo dígitos.
     */
    public static function linkCode(): string
    {
        $digits = random_int(100000, 999999);
        return 'MIKORI-' . $digits;
    }
}
