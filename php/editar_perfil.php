<?php
require_once 'db.php';

header("Content-Type: application/json");

$data = json_decode(file_get_contents("php://input"), true);

if (empty($data['usuario_id'])) {
    http_response_code(400);
    echo json_encode(["error" => "Falta usuario_id"]);
    exit();
}

$usuario_id = $data['usuario_id'];

try {
    // Actualizar nombre si viene
    if (!empty($data['nombre'])) {
        $stmt = $pdo->prepare("UPDATE usuarios SET nombre = ? WHERE id = ?");
        $stmt->execute([$data['nombre'], $usuario_id]);
    }

    // Actualizar contraseña si viene
    if (!empty($data['password'])) {
        $password = password_hash($data['password'], PASSWORD_BCRYPT);
        $stmt = $pdo->prepare("UPDATE usuarios SET password = ? WHERE id = ?");
        $stmt->execute([$password, $usuario_id]);
    }

    // Reiniciar estadísticas si viene el flag
    if (!empty($data['reiniciar_stats']) && $data['reiniciar_stats'] == true) {
        $stmt = $pdo->prepare("DELETE FROM rutas WHERE usuario_id = ?");
        $stmt->execute([$usuario_id]);
    }

    // Devolver datos actualizados
    $stmt = $pdo->prepare("SELECT id, nombre, email, foto_perfil FROM usuarios WHERE id = ?");
    $stmt->execute([$usuario_id]);
    $usuario = $stmt->fetch(PDO::FETCH_ASSOC);

    echo json_encode([
        "success" => true,
        "usuario" => $usuario
    ]);

} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(["error" => "Error al editar perfil: " . $e->getMessage()]);
}
?>
