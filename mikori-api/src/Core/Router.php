<?php

declare(strict_types=1);

namespace Mikori\Core;

use Mikori\Core\Exceptions\HttpException;
use Mikori\Middleware\AuthMiddleware;
use Mikori\Middleware\DeviceAuthMiddleware;
use Throwable;

/**
 * Enrutador REST minimalista con soporte de parámetros de ruta ({id}),
 * middleware de autenticación y manejo centralizado de errores.
 */
final class Router
{
    /** @var list<Route> */
    private array $routes = [];

    public function get(string $path, array $handler): Route
    {
        return $this->add('GET', $path, $handler);
    }

    public function post(string $path, array $handler): Route
    {
        return $this->add('POST', $path, $handler);
    }

    public function put(string $path, array $handler): Route
    {
        return $this->add('PUT', $path, $handler);
    }

    public function patch(string $path, array $handler): Route
    {
        return $this->add('PATCH', $path, $handler);
    }

    public function delete(string $path, array $handler): Route
    {
        return $this->add('DELETE', $path, $handler);
    }

    private function add(string $method, string $path, array $handler): Route
    {
        $paramNames = [];
        $regex = preg_replace_callback('/\{([a-zA-Z_]+)\}/', static function (array $m) use (&$paramNames): string {
            $paramNames[] = $m[1];
            return '([^/]+)';
        }, '/' . trim($path, '/'));

        $route = new Route($method, '#^' . $regex . '$#', $paramNames, $handler);
        $this->routes[] = $route;
        return $route;
    }

    public function dispatch(Request $request): Response
    {
        try {
            $pathMatched = false;

            foreach ($this->routes as $route) {
                if (!preg_match($route->regex, $request->path, $matches)) {
                    continue;
                }
                $pathMatched = true;

                if ($route->method !== $request->method) {
                    continue;
                }

                // Extraer parámetros de ruta.
                array_shift($matches);
                foreach ($route->paramNames as $i => $name) {
                    $request->params[$name] = $matches[$i] ?? '';
                }

                if ($route->guard === 'user') {
                    AuthMiddleware::handle($request);
                } elseif ($route->guard === 'device') {
                    DeviceAuthMiddleware::handle($request);
                }

                [$class, $method] = $route->handler;
                $controller = new $class();
                $result = $controller->{$method}($request);

                return $result instanceof Response
                    ? $result
                    : Response::ok($result);
            }

            if ($pathMatched) {
                return Response::error(405, 'method_not_allowed', 'Método no permitido para esta ruta.');
            }

            return Response::error(404, 'not_found', 'Recurso no encontrado.');
        } catch (HttpException $e) {
            return Response::error($e->status, $e->errorCode, $e->getMessage(), $e->details);
        } catch (Throwable $e) {
            // Manejo centralizado: no filtrar detalles internos en producción.
            $debug = Env::bool('APP_DEBUG', false);
            $details = $debug ? ['exception' => $e->getMessage(), 'file' => $e->getFile(), 'line' => $e->getLine()] : [];
            error_log('[MIKORI] ' . $e->getMessage() . ' @ ' . $e->getFile() . ':' . $e->getLine());
            return Response::error(500, 'server_error', 'Ocurrió un error interno.', $details);
        }
    }
}
