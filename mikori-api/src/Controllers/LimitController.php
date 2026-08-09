<?php

declare(strict_types=1);

namespace Mikori\Controllers;

use Mikori\Core\Request;
use Mikori\Core\Response;
use Mikori\Services\LimitService;

final class LimitController
{
    private LimitService $limits;

    public function __construct()
    {
        $this->limits = new LimitService();
    }

    /** [Parent/Kids] Obtiene los límites diarios de un hijo. */
    public function index(Request $request): Response
    {
        return Response::ok($this->limits->get($request->userId(), (int) $request->param('id')));
    }

    /** [Parent] Define límites (mismo para todos o por día). */
    public function update(Request $request): Response
    {
        return Response::ok($this->limits->set($request->userId(), (int) $request->param('id'), $request->body));
    }
}
