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


    <pre>
<?php

  echo $_SERVER['REQUEST_METHOD'] . ' ' . $_SERVER['REQUEST_URI'] . ' ' . $_SERVER['SERVER_PROTOCOL'] . '<br>';

  function startsWith( $haystack, $needle ) {
    $length = strlen( $needle );
    return substr( $haystack, 0, $length ) === $needle;
  }

  $headers =  getallheaders();
  foreach($headers as $key=>$val){

    if(startsWith($key, "X-Nelly") or startsWith($key, "X-Proxy"))
    {
      echo '<span class="nelly-header">' . $key . ': ' . $val . '</span><br>';
    }
    else
    {
      echo '<span>' . $key . ': ' . $val . '</span><br>';
    }
  }
?>
  </pre>
</body>
</html>