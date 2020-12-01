<!doctype html>

<html lang="en">
<head>
  <meta charset="utf-8">

  <title>Sample Webapp</title>
  <meta name="description" content="Sample Webapp">

  <link rel="stylesheet" href="css/styles.css?v=1.0">

</head>

<style>
pre {background-color: lightgrey; padding: 10px 10px 10px 10px;}
.nelly-header {background-color: lightpink;}
</style>

<body>

    <h1>This is a sample application for NellyGateway</h1>
    <p>Please find out more about NellyGateway on <a href="https://github.com/gianlucafrei/nellygateway">GitHub</a></p>
    
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
  <script src="js/scripts.js"></script>
</body>
</html>