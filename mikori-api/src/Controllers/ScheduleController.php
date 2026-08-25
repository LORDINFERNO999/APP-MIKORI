<?php

declare(strict_types=1);

namespace Mikori\Controllers;

use Mikori\Core\Request;
use Mikori\Core\Response;
use Mikori\Services\ScheduleService;

/** Horarios de bloqueo (V2) — Parent. */
final class ScheduleController
{
    private ScheduleService $schedules;

    public function __construct()
    {
        $this->schedules = new ScheduleService();
    }

    public function index(Request $request): Response
    {
        return Response::ok($this->schedules->list($request->userId(), (int) $request->param('id')));
    }

    public function store(Request $request): Response
    {
        return Response::created($this->schedules->create($request->userId(), (int) $request->param('id'), $request->body));
    }

    public function update(Request $request): Response
    {
        return Response::ok($this->schedules->update(
            $request->userId(),
            (int) $request->param('id'),
            (int) $request->param('sid'),
            $request->body
        ));
    }

    public function destroy(Request $request): Response
    {
        $this->schedules->delete($request->userId(), (int) $request->param('id'), (int) $request->param('sid'));
        return Response::ok(['message' => 'Horario eliminado.']);
    }
}
