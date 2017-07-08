<?php
/**
 * Created by PhpStorm.
 * User: mamdouhelnakeeb
 * Date: 7/8/17
 * Time: 6:46 AM
 */

$mobile = htmlentities($_REQUEST["mobile"]);

if (empty($mobile)){
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

    $returnArray["error"] = FALSE;
    $returnArray["message"] = "User is existed";
    $returnArray["id"] = $user["id"];
    $returnArray["name"] = $user["name"];
    $returnArray["mobile"] = $user["mobile"];

}
else{
    $returnArray["error"] = TRUE;
    $returnArray["message"] = "User isn't existed!";
}
$access->disconnect();
echo json_encode($returnArray);

?>