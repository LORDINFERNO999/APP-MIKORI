<?php

declare(strict_types=1);

namespace Mikori\Controllers;

use Mikori\Core\Request;
use Mikori\Core\Response;
use Mikori\Services\RuleService;

/** Reglas por app (V2) — Parent. */
final class RuleController
{
    private RuleService $rules;

    public function __construct()
    {
        $this->rules = new RuleService();
    }

    /** Catálogo de apps usadas por el hijo. */
    public function apps(Request $request): Response
    {
        return Response::ok($this->rules->apps($request->userId(), (int) $request->param('id')));
    }

    /** Reglas por app actuales. */
    public function index(Request $request): Response
    {
        return Response::ok($this->rules->get($request->userId(), (int) $request->param('id')));
    }

    /** Definir límite/bloqueo por app. */
    public function update(Request $request): Response
    {
        return Response::ok($this->rules->set($request->userId(), (int) $request->param('id'), $request->body));
    }
}
