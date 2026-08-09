<?php

declare(strict_types=1);

namespace Mikori\Core;

/**
 * Representa una ruta registrada. Permite encadenar ->auth() para exigir token.
 */
final class Route
{
    /** Guardia de autenticación requerida: null | 'user' | 'device'. */
    public ?string $guard = null;

    /**
     * @param list<string> $paramNames
     * @param array{0:class-string,1:string} $handler
     */
    public function __construct(
        public string $method,
        public string $regex,
        public array $paramNames,
        public array $handler,
    ) {
    }

    /** Exige token de usuario (padre). */
    public function auth(): self
    {
        $this->guard = 'user';
        return $this;
    }

    /** Exige token de dispositivo (app Kids). */
    public function deviceAuth(): self
    {
        $this->guard = 'device';
        return $this;
    }
}
