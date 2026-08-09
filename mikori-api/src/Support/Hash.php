<?php

declare(strict_types=1);

namespace Mikori\Support;

/**
 * Hash seguro de contraseñas (bcrypt/argon2 según disponibilidad).
 */
final class Hash
{
    public static function make(string $plain): string
    {
        // PASSWORD_DEFAULT usa bcrypt; el sistema puede migrar a argon2 sin cambios de API.
        return password_hash($plain, PASSWORD_DEFAULT);
    }

    public static function verify(string $plain, string $hash): bool
    {
        return password_verify($plain, $hash);
    }

    public static function needsRehash(string $hash): bool
    {
        return password_needs_rehash($hash, PASSWORD_DEFAULT);
    }
}
