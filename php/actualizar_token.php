<?php
require_once 'db.php';

header("Content-Type: application/json");

$data = json_decode(file_get_contents("php://input"), true);

if (empty($data['usuario_id']) || empty($data['token'])) {
    http_response_code(400);
    echo json_encode(["error" => "Faltan campos obligatorios"]);
    exit();
}

try {
    $stmt = $pdo->prepare("UPDATE usuarios SET fcm_token = ? WHERE id = ?");
    $stmt->execute([$data['token'], $data['usuario_id']]);

    echo json_encode(["success" => true]);

} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(["error" => "Error al actualizar token: " . $e->getMessage()]);
}
?>
