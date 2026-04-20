<?php
require_once 'db.php';

header("Content-Type: application/json");

$data = json_decode(file_get_contents("php://input"), true);

if (empty($data['usuario_id']) || empty($data['titulo']) || empty($data['mensaje'])) {
    http_response_code(400);
    echo json_encode(["error" => "Faltan campos obligatorios"]);
    exit();
}

// Obtener token FCM del usuario
$stmt = $pdo->prepare("SELECT fcm_token FROM usuarios WHERE id = ?");
$stmt->execute([$data['usuario_id']]);
$usuario = $stmt->fetch(PDO::FETCH_ASSOC);

if (!$usuario || empty($usuario['fcm_token'])) {
    http_response_code(404);
    echo json_encode(["error" => "Usuario sin token FCM"]);
    exit();
}

// Obtener access token de OAuth 2.0
function getAccessToken() {
    $credenciales = json_decode(
        file_get_contents(__DIR__ . '/private/firebase-credentials.json'), true);

    $ahora = time();
    $expiracion = $ahora + 3600;

    // Codificación correcta en base64url
    $header = rtrim(strtr(base64_encode(json_encode([
        'alg' => 'RS256',
        'typ' => 'JWT'
    ])), '+/', '-_'), '=');

    $payload = rtrim(strtr(base64_encode(json_encode([
        'iss' => $credenciales['client_email'],
        'scope' => 'https://www.googleapis.com/auth/firebase.messaging',
        'aud' => 'https://oauth2.googleapis.com/token',
        'exp' => $expiracion,
        'iat' => $ahora
    ])), '+/', '-_'), '=');

    $firma_input = $header . '.' . $payload;
    
    openssl_sign($firma_input, $firma, $credenciales['private_key'], 'SHA256');
    
    $firma_b64 = rtrim(strtr(base64_encode($firma), '+/', '-_'), '=');
    $jwt = $firma_input . '.' . $firma_b64;

    $ch = curl_init('https://oauth2.googleapis.com/token');
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS,
        'grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Ajwt-bearer&assertion=' . $jwt);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    $respuesta = curl_exec($ch);
    curl_close($ch);

    $token_data = json_decode($respuesta, true);
    return $token_data['access_token'] ?? null;
}

// Obtener project_id del JSON
$credenciales = json_decode(
    file_get_contents(__DIR__ . '/private/firebase-credentials.json'), true);
$project_id = $credenciales['project_id'];

$access_token = getAccessToken();

// Enviar notificación con la nueva API HTTP v1
$mensaje = json_encode([
    'message' => [
        'token' => $usuario['fcm_token'],
        'notification' => [
            'title' => $data['titulo'],
            'body' => $data['mensaje']
        ],
        'data' => [
            'titulo' => $data['titulo'],
            'mensaje' => $data['mensaje']
        ]
    ]
]);

$ch = curl_init(
    "https://fcm.googleapis.com/v1/projects/$project_id/messages:send");
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_HTTPHEADER, [
    'Authorization: Bearer ' . $access_token,
    'Content-Type: application/json'
]);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, $mensaje);

$resultado = curl_exec($ch);

// Según PDF diapositiva 30: depurar errores
if (curl_errno($ch)) {
    echo json_encode(["error" => curl_error($ch)]);
} else {
    echo json_encode([
        "success" => true,
        "fcm_response" => json_decode($resultado)
    ]);
}

curl_close($ch);
?>
