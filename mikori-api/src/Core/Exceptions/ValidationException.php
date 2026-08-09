<?php

declare(strict_types=1);

namespace Mikori\Core\Exceptions;

/**
 * Error de validación de entrada (HTTP 422).
 */
final class ValidationException extends HttpException
{
    /**
     * @param array<string,string> $errors Mapa campo => mensaje.
     */
    public function __construct(array $errors)
    {
        parent::__construct(422, 'validation_error', 'Los datos enviados no son válidos.', ['fields' => $errors]);
    }
}
