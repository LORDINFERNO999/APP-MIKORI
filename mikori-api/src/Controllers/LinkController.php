<?php

declare(strict_types=1);

namespace Mikori\Controllers;

use Mikori\Core\Request;
use Mikori\Core\Response;
use Mikori\Core\Validator;
use Mikori\Services\LinkService;

final class LinkController
{
    private LinkService $links;

    public function __construct()
    {
        $this->links = new LinkService();
    }

    /** [Parent] Genera un código de vinculación para un hijo. */
    public function generate(Request $request): Response
    {
        $result = $this->links->generate($request->userId(), (int) $request->param('id'));
        return Response::created($result);
    }

    /** [Parent] Consulta el estado de la vinculación de un hijo. */
    public function status(Request $request): Response
    {
        return Response::ok($this->links->status($request->userId(), (int) $request->param('id')));
    }

    /** [Kids] Canjea un código enviando la info del dispositivo. Sin autenticación previa. */
    public function redeem(Request $request): Response
    {
        $data = Validator::validate($request->body, [
            'code' => 'required|string|max:20',
            'device_uid' => 'required|string|max:190',
            'model' => 'string|max:120',
            'android_version' => 'string|max:40',
            'fcm_token' => 'string|max:255',
        ]);

        $result = $this->links->redeem(
            $data['code'],
            $data['device_uid'],
            $data['model'] ?? null,
            $data['android_version'] ?? null,
            $data['fcm_token'] ?? null
        );

        return Response::created($result);
    }
}
