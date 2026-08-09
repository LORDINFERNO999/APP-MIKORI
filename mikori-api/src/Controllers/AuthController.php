<?php

declare(strict_types=1);

namespace Mikori\Controllers;

use Mikori\Core\Request;
use Mikori\Core\Response;
use Mikori\Core\Validator;
use Mikori\Services\AuthService;

final class AuthController
{
    private AuthService $auth;

    public function __construct()
    {
        $this->auth = new AuthService();
    }

    public function register(Request $request): Response
    {
        $data = Validator::validate($request->body, [
            'name' => 'required|string|min:2|max:120',
            'email' => 'required|email|max:190',
            'password' => 'required|string|min:8|max:100',
        ]);

        $result = $this->auth->register(
            $data['name'],
            $data['email'],
            $data['password'],
            $this->userAgent($request),
            $this->ip()
        );

        return Response::created($result);
    }

    public function login(Request $request): Response
    {
        $data = Validator::validate($request->body, [
            'email' => 'required|email',
            'password' => 'required|string',
        ]);

        $result = $this->auth->login(
            $data['email'],
            $data['password'],
            $this->userAgent($request),
            $this->ip()
        );

        return Response::ok($result);
    }

    public function logout(Request $request): Response
    {
        $this->auth->logout((int) ($request->authContext['session_id'] ?? 0));
        return Response::ok(['message' => 'Sesión cerrada.']);
    }

    public function refresh(Request $request): Response
    {
        $data = Validator::validate($request->body, ['refresh_token' => 'required|string']);
        return Response::ok($this->auth->refresh($data['refresh_token']));
    }

    public function forgotPassword(Request $request): Response
    {
        $data = Validator::validate($request->body, ['email' => 'required|email']);
        return Response::ok($this->auth->forgotPassword($data['email']));
    }

    public function resetPassword(Request $request): Response
    {
        $data = Validator::validate($request->body, [
            'token' => 'required|string',
            'password' => 'required|string|min:8|max:100',
        ]);
        $this->auth->resetPassword($data['token'], $data['password']);
        return Response::ok(['message' => 'Contraseña actualizada correctamente.']);
    }

    private function userAgent(Request $request): ?string
    {
        $ua = $request->headers['user-agent'] ?? null;
        return $ua !== null ? mb_substr($ua, 0, 255) : null;
    }

    private function ip(): ?string
    {
        return $_SERVER['REMOTE_ADDR'] ?? null;
    }
}
