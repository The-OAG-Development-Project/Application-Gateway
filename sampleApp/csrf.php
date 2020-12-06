<?php
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    echo "success";
    exit();
}
?>
<!doctype html>

<html lang="en">
<head>
  <meta charset="utf-8">
  <title>Sample Webapp</title>
  <meta name="description" content="Sample Webapp">
  <link rel="stylesheet" href="/styles.css">
</head>

<body>

    <h1>This is a sample application for NellyGateway</h1>
    <p>Please find out more about NellyGateway on <a href="https://github.com/gianlucafrei/nellygateway">GitHub</a></p>
    
    <a href="/">Home</a>
    <a href="/auth/google/login">Login with Google</a>
    <a href="/auth/github/login">Login with GitHub</a>
    <a href="/csrf.php">CSRF Example</a>
  
    <h2>Form without CSRF Protection</h2>
    <form action="/csrf.php" method="post" id="formWithoutCsrfProtection">
        <label for="fname">First name:</label>
        <input type="text" id="fname" name="fname"><br><br>
        <label for="lname">Last name:</label>
        <input type="text" id="lname" name="lname"><br><br>
        <input type="submit" value="Submit">
    </form>

    <h2>Form with CSRF Protection</h2>
    <form action="/csrf.php" method="post" id="formWithCsrfProtection">
        <label for="fname">First name:</label>
        <input type="text" id="fname" name="fname"><br><br>
        <label for="lname">Last name:</label>
        <input type="text" id="lname" name="lname"><br><br>
        <input type="submit" value="Submit">
    </form>

    <h2>XHR Request</h2>
    <button onclick="xhr()">Send xhr post</button>
    <button onclick="xhrToken()">Send xhr post with Token</button>

    <script type="text/javascript">

        function getCookie(name) {
        var nameEQ = name + "=";
        var ca = document.cookie.split(';');
            for(var i=0;i < ca.length;i++) {
                var c = ca[i];
                while (c.charAt(0)==' ') c = c.substring(1,c.length);
                if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
            }
            return null;
        }
        var csrf_token = getCookie('csrf');

        // Add csrf token to form
        var input = document.createElement("input");
        input.setAttribute("type", "hidden");
        input.setAttribute("name", "CSRFToken");
        input.setAttribute("value", csrf_token);
        document.getElementById("formWithCsrfProtection").appendChild(input);

        //XHR Request
        function xhr(){
        
            let xhr = new XMLHttpRequest();
            xhr.open('POST', "/csrf.php", true);
            xhr.responseType = 'json';
            xhr.onload = () => {
                let status = xhr.status;
                if (status == 200) {alert("Success");} else {alert("Failed");}
            };
            xhr.send();
        }
        function xhrToken(){
        
            let xhr = new XMLHttpRequest();
            xhr.open('POST', "/csrf.php", true);
            xhr.responseType = 'json';
            xhr.setRequestHeader('X-CSRF-TOKEN', csrf_token);
            xhr.onload = () => {
                let status = xhr.status;
                if (status == 200) {alert("Success");} else {alert("Failed");}
            };
            xhr.send();
        }   
    </script>
</body>
</html>