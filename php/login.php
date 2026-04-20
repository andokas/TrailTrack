<?php
require_once 'db.php';

header("Content-Type: application/json");

$data = json_decode(file_get_contents("php://input"), true);

if (empty($data['email']) || empty($data['password'])) {
    http_response_code(400);
    echo json_encode(["error" => "Faltan campos obligatorios"]);
    exit();
}

$email = $data['email'];
$password = $data['password'];

try {
    $stmt = $pdo->prepare("SELECT * FROM usuarios WHERE email = ?");
    $stmt->execute([$email]);
    $usuario = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$usuario || !password_verify($password, $usuario['password'])) {
        http_response_code(401);
        echo json_encode(["error" => "Email o contraseña incorrectos"]);
        exit();
    }

    echo json_encode([
        "success" => true,
        "id" => $usuario['id'],
        "nombre" => $usuario['nombre'],
        "email" => $usuario['email'],
        "foto_perfil" => $usuario['foto_perfil']
    ]);

} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(["error" => "Error al iniciar sesión: " . $e->getMessage()]);
}
?>
