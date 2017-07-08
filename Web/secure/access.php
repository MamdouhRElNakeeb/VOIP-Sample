<?php
/**
 * Created by PhpStorm.
 * User: Mamdouh El Nakeeb
 * Date: 4/13/17
 * Time: 3:30 PM
 */


class access{
    //connection global variables
    var $host = null;
    var $username = null;
    var $dpass = null;
    var $dname = null;
    var $conn = null;
    var $result = null;

    public function __construct($dbhost, $dbuser, $dbpass, $dbname){

        $this->host = $dbhost;
        $this->username = $dbuser;
        $this->dpass = $dbpass;
        $this->dname = $dbname;
    }

    public function connect(){
        $this->conn = new mysqli($this->host, $this->username, $this->dpass, $this->dname);
        if (mysqli_connect_errno()) {
            echo "Failed to connect to Database: " . mysqli_connect_error();
        }
        $this->conn->set_charset("utf8");
    }

    public function disconnect(){
        if($this->conn != null){
            $this->conn->close();
        }
    }

    /**
     * Users Functions
     */

    // insert user into database
    public function registerUser($name, $password, $salt, $mobile){
        $result = $this->selectUser($mobile);
        if ($result){

            return false;
        }
        else{
            $sql = "INSERT INTO users SET name=?, password=?, salt=?, mobile=?";
            $statement = $this->conn->prepare($sql);

            if(!$statement){
                throw new Exception($statement->error);
            }
            // bind 9 parameters of type string to be placed in $sql command
            $statement->bind_param("ssss", $name, $password, $salt, $mobile);
            $returnValue = $statement->execute();
            return $returnValue;

        }
    }

    // select user form database
    public function selectUser($mobile){
        $sql = "SELECT * FROM users WHERE mobile = '".$mobile."' ";
        $result = $this->conn->query($sql);
        if($result !=null && (mysqli_num_rows($result) >=1)){
            $row = $result->fetch_array(MYSQLI_ASSOC);
            if(!empty($row)){
                $returnArray = $row;
                return $returnArray;
            }
        }
    }

    /**
     * Encrypting password
     * @param password
     * returns salt and encrypted password
     */
    public function hashSSHA($password) {

        $salt = sha1(rand());
        $salt = substr($salt, 0, 10);
        $encrypted = base64_encode(sha1($password . $salt, true) . $salt);
        $hash = array("salt" => $salt, "encrypted" => $encrypted);
        return $hash;
    }

    /**
     * Decrypting password
     * @param salt, password
     * returns hash string
     */
    public function checkhashSSHA($salt, $password) {

        $hash = base64_encode(sha1($password . $salt, true) . $salt);

        return $hash;
    }
}

?>