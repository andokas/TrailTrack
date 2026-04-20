<?php
require_once 'db.php';

header("Content-Type: application/json");

if (empty($_GET['usuario_id'])) {
    http_response_code(400);
    echo json_encode(["error" => "Falta usuario_id"]);
    exit();
}

$usuario_id = $_GET['usuario_id'];

try {
    // Obtener rutas del usuario
    $stmt = $pdo->prepare("SELECT r.*, 
                            GROUP_CONCAT(f.ruta_foto) as fotos
                           FROM rutas r
                           LEFT JOIN fotos_ruta f ON r.id = f.ruta_id
                           WHERE r.usuario_id = ?
                           GROUP BY r.id
                           ORDER BY r.fecha DESC");
    $stmt->execute([$usuario_id]);
    $rutas = $stmt->fetchAll(PDO::FETCH_ASSOC);

    // Convertir puntos y fotos de string a array
    foreach ($rutas as &$ruta) {
        $ruta['puntos'] = json_decode($ruta['puntos'], true);
        $ruta['fotos'] = $ruta['fotos'] ? explode(',', $ruta['fotos']) : [];
    }

    echo json_encode([
        "success" => true,
        "rutas" => $rutas
    ]);

} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(["error" => "Error al obtener rutas: " . $e->getMessage()]);
}
?>
