<?php
require_once 'db.php';

header("Content-Type: application/json");

// Leer el JSON que envía la app
$data = json_decode(file_get_contents("php://input"), true);

// Validar que lleguen los campos necesarios
if (empty($data['nombre']) || empty($data['email']) || empty($data['password'])) {
    http_response_code(400);
    echo json_encode(["error" => "Faltan campos obligatorios"]);
    exit();
}

$nombre = $data['nombre'];
$email = $data['email'];
$password = password_hash($data['password'], PASSWORD_BCRYPT);

try {
    // Comprobar si el email ya existe
    $stmt = $pdo->prepare("SELECT id FROM usuarios WHERE email = ?");
    $stmt->execute([$email]);

    if ($stmt->rowCount() > 0) {
        http_response_code(409);
        echo json_encode(["error" => "El email ya está registrado"]);
        exit();
    }

    // Insertar el usuario
    $stmt = $pdo->prepare("INSERT INTO usuarios (nombre, email, password) VALUES (?, ?, ?)");
    $stmt->execute([$nombre, $email, $password]);

    $id = $pdo->lastInsertId();

    http_response_code(201);
    echo json_encode([
        "success" => true,
        "id" => $id,
        "nombre" => $nombre,
        "email" => $email
    ]);

} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(["error" => "Error al registrar: " . $e->getMessage()]);
}
?>
