<?php
require_once 'db.php';

header("Content-Type: application/json");

if (empty($_POST['usuario_id']) || empty($_FILES['foto'])) {
    http_response_code(400);
    echo json_encode(["error" => "Faltan campos obligatorios"]);
    exit();
}

$usuario_id = $_POST['usuario_id'];
$tipo = $_POST['tipo'] ?? 'perfil'; // 'perfil' o 'ruta'
$ruta_id = $_POST['ruta_id'] ?? null;

// Validar que sea una imagen
$allowed = ['image/jpeg', 'image/png', 'image/jpg'];
if (!in_array($_FILES['foto']['type'], $allowed)) {
    http_response_code(400);
    echo json_encode(["error" => "Formato de imagen no válido"]);
    exit();
}

// Crear carpeta si no existe
$carpeta = "uploads/";
if (!is_dir($carpeta)) {
    mkdir($carpeta, 0777, true);
}

// Generar nombre único
$extension = pathinfo($_FILES['foto']['name'], PATHINFO_EXTENSION);
$nombre_archivo = uniqid("foto_") . "." . $extension;
$ruta_archivo = $carpeta . $nombre_archivo;

if (!move_uploaded_file($_FILES['foto']['tmp_name'], $ruta_archivo)) {
    http_response_code(500);
    echo json_encode(["error" => "Error al guardar la imagen"]);
    exit();
}

$url = "http://" . $_SERVER['HTTP_HOST'] . "/" . $ruta_archivo;

try {
    if ($tipo === 'perfil') {
        // Actualizar foto de perfil del usuario
        $stmt = $pdo->prepare("UPDATE usuarios SET foto_perfil = ? WHERE id = ?");
        $stmt->execute([$url, $usuario_id]);
    } else if ($tipo === 'ruta' && $ruta_id) {
        // Insertar foto asociada a una ruta
        $stmt = $pdo->prepare("INSERT INTO fotos_ruta (ruta_id, ruta_foto) VALUES (?, ?)");
        $stmt->execute([$ruta_id, $url]);
    }

    echo json_encode([
        "success" => true,
        "url" => $url
    ]);

} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(["error" => "Error en BD: " . $e->getMessage()]);
}
?>
