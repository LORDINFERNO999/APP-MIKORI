<?php

declare(strict_types=1);

namespace Mikori\Controllers;

use Mikori\Core\Request;
use Mikori\Core\Response;
use Mikori\Services\PauseService;

/** Pausas temporales (V2) — Parent. */
final class PauseController
{
    private PauseService $pauses;

    public function __construct()
    {
        $this->pauses = new PauseService();
    }

    public function start(Request $request): Response
    {
        return Response::created($this->pauses->start($request->userId(), (int) $request->param('id'), $request->body));
    }

    public function cancel(Request $request): Response
    {
        $this->pauses->cancel($request->userId(), (int) $request->param('id'));
        return Response::ok(['message' => 'Pausa cancelada.']);
    }
}
