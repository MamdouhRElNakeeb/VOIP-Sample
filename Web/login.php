<?php
/**
 * Created by PhpStorm.
 * User: mamdouhelnakeeb
 * Date: 6/16/17
 * Time: 8:50 PM
 */


$mobile = htmlentities($_REQUEST["mobile"]);
$password = htmlentities($_REQUEST["password"]);

if (empty($mobile) || empty($password)){
    $returnArray["status"] = "400";
    $returnArray["message"] = "Missing Fields!";
    echo json_encode($returnArray);
    return;
}

require ("secure/access.php");
require ("secure/RAMconn.php");

$access = new access(DB_HOST, DB_USER, DB_PASSWORD, DB_NAME);
$access->connect();
$user = $access->selectUser($mobile);

if ($user){
    // verifying user password
    $salt = $user['salt'];
    $secured_password = $user['password'];
    $hash = $access->checkhashSSHA($salt, $password);

    // check for password equality
    if ($hash == $secured_password){
        $returnArray["error"] = FALSE;
        $returnArray["message"] = "Login is Successful";
        $returnArray["id"] = $user["id"];
        $returnArray["name"] = $user["name"];
        $returnArray["mobile"] = $user["mobile"];
    }
    else{
        $returnArray["error"] = TRUE;
        $returnArray["message"] = "Password is Incorrect";
    }
}
else{
    $returnArray["error"] = TRUE;
    $returnArray["message"] = "User isn't existed!";
}
$access->disconnect();
echo json_encode($returnArray);

?>