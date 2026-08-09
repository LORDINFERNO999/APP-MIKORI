<?php

declare(strict_types=1);

namespace Mikori\Core;

/**
 * Respuesta JSON estandarizada.
 *
 * Formato de éxito: { "data": ..., "meta"?: ... }
 * Formato de error: { "error": { "code": ..., "message": ..., "details"?: ... } }
 */
final class Response
{
    /**
     * @param array<string,mixed>|null $payload
     */
    public function __construct(
        public int $status = 200,
        public ?array $payload = null,
    ) {
    }

    public static function ok(mixed $data, ?array $meta = null): self
    {
        $payload = ['data' => $data];
        if ($meta !== null) {
            $payload['meta'] = $meta;
        }
        return new self(200, $payload);
    }

    public static function created(mixed $data): self
    {
        return new self(201, ['data' => $data]);
    }

    public static function noContent(): self
    {
        return new self(204, null);
    }

    public static function error(int $status, string $code, string $message, array $details = []): self
    {
        $error = ['code' => $code, 'message' => $message];
        if ($details !== []) {
            $error['details'] = $details;
        }
        return new self($status, ['error' => $error]);
    }

    public function send(): void
    {
        http_response_code($this->status);
        header('Content-Type: application/json; charset=utf-8');
        header('X-Content-Type-Options: nosniff');

        if ($this->status !== 204 && $this->payload !== null) {
            echo json_encode($this->payload, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
        }
    }
}
