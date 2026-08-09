<?php

declare(strict_types=1);

namespace Mikori\Repositories;

use Mikori\Core\Database;
use PDO;

/**
 * Base para repositorios: expone la conexión PDO y helpers comunes.
 * Todas las consultas usan sentencias preparadas.
 */
abstract class BaseRepository
{
    protected PDO $db;

    public function __construct()
    {
        $this->db = Database::connection();
    }

    /**
     * @param array<string,mixed> $params
     */
    protected function run(string $sql, array $params = []): \PDOStatement
    {
        $stmt = $this->db->prepare($sql);
        $stmt->execute($params);
        return $stmt;
    }

    protected function lastId(): int
    {
        return (int) $this->db->lastInsertId();
    }
}
