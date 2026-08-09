<?php

declare(strict_types=1);

namespace Mikori\Core\Exceptions;

use RuntimeException;

/**
 * Excepción base que se traduce directamente a una respuesta HTTP.
 * El manejador central de errores la usa para responder con el código
 * y el código de dominio adecuados.
 */
class HttpException extends RuntimeException
{
    /** @var array<string,mixed> */
    public array $details;

    public function __construct(
        public int $status,
        public string $errorCode,
        string $message,
        array $details = []
    ) {
        parent::__construct($message);
        $this->details = $details;
    }
}
