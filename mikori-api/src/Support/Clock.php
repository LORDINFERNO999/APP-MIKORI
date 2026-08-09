<?php

declare(strict_types=1);

namespace Mikori\Support;

use DateTimeImmutable;
use DateTimeZone;

/**
 * Utilidades de tiempo en UTC, formateadas para almacenamiento portable
 * entre MySQL (DATETIME) y SQLite (TEXT).
 */
final class Clock
{
    private const FORMAT = 'Y-m-d H:i:s';

    public static function now(): DateTimeImmutable
    {
        return new DateTimeImmutable('now', new DateTimeZone('UTC'));
    }

    public static function nowString(): string
    {
        return self::now()->format(self::FORMAT);
    }

    public static function today(): string
    {
        return self::now()->format('Y-m-d');
    }

    public static function inMinutes(int $minutes): string
    {
        return self::now()->modify("+{$minutes} minutes")->format(self::FORMAT);
    }

    public static function daysAgo(int $days): string
    {
        return self::now()->modify("-{$days} days")->format('Y-m-d');
    }

    public static function isPast(string $datetime): bool
    {
        return strtotime($datetime) < time();
    }
}
