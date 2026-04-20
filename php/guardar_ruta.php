<?php
require_once 'db.php';

header("Content-Type: application/json");

$data = json_decode(file_get_contents("php://input"), true);

if (empty($data['usuario_id']) || empty($data['puntos'])) {
    http_response_code(400);
    echo json_encode(["error" => "Faltan campos obligatorios"]);
    exit();
}

$usuario_id = $data['usuario_id'];
$nombre = $data['nombre'] ?? 'Ruta sin nombre';
$distancia = $data['distancia'] ?? 0;
$duracion = $data['duracion'] ?? 0;
$puntos = json_encode($data['puntos']); // Array de {lat, lng}

try {
    $stmt = $pdo->prepare("INSERT INTO rutas (usuario_id, nombre, distancia, duracion, puntos) 
                           VALUES (?, ?, ?, ?, ?)");
    $stmt->execute([$usuario_id, $nombre, $distancia, $duracion, $puntos]);

    $id = $pdo->lastInsertId();

    echo json_encode([
        "success" => true,
        "id" => $id,
        "nombre" => $nombre,
        "distancia" => $distancia,
        "duracion" => $duracion
    ]);

} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(["error" => "Error al guardar ruta: " . $e->getMessage()]);
}
?>
