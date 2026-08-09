<?php

declare(strict_types=1);

namespace Mikori\Controllers;

use Mikori\Core\Request;
use Mikori\Core\Response;
use Mikori\Core\Validator;
use Mikori\Services\ChildService;

final class ChildController
{
    private ChildService $children;

    public function __construct()
    {
        $this->children = new ChildService();
    }

    public function index(Request $request): Response
    {
        return Response::ok($this->children->list($request->userId()));
    }

    public function store(Request $request): Response
    {
        $data = Validator::validate($request->body, [
            'name' => 'required|string|min:1|max:120',
            'birthdate' => 'date',
            'avatar' => 'string|max:190',
        ]);

        $child = $this->children->create(
            $request->userId(),
            $data['name'],
            $data['birthdate'] ?? null,
            $data['avatar'] ?? null
        );

        return Response::created($child);
    }

    public function show(Request $request): Response
    {
        return Response::ok($this->children->get($request->userId(), (int) $request->param('id')));
    }

    public function update(Request $request): Response
    {
        $data = Validator::validate($request->body, [
            'name' => 'string|min:1|max:120',
            'birthdate' => 'date',
            'avatar' => 'string|max:190',
        ]);

        $child = $this->children->update($request->userId(), (int) $request->param('id'), $data);
        return Response::ok($child);
    }

    public function destroy(Request $request): Response
    {
        $this->children->delete($request->userId(), (int) $request->param('id'));
        return Response::ok(['message' => 'Perfil eliminado.']);
    }
}
