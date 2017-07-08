<?php
/**
 * Created by PhpStorm.
 * User: mamdouhelnakeeb
 * Date: 6/16/17
 * Time: 6:38 PM
 */

$name = htmlentities($_REQUEST["name"]);
$mobile = htmlentities($_REQUEST["mobile"]);
$password = htmlentities($_REQUEST["password"]);

if (empty($name) || empty($password) || empty($mobile)){
    $returnArray["error"] = TRUE;
    $returnArray["message"] = "Missing Fields!";
    echo json_encode($returnArray);
    return;
}

require ("secure/access.php");
require ("secure/RAMconn.php");

$access = new access(DB_HOST, DB_USER, DB_PASSWORD, DB_NAME);
$access->connect();

//secure password
$hash = $access->hashSSHA($password);
$secured_password = $hash["encrypted"]; // encrypted password
$salt = $hash["salt"]; // salt
$result = $access->registerUser($name, $secured_password, $salt, $mobile);

if ($result){
    $user = $access->selectUser($mobile);
    $returnArray["error"] = FALSE;
    $returnArray["message"] = "Registration is Successful";
    $returnArray["id"] = $user["id"];
    $returnArray["name"] = $user["name"];
    $returnArray["mobile"] = $user["mobile"];
}
else{
    $returnArray["error"] = TRUE;
    $returnArray["message"] = "User already existed!";
}
$access->disconnect();
echo json_encode($returnArray);

?>