<?php

declare(strict_types=1);

namespace Mikori\Repositories;

use Mikori\Support\Clock;

final class ApplicationRepository extends BaseRepository
{
    /**
     * Devuelve el id de la app por package_name, creándola si no existe.
     * Upsert portable (select-then-insert) válido en MySQL y SQLite.
     */
    public function findOrCreate(string $packageName, ?string $label, ?string $category): int
    {
        $row = $this->run(
            'SELECT id FROM applications WHERE package_name = :pkg',
            [':pkg' => $packageName]
        )->fetch();

        if ($row) {
            // Actualiza etiqueta/categoría si llegan datos nuevos y faltaban.
            if ($label !== null) {
                $this->run(
                    'UPDATE applications SET app_label = :label, category = COALESCE(:cat, category) WHERE id = :id',
                    [':label' => $label, ':cat' => $category, ':id' => (int) $row['id']]
                );
            }
            return (int) $row['id'];
        }

        $this->run(
            'INSERT INTO applications (package_name, app_label, category, created_at)
             VALUES (:pkg, :label, :cat, :now)',
            [':pkg' => $packageName, ':label' => $label, ':cat' => $category, ':now' => Clock::nowString()]
        );
        return $this->lastId();
    }
}
