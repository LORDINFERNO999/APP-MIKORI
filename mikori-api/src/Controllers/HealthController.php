<?php

declare(strict_types=1);

namespace Mikori\Controllers;

use Mikori\Core\Database;
use Mikori\Core\Request;
use Mikori\Core\Response;

final class HealthController
{
    public function index(Request $request): Response
    {
        return Response::ok([
            'service' => 'MIKORI API',
            'status' => 'ok',
            'version' => 'v1',
            'db_driver' => Database::driver(),
            'time' => gmdate('c'),
        ]);
    }
}
