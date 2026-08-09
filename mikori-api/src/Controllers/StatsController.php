<?php

declare(strict_types=1);

namespace Mikori\Controllers;

use Mikori\Core\Exceptions\ValidationException;
use Mikori\Core\Request;
use Mikori\Core\Response;
use Mikori\Services\StatsService;

final class StatsController
{
    private StatsService $stats;

    public function __construct()
    {
        $this->stats = new StatsService();
    }

    /** [Kids] Ingesta por lotes del uso recolectado. Auth de dispositivo. */
    public function ingest(Request $request): Response
    {
        $items = $request->input('items');
        if (!is_array($items)) {
            throw new ValidationException(['items' => 'Debe ser una lista de registros de uso.']);
        }

        $result = $this->stats->ingest($request->deviceId(), $items);
        return Response::created($result);
    }

    /** [Parent] Resumen de hoy. */
    public function today(Request $request): Response
    {
        return Response::ok($this->stats->today($request->userId(), (int) $request->param('id')));
    }

    /** [Parent] Datos semanales. */
    public function week(Request $request): Response
    {
        return Response::ok($this->stats->week($request->userId(), (int) $request->param('id')));
    }

    /** [Parent] Uso por aplicación (rango opcional ?from=YYYY-MM-DD&to=YYYY-MM-DD). */
    public function apps(Request $request): Response
    {
        $from = $request->query['from'] ?? null;
        $to = $request->query['to'] ?? null;
        return Response::ok($this->stats->apps($request->userId(), (int) $request->param('id'), $from, $to));
    }
}
