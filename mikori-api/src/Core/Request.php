<?php

declare(strict_types=1);

namespace Mikori\Core;

/**
 * Abstracción de la petición HTTP entrante.
 */
final class Request
{
    /**
     * @param array<string,mixed> $body   Cuerpo JSON decodificado.
     * @param array<string,string> $query  Parámetros de query string.
     * @param array<string,string> $params Parámetros de ruta (/children/{id}).
     * @param array<string,string> $headers Cabeceras (claves en minúscula).
     */
    /**
     * Usuario autenticado resuelto por AuthMiddleware.
     * Estructura: ['user' => array, 'session_id' => int].
     * @var array{user:array<string,mixed>,session_id:int}|null
     */
    public ?array $authContext = null;

    public function __construct(
        public readonly string $method,
        public readonly string $path,
        public array $body,
        public array $query,
        public array $params,
        public array $headers,
    ) {
    }

    /** ID del usuario autenticado (0 si no hay). */
    public function userId(): int
    {
        return (int) ($this->authContext['user']['id'] ?? 0);
    }

    /** Dispositivo autenticado (app Kids), o null. */
    public function device(): ?array
    {
        return $this->authContext['device'] ?? null;
    }

    /** ID del dispositivo autenticado (0 si no hay). */
    public function deviceId(): int
    {
        return (int) ($this->authContext['device']['id'] ?? 0);
    }

    public static function fromGlobals(): self
    {
        $method = strtoupper($_SERVER['REQUEST_METHOD'] ?? 'GET');
        $uri = $_SERVER['REQUEST_URI'] ?? '/';
        $path = parse_url($uri, PHP_URL_PATH) ?: '/';
        $path = '/' . trim($path, '/');

        $raw = file_get_contents('php://input') ?: '';
        $body = [];
        if ($raw !== '') {
            $decoded = json_decode($raw, true);
            if (is_array($decoded)) {
                $body = $decoded;
            }
        }

        $headers = [];
        foreach ($_SERVER as $key => $value) {
            if (str_starts_with($key, 'HTTP_')) {
                $name = strtolower(str_replace('_', '-', substr($key, 5)));
                $headers[$name] = (string) $value;
            }
        }
        if (isset($_SERVER['CONTENT_TYPE'])) {
            $headers['content-type'] = (string) $_SERVER['CONTENT_TYPE'];
        }

        return new self($method, $path, $body, $_GET, [], $headers);
    }

    public function input(string $key, mixed $default = null): mixed
    {
        return $this->body[$key] ?? $default;
    }

    public function param(string $key, ?string $default = null): ?string
    {
        return $this->params[$key] ?? $default;
    }

    public function bearerToken(): ?string
    {
        $auth = $this->headers['authorization'] ?? '';
        if (stripos($auth, 'Bearer ') === 0) {
            return substr($auth, 7);
        }
        return null;
    }
}
