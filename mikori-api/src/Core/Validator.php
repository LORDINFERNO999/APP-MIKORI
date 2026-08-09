<?php

declare(strict_types=1);

namespace Mikori\Core;

use Mikori\Core\Exceptions\ValidationException;

/**
 * Validador ligero basado en reglas simples separadas por '|'.
 *
 * Reglas soportadas:
 *   required, string, integer, email, boolean, date,
 *   min:N, max:N (longitud para strings, valor para enteros),
 *   in:a,b,c
 *
 * Uso:
 *   $data = Validator::validate($request->body, [
 *       'email'    => 'required|email',
 *       'password' => 'required|string|min:8',
 *   ]);
 */
final class Validator
{
    /**
     * @param array<string,mixed> $data
     * @param array<string,string> $rules
     * @return array<string,mixed> Solo los campos validados presentes.
     */
    public static function validate(array $data, array $rules): array
    {
        $errors = [];
        $clean = [];

        foreach ($rules as $field => $ruleString) {
            $ruleList = explode('|', $ruleString);
            $value = $data[$field] ?? null;
            $isRequired = in_array('required', $ruleList, true);
            $present = array_key_exists($field, $data) && $value !== null && $value !== '';

            if (!$present) {
                if ($isRequired) {
                    $errors[$field] = 'Este campo es obligatorio.';
                }
                continue;
            }

            $fieldError = self::applyRules($field, $value, $ruleList);
            if ($fieldError !== null) {
                $errors[$field] = $fieldError;
                continue;
            }

            $clean[$field] = $value;
        }

        if ($errors !== []) {
            throw new ValidationException($errors);
        }

        return $clean;
    }

    /**
     * @param list<string> $ruleList
     */
    private static function applyRules(string $field, mixed $value, array $ruleList): ?string
    {
        foreach ($ruleList as $rule) {
            if ($rule === 'required' || $rule === '') {
                continue;
            }

            [$name, $arg] = array_pad(explode(':', $rule, 2), 2, null);

            switch ($name) {
                case 'string':
                    if (!is_string($value)) {
                        return 'Debe ser texto.';
                    }
                    break;
                case 'integer':
                    if (!is_int($value) && !(is_string($value) && ctype_digit($value))) {
                        return 'Debe ser un número entero.';
                    }
                    break;
                case 'boolean':
                    if (!is_bool($value) && !in_array($value, [0, 1, '0', '1', true, false], true)) {
                        return 'Debe ser verdadero o falso.';
                    }
                    break;
                case 'email':
                    if (!is_string($value) || !filter_var($value, FILTER_VALIDATE_EMAIL)) {
                        return 'Debe ser un correo válido.';
                    }
                    break;
                case 'date':
                    if (!is_string($value) || strtotime($value) === false) {
                        return 'Debe ser una fecha válida.';
                    }
                    break;
                case 'min':
                    if (is_string($value) && mb_strlen($value) < (int) $arg) {
                        return "Debe tener al menos {$arg} caracteres.";
                    }
                    if (is_numeric($value) && (int) $value < (int) $arg) {
                        return "Debe ser al menos {$arg}.";
                    }
                    break;
                case 'max':
                    if (is_string($value) && mb_strlen($value) > (int) $arg) {
                        return "No debe superar {$arg} caracteres.";
                    }
                    if (is_numeric($value) && (int) $value > (int) $arg) {
                        return "No debe superar {$arg}.";
                    }
                    break;
                case 'in':
                    $options = explode(',', (string) $arg);
                    if (!in_array((string) $value, $options, true)) {
                        return 'Valor no permitido.';
                    }
                    break;
            }
        }

        return null;
    }
}
